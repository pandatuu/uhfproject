package com.example.uhfproject.ui.fragment.pon

import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.example.uhfproject.app.MyApplication
import com.example.uhfproject.databinding.FragmentPonCommonBinding
import com.example.uhfproject.utils.BaseFragment
import com.example.uhfproject.utils.Const.ponInboundPower
import com.seuic.uhf.UHFService

class FragmentPonInbound: BaseFragment<FragmentPonCommonBinding>(){

    private val dataList = arrayOf("选项一", "选项二", "选项三")
    private val mAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dataList)

    override fun initView() {
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mBinding.spinner.adapter = mAdapter
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
    }

    override fun observeData() {

    }
}