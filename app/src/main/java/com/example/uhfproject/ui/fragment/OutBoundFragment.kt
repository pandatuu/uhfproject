package com.example.uhfproject.ui.fragment

import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uhfproject.R
import com.example.uhfproject.databinding.FragmentBoundBinding
import com.example.uhfproject.ui.BaseFragment
import com.example.uhfproject.utils.Const.outBoundPower
import com.example.uhfproject.utils.Const.simpleAlert
import com.example.uhfproject.utils.Const.simpleEditAlert
import com.seuic.uhf.UHFService

class OutBoundFragment : BaseFragment<FragmentBoundBinding>() {

    private val mAdapter: CommonItemAdapter by lazy {
        CommonItemAdapter()
    }

    override fun initView() {
        mBinding.tvTitle.text = getString(R.string.outbound_title)
        mBinding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
        mBinding.tvRfidCount.text = "(${mAdapter.data.size})"
    }

    override fun initData() {
        UHFService.getInstance().power = outBoundPower

        mBinding.tvBack.setOnClickListener {
            simpleAlert(
                requireContext(), getString(R.string.outbound_click_back_title), getString(
                    R.string.outbound_click_back_hint
                )
            ) {
                findNavController().popBackStack()
            }
        }
        mBinding.imgPower.setOnClickListener {
            simpleEditAlert(
                requireContext(),
                getString(R.string.outbound_click_power_hint),
                outBoundPower.toString()
            ) {
                outBoundPower = it
                UHFService.getInstance().power = outBoundPower
            }
        }
        mBinding.btnClear.setOnClickListener {
            simpleAlert(requireContext(), getString(R.string.clear_click)){
                mAdapter.setList(emptyList())
                mBinding.tvRfidCount.text = "(${mAdapter.data.size})"
            }
        }
        mBinding.btnUpload.setOnClickListener {
            simpleAlert(requireContext(), getString(R.string.submit_click)){
                mainViewModel.submitOutBound()
            }
        }
    }

    override fun observeData() {
        mainViewModel.boundExcel.observe(viewLifecycleOwner){
            mAdapter.setList(it)
            mBinding.tvRfidCount.text = "(${mAdapter.data.size})"
        }
    }
}