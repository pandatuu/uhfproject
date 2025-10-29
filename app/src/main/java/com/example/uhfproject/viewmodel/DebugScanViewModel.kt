package com.example.uhfproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DebugScanViewModel : ViewModel() {

    val triggerStatus = MutableLiveData<String>("Trigger: Released")
    val serviceStatus = MutableLiveData<String>("UHF Service: Closed")
    val inventoryStatus = MutableLiveData<String>("Inventory: Stopped")

    val epcResult = MutableLiveData<String>("")
    val barcodeResult = MutableLiveData<String>("")

    val dbStatus = MutableLiveData<String>("DB Status: Waiting for EPC...")

    fun clearScanResults() {
        epcResult.postValue("")
        barcodeResult.postValue("")
        dbStatus.postValue("DB Status: Waiting for EPC...")
    }
}
