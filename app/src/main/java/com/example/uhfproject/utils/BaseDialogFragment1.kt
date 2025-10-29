package com.example.uhfproject.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding

/**
 * 完全由xml控制宽高的
 */
abstract class BaseDialogFragment1<T : ViewBinding> : DialogFragment() {

    private var _binding: T? = null
    protected val mBinding get() = _binding!!

    abstract fun initBinding(inflater: LayoutInflater, container: ViewGroup?): T

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = initBinding(inflater, container)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        initView()
        initData()
        initListener()
    }
    override fun onStart() {
        super.onStart()
        // 在onStart中设置窗口属性，确保布局正确应用
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected abstract fun initView()
    protected abstract fun initData()
    protected abstract fun initListener()
}