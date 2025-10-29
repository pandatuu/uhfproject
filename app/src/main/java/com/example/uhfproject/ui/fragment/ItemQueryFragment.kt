package com.example.uhfproject.ui.fragment

import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.uhfproject.app.MyApplication
import com.example.uhfproject.databinding.FragmentItemQueryBinding
import com.example.uhfproject.ui.MainActivity
import com.example.uhfproject.utils.BaseFragment
import com.example.uhfproject.utils.Const
import com.example.uhfproject.utils.ScanMode
import com.seuic.uhf.UHFService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ItemQueryFragment : BaseFragment<FragmentItemQueryBinding>() {

    private var mActivity: MainActivity? = null

    override fun initView() {
        mActivity = requireActivity() as MainActivity
        mainViewModel.setScanMode(ScanMode.ITEM_QUERY)
    }

    override fun initData() {
        UHFService.getInstance(MyApplication.appContext).power = Const.itemQueryPower

        mBinding.tvBack.setOnClickListener {
            mainViewModel.stopLoading()
            findNavController().popBackStack()
        }
        mBinding.imgPower.setOnClickListener {
            Const.simpleEditAlert(
                requireContext(),
                "Set ItemQuery power (numeric only)",
                Const.itemQueryPower.toString()
            ) {
                Const.itemQueryPower = it
                UHFService.getInstance(MyApplication.appContext).power = Const.itemQueryPower
            }
        }
    }

    override fun observeData() {
        mainViewModel.findList.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                mBinding.epc.text = it[0].getId()
                mainViewModel.getLifeCycleByEPCRep(it[0].getId()) { item ->
                    lifecycleScope.launch(Dispatchers.Main) {
                        mBinding.trackingId.text = item.trackingNumber
                        mBinding.postalCode.text = item.postCode
                        mBinding.beat.text = item.bitCode
                        mBinding.rb.text = item.db

                        mBinding.uploadTime.text =
                            "Upload date & time: ${item.uploadTime?:"nothing"}\nBy: ${item.uploadBy?:"nothing"}"
                        mBinding.printTime.text =
                            "Print date & time: ${item.rfidPrintTime?:"nothing"}\nBy: ${item.printBy?:"nothing"}"
                        mBinding.inboundTime.text =
                            "Inbound date & time: ${item.inboundDate?:"nothing"}\nBy: ${item.inboundBy?:"nothing"}"
                        mBinding.outboundTime.text =
                            "Outbound date & time: ${item.outboundDate?:"nothing"}\nBy: ${item.outboundBy?:"nothing"}"
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainViewModel.setScanMode(ScanMode.NONE)
    }
}
