package kr.puze.cog

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.android.synthetic.main.item_recycler_cog.view.*
import kr.puze.cog.Data.CogData
import www.okit.co.Utils.DialogUtil
import www.okit.co.Utils.ToastUtil
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CogRecyclerAdapter(var items: ArrayList<CogData>, var context: Context, var toastUtil: ToastUtil, var dialogUtil: DialogUtil) : RecyclerView.Adapter<CogRecyclerAdapter.ViewHolder>() {
    @SuppressLint("LongLogTag")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("LOGTAG, ChatRecyclerAdapter", "onCreate")
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recycler_cog, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        holder.itemView.setOnClickListener {
            itemClick?.onItemClick(holder.itemView, position)
        }

        holder.itemView.button_add_cog.setOnClickListener { dialogUtil.dialogPay(items[position]) }
        holder.itemView.button_edit_cog.setOnClickListener { dialogUtil.dialogEdit(items[position]) }
        holder.itemView.button_delete_cog.setOnClickListener {
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val reference: DatabaseReference = database.getReference("Cogs")
            reference.child(items[position].number).ref.removeValue().addOnCompleteListener {
                if(it.isSuccessful){
                    toastUtil.short("장부 삭제 성공.")
                }else{
                    toastUtil.short("장부 삭제 실패.")
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context = itemView.context!!
        fun bind(item: CogData) {

            val simpleDateFormat = (SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()))
            val dates = ArrayList<CalendarDay>()

            var dateString = "대출: "
            for (i in item.date){
                dateString += "$i, "
                val date = simpleDateFormat.parse(i)
                val yy = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)
                val mm = SimpleDateFormat("MM", Locale.getDefault()).format(date)
                val dd = SimpleDateFormat("dd", Locale.getDefault()).format(date)
                Log.d("LOGTAG/COGDATA", " $yy $mm $dd")
                dates.add(CalendarDay.from(yy.toInt(),  mm.toInt()-1, dd.toInt())) // year, month, date
            }

            var payerString = "입금자명: "
            for (i in item.payName){
                payerString += "$i, "
            }

            Log.d("LOGTAG/COGDATA", " ${item.payCount}번(${item.pay}원)")

            itemView.text_name_item_cog.text = "${item.name}"
            itemView.text_number_item_cog.text = item.number
            itemView.text_money_item_cog.text = "대출금액: ${item.loanMoney} 원"
            itemView.text_pay_item_cog.text = "납입횟수: ${item.payCount}번(${item.pay}원)"
            itemView.text_unpaid_item_cog.text = "미납금액: ${item.loanMoney-item.pay}원"
            itemView.text_loan_count_item_cog.text = "대출횟수: ${item.loanCount}번"
            itemView.text_first_loan_date_item_cog.text = "첫대출: ${item.date[0]}"
            itemView.text_loan_date_item_cog.text = dateString
            itemView.text_payer_item_cog.text = payerString

            itemView.calendar_item_cog.addDecorators(EventDecorator(Color.RED, dates))
        }
    }

    var itemClick: ItemClick? = null

    interface ItemClick {
        fun onItemClick(view: View?, position: Int)
    }
}
