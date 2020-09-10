package kr.puze.cog

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Context
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
import android.widget.Toast
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
            val phoneDataList: ArrayList<PhoneData> = getContacts(this@MainActivity)
            Log.d("LOGTAG/CONTACTS", "$phoneDataList")
            addContact(phoneDataList)
        }
        button_cog.setOnClickListener {
            if(isNumber && (edit_money.text.trim().isNotEmpty())){
                addCog(spinner_phone.selectedItemPosition, edit_money.text.toString().toInt())
            }
        }
    }

    private fun getFirebaseData(){
        getSpinner()
        getRecyclerData()
    }

    private fun addContact(contactList: ArrayList<PhoneData>){
        var phoneNum = getPhone()
        if(phoneNum != null){
            if(phoneNum.startsWith("+82")){
                phoneNum = phoneNum.replace("+82", "0")
            }
            prefUtil.phone = phoneNum
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val reference: DatabaseReference = database.getReference("Address")
            reference.child(phoneNum).setValue(contactList).addOnCompleteListener {
                if(it.isSuccessful){
                    toastUtil.short("전화번호부 업데이트 성공.")
                }else{
                    toastUtil.short("전화번호부 업데이트 실패.")
                }
                dismiss()
            }
        }else{
            dismiss()
            toastUtil.short("해당 기기의 전화번호를 알지 못하면 전화번호부를 등록할 수 없습니다.")
        }
    }

    private fun getPhone(): String? {
        val phoneMgr = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            null
        } else phoneMgr.line1Number
    }

    private fun addCog(index: Int, money: Int){
        val timeStamp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().timeInMillis)
        val cogData = CogData(phoneList[index].name, phoneList[index].tel, timeStamp, money, 0, 0)
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val reference: DatabaseReference = database.getReference("Cogs")
        reference.child(prefUtil.phone).child(phoneList[index].tel).setValue(cogData).addOnCompleteListener {
            if(it.isSuccessful){
                toastUtil.short("장부 등록 성공.")
            }else{
                toastUtil.short("장부 등록 실패.")
            }
            dismiss()
        }
    }

    private fun getContacts(context: Context): ArrayList<PhoneData>{
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
                val number: String = cursor.getString(numberIndex)

                val phoneData: PhoneData =
                    PhoneData(id, name, number)
                phoneDataList.add(phoneData)
            }
            cursor.close()
        }
        return phoneDataList
    }

    private fun getSpinner(){
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val reference: DatabaseReference = database.getReference("Address")
        reference.child(prefUtil.phone).addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                toastUtil.short("데이터 읽기 실패.")
                spinnerList.clear()
                spinnerList.add("010-1234-1234")
                spinnerList.add("010-1234-1234")
                spinnerList.add("010-1234-1234")
                setSpinner(spinnerList)
            }

            override fun onDataChange(dataSnapShot: DataSnapshot) {
                spinnerList.clear()
                phoneList.clear()
                dataSnapShot.children.forEach{
                    it.getValue(PhoneData::class.java)?.let { data ->
                        phoneList.add(PhoneData(data.id, data.name, data.tel))
                        data.tel?.let { it -> spinnerList.add(it) }
                    }
                    setSpinner(spinnerList)
                }
            }
        })
    }

    private fun setSpinner(items: ArrayList<String>){
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        spinner_phone.adapter = spinnerAdapter
        spinner_phone.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                Log.d("LOGTAG/PHONEACTIVITY", "${parent.getItemAtPosition(position)}")
                isNumber = true
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                isNumber = false
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
                cogList.clear()
                cogList.add(CogData("이름","010-1234-1234","2020.01.01",30000,20000,2))
                cogList.add(CogData("이름","010-1234-1234","2020.01.01",30000,20000,2))
                cogList.add(CogData("이름","010-1234-1234","2020.01.01",30000,20000,2))
                setRecyclerView(cogList)
            }

            override fun onDataChange(dataSnapShot: DataSnapshot) {
                cogList.clear()
                dataSnapShot.children.forEach{
                    it.getValue(CogData::class.java)?.let { data ->
                        cogList.add(CogData(data.name, data.number, data.date, data.money, data.pay, data.count))
                    }
                    setRecyclerView(cogList)
                }
            }
        })
    }

    private fun setRecyclerView(items: ArrayList<CogData>){
        cogAdapter = CogRecyclerAdapter(items, this@MainActivity, toastUtil, dialogUtil, prefUtil.phone)
        recycler_main.adapter = cogAdapter
        (recycler_main.adapter as CogRecyclerAdapter).notifyDataSetChanged()
    }

    companion object{
        lateinit var cogAdapter: CogRecyclerAdapter
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
}