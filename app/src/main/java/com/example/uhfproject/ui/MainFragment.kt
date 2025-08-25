package com.example.uhfproject.ui

import androidx.navigation.fragment.findNavController
import com.example.uhfproject.R
import com.example.uhfproject.databinding.FragmentMainBinding
import com.example.uhfproject.utils.LogUtil

class MainFragment: BaseFragment<FragmentMainBinding>() {

    override fun initView() {
        LogUtil.d("MainFragment-initView")
    }

    override fun initData() {
        mBinding.btnInbound.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_inBoundFragment)
        }
        mBinding.btnOutbound.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_outBoundFragment)
        }
        mBinding.btnInventory.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_inventoryFragment)
        }
        mBinding.btnFindItem.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_findItemFragment)
        }
    }

    override fun observeData() {

    }
}