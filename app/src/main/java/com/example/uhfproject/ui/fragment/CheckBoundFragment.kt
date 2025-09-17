package com.example.uhfproject.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uhfproject.databinding.FragmentCheckBoundBinding
import com.example.uhfproject.utils.BaseDialogFragment
import com.scwang.smart.refresh.footer.ClassicsFooter
import kotlinx.coroutines.Dispatchers

class CheckBoundFragment(private val title: String) : BaseDialogFragment<FragmentCheckBoundBinding>() {

    private val mViewModel: CheckBoundViewModel by viewModels()

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
        mainViewModel?.startQuest = false
        mBinding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }

        mBinding.smartRefresh.apply {
            setRefreshFooter(ClassicsFooter(requireContext()))
            setOnLoadMoreListener {
                if(title == "Pending Inbound"){
                    mViewModel.getCheckInboundList({
                        it.finishLoadMore(500)
                    }, {
                        it.finishLoadMoreWithNoMoreData()
                    })
                }else{
                    mViewModel.getCheckOutboundList({
                        it.finishLoadMore(500)
                    }, {
                        it.finishLoadMoreWithNoMoreData()
                    })
                }
            }
        }
        mAdapter.submitList(emptyList())

        if(title == "Pending Inbound"){
            mViewModel.getCheckInboundList({
                mBinding.smartRefresh.finishLoadMore(500)
            }, {
                mBinding.smartRefresh.finishLoadMoreWithNoMoreData()
            })
        }else{
            mViewModel.getCheckOutboundList({
                mBinding.smartRefresh.finishLoadMore(500)
            }, {
                mBinding.smartRefresh.finishLoadMoreWithNoMoreData()
            })
        }
    }

    override fun initListener() {
        mViewModel.checkInbound.observe(viewLifecycleOwner){
            mAdapter.submitList(it)
        }
        mViewModel.checkOutbound.observe(viewLifecycleOwner){
            mAdapter.submitList(it)
        }
    }

    override fun initResume() {

    }
}

fun Fragment.showCheckBoundFragment(title: String){
    CheckBoundFragment(title).show(childFragmentManager, "CheckBoundFragment")
}