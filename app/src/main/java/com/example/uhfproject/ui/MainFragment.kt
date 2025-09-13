package com.example.uhfproject.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.uhfproject.R
import com.example.uhfproject.databinding.FragmentMainBinding
import com.example.uhfproject.utils.Const
import com.example.uhfproject.utils.retrofit.RetrofitClient

class MainFragment: Fragment() {

    private lateinit var mBinding: FragmentMainBinding

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentMainBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.loginUser.text = "Welcome, ${mainViewModel.username}"

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
                RetrofitClient.updateTokenAndRefreshToken("")
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }
        }

    }
}