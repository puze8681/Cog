package kr.puze.cog

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_recycler_cog.view.*
import kr.puze.cog.Data.CogData

class CogRecyclerAdapter(var items: ArrayList<CogData>, var context: Context) : RecyclerView.Adapter<CogRecyclerAdapter.ViewHolder>() {
    @SuppressLint("LongLogTag")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("LOGTAG, ChatRecyclerAdapter", "onCreate")
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recycler_cog, null))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        holder.itemView.setOnClickListener {
            itemClick?.onItemClick(holder.itemView, position)
        }

        holder.itemView.button_add_cog.setOnClickListener {  }
        holder.itemView.button_edit_cog.setOnClickListener {  }
        holder.itemView.button_delete_cog.setOnClickListener {  }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context = itemView.context!!
        fun bind(item: CogData) {
            itemView.text_number_cog.text = item.number
            itemView.text_date_cog.text = item.date
            itemView.text_money_cog.text = "${item.money}"
            itemView.text_pay_cog.text = "${item.pay}"
            itemView.text_count_cog.text = "${item.count}"
        }
    }

    var itemClick: ItemClick? = null

    interface ItemClick {
        fun onItemClick(view: View?, position: Int)
    }
}
