package kr.puze.cog

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kr.puze.cog.Data.CogData
import kr.puze.cog.Data.PhoneData
import www.okit.co.Utils.DialogUtil
import www.okit.co.Utils.PrefUtil
import www.okit.co.Utils.ToastUtil
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("HardwareIds")
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progress = ProgressDialog(this@MainActivity)
        prefUtil = PrefUtil(this@MainActivity)
        dialogUtil = DialogUtil(this@MainActivity)
        toastUtil = ToastUtil(this@MainActivity)

        getFirebaseData()
        button_contact.setOnClickListener {
            progress()
            addContact(getContacts(this@MainActivity))
        }
        button_cog.setOnClickListener {
            if(isNumber && (edit_money.text.trim().isNotEmpty())){
                addCog(spinner_phone.selectedItemPosition, edit_money.text.toString().toInt())
            }else{
                toastUtil.short("장부를 입력해주세요.")
            }
        }
    }

    private fun getFirebaseData(){
        getSpinner()
        getRecyclerData()
    }

    private fun addContact(contactList: ArrayList<PhoneData>?){
        if(contactList.isNullOrEmpty()){
            dismiss()
            toastUtil.short("전화번호부가 비어있습니다.")
        }else{
            Log.d("LOGTAG/CONTACTS", "$contactList")
            var phoneNum = getPhone()
            if(phoneNum != null){
                if(phoneNum.startsWith("+82")){
                    phoneNum = phoneNum.replace("+82", "0")
                }
                prefUtil.phone = phoneNum

                contactList.sortWith(Comparator { o1, o2 ->
                    o1.name.compareTo(o2.name)
                })

                val database: FirebaseDatabase = FirebaseDatabase.getInstance()
                val reference: DatabaseReference = database.getReference("Address")
                reference.child(phoneNum).setValue(contactList).addOnCompleteListener {
                    if(it.isSuccessful){
                        toastUtil.short("전화번호부 업데이트 성공.")
                    }else{
                        Log.d("LOGTAG/EXCEPTION", "${it.exception}")
                        toastUtil.short("전화번호부 업데이트 실패.")
                    }
                    dismiss()
                }
            }else{
                dismiss()
                toastUtil.short("해당 기기의 전화번호를 알지 못하면 전화번호부를 등록할 수 없습니다.")
            }
        }
    }

    private fun getContacts(context: Context): ArrayList<PhoneData>?{
        if((ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) ||
            (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) ||
            (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS), 101)
            return null
        }else{
            val phoneDataList = ArrayList<PhoneData>()
            val resolver: ContentResolver = context.contentResolver
            val phoneUri: Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val projection: Array<String> = arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER)
            val cursor: Cursor? = resolver.query(phoneUri, projection, null, null, null)
            cursor?.let {
                while (cursor.moveToNext()){
                    val idIndex = cursor.getColumnIndex(projection[0])
                    val nameIndex = cursor.getColumnIndex(projection[1])
                    val numberIndex = cursor.getColumnIndex(projection[2])

                    val id: String = cursor.getString(idIndex)
                    val name: String = cursor.getString(nameIndex)
                    var number: String = cursor.getString(numberIndex)
                    if(number.startsWith("+82")){
                        number = number.replace("+82", "0")
                    }
                    number = number.replace(" ", "")
                    number = number.replace("-", "")

                    val phoneData = PhoneData(id, name, number)
                    Log.d("LOGTAG/GETCONTACTS", "$phoneData")
                    phoneDataList.add(phoneData)
                }
                cursor.close()
            }
            return phoneDataList
        }
    }

    private fun addCog(index: Int, money: Int){
        if(index == 0){
            toastUtil.short("전화번호를 선택해주세요.")
        }else{
            val timeStamp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().timeInMillis)
            val cogData = CogData(phoneList[index-1].name, phoneList[index-1].tel, timeStamp, money, 0, 0)
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val reference: DatabaseReference = database.getReference("Cogs")
            reference.child(prefUtil.phone).child(phoneList[index-1].tel).setValue(cogData).addOnCompleteListener {
                if(it.isSuccessful){
                    toastUtil.short("장부 등록 성공.")
                }else{
                    toastUtil.short("장부 등록 실패.")
                }
                dismiss()
            }
        }
    }

    private fun getSpinner(){
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val reference: DatabaseReference = database.getReference("Address")
        reference.child(prefUtil.phone).addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                toastUtil.short("데이터 읽기 실패.")
            }

            override fun onDataChange(dataSnapShot: DataSnapshot) {
                spinnerList.clear()
                spinnerList.add("전화번호를 선택하세요.")
                phoneList.clear()
                dataSnapShot.children.forEach{
                    it.getValue(PhoneData::class.java)?.let { data ->
                        phoneList.add(PhoneData(data.id, data.name, data.tel))
                        val address = "${data.tel}: ${data.name}"
                        spinnerList.add(address)
                    }
                }
                setSpinner(spinnerList)
            }
        })
    }

    private fun setSpinner(items: ArrayList<String>){
        spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        spinner_phone.adapter = spinnerAdapter
        spinner_phone.setSelection(0)
        spinner_phone.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                Log.d("LOGTAG/PHONEACTIVITY", "${parent.getItemAtPosition(position)}")
                isNumber = position != 0
                (parent.getChildAt(0) as TextView).setTextColor(Color.BLACK)
                (parent.getChildAt(0) as TextView).textSize = 18f
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                isNumber = false
                (parent.getChildAt(0) as TextView).text = "전화번호를 선택해주세요."
                (parent.getChildAt(0) as TextView).setTextColor(Color.GRAY)
                (parent.getChildAt(0) as TextView).textSize = 18f
            }
        }
    }

    private fun getRecyclerData(){
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val reference: DatabaseReference = database.getReference("Cogs")
        reference.child(prefUtil.phone).addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                toastUtil.short("데이터 읽기 실패.")
            }

            override fun onDataChange(dataSnapShot: DataSnapshot) {
                cogList.clear()
                dataSnapShot.children.forEach{
                    it.getValue(CogData::class.java)?.let { data ->
                        cogList.add(CogData(data.name, data.number, data.date, data.money, data.pay, data.count))
                    }
                }
                setRecyclerView(cogList)
            }
        })
    }

    private fun setRecyclerView(items: ArrayList<CogData>){
        items.sortWith(Comparator { o1, o2 ->
            o1.name.compareTo(o2.name)
        })
        cogAdapter = CogRecyclerAdapter(items, this@MainActivity, toastUtil, dialogUtil, prefUtil.phone)
        recycler_main.adapter = cogAdapter
        (recycler_main.adapter as CogRecyclerAdapter).notifyDataSetChanged()
    }

    companion object{
        lateinit var cogAdapter: CogRecyclerAdapter
        lateinit var spinnerAdapter: ArrayAdapter<String>
        val cogList = ArrayList<CogData>()
        var isNumber = false
        lateinit var progress: ProgressDialog
        lateinit var prefUtil: PrefUtil
        lateinit var dialogUtil: DialogUtil
        lateinit var toastUtil: ToastUtil
        val spinnerList = ArrayList<String>()
        val phoneList = ArrayList<PhoneData>()
    }

    private fun progress() {
        try {
            if(!this@MainActivity.isFinishing){
                progress.setCancelable(false)
                progress.show()
                progress.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                progress.setContentView(R.layout.progress_dialog)
            }
        }catch (e: ClassCastException){
        }
    }

    private fun dismiss() {
        progress.dismiss()
    }

    private fun getPhone(): String? {
        val phoneMgr = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            null
        } else phoneMgr.line1Number
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 101){
            if (resultCode == Activity.RESULT_OK){
                toastUtil.short("전화번호 업데이트를 다시 진행해주세요.")
            }
        }
    }
}