package com.example.uhfproject.ui.fragment

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.uhfproject.R
import com.example.uhfproject.model.ExcelDownloadVO

class CommonItemAdapter: BaseQuickAdapter<ExcelDownloadVO, BaseViewHolder>(R.layout.item_rv_scan) {

    override fun convert(holder: BaseViewHolder, item: ExcelDownloadVO) {
        holder.setText(R.id.item_sn, item.sn.toString())
        holder.setText(R.id.item_traking_id, item.trackingNumber?:"")
        holder.setText(R.id.item_postal_code, item.postalCode?:"")
        holder.setText(R.id.item_beat, item.beat?:"")
        holder.setText(R.id.item_other, item.db?:"")
    }
}