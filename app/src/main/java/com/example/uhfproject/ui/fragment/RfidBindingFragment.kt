package com.example.uhfproject.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import com.example.uhfproject.R
import com.example.uhfproject.app.MyApplication
import com.example.uhfproject.databinding.FragmentRfidBindingBinding
import com.example.uhfproject.model.BindBody
import com.example.uhfproject.ui.MainActivity
import com.example.uhfproject.utils.BaseFragment
import com.example.uhfproject.utils.Const
import com.example.uhfproject.utils.Const.clean
import com.example.uhfproject.utils.LogUtil
import com.example.uhfproject.utils.ScanMode
import com.seuic.uhf.EPC
import com.seuic.uhf.UHFService

class RfidBindingFragment : BaseFragment<FragmentRfidBindingBinding>() {

    private var scanCode: String? = null
    private var rfidValue: String? = null

    private lateinit var epcAdapter: EpcAdapter
    private val scannedEpc = mutableListOf<Pair<String, Int>>()

    private enum class ScanStatus {
        IDLE, SCANNING, PAIRING, SUCCESS, ERROR
    }

    private val scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action.equals(Const.SCAN_ACTION)) {
                val code = intent.getStringExtra("scannerdata")?.clean()?.trim()
                code?.let {
                    LogUtil.d("binding-barcode receive scan:${it},${it.length}")
                    if (!mainViewModel.validateBarcode(it)) {
                        updateStatus(ScanStatus.ERROR, "Error 1: Invalid Barcode.")
                        return
                    }
                    scanCode = it
                    mBinding.edtBarcode.setText(it)
                    scannedEpc.clear()
                    epcAdapter.notifyDataSetChanged()
                    updateStatus(ScanStatus.SCANNING)
                    mainViewModel.startScanning()
                }
            }
        }
    }

    override fun initView() {
        setupRecyclerView()
        updateStatus(ScanStatus.IDLE)
    }

    override fun initData() {
        mainViewModel.setScanMode(ScanMode.RFID_BINDING)
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
            ) { power ->
                Const.rfidBindingPower = power
                UHFService.getInstance(MyApplication.appContext).power = power
            }
        }
    }

    override fun observeData() {
        mainViewModel.findList.observe(viewLifecycleOwner) {
            LogUtil.d("binding-rfid:${it.map { it.getId() }}")
            if (it.isNotEmpty()) {
                handleEpcData(it)
                rfidValue = it[0].getId() // Preserve original logic to trigger binding
                checkBothValuesReady()
            }
        }

        mainViewModel.bindResult.observe(viewLifecycleOwner) { result ->
            if (result.isSuccess) {
                updateStatus(ScanStatus.SUCCESS, "Successfully paired!")
            } else {
                handleBindingError(result.message)
            }
        }
    }

    private fun handleEpcData(epcs: List<EPC>) {
        for (tagInfo in epcs) {
            val epc = tagInfo.getId()
            val rssi = tagInfo.rssi
            if (epc.isNotEmpty()) {
                val index = scannedEpc.indexOfFirst { it.first == epc }
                if (index != -1) {
                    scannedEpc[index] = Pair(epc, rssi)
                } else {
                    scannedEpc.add(Pair(epc, rssi))
                }
            }
        }
        scannedEpc.sortByDescending { it.second }
        epcAdapter.notifyDataSetChanged()
    }

    private fun checkBothValuesReady() {
        if (!scanCode.isNullOrEmpty() && !rfidValue.isNullOrEmpty()) {
            onBothValuesReady(scanCode!!, rfidValue!!)
            scanCode = null
            rfidValue = null
        }
    }

    private fun onBothValuesReady(scanCode: String, rfidValue: String) {
        mainViewModel.stopScanning()
        updateStatus(ScanStatus.PAIRING)
        // Bind the EPC with the highest RSSI, not just the first one detected
        val bestEpc = scannedEpc.maxByOrNull { it.second }?.first ?: rfidValue
        mainViewModel.bind(BindBody(scanCode, bestEpc))
    }

    private fun setupRecyclerView() {
        epcAdapter = EpcAdapter(scannedEpc)
        mBinding.rvEpcList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = epcAdapter
        }
    }

    private fun updateStatus(status: ScanStatus, message: String? = null) {
        when (status) {
            ScanStatus.IDLE -> {
                mBinding.tvStatus.text = "Idle"
                mBinding.tvStatus.setTextColor(Color.BLACK)
                mBinding.tvErrorMessage.text = ""
            }
            ScanStatus.SCANNING -> {
                mBinding.tvStatus.text = "Scanning..."
                mBinding.tvStatus.setTextColor(Color.BLUE)
            }
            ScanStatus.PAIRING -> {
                mBinding.tvStatus.text = "Pairing..."
                mBinding.tvStatus.setTextColor(Color.MAGENTA)
            }
            ScanStatus.SUCCESS -> {
                mBinding.tvStatus.text = "Success"
                mBinding.tvStatus.setTextColor(Color.parseColor("#00C853"))
                mBinding.tvErrorMessage.text = message ?: "Success"
                mBinding.tvErrorMessage.setTextColor(Color.parseColor("#00C853"))
            }
            ScanStatus.ERROR -> {
                mBinding.tvStatus.text = "Error"
                mBinding.tvStatus.setTextColor(Color.RED)
                mBinding.tvErrorMessage.text = message ?: "An unknown error occurred."
                mBinding.tvErrorMessage.setTextColor(Color.RED)
            }
        }
    }

    private fun handleBindingError(message: String?) {
        val errorMessage = message ?: "Unknown error"
        when {
            errorMessage.contains("already bound to EPC") -> {
                updateStatus(ScanStatus.ERROR, "Error 4: Barcode already paired. Details: $errorMessage")
            }
            errorMessage.contains("EPC already bound to tracking") -> {
                updateStatus(ScanStatus.ERROR, "Error 5: EPC already paired. Details: $errorMessage")
            }
            else -> {
                updateStatus(ScanStatus.ERROR, "Error 3: Binding failed. Details: $errorMessage")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(Const.SCAN_ACTION)
        intentFilter.priority = Int.MAX_VALUE
        requireActivity().registerReceiver(scanReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(scanReceiver)
        mainViewModel.stopScanning()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainViewModel.setScanMode(ScanMode.NONE)
    }

    inner class EpcAdapter(private val items: List<Pair<String, Int>>) : RecyclerView.Adapter<EpcAdapter.EpcViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpcViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_epc_rssi, parent, false)
            return EpcViewHolder(view)
        }

        override fun onBindViewHolder(holder: EpcViewHolder, position: Int) {
            val (epc, rssi) = items[position]
            holder.epcTextView.text = epc
            holder.rssiTextView.text = rssi.toString()
        }

        override fun getItemCount() = items.size

        inner class EpcViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val epcTextView: TextView = itemView.findViewById(R.id.tv_epc)
            val rssiTextView: TextView = itemView.findViewById(R.id.tv_rssi)
        }
    }
}