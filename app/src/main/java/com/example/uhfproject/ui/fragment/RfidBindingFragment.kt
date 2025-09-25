package com.example.uhfproject.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.KeyEvent
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.uhfproject.app.MyApplication
import com.example.uhfproject.databinding.FragmentRfidBindingBinding
import com.example.uhfproject.model.BindBody
import com.example.uhfproject.ui.MainActivity
import com.example.uhfproject.utils.BaseFragment
import com.example.uhfproject.utils.Const
import com.seuic.uhf.UHFService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class RfidBindingFragment : BaseFragment<FragmentRfidBindingBinding>() {

    private var scanCode: String? = null
    private var rfidValue: String? = null

    private val scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action.equals(Const.SCAN_ACTION)) {
                val code = intent.getStringExtra("scannerdata")
                mBinding.edtRfid.editText?.setText(code)
                scanCode = code
                checkBothValuesReady()
            }
        }
    }
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
            if (it.isNotEmpty() && mBinding.edtRfid.editText?.text.toString().isNotEmpty()) {
                rfidValue = it[0].getId()
                checkBothValuesReady()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        //注册广播接收器
        val intentFilter = IntentFilter()
        intentFilter.addAction(Const.SCAN_ACTION)
        intentFilter.priority = Int.MAX_VALUE
        requireActivity().registerReceiver(scanReceiver, intentFilter)

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

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(scanReceiver)
    }

    private fun checkBothValuesReady() {
        if (!scanCode.isNullOrEmpty() && !rfidValue.isNullOrEmpty()) {
            // 两个值都准备好了，调用你的方法
            onBothValuesReady(scanCode!!, rfidValue!!)

            // 可选：重置值，避免重复调用
             scanCode = null
             rfidValue = null
        }
    }

    private fun onBothValuesReady(scanCode: String, rfidValue: String) {
        // 在这里调用你的另一个方法
        mainViewModel.bind(BindBody(scanCode, rfidValue))
    }

}