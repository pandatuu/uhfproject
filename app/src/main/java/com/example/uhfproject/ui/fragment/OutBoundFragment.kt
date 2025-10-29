package com.example.uhfproject.ui.fragment

import android.graphics.Color
import android.view.KeyEvent
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uhfproject.R
import com.example.uhfproject.app.MyApplication.Companion.appContext
import com.example.uhfproject.databinding.FragmentBoundBinding
import com.example.uhfproject.ui.MainActivity
import com.example.uhfproject.utils.BaseFragment
import com.example.uhfproject.utils.Const.outBoundPower
import com.example.uhfproject.utils.Const.simpleAlert
import com.example.uhfproject.utils.Const.simpleEditAlert
import com.example.uhfproject.utils.LogUtil
import com.example.uhfproject.utils.ScanMode
import com.seuic.uhf.UHFService
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OutBoundFragment : BaseFragment<FragmentBoundBinding>() {

    private val mAdapter: CommonItemAdapter by lazy {
        CommonItemAdapter {}
    }

    override fun initView() {
        mActivity = requireActivity() as MainActivity
        mainViewModel.setScanMode(ScanMode.OUTBOUND)
        mBinding.tvTitle.text = getString(R.string.outbound_title)
        mBinding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
        mAdapter.submitList(emptyList())
        mBinding.tvRfidCount.text = mAdapter.itemCount.toString()
    }

    override fun initData() {
        //  mainViewModel.getBoundList(2){} //preload data
        UHFService.getInstance(appContext).power = outBoundPower

        mBinding.tvBack.setOnClickListener {
            if(mAdapter.itemCount>0){
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
                mAdapter.submitList(emptyList())
                mBinding.tvRfidCount.text = mAdapter.itemCount.toString()
            }
        }
        mBinding.btnUpload.setOnClickListener {
            simpleAlert(requireContext(), getString(R.string.submit_click)){
                mainViewModel.stopScanning()
                mainViewModel.submitOutBound(mAdapter.getList().map { it.epc?:"" }){
                    lifecycleScope.launch(Dispatchers.Main){
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