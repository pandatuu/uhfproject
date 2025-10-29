package com.example.uhfproject.ui.fragment

import android.graphics.Color
import android.view.KeyEvent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uhfproject.R
import com.example.uhfproject.app.MyApplication.Companion.appContext
import com.example.uhfproject.databinding.FragmentBoundBinding
import com.example.uhfproject.ui.MainActivity
import com.example.uhfproject.utils.BaseFragment
import com.example.uhfproject.utils.Const.inBoundPower
import com.example.uhfproject.utils.Const.simpleAlert
import com.example.uhfproject.utils.Const.simpleEditAlert
import com.example.uhfproject.utils.LogUtil
import com.example.uhfproject.utils.ScanMode
import com.seuic.uhf.UHFService
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class InBoundFragment : BaseFragment<FragmentBoundBinding>() {

    private val mAdapter: CommonItemAdapter by lazy {
        CommonItemAdapter{}
    }

    override fun initView() {
        mActivity = requireActivity() as MainActivity
        mainViewModel.setScanMode(ScanMode.INBOUND)
        mBinding.tvTitle.text = getString(R.string.inbound_title)
        mBinding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
        mAdapter.submitList(emptyList())
        mBinding.tvRfidCount.text = mAdapter.itemCount.toString()
    }

    override fun initData() {
        //  mainViewModel.getBoundList(1){} //preload data
        UHFService.getInstance(appContext).power = inBoundPower

        mBinding.tvBack.setOnClickListener {
            if(mAdapter.itemCount>0){
                simpleAlert(requireContext(), getString(R.string.inbound_click_back_title),getString(R.string.inbound_click_back_hint)){
                    exit()
                }
            }else{
                exit()
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
                mAdapter.submitList(emptyList())
                mBinding.tvRfidCount.text = mAdapter.itemCount.toString()
            }
        }
        mBinding.btnUpload.setOnClickListener {
            simpleAlert(requireContext(), getString(R.string.submit_click)){
                mainViewModel.stopScanning()
                mainViewModel.startLoading()
                mainViewModel.submitInBound(mAdapter.getList().map { it.epc?:"" }){
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
            mAdapter.submitList(it)
            mBinding.tvRfidCount.text = mAdapter.itemCount.toString()
        }
    }

    private var mActivity: MainActivity? = null

    override fun onResume() {
        super.onResume()
        // 按下时调用方法2
    }

    override fun onDestroyView() {
        mainViewModel.setScanMode(ScanMode.NONE)
        super.onDestroyView()
    }

    private fun exit(){
        mainViewModel.stopLoading()
        findNavController().popBackStack()
    }
}