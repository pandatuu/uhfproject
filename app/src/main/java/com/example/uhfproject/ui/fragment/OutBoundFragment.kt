package com.example.uhfproject.ui.fragment

import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uhfproject.R
import com.example.uhfproject.app.MyApplication.Companion.appContext
import com.example.uhfproject.databinding.FragmentBoundBinding
import com.example.uhfproject.utils.BaseFragment
import com.example.uhfproject.utils.Const.outBoundPower
import com.example.uhfproject.utils.Const.simpleAlert
import com.example.uhfproject.utils.Const.simpleEditAlert
import com.seuic.uhf.UHFService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OutBoundFragment : BaseFragment<FragmentBoundBinding>() {

    private val mAdapter: CommonItemAdapter by lazy {
        CommonItemAdapter()
    }

    override fun initView() {
        mainViewModel.startQuest = true
        mBinding.tvTitle.text = getString(R.string.outbound_title)
        mBinding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
        mAdapter.setList(emptyList())
        mBinding.tvRfidCount.text = mAdapter.data.size.toString()
    }

    override fun initData() {
        UHFService.getInstance(appContext).power = outBoundPower

        mBinding.tvBack.setOnClickListener {
            if(mAdapter.data.isNotEmpty()){
                simpleAlert(requireContext(), getString(R.string.outbound_click_back_title),getString(R.string.outbound_click_back_hint)){
                    exit()
                }
            }else{
                exit()
            }
        }
        mBinding.imgPower.setOnClickListener {
            simpleEditAlert(
                requireContext(),
                getString(R.string.outbound_click_power_hint),
                outBoundPower.toString()
            ) {
                outBoundPower = it
                UHFService.getInstance(appContext).power = outBoundPower
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
                mainViewModel.submitOutBound(mAdapter.data.map { it.epc?:"" }){
                    lifecycleScope.launch(Dispatchers.Main){
                        mainViewModel.stopLoading()
                        exit()
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
        mainViewModel.stopStock()
        mainViewModel.clearBoundList()
        super.onStop()
    }

    private fun exit(){
        findNavController().popBackStack()
    }
}