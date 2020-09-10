package kr.puze.cog

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
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
import kotlinx.android.synthetic.main.activity_main.*
import kr.puze.cog.Data.CogData
import kr.puze.cog.Data.PhoneData
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("HardwareIds")
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getFirebaseData()

        button_contact.setOnClickListener {
            val phoneDataList: ArrayList<PhoneData> = getContacts(this@MainActivity)
            Log.d("LOGTAG/CONTACTS", "$phoneDataList")
            addContact(phoneDataList)
        }

        button_cog.setOnClickListener {
            if(isNumber && (edit_money.text.trim().isNotEmpty())){
                addCog(spinner_phone.selectedItem.toString(), edit_money.text.toString().toInt())
            }
        }
    }

    private fun getFirebaseData(){
        val phoneDataList = ArrayList<PhoneData>()
        getSpinner(phoneDataList)
        getRecyclerData()
    }

    private fun addContact(contactList: ArrayList<PhoneData>){
        var phoneNum = getPhone()
        if(phoneNum != null){
            if(phoneNum.startsWith("+82")){
                phoneNum = phoneNum.replace("+82", "0")
            }
        }else{
            Toast.makeText(this@MainActivity, "해당 기기의 전화번호를 알지 못하면 전화번호부를 등록할 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getPhone(): String? {
        val phoneMgr = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            null
        } else phoneMgr.line1Number
    }


    private fun addCog(phone: String, money: Int){
        val timeStamp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().timeInMillis)
        val cogData: CogData = CogData(phone, timeStamp, money, 0, 0)
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

    private fun getSpinner(items: ArrayList<PhoneData>){
        val spinnerList = ArrayList<String>()
        for (item in items){
            item.tel?.let { spinnerList.add(it) }
        }
        setSpinner(spinnerList)
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
        cogList.clear()
        cogList.add(CogData("010-9790-8310","2020.01.01",30000,20000,2))
        cogList.add(CogData("010-9790-8310","2020.01.01",30000,20000,2))
        cogList.add(CogData("010-9790-8310","2020.01.01",30000,20000,2))
        cogList.add(CogData("010-9790-8310","2020.01.01",30000,20000,2))
        cogList.add(CogData("010-9790-8310","2020.01.01",30000,20000,2))
        setRecyclerView(cogList)
    }

    private fun setRecyclerView(items: ArrayList<CogData>){
        cogAdapter = CogRecyclerAdapter(items, this@MainActivity)
        recycler_main.adapter = cogAdapter
        (recycler_main.adapter as CogRecyclerAdapter).notifyDataSetChanged()
    }

    companion object{
        lateinit var cogAdapter: CogRecyclerAdapter
        val cogList = ArrayList<CogData>()
        var isNumber = false
    }
}