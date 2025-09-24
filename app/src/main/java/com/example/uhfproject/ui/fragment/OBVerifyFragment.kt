package com.example.uhfproject.ui.fragment

import android.graphics.Color
import android.view.KeyEvent
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uhfproject.R
import com.example.uhfproject.app.MyApplication
import com.example.uhfproject.databinding.FragmentObVerifyBinding
import com.example.uhfproject.ui.MainActivity
import com.example.uhfproject.utils.BaseFragment
import com.example.uhfproject.utils.Const
import com.example.uhfproject.utils.LogUtil
import com.seuic.uhf.UHFService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OBVerifyFragment: BaseFragment<FragmentObVerifyBinding>() {

    private val mAdapter: CommonItemAdapter by lazy {
        CommonItemAdapter{}
    }
    private var keyStatus = false
    private var mActivity: MainActivity? = null

    override fun initView() {
        mActivity = requireActivity() as MainActivity
        mBinding.vScanHint.setBackgroundColor(Color.GRAY)
        mBinding.tvScanHint.text = "Not Scanned"
        mainViewModel.startQuest = true
        mBinding.tvTotalNum.text = "T:0"
        mBinding.tvCompletedNum.text = "C:0"
        mBinding.tvErrorNum.text = "E:0"
        mBinding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
        mAdapter.submitList(emptyList())
        mBinding.tvRfidCount.text = mAdapter.itemCount.toString()
    }

    override fun initData() {
        mainViewModel.getBoundList(2){
            mBinding.tvTotalNum.text = it.size.toString()
        }
        UHFService.getInstance(MyApplication.appContext).power = Const.outBoundPower

        mBinding.tvBack.setOnClickListener {
            if(mAdapter.itemCount>0){
                Const.simpleAlert(
                    requireContext(),
                    getString(R.string.outbound_click_back_title),
                    getString(R.string.outbound_click_back_hint)
                ) {
                    exit()
                }
            }else{
                exit()
            }
        }
        mBinding.imgPower.setOnClickListener {
            Const.simpleEditAlert(
                requireContext(),
                getString(R.string.outbound_click_power_hint),
                Const.outBoundPower.toString()
            ) {
                Const.outBoundPower = it
                UHFService.getInstance(MyApplication.appContext).power = Const.outBoundPower
            }
        }
        mBinding.btnClear.setOnClickListener {
            Const.simpleAlert(requireContext(), getString(R.string.clear_click)) {
                mAdapter.submitList(emptyList())
                mBinding.tvRfidCount.text = mAdapter.itemCount.toString()
            }
        }
        mBinding.btnUpload.setOnClickListener {
            Const.simpleAlert(requireContext(), getString(R.string.submit_click)) {
                mainViewModel.stopStock()
                mainViewModel.startLoading()
                mainViewModel.submitOutBound(mAdapter.getList().map { it.epc ?: "" }) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        mainViewModel.stopLoading()
                        exit()
                    }
                }
            }
        }
    }

    override fun observeData() {
        mainViewModel.boundExcel.observe(viewLifecycleOwner){
            if(mBinding.edtDb.text.toString().isNotEmpty()){
                val result = it.filter { it.db!=null && it.db.contains(mBinding.edtDb.text.toString()) }
                val result2 = it.filter { it.db!=null && !it.db.contains(mBinding.edtDb.text.toString()) }
                mBinding.tvErrorNum.text = result2.size.toString()
                mAdapter.submitList(result)
            }else{
                mAdapter.submitList(it)
                mBinding.tvErrorNum.text = "0"
            }
            mBinding.tvRfidCount.text = mAdapter.itemCount.toString()
            mBinding.tvCompletedNum.text = mAdapter.itemCount.toString()
        }
    }
    override fun onResume() {
        super.onResume()
        // 按下时调用方法2
        mActivity?.onKeyDownCallback = { keyCode, event ->
            if (keyCode == 142 && event?.action == KeyEvent.ACTION_DOWN) {
                if(!keyStatus){
                    keyStatus = true
                    mBinding.vScanHint.setBackgroundColor(Color.parseColor("#0055A3"))
                    mBinding.tvScanHint.text = "Scanning"
                    mainViewModel.startStock()
                }else{
                    keyStatus = false
                    mBinding.vScanHint.setBackgroundColor(Color.GRAY)
                    mBinding.tvScanHint.text = "Not Scanned"
                    mainViewModel.stopStock()
                }
                true
            }else{
                false
            }
        }
    }
    override fun onStop() {
        mainViewModel.clearBoundList()
        mainViewModel.stopStock()
        mActivity?.onKeyDownCallback = null
        super.onStop()
    }

    private fun exit(){
        findNavController().popBackStack()
    }

}