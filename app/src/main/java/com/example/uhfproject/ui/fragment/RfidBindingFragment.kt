package com.example.uhfproject.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.uhfproject.app.MyApplication
import com.example.uhfproject.databinding.FragmentRfidBindingBinding
import com.example.uhfproject.model.BindBody
import com.example.uhfproject.ui.GreenFlashActivity
import com.example.uhfproject.ui.MainActivity
import com.example.uhfproject.utils.BaseFragment
import com.example.uhfproject.utils.Const
import com.example.uhfproject.utils.Const.clean
import com.example.uhfproject.utils.LogUtil
import com.google.android.material.textfield.TextInputLayout
import com.seuic.uhf.UHFService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class RfidBindingFragment : BaseFragment<FragmentRfidBindingBinding>() {

    private var scanCode: String? = null
    private var rfidValue: String? = null

    private val scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            LogUtil.d("广播ACTION:${intent.action}")
            if (intent.action.equals(Const.SCAN_ACTION)) {
                val code = intent.getStringExtra("scannerdata")?.clean()?.trim()
                val code1 = intent.getStringExtra("scannerdata")
                LogUtil.d("binding-条形码接收扫描:${code1},${code1?.length}")
                LogUtil.d("binding-条形码接收扫描:${code},${code?.length}")
                mBinding.edtRfid.editText?.setText(code)
                scanCode = code
            }
        }
    }
    private var keyStatus = false
    private var mActivity: MainActivity? = null

    override fun initView() {
        mActivity = requireActivity() as MainActivity
        mBinding.edtRfid.requestFocus()

    }

    override fun initData() {
        mainViewModel.startQuest = false
        UHFService.getInstance(MyApplication.appContext).power = Const.rfidBindingPower

        mBinding.tvBack.setOnClickListener {
            mainViewModel.stopLoading()
            findNavController().popBackStack()
        }
        mBinding.imgPower.setOnClickListener {
            Const.simpleEditAlert(
                requireContext(),
                "Set RfidBinding power (numeric only)",
                Const.rfidBindingPower.toString()
            ) {
                Const.rfidBindingPower = it
                UHFService.getInstance(MyApplication.appContext).power = Const.rfidBindingPower
            }
        }
    }

    override fun observeData() {
        mainViewModel.findList.observe(viewLifecycleOwner) {
            LogUtil.d("binding-rfid:${it.map { it.getId() }}")
            if (it.isNotEmpty()) {
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
                if (!keyStatus && scanCode != null) {
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
        mainViewModel.stopStock()
        mainViewModel.bind(BindBody(scanCode, rfidValue),
            success = {
                lifecycleScope.launch(Dispatchers.Main) {
                    mBinding.tvScanHintWord.text = "tracking#: ${scanCode}\n" +
                            "epc: ${rfidValue}\n" +
                            "RFlD bound successfully!"
                    mBinding.tvScanHintWord.setTextColor(Color.GREEN)
                    mBinding.edtRfid.editText?.setText("")
                    val intent = Intent(requireActivity(), GreenFlashActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }, failed = {
                lifecycleScope.launch(Dispatchers.Main) {
                    mBinding.tvScanHintWord.text = "tracking#: ${scanCode}\n" +
                            "epc: ${rfidValue}\n" + "ErrorMsg: ${it}"
                    mBinding.tvScanHintWord.setTextColor(Color.RED)
                    mBinding.edtRfid.editText?.setText("")
                }
            })
    }

}