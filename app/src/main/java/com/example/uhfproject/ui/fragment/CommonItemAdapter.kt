package com.example.uhfproject.ui.fragment

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.uhfproject.R

data class AdapterItem(
    var one: String,
    var two: String,
    var three: String,
    var four: String
)

class CommonItemAdapter: BaseQuickAdapter<AdapterItem, BaseViewHolder>(R.layout.item_rv_scan) {

    override fun convert(holder: BaseViewHolder, item: AdapterItem) {
        holder.setText(R.id.item_sn, item.one)
        holder.setText(R.id.item_traking_id, item.two)
        holder.setText(R.id.item_post_code, item.three)
        holder.setText(R.id.item_bit_code, item.four)
    }
}