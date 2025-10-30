package com.example.uhfproject.ui.fragment.pon

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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

    private lateinit var mAdapter: ArrayAdapter<String>

    private val rvAdapter: PonInboundAdapter by lazy { PonInboundAdapter() }

    override fun initView() {
        mainViewModel.setScanMode(ScanMode.PON_INBOUND)
        mAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item_draw, mainViewModel.siteList.map { it.siteName?:"" })
        mAdapter.setDropDownViewResource(R.layout.spinner_item)

        mBinding.spinner.adapter = mAdapter
        mBinding.spinner.setSelection(0)
        mainViewModel.currentSiteId = mainViewModel.siteList[0].siteId.toIntOrNull()?:-1

        mBinding.inboundRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
        }
    }

    override fun initData() {
        mainViewModel.getBoundAndInboundCount {
            mBinding.tvProgress.text = "Progress:${it.inboundCount}/${it.boundCount}"
        }
        UHFService.getInstance(MyApplication.appContext).power = ponInboundPower

        mBinding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                mainViewModel.currentSiteId = mainViewModel.siteList[position].siteId.toIntOrNull()?:-1
                mainViewModel.getBoundAndInboundCount {
                    mBinding.tvProgress.text = "Progress:${it.inboundCount}/${it.boundCount}"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
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
            val exceptionList = mainViewModel.ponException.value
            val bundle = Bundle()
            bundle.putString("exceptionList", Gson().toJson(exceptionList))
            findNavController().navigate(R.id.action_fragmentPonInbound_to_fragmentPonExceptions, bundle)
        }
        mBinding.btnClear.setOnClickListener {
            rvAdapter.setList(emptyList())
            mainViewModel.exitPonInbound()
            mBinding.btnExceptions.visibility = View.GONE
        }
        mBinding.btnUpload.setOnClickListener {
            mainViewModel.submitInBoundPon{
                lifecycleScope.launch(Dispatchers.Main) {
                    rvAdapter.setList(emptyList())
                }
                mainViewModel.matchList.clear()
                mainViewModel.otherSiteList.clear()
                mainViewModel.getBoundAndInboundCount {
                    lifecycleScope.launch(Dispatchers.Main){
                        mBinding.tvProgress.text = "Progress:${it.inboundCount}/${it.boundCount}"
                    }
                }
            }
        }
        mBinding.tvBack.setOnClickListener {
            findNavController().popBackStack()
        }
        mBinding.imgPower.setOnClickListener {
            showDialogPower(ponInboundPower){
                UHFService.getInstance(MyApplication.appContext).power = it
                ponInboundPower = it
            }
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