package com.example.uhfproject.ui.fragment

import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uhfproject.R
import com.example.uhfproject.databinding.FragmentBoundBinding
import com.example.uhfproject.ui.BaseFragment
import com.example.uhfproject.utils.Const
import com.example.uhfproject.utils.Const.inBoundPower
import com.example.uhfproject.utils.Const.simpleAlert
import com.example.uhfproject.utils.Const.simpleEditAlert
import com.example.uhfproject.utils.LogUtil
import com.seuic.uhf.UHFService

class InBoundFragment : BaseFragment<FragmentBoundBinding>() {

    private val mAdapter: CommonItemAdapter by lazy {
        CommonItemAdapter()
    }

    override fun initView() {
        mBinding.tvTitle.text = getString(R.string.inbound_title)
        mBinding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
    }

    override fun initData() {
        UHFService.getInstance().power = inBoundPower

        mAdapter.setList(listOf(
            AdapterItem("1","AD123312412","123-123","12331"),
            AdapterItem("2","SF234234","123-123","12331"),
            AdapterItem("3","F35436436346","123-123","12331"),
            AdapterItem("4","GF2342432543768012","123-123","12331"),
        ))

        mBinding.tvBack.setOnClickListener {
            simpleAlert(requireContext(), getString(R.string.inbound_click_back_title),getString(R.string.inbound_click_back_hint)){
                findNavController().popBackStack()
            }
        }
        mBinding.imgPower.setOnClickListener {
            simpleEditAlert(
                requireContext(),
                getString(R.string.inbound_click_power_hint),
                inBoundPower.toString()
            ){
                inBoundPower = it
                UHFService.getInstance().power = inBoundPower
            }
        }
    }

    override fun observeData() {
        mainViewModel.epcList.observe(viewLifecycleOwner){
            LogUtil.d("InBoundFragment-epcList:$it")
        }
    }
}