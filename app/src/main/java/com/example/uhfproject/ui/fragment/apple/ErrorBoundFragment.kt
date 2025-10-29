package com.example.uhfproject.ui.fragment.apple

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uhfproject.databinding.FragmentCheckBoundBinding
import com.example.uhfproject.model.ExcelDownloadVO
import com.example.uhfproject.utils.BaseDialogFragment

class ErrorBoundFragment(private val errorList: List<ExcelDownloadVO>) : BaseDialogFragment<FragmentCheckBoundBinding>() {

    private val mAdapter: CommonItemAdapter by lazy {
        CommonItemAdapter{}
    }

    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCheckBoundBinding = FragmentCheckBoundBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        dialogWidth = 322f
        dialogHeight = 480f
        mBinding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }

        mAdapter.submitList(errorList)
    }

    override fun initListener() {

    }

    override fun initResume() {

    }
}

fun Fragment.showErrorBoundFragment(title: List<ExcelDownloadVO>){
    ErrorBoundFragment(title).show(childFragmentManager, "ErrorBoundFragment")
}