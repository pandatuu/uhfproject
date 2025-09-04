package com.example.uhfproject.ui.fragment

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.uhfproject.R
import com.example.uhfproject.databinding.FragmentDashboardBinding
import com.example.uhfproject.model.DashboardTopVO
import com.example.uhfproject.utils.BaseFragment
import com.example.uhfproject.utils.Const
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class DashboardFragment:BaseFragment<FragmentDashboardBinding>() {

    private val mAdapter: DashboardAdapter by lazy { DashboardAdapter() }

    override fun initView() {
        mBinding.dashboardRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
    }

    override fun initData() {
        mainViewModel.getHeadInfo{
            lifecycleScope.launch(Dispatchers.Main){
                mBinding.totalNum.text = formatNumberWithCommas(it.total?:0)
                mBinding.completedInbound.text = it.inboundNumber.toString()
                mBinding.completedOutbound.text = it.outboundNumber.toString()
                mBinding.pendingInbound.text = it.pendingInboundNumber.toString()
                mBinding.pendingOutbound.text = it.outboundRemainingNumber.toString()
            }
        }
        mainViewModel.getRvList {
            lifecycleScope.launch(Dispatchers.Main){
                mAdapter.setList(it)
            }
        }
        mBinding.tvBack.setOnClickListener {
            exit()
        }
        mBinding.pendingInboundLayout.setOnClickListener {
            showCheckBoundFragment("Pending Inbound")
        }
        mBinding.pendingOutboundLayout.setOnClickListener {
            showCheckBoundFragment("Pending Outbound")
        }
    }

    override fun observeData() {

    }

    private fun exit(){
        findNavController().popBackStack()
    }

    private fun formatNumberWithCommas(number: Int): String {
        val numberFormat = NumberFormat.getNumberInstance(Locale.US) // 使用美国 locale（逗号分隔）
        return numberFormat.format(number)
    }
}

class DashboardAdapter: BaseQuickAdapter<DashboardTopVO, BaseViewHolder>(R.layout.item_dashboard){
    override fun convert(holder: BaseViewHolder, item: DashboardTopVO) {
        holder.setText(R.id.item_sn, item.sn.toString())
        holder.setText(R.id.item_db, item.db.toString())
        holder.setText(R.id.item_total, item.total.toString())
        holder.setText(R.id.item_inbound_completed, item.inboundComplete.toString())
        holder.setText(R.id.item_outbound_completed, item.outboundComplete.toString())
    }

}