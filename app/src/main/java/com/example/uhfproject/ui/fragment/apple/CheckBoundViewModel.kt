package com.example.uhfproject.ui.fragment.apple

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uhfproject.app.MyApplication
import com.example.uhfproject.model.ExcelDownloadVO
import com.example.uhfproject.utils.SingleLiveEvent
import com.example.uhfproject.utils.retrofit.ApiRepository
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CheckBoundViewModel: ViewModel() {
    private val rep: ApiRepository = ApiRepository.instance

    var inboundPageNum = 0 // 当前页面索引
    private val inboundPageSize = 10 // 每页数量
    var inboundHasMore = true // 是否还有更多数据
    private val _checkInbound: SingleLiveEvent<List<ExcelDownloadVO>> = SingleLiveEvent()
    val checkInbound: LiveData<List<ExcelDownloadVO>> = _checkInbound
    fun getCheckInboundList(endLoading: () -> Unit, noMore: () -> Unit) {
        if (!inboundHasMore) {
            noMore.invoke()
            return
        }
        inboundPageNum++
        viewModelScope.launch(Dispatchers.IO) {
            //继续查询排序结果
            rep.getPendingInboundRep(inboundPageNum, inboundPageSize)
                .onSuccessOrNull {
                    it?.let {
                        if (it.isNotEmpty()) {
                            inboundHasMore = true
                            val current = _checkInbound.value?.toMutableList() ?: mutableListOf()
                            current.addAll(it)
                            if (it.size != inboundPageSize) {
                                inboundHasMore = false
                                withContext(Dispatchers.Main){
                                    noMore.invoke()
                                }
                            } else {
                                withContext(Dispatchers.Main){
                                    endLoading.invoke()
                                }
                            }
                            _checkInbound.postValue(current)
                        }else{
                            inboundPageNum--
                            inboundHasMore = false
                            withContext(Dispatchers.Main){
                                noMore.invoke()
                            }
                        }
                    }?: kotlin.run {
                        inboundPageNum--
                        inboundHasMore = false
                        withContext(Dispatchers.Main){
                            noMore.invoke()
                        }
                    }
                }.onServerError { _, msg ->
                    showError("Query failed")
                    withContext(Dispatchers.Main){
                        endLoading.invoke()
                    }
                }.onOtherError {
                    showError(it.message ?: "")
                    withContext(Dispatchers.Main){
                        endLoading.invoke()
                    }
                }
        }
    }

    var outboundPageNum = 0 // 当前页面索引
    private val outboundPageSize = 10 // 每页数量
    var outboundHasMore = true // 是否还有更多数据
    private val _checkOutbound: SingleLiveEvent<List<ExcelDownloadVO>> = SingleLiveEvent()
    val checkOutbound: LiveData<List<ExcelDownloadVO>> = _checkOutbound
    fun getCheckOutboundList(endLoading: () -> Unit, noMore: () -> Unit) {
        if (!outboundHasMore) {
            noMore.invoke()
            return
        }
        outboundPageNum++
        viewModelScope.launch(Dispatchers.IO) {
            //继续查询排序结果
            rep.getPendingOutboundRep(outboundPageNum, outboundPageSize)
                .onSuccessOrNull {
                    it?.let {
                        if (it.isNotEmpty()) {
                            outboundHasMore = true
                            val current = _checkOutbound.value?.toMutableList() ?: mutableListOf()
                            current.addAll(it)
                            if (it.size != outboundPageSize) {
                                outboundHasMore = false
                                withContext(Dispatchers.Main){
                                    noMore.invoke()
                                }
                            } else {
                                withContext(Dispatchers.Main){
                                    endLoading.invoke()
                                }
                            }
                            _checkOutbound.postValue(current)
                        }else{
                            outboundPageNum--
                            outboundHasMore = false
                            withContext(Dispatchers.Main){
                                noMore.invoke()
                            }
                        }
                    }?: kotlin.run {
                        outboundPageNum--
                        outboundHasMore = false
                        withContext(Dispatchers.Main){
                            noMore.invoke()
                        }
                    }
                }.onServerError { _, msg ->
                    showError("Query failed")
                    withContext(Dispatchers.Main){
                        endLoading.invoke()
                    }
                }.onOtherError {
                    showError(it.message ?: "")
                    withContext(Dispatchers.Main){
                        endLoading.invoke()
                    }
                }
        }
    }

    private suspend fun showError(msg: String) {
        withContext(Dispatchers.Main) {
            Toasty.error(MyApplication.appContext, msg).show()
        }
    }
}

/*
class CheckBoundRep {
    private val service = RetrofitClient.createService<MainService>()

    suspend fun getPendingInboundRep(pageNum: Int, sortPageSize: Int): APIResult<List<ExcelDownloadVO>> {
        return safeNetWorkPagerInvoke {
            service.getPendingInboundNet(pageNum, sortPageSize)
        }
    }

    suspend fun getPendingOutboundRep(pageNum: Int, sortPageSize: Int): APIResult<List<ExcelDownloadVO>> {
        return safeNetWorkPagerInvoke {
            service.getPendingOutboundNet(pageNum, sortPageSize)
        }
    }
}
*/