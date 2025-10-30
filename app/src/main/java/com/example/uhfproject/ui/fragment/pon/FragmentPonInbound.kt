package com.example.uhfproject.ui.fragment.pon

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Visibility
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.uhfproject.R
import com.example.uhfproject.app.MyApplication
import com.example.uhfproject.databinding.FragmentPonCommonBinding
import com.example.uhfproject.model.TrackingVO
import com.example.uhfproject.utils.BaseFragment
import com.example.uhfproject.utils.Const.ponInboundPower
import com.example.uhfproject.utils.ScanMode
import com.google.gson.Gson
import com.seuic.uhf.UHFService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FragmentPonInbound: BaseFragment<FragmentPonCommonBinding>(){

    private val dataList = arrayOf("选项一", "选项二", "选项三")
    private val mAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dataList)

    private val rvAdapter: PonInboundAdapter by lazy { PonInboundAdapter() }

    override fun initView() {
        mainViewModel.setScanMode(ScanMode.PON_INBOUND)
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mBinding.spinner.adapter = mAdapter
        mBinding.inboundRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
        }
    }

    override fun initData() {
        mBinding.tvBack.setOnClickListener {
            findNavController().popBackStack()
        }
        mBinding.imgPower.setOnClickListener {
            showDialogPower(ponInboundPower){
                UHFService.getInstance(MyApplication.appContext).power = it
                ponInboundPower = it
            }
        }
        mBinding.btnExceptions.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("exceptionList", Gson().toJson(mainViewModel.ponException.value))
            findNavController().navigate(R.id.action_fragmentPonInbound_to_fragmentPonExceptions)
        }
    }

    override fun observeData() {
        mainViewModel.ponInboundExcel.observe(viewLifecycleOwner){
            rvAdapter.setList(it)
        }
        mainViewModel.ponException.observe(viewLifecycleOwner){
            lifecycleScope.launch(Dispatchers.Main){
                if (it.isNotEmpty()){
                    mBinding.btnExceptions.text = "Exceptions:${it.size}"
                    mBinding.btnExceptions.visibility = View.VISIBLE
                }else {
                    mBinding.btnExceptions.text = "Exceptions:0"
                    mBinding.btnExceptions.visibility = View.GONE
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mainViewModel.exitPonInbound()
    }
}

class PonInboundAdapter: BaseQuickAdapter<TrackingVO, BaseViewHolder>(R.layout.item_pon_inbound){
    override fun convert(holder: BaseViewHolder, item: TrackingVO) {
        holder.setText(R.id.item_sn, (holder.absoluteAdapterPosition+1).toString())
        holder.setText(R.id.item_traking_id, item.trackingNumber)
        holder.setText(R.id.item_post_office, item.siteName)
    }
}