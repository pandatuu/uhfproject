package com.example.uhfproject.ui

import androidx.lifecycle.lifecycleScope
import com.example.uhfproject.databinding.FragmentMainBinding
import com.example.uhfproject.utils.LogUtil
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.observeOn

class MainFragment: BaseFragment<FragmentMainBinding>() {

    override fun initView() {
        LogUtil.d("MainFragment-initView")
    }

    override fun initData() {

    }

    override fun observeData() {
        mainViewModel.epcList.observe(viewLifecycleOwner) {
            LogUtil.d("当前扫描的标签:$it")
        }
    }
}