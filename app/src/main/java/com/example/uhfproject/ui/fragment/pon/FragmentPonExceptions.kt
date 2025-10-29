package com.example.uhfproject.ui.fragment.pon

import android.view.View
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.example.uhfproject.app.MyApplication
import com.example.uhfproject.databinding.FragmentPonCommonBinding
import com.example.uhfproject.utils.BaseFragment
import com.example.uhfproject.utils.Const.ponExceptionsPower
import com.seuic.uhf.UHFService

class FragmentPonExceptions: BaseFragment<FragmentPonCommonBinding>(){

    private val dataList = arrayOf("选项一", "选项二", "选项三")
    private val mAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dataList)

    override fun initView() {
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mBinding.spinner.adapter = mAdapter
        mBinding.tvTitle.text = "Exceptions"
        mBinding.tvPostOffice.text = "Reason"
        mBinding.btnUpload.visibility = View.GONE
    }

    override fun initData() {
        mBinding.tvBack.setOnClickListener {
            findNavController().popBackStack()
        }
        mBinding.imgPower.setOnClickListener {
            showDialogPower(ponExceptionsPower){
                UHFService.getInstance(MyApplication.appContext).power = it
                ponExceptionsPower = it
            }
        }
    }

    override fun observeData() {

    }
}