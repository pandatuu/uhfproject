package com.example.uhfproject.ui.fragment.apple

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.text.method.ScrollingMovementMethod
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.uhfproject.R
import com.example.uhfproject.app.MyApplication
import com.example.uhfproject.databinding.FragmentDebugScanBinding
import com.example.uhfproject.utils.BaseFragment
import com.example.uhfproject.utils.Const
import com.example.uhfproject.utils.ScanMode
import com.example.uhfproject.viewmodel.DebugScanViewModel
import com.seuic.uhf.EPC
import com.seuic.uhf.UHFService
import com.seuic.uhfutils.EpcSearch

class DebugScanFragment : BaseFragment<FragmentDebugScanBinding>() {

    private val debugViewModel: DebugScanViewModel by viewModels()
    private var uhfService: UHFService? = null

    private val scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Const.SCAN_ACTION) {
                val barcode = intent.getStringExtra("scannerdata")?.trim()
                barcode?.let {
                    debugViewModel.barcodeResult.postValue(it)
                }
            }
        }
    }

    override fun initView() {
        uhfService = UHFService.getInstance(MyApplication.appContext)
        // uhfService.inventoryStop() // make sure you stop the inventory process here
        mBinding.tvEpcResult.movementMethod = ScrollingMovementMethod()
    }

    override fun initData() {
        mBinding.tvPower.text = "Power: ${Const.findItemPower}"

        mBinding.tvBack.setOnClickListener {
            findNavController().popBackStack()
        }

        mBinding.tvPower.setOnClickListener {
            Const.simpleEditAlert(
                requireContext(),
                getString(R.string.finditem_click_power_hint),
                Const.findItemPower.toString()
            ) { power ->
                Const.findItemPower = power
                uhfService?.power = power
                mBinding.tvPower.text = "Power: $power"
            }
        }
    }

    override fun observeData() {
        debugViewModel.triggerStatus.observe(viewLifecycleOwner) { mBinding.tvTriggerStatus.text = it }
        debugViewModel.serviceStatus.observe(viewLifecycleOwner) { mBinding.tvServiceStatus.text = it }
        debugViewModel.inventoryStatus.observe(viewLifecycleOwner) { mBinding.tvInventoryStatus.text = it }
        debugViewModel.epcResult.observe(viewLifecycleOwner) { mBinding.tvEpcResult.text = it }
        debugViewModel.barcodeResult.observe(viewLifecycleOwner) { mBinding.tvBarcodeResult.text = it }
        debugViewModel.dbStatus.observe(viewLifecycleOwner) { mBinding.tvDbStatus.text = it }

        mainViewModel.findList.observe(viewLifecycleOwner) { epcs ->
            val searchedEpcs = EpcSearch.search(epcs)
            if (!searchedEpcs.isNullOrEmpty()) {
                handleEpcData(searchedEpcs.toList())
            }
        }
    }

    private fun handleEpcData(epcs: List<EPC>) {
        val currentEpcText = mBinding.tvEpcResult.text.toString()
        val newEpcEntries = epcs.filter { epc ->
            !currentEpcText.contains(epc.getId())
        }.joinToString("\n") { epc ->
            "EPC: ${epc.getId()}, RSSI: ${epc.rssi}"
        }

        if (newEpcEntries.isNotEmpty()) {
            val newText = if (currentEpcText.isEmpty()) newEpcEntries else "$newEpcEntries\n$currentEpcText"
            debugViewModel.epcResult.postValue(newText)
        }
    }

    fun onTriggerDown() {
        debugViewModel.clearScanResults()
        debugViewModel.triggerStatus.postValue("Trigger: Held Down")
        mainViewModel.startScanning() // uhfservice.getinstance(appcontext).inventorystart() should be called in the related methods
        debugViewModel.inventoryStatus.postValue("Inventory: Started")
    }

    fun onTriggerUp() {
        debugViewModel.triggerStatus.postValue("Trigger: Released")
        mainViewModel.stopScanning()
        debugViewModel.inventoryStatus.postValue("Inventory: Stopped")
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.setScanMode(ScanMode.DEBUG)
        val ret = uhfService?.open() ?: false
        if (ret) {
            debugViewModel.serviceStatus.postValue("UHF Service: Opened Successfully")
            uhfService?.power = Const.findItemPower
        } else {
            debugViewModel.serviceStatus.postValue("UHF Service: Open Failed")
        }
        val intentFilter = IntentFilter(Const.SCAN_ACTION)
        requireActivity().registerReceiver(scanReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        mainViewModel.setScanMode(ScanMode.NONE)
        mainViewModel.stopScanning()
        uhfService?.close()
        debugViewModel.serviceStatus.postValue("UHF Service: Closed")
        requireActivity().unregisterReceiver(scanReceiver)
    }
}