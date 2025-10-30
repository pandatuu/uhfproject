package com.example.uhfproject.ui.fragment.pon

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.uhfproject.R
import com.example.uhfproject.app.MyApplication
import com.example.uhfproject.databinding.FragmentPonCommonBinding
import com.example.uhfproject.model.TrackingVO
import com.example.uhfproject.utils.BaseFragment
import com.example.uhfproject.utils.Const.fromJsonToList
import com.example.uhfproject.utils.Const.ponExceptionsPower
import com.example.uhfproject.utils.ScanMode
import com.seuic.uhf.UHFService

class FragmentPonExceptions: BaseFragment<FragmentPonCommonBinding>(){

    private lateinit var mAdapter: ArrayAdapter<String>
    private var jsonList: List<TrackingVO> = emptyList()

    private val rvAdapter: PonExceptionAdapter by lazy { PonExceptionAdapter() }

    override fun initView() {
        arguments?.getString("exceptionList")?.let {
            jsonList = it.fromJsonToList()
        }
        mainViewModel.setScanMode(ScanMode.NONE)
        mAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item_draw, mainViewModel.siteList.map { it.siteName?:"" })
        mAdapter.setDropDownViewResource(R.layout.spinner_item)
        mBinding.spinner.adapter = mAdapter
        mBinding.spinner.setSelection(0)
        mainViewModel.currentSiteId = mainViewModel.siteList[0].siteId.toIntOrNull()?:-1
        mBinding.tvTitle.text = "Exceptions"
        mBinding.tvPostOffice.text = "Reason"
        mBinding.btnUpload.visibility = View.GONE
        mBinding.btnClear.visibility = View.GONE

        mBinding.inboundRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
        }
    }

    override fun initData() {
        rvAdapter.setList(jsonList.filter { it.siteId == mainViewModel.currentSiteId })
        mainViewModel.getBoundAndInboundCount {
            mBinding.tvProgress.text = "Progress:${it.inboundCount}/${it.boundCount}"
        }

        mBinding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                mainViewModel.currentSiteId = mainViewModel.siteList[position].siteId.toIntOrNull()?:-1
                rvAdapter.setList(jsonList.filter { it.siteId == mainViewModel.currentSiteId })
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
            showDialogPower(ponExceptionsPower){
                UHFService.getInstance(MyApplication.appContext).power = it
                ponExceptionsPower = it
            }
        }
        mBinding.btnClear.setOnClickListener {
            rvAdapter.setList(emptyList())
        }
    }

    override fun observeData() {

    }
}
class PonExceptionAdapter: BaseQuickAdapter<TrackingVO, BaseViewHolder>(R.layout.item_pon_inbound){
    override fun convert(holder: BaseViewHolder, item: TrackingVO) {
        holder.setText(R.id.item_sn, (holder.absoluteAdapterPosition+1).toString())
        holder.setText(R.id.item_traking_id, item.trackingNumber)
        val status = if(item.resultStatus == 0) "No Record" else "Other Post Office"
        holder.setText(R.id.item_post_office, status)
    }
}