package com.example.uhfproject.ui.fragment.pon

import android.view.View
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.example.uhfproject.databinding.FragmentPonCommonBinding
import com.example.uhfproject.utils.BaseFragment
import com.seuic.uhf.UHFService

class FragmentPonBoundList: BaseFragment<FragmentPonCommonBinding>(){

    private val dataList = arrayOf("选项一")
    private val mAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dataList)

    override fun initView() {
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mBinding.spinner.adapter = mAdapter
        mBinding.spinner.isEnabled = false
        mBinding.spinner.setSelection(0)
        mBinding.spinner.isClickable = false
        mBinding.spinner.isSelected = false
        mBinding.tvTitle.text = "Bound List"
        mBinding.tvTrackingId.text = "RFID/Tracking#"
        mBinding.tvPostOffice.text = "Bound Time"
        mBinding.btnUpload.visibility = View.GONE
        mBinding.btnClear.visibility = View.GONE
    }

    override fun initData() {
        mBinding.tvBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun observeData() {

    }
}