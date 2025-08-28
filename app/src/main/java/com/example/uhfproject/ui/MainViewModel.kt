package com.example.uhfproject.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.uhfproject.app.MyApplication.Companion.appContext
import com.example.uhfproject.model.ExcelDownloadVO
import com.example.uhfproject.model.InfoPromptsVO
import com.example.uhfproject.utils.LogUtil
import com.example.uhfproject.utils.SingleLiveEvent
import com.example.uhfproject.utils.retrofit.*
import com.seuic.uhf.EPC
import com.seuic.uhf.UHFService
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val rep = MainRep()


    private val _loading: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val loading: LiveData<Boolean> = _loading

    private var loadingTimer: Job? = null

    fun startLoading() {
        _loading.postValue(true)
        if (loadingTimer == null) {
            loadingTimer = viewModelScope.launch(Dispatchers.IO) {
                delay(10 * 1000)
                showWarn("Load failed")
                _loading.postValue(false)
                LogUtil.d("loading：倒计时结束")
            }
        }
    }

    fun stopLoading() {
        _loading.postValue(false)
        if (loadingTimer != null) {
            loadingTimer!!.cancel()
            loadingTimer == null
        }
    }




    private val _epcList: MutableLiveData<List<String>> = MutableLiveData()
    val epcList: LiveData<List<String>> = _epcList
    private val _findList: MutableLiveData<List<EPC>> = MutableLiveData()
    val findList: LiveData<List<EPC>> = _findList

    /**
     * 单次扫描
     */
    fun onceScan() {
        val epc = EPC()
        if (UHFService.getInstance(appContext).inventoryOnce(epc, 100)) {
            val id = epc.getId()
            if (id != null && "" != id) {
                val currentList = _epcList.value?.toMutableList() ?: mutableListOf()
                if (currentList.all { it != id }) {
                    currentList.add(epc.getId())
                    _epcList.postValue(currentList)
                }
            }
        }
    }

    private val _startBtn: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val startBtn: LiveData<Boolean> = _startBtn
    private val _stopBtn: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val stopBtn: LiveData<Boolean> = _stopBtn

    var startQuest = false
    /**
     * 盘点监听
     */
    private var stockListener: Job? = null
    private var mInventoryStart = true
    fun startStock() {
        if (UHFService.getInstance(appContext).inventoryStart()) {
            LogUtil.d("UHF盘点开启")
            mInventoryStart = true
            _startBtn.postValue(true)
            //开始盘点
            if (stockListener == null) {
                stockListener = viewModelScope.launch(Dispatchers.IO) {
                    while (mInventoryStart) {
                        val tagIds = UHFService.getInstance(appContext).tagIDs.toSet()
                        _findList.postValue(tagIds.toList())
                        val epcList1 = tagIds.map { it.getId() }
                        LogUtil.d("tagIds-$epcList1")
//                        _epcList.postValue(tagIds.map { it.getId() })
                        if(startQuest){
                            getExcelDownloadByEmp(epcList1)
                        }
                        delay(100)
                    }
                }
            } else {
                if (!stockListener!!.isActive) {
                    stockListener!!.start()
                }
            }
        } else {
            //盘点失败
            LogUtil.d("scan-盘点失败")
        }
    }

    fun stopStock() {
        if (UHFService.getInstance(appContext).inventoryStop()) {
            _stopBtn.postValue(true)
            mInventoryStart = false
            LogUtil.d("UHF盘点关闭")
            //停止盘点
            stockListener?.cancel()
            stockListener = null
        } else {
            //停止失败
            LogUtil.d("scan-盘点停止失败")
        }
    }

    //-----------------------InBound--------------------------------

    private val _boundExcel: MutableLiveData<List<ExcelDownloadVO>> = MutableLiveData()
    val boundExcel: LiveData<List<ExcelDownloadVO>> = _boundExcel

    fun clearBoundList(){
        _boundExcel.postValue(emptyList())
    }

    private fun getExcelDownloadByEmp(epcList: List<String>) {
        if(epcList.isNotEmpty()){
            viewModelScope.launch(Dispatchers.IO) {
                rep.getTrackingIdByEPCRep(epcList)
                    .onSuccess {
                        LogUtil.d("getTrackingIdByEPCRep-result:${this}")
                        _boundExcel.postValue(this)
                    }.onServerError { code, msg ->
                        showWarn(msg)
                        LogUtil.d("code:$code, msg:$msg")
                    }.onOtherError {
                        showWarn(it.message ?: "")
                        LogUtil.d(it.message ?: "")
                    }
            }
        }
    }

    fun submitInBound(list: List<String>, success: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            rep.inboundRep(list)
                .onSuccess {
                    withContext(Dispatchers.Main) {
                        Toasty.success(appContext, this@onSuccess.msg?:"Submission Success", Toasty.LENGTH_LONG).show()
                    }
                    success.invoke()
                }.onServerError { _, msg ->
                    showWarn("Submission failed")
                    stopLoading()
                }.onOtherError {
                    showWarn(it.message ?: "")
                    stopLoading()
                }
        }
    }


    //-----------------------OutBound--------------------------------
//
//    private val _outboundExcel: MutableLiveData<List<ExcelDownloadVO>> = MutableLiveData()
//    val outboundExcel: LiveData<List<ExcelDownloadVO>> = _outboundExcel
//

    fun submitOutBound(list: List<String>, success: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            rep.outboundRep(list)
                .onSuccess {
                    withContext(Dispatchers.Main) {
                        Toasty.success(appContext, this@onSuccess.msg?:"Submission Success", Toasty.LENGTH_LONG).show()
                    }
                    success.invoke()
                }.onServerError { _, msg ->
                    showWarn("Submission failed")
                    stopLoading()
                }.onOtherError {
                    showWarn(it.message ?: "")
                    stopLoading()
                }
        }
    }


    //-----------------------Inventory--------------------------------

    /**
     * 状态：  0-空   1-图标下   2-图标上
     */
    var snSort: Int = 0
    var trackingIdSort: Int = 0
    var postCodeSort: Int = 0
    var bitCodeSort: Int = 0
    var otherSort: Int = 0

    var sortPageNum = 0 // 当前页面索引
    private val sortPageSize = 20 // 每页数量
    var sortHasMore = true // 是否还有更多数据

    private val _inventoryList: MutableLiveData<List<ExcelDownloadVO>> = MutableLiveData()
    val inventoryList: LiveData<List<ExcelDownloadVO>> = _inventoryList
    private val _sortList: MutableLiveData<List<ExcelDownloadVO>> = MutableLiveData()
    val sortList: LiveData<List<ExcelDownloadVO>> = _sortList
    fun clearInventoryList(){
        _inventoryList.postValue(emptyList())
        _sortList.postValue(emptyList())

        sortPageNum = 0
        sortHasMore = true
    }

    fun getListBySort(endLoading: () -> Unit, noMore: () -> Unit) {
        if (!sortHasMore) {
            noMore.invoke()
            return
        }
        sortPageNum++
        viewModelScope.launch(Dispatchers.IO) {
            //继续查询排序结果
            rep.getTrackingIdListRep(sortPageNum, sortPageSize)
                .onSuccessOrNull {
                    it?.let {
                        if (it.isNotEmpty()) {
                            sortHasMore = true
                            val current = _inventoryList.value?.toMutableList() ?: mutableListOf()
                            current.addAll(it)
                            if (it.size != sortPageSize) {
                                sortHasMore = false
                                noMore.invoke()
                            } else {
                                endLoading.invoke()
                            }
                            _inventoryList.postValue(current)
                        }else{
                            sortPageNum--
                            sortHasMore = false
                            noMore.invoke()
                        }
                    }?: kotlin.run {
                        sortPageNum--
                        sortHasMore = false
                        noMore.invoke()
                    }
                }.onServerError { _, msg ->
                    showWarn("Query failed")
                    stopLoading()
                }.onOtherError {
                    showWarn(it.message ?: "")
                    stopLoading()
                }
        }
    }

    fun listSort(){
        val currentList = if(_sortList.value == null || _sortList.value?.isEmpty() == true){
            _inventoryList.value?.toMutableList() ?: mutableListOf()
        }else{
            _sortList.value!!.toMutableList()
        }
        var comparator: Comparator<ExcelDownloadVO>? = null

        fun addComparator(newComp: Comparator<ExcelDownloadVO>) {
            comparator = if (comparator == null) {
                newComp
            } else {
                comparator!!.thenComparing(newComp)
            }
        }

        when(snSort){
            1 -> addComparator(compareByDescending { it.sn })
            2 -> addComparator(compareBy { it.sn })
            else -> {}
        }
        when(trackingIdSort){
            1 -> addComparator(compareByDescending { it.trackingNumber })
            2 -> addComparator(compareBy { it.trackingNumber })
            else -> {}
        }
        when(postCodeSort){
            1 -> addComparator(compareByDescending { it.postCode })
            2 -> addComparator(compareBy { it.postCode })
            else -> {}
        }
        when(bitCodeSort){
            1 -> addComparator(compareByDescending { it.bitCode })
            2 -> addComparator(compareBy { it.bitCode })
            else -> {}
        }
        when(otherSort){
            1 -> addComparator(compareByDescending { it.db })
            2 -> addComparator(compareBy { it.db })
            else -> {}
        }

        // 如果所有都是 NONE，就按原始顺序
        if (comparator == null) {
            val list = _inventoryList.value ?: emptyList()
            _sortList.postValue(list)
            return
        }

        _sortList.postValue(currentList.sortedWith(comparator!!))
    }

    fun getItemByTracking(tracking: String, success: (ExcelDownloadVO) -> Unit, empty: () -> Unit){
        viewModelScope.launch(Dispatchers.IO){
            rep.getItemByTrackingIdRep(tracking,0,1)
                .onSuccessOrNull {
                    it?.let {
                        if(it.isEmpty()){
                            empty.invoke()
                        }else{
                            success.invoke(it[0])
                        }
                    }?: kotlin.run {
                        empty.invoke()
                    }
                }.onServerError { _, msg ->
                    showWarn("Submission failed")
                    stopLoading()
                }.onOtherError {
                    showWarn(it.message ?: "")
                    stopLoading()
                }
        }
    }

    private suspend fun showSuccess(msg: String) {
        withContext(Dispatchers.Main) {
            Toasty.success(appContext, msg).show()
        }
    }

    private suspend fun showWarn(msg: String) {
        withContext(Dispatchers.Main) {
            Toasty.warning(appContext, msg).show()
        }
    }

    private suspend fun showError(msg: String) {
        withContext(Dispatchers.Main) {
            Toasty.error(appContext, msg).show()
        }
    }

}

class MainRep() {
    private val service = RetrofitClient.createService<MainService>()
//    private val database = AppDatabase.getInstance(appContext)

    suspend fun getTrackingIdByEPCRep(epcList: List<String>): APIResult<List<ExcelDownloadVO>> {
        return safeNetworkInvoke {
            service.getTrackingIdByEPCNet(epcList)
        }
    }

    suspend fun inboundRep(body: List<String>): APIResult<InfoPromptsVO> {
        return safeNetworkInvoke {
            service.inboundNet(body)
        }
    }

    suspend fun outboundRep(body: List<String>): APIResult<InfoPromptsVO> {
        return safeNetworkInvoke {
            service.outboundNet(body)
        }
    }

    suspend fun getTrackingIdListRep(pageNum: Int, sortPageSize: Int): APIResult<List<ExcelDownloadVO>> {
        return safeNetWorkPagerInvoke {
            service.getTrackingIdListNet(2,pageNum, sortPageSize)
        }
    }

    suspend fun getItemByTrackingIdRep(tracking: String, pageNum: Int, sortPageSize: Int): APIResult<List<ExcelDownloadVO>> {
        return safeNetWorkPagerInvoke {
            service.getItemByTrackingIdNet(tracking,2,pageNum, sortPageSize)
        }
    }

}