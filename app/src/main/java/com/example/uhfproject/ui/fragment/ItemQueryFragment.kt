package com.example.uhfproject.ui.fragment

import android.view.KeyEvent
import androidx.navigation.fragment.findNavController
import com.example.uhfproject.app.MyApplication
import com.example.uhfproject.databinding.FragmentItemQueryBinding
import com.example.uhfproject.ui.MainActivity
import com.example.uhfproject.utils.BaseFragment
import com.example.uhfproject.utils.Const
import com.seuic.uhf.UHFService

class ItemQueryFragment : BaseFragment<FragmentItemQueryBinding>() {

    private var keyStatus = false
    private var mActivity: MainActivity? = null

    override fun initView() {
        mActivity = requireActivity() as MainActivity
    }

    override fun initData() {
        mainViewModel.startQuest = false
        UHFService.getInstance(MyApplication.appContext).power = Const.itemQueryPower

        mBinding.tvBack.setOnClickListener {
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
                mainViewModel.getLifeCycleByEPCRep(it[it.lastIndex].getId()) { item ->
                    mBinding.epc.text = item.epc
                    mBinding.trackingId.text = item.trackingNumber
                    mBinding.postalCode.text = item.postCode
                    mBinding.beat.text = item.bitCode
                    mBinding.rb.text = item.db

                    mBinding.uploadTime.text =
                        "Upload date & time: ${item.uploadTime} By: ${item.uploadBy}"
                    mBinding.printTime.text =
                        "Print date & time: ${item.rfidPrintTime} By: ${item.printBy}"
                    mBinding.inboundTime.text =
                        "Inbound date & time: ${item.inboundDate} By: ${item.inboundBy}"
                    mBinding.outboundTime.text =
                        "Outbound date & time: ${item.outboundDate} By: ${item.outboundBy}"
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mActivity?.onKeyDownCallback = { keyCode, event ->
            if (keyCode == 142 && event?.action == KeyEvent.ACTION_DOWN) {
                if (!keyStatus) {
                    keyStatus = true
                    mainViewModel.startStock()
                }
                true
            } else {
                false
            }
        }
        mActivity?.onKeyUpCallback = { keyCode, event ->
            if (keyCode == 142 && event?.action == KeyEvent.ACTION_UP) {
                keyStatus = false
                mainViewModel.stopStock()
                true
            } else {
                false
            }
        }
    }
}