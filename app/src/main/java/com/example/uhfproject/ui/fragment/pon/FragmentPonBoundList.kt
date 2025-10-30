package com.example.uhfproject.ui.fragment.pon

import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.uhfproject.R
import com.example.uhfproject.databinding.FragmentPonCommonBinding
import com.example.uhfproject.model.TrackingVO
import com.example.uhfproject.utils.BaseFragment

class FragmentPonBoundList: BaseFragment<FragmentPonCommonBinding>(){

    private val rvAdapter: BoundListAdapter by lazy { BoundListAdapter() }

    override fun initView() {
        mBinding.spinner.visibility = View.GONE
        mBinding.tvSite.visibility = View.VISIBLE
        mBinding.tvSite.text = mainViewModel.siteList.find { it.siteId.toIntOrNull() == mainViewModel.siteIdByUser }?.siteName?:"none"
        mainViewModel.currentSiteId = mainViewModel.siteIdByUser
        mBinding.spinner.isEnabled = false
        mBinding.spinner.isClickable = false
        mBinding.spinner.isSelected = false
        mBinding.tvTitle.text = "Bound List"
        mBinding.tvTrackingId.text = "RFID/Tracking#"
        mBinding.tvPostOffice.text = "Bound Time"
        mBinding.btnUpload.visibility = View.GONE
        mBinding.btnClear.visibility = View.GONE
        mBinding.tvProgress.text = "Progress:0"

        mBinding.inboundRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
        }
    }

    override fun initData() {
        mainViewModel.getBoundAndInboundCount {
            mBinding.tvProgress.text = "Progress:${it.boundCount}"
        }
        mainViewModel.getBoundDataBySiteId {
            rvAdapter.setList(it)
        }
        mBinding.tvBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun observeData() {

    }
}
class BoundListAdapter: BaseQuickAdapter<TrackingVO, BaseViewHolder>(R.layout.item_pon_inbound){
    override fun convert(holder: BaseViewHolder, item: TrackingVO) {
        holder.setText(R.id.item_sn, (holder.absoluteAdapterPosition+1).toString())
        holder.setText(R.id.item_traking_id, item.trackingNumber)
        holder.setText(R.id.item_post_office, item.uploadTime)
    }
}