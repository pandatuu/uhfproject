package com.example.uhfproject.utils

import com.example.uhfproject.app.MyApplication
import com.seuic.uhf.EPC
import com.seuic.uhf.UHFService
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ScanningManager(
    private val coroutineScope: CoroutineScope,
    private val listener: ScanResultListener
) {

    private val uhfService: UHFService = UHFService.getInstance(MyApplication.appContext)
    
    // Buffer for general scanning (collects all unique tags)
    private val generalTagBuffer = mutableSetOf<EPC>()
    // Buffer for binding mode (keeps only the tag with the highest RSSI)
    private var bindingTagBuffer: EPC? = null

    private val bufferMutex = Mutex()
    private var currentMode: ScanMode = ScanMode.NONE

    private var pollingJob: Job? = null
    private var batchingJob: Job? = null

    fun start(mode: ScanMode) {
        if (pollingJob?.isActive == true) return // Already running
        if (uhfService.inventoryStart()){
            UHFService.getInstance().registerReadTags {  }
            currentMode = mode
            pollingJob = coroutineScope.launch(Dispatchers.IO) {
                while (isActive) {
                    val newTags: List<EPC>? = uhfService.tagIDs
                    BeepSound.play() // does enter here
                    if (!newTags.isNullOrEmpty()) {
                        // BeepSound.play()
                        bufferMutex.withLock {
                            if (currentMode == ScanMode.RFID_BINDING) {
                                // Find the tag with the highest RSSI from the new batch
                                val strongestNewTag = newTags.maxByOrNull { it.rssi }
                                // If it's stronger than the currently stored one, replace it
                                if (strongestNewTag != null && (bindingTagBuffer == null || strongestNewTag.rssi > bindingTagBuffer!!.rssi)) {
                                    bindingTagBuffer = strongestNewTag
                                }
                                else
                                {
                                    // do nothing
                                }
                            } else {
                                // For all other modes, just add all unique tags
                                generalTagBuffer.addAll(newTags)
                            }
                        }
                    }
                    delay(100) // 10Hz polling
                }
            }
            batchingJob = coroutineScope.launch(Dispatchers.Default) {
                while (isActive) {
                    delay(300) // 300ms batching, 500ms feels slow
                    flushBuffer()
                }
            }
        }
        else
        {
            LogUtil.d("scan-盘点失败")
        }
    }

    fun stop() {
        if(uhfService.inventoryStop()){
        pollingJob?.cancel()
        batchingJob?.cancel()
        pollingJob = null
        batchingJob = null

        // Perform one final flush
        coroutineScope.launch {
            flushBuffer(finalFlush = true)
        }
        }
        else
        {
            LogUtil.d("scan-盘点停止失败")
        }
    }

    private suspend fun flushBuffer(finalFlush: Boolean = false) {
        val batch = mutableSetOf<EPC>()
        bufferMutex.withLock {
            if (currentMode == ScanMode.RFID_BINDING) {
                bindingTagBuffer?.let {
                    batch.add(it)
                }
                bindingTagBuffer = null // Clear after flushing
            } else {
                if (generalTagBuffer.isNotEmpty()) {
                    batch.addAll(generalTagBuffer)
                    generalTagBuffer.clear()
                }
            }
        }
        
        if (batch.isNotEmpty()) {
            if (finalFlush) {
                listener.onScanResults(batch) // this invokes excel submission
            } else {
                withContext(Dispatchers.Main) {
                    listener.onScanResults(batch)
                }
            }
        }
    }
}