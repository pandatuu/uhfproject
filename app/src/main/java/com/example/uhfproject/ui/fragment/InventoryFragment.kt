package com.example.uhfproject.ui.fragment

import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uhfproject.R
import com.example.uhfproject.app.MyApplication.Companion.appContext
import com.example.uhfproject.databinding.FragmentInventoryBinding
import com.example.uhfproject.ui.BaseFragment
import com.example.uhfproject.utils.Const
import com.example.uhfproject.utils.Const.inventoryPower
import com.seuic.uhf.UHFService

class InventoryFragment : BaseFragment<FragmentInventoryBinding>() {

    private val mAdapter: CommonItemAdapter by lazy {
        CommonItemAdapter()
    }

    override fun initView() {

        mBinding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
    }

    override fun initData() {
        UHFService.getInstance(appContext).power = inventoryPower

        mainViewModel.snSort = 0
        mainViewModel.trackingIdSort = 0
        mainViewModel.postCodeSort = 0
        mainViewModel.bitCodeSort = 0
        mainViewModel.otherSort = 0


        mBinding.tvBack.setOnClickListener {
            Const.simpleAlert(
                requireContext(),
                getString(R.string.inventory_click_back_title),
                getString(R.string.inventory_click_back_hint)
            ) {
                findNavController().popBackStack()
            }
        }
        mBinding.imgPower.setOnClickListener {
            Const.simpleEditAlert(
                requireContext(),
                getString(R.string.inventory_click_power_hint),
                inventoryPower.toString()
            ) {
                inventoryPower = it
                UHFService.getInstance(appContext).power = Const.outBoundPower
            }
        }
        mBinding.snHeadLayout.setOnClickListener {
            mainViewModel.snSort++
            when (mainViewModel.snSort) {
                1 -> {
                    mBinding.imgSn.setImageResource(R.drawable.ic_sort_down)
                }
                2 -> {
                    mBinding.imgSn.setImageResource(R.drawable.ic_sort_up)
                }
                3 -> {
                    mBinding.imgSn.setImageResource(0)
                    mainViewModel.snSort = 0
                }
            }
            mainViewModel.getListBySort()
        }
        mBinding.trackingIdHeadLayout.setOnClickListener {
            mainViewModel.trackingIdSort++
            when (mainViewModel.trackingIdSort) {
                1 -> {
                    mBinding.imgTrackingId.setImageResource(R.drawable.ic_sort_down)
                }
                2 -> {
                    mBinding.imgTrackingId.setImageResource(R.drawable.ic_sort_up)
                }
                3 -> {
                    mBinding.imgTrackingId.setImageResource(0)
                    mainViewModel.trackingIdSort = 0
                }
            }
            mainViewModel.getListBySort()
        }
        mBinding.postCodeHeadLayout.setOnClickListener {
            mainViewModel.postCodeSort++
            when (mainViewModel.postCodeSort) {
                1 -> {
                    mBinding.imgPostCode.setImageResource(R.drawable.ic_sort_down)
                }
                2 -> {
                    mBinding.imgPostCode.setImageResource(R.drawable.ic_sort_up)
                }
                3 -> {
                    mBinding.imgPostCode.setImageResource(0)
                    mainViewModel.postCodeSort = 0
                }
            }
            mainViewModel.getListBySort()
        }
        mBinding.bitCodeHeadLayout.setOnClickListener {
            mainViewModel.bitCodeSort++
            when (mainViewModel.bitCodeSort) {
                1 -> {
                    mBinding.imgBitCode.setImageResource(R.drawable.ic_sort_down)
                }
                2 -> {
                    mBinding.imgBitCode.setImageResource(R.drawable.ic_sort_up)
                }
                3 -> {
                    mBinding.imgBitCode.setImageResource(0)
                    mainViewModel.bitCodeSort = 0
                }
            }
            mainViewModel.getListBySort()
        }
        mBinding.otherHeadLayout.setOnClickListener {
            mainViewModel.otherSort++
            when (mainViewModel.otherSort) {
                1 -> {
                    mBinding.imgOther.setImageResource(R.drawable.ic_sort_down)
                }
                2 -> {
                    mBinding.imgOther.setImageResource(R.drawable.ic_sort_up)
                }
                3 -> {
                    mBinding.imgOther.setImageResource(0)
                    mainViewModel.otherSort = 0
                }
            }
            mainViewModel.getListBySort()
        }
    }

    override fun observeData() {
        mainViewModel.inventoryList.observe(viewLifecycleOwner) {
            mAdapter.setList(it)
        }
    }
}