package com.example.uhfproject.ui.fragment.apple

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.uhfproject.R
import com.example.uhfproject.model.ExcelDownloadVO

class CommonItemAdapter(private val clickItem:(ExcelDownloadVO)->Unit) : RecyclerView.Adapter<CommonItemAdapter.ItemViewHolder>() {
    private val items = mutableListOf<ExcelDownloadVO>()

    fun submitList(newList: List<ExcelDownloadVO>) {
        val updatedList = newList.mapIndexed { index, item ->
            item.copy(position = index + 1) // 为每个item重新计算序号
        }
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size
            override fun getNewListSize() = updatedList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition].epc == updatedList[newItemPosition].epc
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition] == updatedList[newItemPosition]
            }
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items.clear()
        items.addAll(updatedList)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rv_scan, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.snTv.text = item.position.toString()
        holder.trakingIdTv.text = item.trackingNumber.toString()
        holder.posralCodeTv.text = item.postalCode.toString()
        holder.beatTv.text = item.beat.toString()
        holder.otherTv.text = item.db.toString()
        holder.itemView.setOnClickListener {
            clickItem.invoke(item)
        }
    }
    override fun getItemCount() = items.size

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val snTv: TextView = itemView.findViewById(R.id.item_sn)
        val trakingIdTv: TextView = itemView.findViewById(R.id.item_traking_id)
        val posralCodeTv: TextView = itemView.findViewById(R.id.item_postal_code)
        val beatTv: TextView = itemView.findViewById(R.id.item_beat)
        val otherTv: TextView = itemView.findViewById(R.id.item_other)
    }

    fun getList() = items
}
