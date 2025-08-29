package com.example.uhfproject.ui.fragment

import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uhfproject.R
import com.example.uhfproject.app.MyApplication.Companion.appContext
import com.example.uhfproject.databinding.FragmentBoundBinding
import com.example.uhfproject.utils.BaseFragment
import com.example.uhfproject.utils.Const.inBoundPower
import com.example.uhfproject.utils.Const.simpleAlert
import com.example.uhfproject.utils.Const.simpleEditAlert
import com.seuic.uhf.UHFService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InBoundFragment : BaseFragment<FragmentBoundBinding>() {

    private val mAdapter: CommonItemAdapter by lazy {
        CommonItemAdapter()
    }

    override fun initView() {
        mainViewModel.startQuest = true
        mBinding.tvTitle.text = getString(R.string.inbound_title)
        mBinding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
        mAdapter.setList(emptyList())
        mBinding.tvRfidCount.text = mAdapter.data.size.toString()
    }

    override fun initData() {
        UHFService.getInstance(appContext).power = inBoundPower

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
                UHFService.getInstance(appContext).power = inBoundPower
            }
        }
        mBinding.btnClear.setOnClickListener {
            simpleAlert(requireContext(), getString(R.string.clear_click)){
                mAdapter.setList(emptyList())
                mBinding.tvRfidCount.text = mAdapter.data.size.toString()
            }
        }
        mBinding.btnUpload.setOnClickListener {
            simpleAlert(requireContext(), getString(R.string.submit_click)){
                mainViewModel.stopStock()
                mainViewModel.startLoading()
                mainViewModel.submitInBound(mAdapter.data.map { it.epc?:"" }){
                    lifecycleScope.launch(Dispatchers.Main){
                        mainViewModel.stopLoading()
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    override fun observeData() {
        mainViewModel.boundExcel.observe(viewLifecycleOwner){
            mAdapter.setList(it)
            mBinding.tvRfidCount.text = mAdapter.data.size.toString()
        }
    }

    override fun onStop() {
        mainViewModel.clearBoundList()
        mainViewModel.stopStock()
        super.onStop()
    }
}