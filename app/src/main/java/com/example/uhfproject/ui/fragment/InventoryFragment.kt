package com.example.uhfproject.ui.fragment

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uhfproject.R
import com.example.uhfproject.databinding.FragmentInventoryBinding
import com.example.uhfproject.model.ExcelDownloadVO
import com.example.uhfproject.utils.BaseFragment
import com.example.uhfproject.utils.Const.simpleAlert
import com.scwang.smart.refresh.footer.ClassicsFooter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InventoryFragment : BaseFragment<FragmentInventoryBinding>() {

    private val mAdapter: CommonItemAdapter by lazy {
        CommonItemAdapter()
    }

    override fun initView() {
        mainViewModel.startQuest = false
        mBinding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
        mBinding.tvRfidCount.text = mAdapter.data.size.toString()

        mBinding.smartRefresh.apply {
            setRefreshFooter(ClassicsFooter(requireContext()))
            setOnLoadMoreListener {
                mainViewModel.getListBySort({
                    it.finishLoadMore(500)
                }, {
                    it.finishLoadMoreWithNoMoreData()
                })
            }
        }
    }

    override fun initData() {
        mainViewModel.snSort = 0
        mainViewModel.trackingIdSort = 0
        mainViewModel.postCodeSort = 0
        mainViewModel.bitCodeSort = 0
        mainViewModel.otherSort = 0

        mBinding.tvBack.setOnClickListener {
            findNavController().popBackStack()
        }
        mAdapter.setOnItemClickListener{adapter, v ,position ->
            simpleAlert(requireContext(), "Jump to FindItem page?"){
                val item = adapter.getItem(position) as ExcelDownloadVO
                val bundle = Bundle()
                bundle.putParcelable("item", item)
                findNavController().navigate(R.id.action_inventoryFragment_to_findItemFragment, bundle)
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
            mainViewModel.listSort()
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
            mainViewModel.listSort()
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
            mainViewModel.listSort()
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
            mainViewModel.listSort()
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
            mainViewModel.listSort()
        }
    }

    override fun observeData() {
        mainViewModel.inventoryList.observe(viewLifecycleOwner) {
            mAdapter.setList(it)
            mBinding.tvRfidCount.text = mAdapter.data.size.toString()
        }
        mainViewModel.sortList.observe(viewLifecycleOwner) {
            mAdapter.setList(it)
            mBinding.tvRfidCount.text = mAdapter.data.size.toString()
        }
    }

    override fun onResume() {
        super.onResume()

        mainViewModel.getListBySort({
            lifecycleScope.launch(Dispatchers.Main){
                mBinding.smartRefresh.finishLoadMore(500)
            }
        }, {
            lifecycleScope.launch(Dispatchers.Main){
                mBinding.smartRefresh.finishLoadMoreWithNoMoreData()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.clearInventoryList()
    }
}