package com.example.uhfproject.ui.fragment.apple

import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uhfproject.R
import com.example.uhfproject.app.MyApplication
import com.example.uhfproject.databinding.FragmentIbVerifyBinding
import com.example.uhfproject.model.ExcelDownloadVO
import com.example.uhfproject.ui.MainActivity
import com.example.uhfproject.utils.BaseFragment
import com.example.uhfproject.utils.Const
import com.example.uhfproject.utils.Const.hideKeyboard
import com.example.uhfproject.utils.LogUtil
import com.example.uhfproject.utils.ScanMode
import com.seuic.uhf.UHFService
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.launch

class IBVerifyFragment : BaseFragment<FragmentIbVerifyBinding>() {

    private val mAdapter: CommonItemAdapter by lazy {
        CommonItemAdapter {}
    }
    private var mActivity: MainActivity? = null

    private var errorList = listOf<ExcelDownloadVO>()

    override fun initView() {
        mActivity = requireActivity() as MainActivity
        mainViewModel.setScanMode(ScanMode.IB_VERIFY)
        mBinding.tvBacklogNum.text = "B:0"
        mBinding.tvTotalNum.text = "T:0"
        mBinding.tvCompletedNum.text = "C:0"
        mBinding.tvErrorNum.text = "E:0"
        errorList = emptyList()
        mBinding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
        mAdapter.submitList(emptyList())
        mBinding.tvRfidCount.text = mAdapter.itemCount.toString()
    }

    override fun initData() {
        UHFService.getInstance(MyApplication.appContext).power = Const.ibVerifyPower

        mBinding.btnQuery.setOnClickListener {
            mBinding.edtDb.clearFocus()
            hideKeyboard()
            if (mBinding.edtDb.text.isEmpty()) {
                Toasty.warning(requireContext(), "Cannot be empty", Toasty.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (mAdapter.itemCount > 0) {
                Const.simpleAlert(
                    requireContext(),
                    "Hint",
                    "List has data. Clear and query?"
                ) {
                    mAdapter.submitList(emptyList())
                    mBinding.tvRfidCount.text = "0"
                    startQuery()
                }
            } else {
                startQuery()
            }
        }
        mBinding.tvBack.setOnClickListener {
            if (mAdapter.itemCount > 0) {
                Const.simpleAlert(
                    requireContext(),
                    getString(R.string.inbound_click_back_title),
                    getString(R.string.inbound_click_back_hint)
                ) {
                    exit()
                }
            } else {
                exit()
            }
        }
        mBinding.imgPower.setOnClickListener {
            Const.simpleEditAlert(
                requireContext(),
                "Set IB Verify power (numeric only)",
                Const.ibVerifyPower.toString()
            ) {
                Const.ibVerifyPower = it
                UHFService.getInstance(MyApplication.appContext).power = Const.ibVerifyPower
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
                mainViewModel.stopScanning()
                mainViewModel.submitInBound(mAdapter.getList().map { it.epc ?: "" }) {
                    mAdapter.submitList(emptyList())
                    mBinding.tvErrorNum.text = "E:0"
                    errorList = emptyList()
                    mainViewModel.getBeatData(mBinding.edtDb.text.toString()) {
                        lifecycleScope.launch {
                            mBinding.tvBacklogNum.text = "B:${it.backlog}"
                            mBinding.tvTotalNum.text = "T:${it.total}"
                            mBinding.tvCompletedNum.text = "C:${it.completed}"
                        }
                    }
                }
            }
        }
        mBinding.tvErrorNum.setOnClickListener {
            if (errorList.isNotEmpty()) {
                showErrorBoundFragment(errorList)
            }
        }
    }

    override fun observeData() {
        mainViewModel.boundExcel.observe(viewLifecycleOwner) {
            LogUtil.d("rfid-beat:${it.map { it.beat }}")
            if (mBinding.edtDb.text.toString().isNotEmpty()) {
                val result =
                    it.filter { it.beat != null && it.beat.contains(mBinding.edtDb.text.toString()) }
                val result2 =
                    it.filter { it.beat != null && !it.beat.contains(mBinding.edtDb.text.toString()) }
                errorList = result2
                mBinding.tvErrorNum.text = "E:${errorList.size}"
                mAdapter.submitList(result)
                mBinding.tvRfidCount.text = mAdapter.itemCount.toString()
            }
        }
    }

    override fun onDestroyView() {
        mainViewModel.setScanMode(ScanMode.NONE)
        super.onDestroyView()
    }

    private fun startQuery() {
        mBinding.tvErrorNum.text = "E:0"
        errorList = emptyList()
        mainViewModel.getBeatData(mBinding.edtDb.text.toString()) {
            lifecycleScope.launch {
                mBinding.tvBacklogNum.text = "B:${it.backlog}"
                mBinding.tvTotalNum.text = "T:${it.total}"
                mBinding.tvCompletedNum.text = "C:${it.completed}"
            }
        }
    }

    private fun exit() {
        mainViewModel.stopLoading()
        findNavController().popBackStack()
    }

}