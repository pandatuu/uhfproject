package com.example.uhfproject.ui

import android.content.Intent
import androidx.navigation.fragment.findNavController
import com.example.uhfproject.R
import com.example.uhfproject.databinding.FragmentMainBinding
import com.example.uhfproject.utils.BaseFragment
import com.example.uhfproject.utils.Const

class MainFragment: BaseFragment<FragmentMainBinding>() {

    override fun initView() {
        mBinding.loginUser.text = "Welcome, ${mainViewModel.username}"
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
        mBinding.btnDashboard.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_dashboardFragment)
        }
        mBinding.logoutBtn.setOnClickListener {
            Const.simpleAlert(requireContext(), "Log out?"){
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }
        }
    }

    override fun observeData() {

    }
}