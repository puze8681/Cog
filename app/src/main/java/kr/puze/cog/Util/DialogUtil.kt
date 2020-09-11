package www.okit.co.Utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.Window
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.dialog_edit.*
import kotlinx.android.synthetic.main.dialog_pay.*
import kr.puze.cog.Data.CogData
import kr.puze.cog.R


@Suppress("DEPRECATION")
class DialogUtil(context: Context) {
    var context: Context = context
    var prefUtil: PrefUtil = PrefUtil(context)
    var toastUtil: ToastUtil

    init {
        prefUtil = PrefUtil(context)
        toastUtil = ToastUtil(context)
    }

    @SuppressLint("SetTextI18n")
    fun dialogPay(myPhone: String, cog: CogData) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_pay)
        dialog.edit_dialog_pay
        dialog.button_dialog_pay.setOnClickListener {
            addPay(myPhone, cog, dialog.edit_dialog_pay.text.toString().toInt())
            dialog.dismiss()
        }
        dialog.button_dialog_pay_cancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun addPay(myPhone: String, cog: CogData, pay: Int){
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val reference: DatabaseReference = database.getReference("Cogs")
        cog.pay = cog.pay + pay
        cog.count = cog.count + 1
        reference.child(myPhone).child(cog.number).setValue(cog).addOnCompleteListener {
            if(it.isSuccessful){
                toastUtil.short("추가 납입 성공.")
            }else{
                toastUtil.short("추가 납입 실패.")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun dialogEdit(myPhone: String, cog: CogData) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_edit)
        dialog.edit_number_dialog_edit.setText("${cog.number}")
        dialog.edit_money_dialog_edit.setText("${cog.money}")
        dialog.edit_pay_dialog_edit.setText("${cog.pay}")
        dialog.edit_count_dialog_edit.setText("${cog.count}")
        dialog.button_dialog_edit.setOnClickListener {
            editPay(myPhone, cog, dialog.edit_number_dialog_edit.text.toString(), dialog.edit_money_dialog_edit.text.toString().toInt(), dialog.edit_pay_dialog_edit.text.toString().toInt(), dialog.edit_count_dialog_edit.text.toString().toInt())
            dialog.dismiss()
        }
        dialog.button_dialog_edit_cancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun editPay(myPhone: String, cog: CogData, number: String, money: Int, pay: Int, count: Int){
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val reference: DatabaseReference = database.getReference("Cogs")
        reference.child(myPhone).child(cog.number).ref.removeValue().addOnCompleteListener {
            if(it.isSuccessful){
                cog.number = number
                cog.money = money
                cog.pay = pay
                cog.count = count
                reference.child(myPhone).child(number).setValue(cog).addOnCompleteListener {
                    if(it.isSuccessful){
                        toastUtil.short("장부 변경 성공.")
                    }else{
                        toastUtil.short("장부 변경 실패.")
                    }
                }
            }else{
                toastUtil.short("장부 변경 실패.")
            }
        }
    }
}