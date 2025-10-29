package com.example.uhfproject.utils

import com.seuic.uhf.EPC

interface ScanResultListener {
    fun onScanResults(tags: Set<EPC>)
}
