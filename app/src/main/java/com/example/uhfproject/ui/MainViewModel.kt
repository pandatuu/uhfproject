package com.example.uhfproject.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.uhfproject.app.MyApplication.Companion.appContext
import com.example.uhfproject.model.*
import com.example.uhfproject.utils.*
import com.example.uhfproject.utils.retrofit.*
import com.seuic.uhf.EPC
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application), ScanResultListener {
    private val rep = ApiRepository.instance
    private val scanningManager = ScanningManager(viewModelScope, this)

    private val _loading: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val loading: LiveData<Boolean> = _loading

    private val _findList: SingleLiveEvent<List<EPC>> = SingleLiveEvent()
    val findList: LiveData<List<EPC>> = _findList

    private val _startBtn: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val startBtn: LiveData<Boolean> = _startBtn
    private val _stopBtn: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val stopBtn: LiveData<Boolean> = _stopBtn

    private val _isScanning = MutableLiveData(false)
    val isScanning: LiveData<Boolean> = _isScanning

    private val _boundExcel: MutableLiveData<List<ExcelDownloadVO>> = MutableLiveData()
    val boundExcel: LiveData<List<ExcelDownloadVO>> = _boundExcel

    private val _inventoryList: MutableLiveData<List<ExcelDownloadVO>> = MutableLiveData()
    val inventoryList: LiveData<List<ExcelDownloadVO>> = _inventoryList
    private val _sortList: MutableLiveData<List<ExcelDownloadVO>> = MutableLiveData()
    val sortList: LiveData<List<ExcelDownloadVO>> = _sortList

    private val _scanMode = MutableStateFlow(ScanMode.NONE)
    val scanMode: StateFlow<ScanMode> = _scanMode


    private val _ponInboundExcel: MutableLiveData<List<TrackingVO>> = MutableLiveData()
    val ponInboundExcel: LiveData<List<TrackingVO>> = _ponInboundExcel

    private val _ponException: MutableLiveData<List<TrackingVO>> = MutableLiveData()
    val ponException: LiveData<List<TrackingVO>> = _ponException

    private var cacheInboundEpcList = mutableListOf<TrackingVO>()
    val matchList = mutableListOf<TrackingVO>()
    val otherSiteList = mutableListOf<TrackingVO>()

    fun exitPonInbound(){
        //清除缓存
        cacheInboundEpcList.clear()
        matchList.clear()
        otherSiteList.clear()
    }

    override fun onScanResults(tags: Set<EPC>) {
        when (_scanMode.value) {
            ScanMode.PON_INBOUND -> {
                viewModelScope.launch(Dispatchers.IO) {
                    // 1. Get current state and filter for truly new EPCs
                    val epcStrings = tags.map { it.getId() }.toSet()
                    val newEpcStrings = epcStrings.filter { epc ->
                        !cacheInboundEpcList.any { it.epc == epc }
                    }
                    if (newEpcStrings.isEmpty()) {
                        return@launch // Nothing new to process
                    }
                    LogUtil.d("PON_INBOUND-----newEpc:${newEpcStrings}")

                    rep.getPonTrackingIdByEPCRep(newEpcStrings)
                        .onSuccess {
                            LogUtil.d("PON_INBOUND-----接口:${this.map { it.epc }}")
                            newEpcStrings.forEach { epc->
                                this.find { it.epc == epc }?.let { apiResult->
                                    LogUtil.d("PON_INBOUND-----找到item:$apiResult")
                                    cacheInboundEpcList.add(apiResult)
                                    //比较邮局名称
                                    when{
                                        apiResult.siteId == null -> {
                                            //无邮局记录
                                        }
                                        apiResult.trackingNumber == null ->{
                                            apiResult.resultStatus = 0
                                            otherSiteList.add(apiResult)
                                        }
                                        currentSiteId == apiResult.siteId && apiResult.trackingNumber!=null  -> {
                                            //匹配数据
                                            apiResult.resultStatus = 2
                                            matchList.add(apiResult)
                                        }
                                        currentSiteId != apiResult.siteId -> {
                                            //其他邮局
                                            apiResult.resultStatus = 1
                                            otherSiteList.add(apiResult)
                                        }
                                    }
                                }
                            }
                            LogUtil.d("PON_INBOUND-----接口完成")
                            _ponInboundExcel.postValue(matchList)
                            _ponException.postValue(otherSiteList)
                        }
                        .onServerError { _, msg ->
                            // On a server error, do nothing but show a warning.
                            // No records are created, no UI lists are updated.
                            showWarn(msg)
                        }
                        .onOtherError {
                            showWarn(it.message ?: "Unknown error")
                        }
                }
            }
            ScanMode.OUTBOUND,ScanMode.INBOUND, ScanMode.IB_VERIFY, ScanMode.OB_VERIFY -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val epcStrings = tags.map { it.getId() }
                    rep.getTrackingIdByEPCRep(epcStrings)
                        .onSuccess {
                            val scannedItems = this
                            val currentList = _boundExcel.value ?: emptyList()
                            val newList = updateListWithNewItemsAtTop(currentList, scannedItems) { it.epc }
                            if (newList !== currentList) { // Post only if the list has changed
                                _boundExcel.postValue(newList)
                            }
                        }
                        .onServerError { _, msg -> showWarn(msg) }
                        .onOtherError { showWarn(it.message ?: "Unknown error") }
                }
            }
            ScanMode.FIND_ITEM, ScanMode.RFID_BINDING, ScanMode.ITEM_QUERY, ScanMode.DEBUG -> {
                _findList.postValue(tags.toList())
            }
            ScanMode.INVENTORY -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val epcStrings = tags.map { it.getId() }
                    rep.getTrackingIdByEPCRep(epcStrings)
                        .onSuccess {
                            val scannedItems = this
                            val currentList = _inventoryList.value ?: emptyList()
                            val newList = updateListWithNewItemsAtTop(currentList, scannedItems) { it.epc }
                            if (newList !== currentList) { // Post only if the list has changed
                                _inventoryList.postValue(newList)
                            }
                        }
                        .onServerError { _, msg -> showWarn(msg) }
                        .onOtherError { showWarn(it.message ?: "Unknown error") }
                }
            }
            ScanMode.NONE -> {
                // Do nothing
            }
        }
    }

    /**
     * Updates a list by adding new items to the top.
     * This function ensures that only items not already in the list are added.
     *
     * @param currentList The current list of items.
     * @param newItems The new items to potentially add.
     * @param keySelector A function to extract a unique key from each item.
     * @return A new list with new items at the top, or the original list if no new items were found.
     */
    private fun <T, K> updateListWithNewItemsAtTop(
        currentList: List<T>,
        newItems: List<T>,
        keySelector: (T) -> K
    ): List<T> {
        if (newItems.isEmpty()) {
            return currentList // No new items to add
        }

        val existingKeys = currentList.map(keySelector).toSet()
        val trulyNewItems = newItems.filter { keySelector(it) !in existingKeys }

        return if (trulyNewItems.isNotEmpty()) {
            trulyNewItems + currentList // Add new items to the top
        } else {
            currentList // No changes, return the original list
        }
    }

    fun setScanMode(mode: ScanMode) {
        _scanMode.value = mode
        if (mode == ScanMode.NONE) {
            clearBoundList()
        }
    }

    fun startScanning() {
        // The internal logic in ScanningManager and onScanResults already handles the NONE case gracefully.
        _isScanning.value = true
        scanningManager.start(_scanMode.value)
        _startBtn.postValue(true)
    }

    fun stopScanning() {
        _isScanning.value = false
        scanningManager.stop()
        _stopBtn.postValue(true)
    }

    var username = ""
    var userPermission = true
    var mode = 0

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
            loadingTimer = null
        }
    }

    var sumBoundList: List<ExcelDownloadVO> = emptyList()

    fun getBoundList(status: Int, success: (List<ExcelDownloadVO>) -> Unit){
        startLoading()
        viewModelScope.launch(Dispatchers.IO) {
            rep.getBoundListRep(status)
                .onSuccess{
                    sumBoundList = this
                    success.invoke(this)
                    stopLoading()
                }.onServerError { code, msg ->
                    showWarn(msg)
                    LogUtil.d("code:$code, msg:$msg")
                    stopLoading()
                }.onOtherError {
                    showWarn(it.message ?: "")
                    LogUtil.d(it.message ?: "")
                    stopLoading()
                }
        }
    }

    //-----------------------InBound--------------------------------

    fun clearBoundList(){
        _boundExcel.postValue(emptyList())
    }

    fun submitInBound(list: List<String>, success: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            rep.inboundRep(list)
                .onSuccess {
                    showSuccess(this.msg?:"Submission Success")
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

    fun submitInBoundPon(success: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val dto = PONInboundDTO(
                matchList.map { it.epc },
                currentSiteId
            )
            rep.inboundPonRep(dto)
                .onSuccess {
                    showSuccess(this.msg?:"Submission Success")
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

    fun submitOutBound(list: List<String>, success: () -> Unit) {
        startLoading()
        viewModelScope.launch(Dispatchers.IO) {
            rep.outboundRep(list)
                .onSuccess {
                    showSuccess(this.msg?:"Submission Success")
                    success.invoke()
                    stopLoading()
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
    var postalCodeSort: Int = 0
    var beatSort: Int = 0
    var otherSort: Int = 0

    var sortPageNum = 0 // 当前页面索引
    private val sortPageSize = 20 // 每页数量
    var sortHasMore = true // 是否还有更多数据

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
                    endLoading.invoke()
                    stopLoading()
                }.onOtherError {
                    showWarn(it.message ?: "")
                    endLoading.invoke()
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
        when(postalCodeSort){
            1 -> addComparator(compareByDescending { it.postalCode })
            2 -> addComparator(compareBy { it.postalCode })
            else -> {}
        }
        when(beatSort){
            1 -> addComparator(compareByDescending { it.beat })
            2 -> addComparator(compareBy { it.beat })
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

    fun getItemByTracking(tracking: String, success: (String?) -> Unit, empty: () -> Unit){
        viewModelScope.launch(Dispatchers.IO){
            if(mode == 0){
                rep.getPonItemByTrackingIdRep(tracking,0,1)
                    .onSuccessOrNull {
                        it?.let {
                            if(it.isEmpty()){
                                empty.invoke()
                            }else{
                                success.invoke(it[0].epc)
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
            }else{
                rep.getItemByTrackingIdRep(tracking,0,1)
                    .onSuccessOrNull {
                        it?.let {
                            if(it.isEmpty()){
                                empty.invoke()
                            }else{
                                success.invoke(it[0].epc)
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
    }

    //-----------------------Dashboard--------------------------------

    fun getHeadInfo(success: (DashboardNumberVO) -> Unit){
        viewModelScope.launch(Dispatchers.IO){
            rep.getStatisticsRep()
                .onSuccess {
                    success.invoke(this)
                }.onServerError { code, msg ->
                    showError(msg)
                }.onOtherError {
                    showError(it.message?: "")
                }
        }
    }
    fun getPonHeadInfo(success: (StatisticsVO) -> Unit){
        viewModelScope.launch(Dispatchers.IO){
            rep.getPonStatisticsRep()
                .onSuccess {
                    success.invoke(this)
                }.onServerError { code, msg ->
                    showError(msg)
                }.onOtherError {
                    showError(it.message?: "")
                }
        }
    }

    fun getRvList(success: (List<DashboardTopVO>) -> Unit){
        viewModelScope.launch(Dispatchers.IO){
            rep.getTopRep()
                .onSuccess {
                    success.invoke(this)
                }.onServerError { code, msg ->
                    showError(msg)
                }.onOtherError {
                    showError(it.message?: "")
                }
        }
    }


    fun getBeatData(db: String, success: (OutboundVerifyVO) -> Unit){
        viewModelScope.launch(Dispatchers.IO){
            rep.getBeatDataRep(db)
                .onSuccess {
                    success.invoke(this)
                    showSuccess("Query Success")
                }.onServerError { code, msg ->
                    showError(msg)
                }.onOtherError {
                    showError(it.message?: "")
                }
        }
    }

    fun getLifeCycleByEPCRep(epc: String, success: (LifeCycleVO) -> Unit) {
        viewModelScope.launch(Dispatchers.IO){
            rep.getLifeCycleByEPCRep(epc)
                .onSuccess {
                    LogUtil.d("getTrackingIdByEPCRep-result:${this}")
                    success.invoke(this)
                }.onServerError { code, msg ->
                    showWarn(msg)
                    LogUtil.d("code:$code, msg:$msg")
                }.onOtherError {
                    showWarn(it.message ?: "")
                    LogUtil.d(it.message ?: "")
                }
        }
    }
    fun getPonLifeCycleByEPCRep(epc: String, success: (PONLifeCycleVO) -> Unit) {
        viewModelScope.launch(Dispatchers.IO){
            rep.getPonLifeCycleByEPCRep(epc)
                .onSuccess {
                    LogUtil.d("getTrackingIdByEPCRep-result:${this}")
                    success.invoke(this)
                }.onServerError { code, msg ->
                    showWarn(msg)
                    LogUtil.d("code:$code, msg:$msg")
                }.onOtherError {
                    showWarn(it.message ?: "")
                    LogUtil.d(it.message ?: "")
                }
        }
    }

    fun validateBarcode(barcode: String): Boolean {
        // Placeholder validation
        return true
    }

    fun validateEPC(epc: String): Boolean {
        // Placeholder validation
        return true
    }

    val bindResult = SingleLiveEvent<CommonModel>()
    fun bind(body: BindBody){
        viewModelScope.launch(Dispatchers.IO){
            rep.bindRep(listOf(body))
                .onSuccess {
                    bindResult.postValue(CommonModel(true, "Bind Success"))
                }.onServerError { code, msg ->
                    bindResult.postValue(CommonModel(false, msg))
                    LogUtil.d("code:$code, msg:$msg")
                }.onOtherError {
                    bindResult.postValue(CommonModel(false, it.message ?: ""))
                    LogUtil.d(it.message ?: "")
                }
        }
    }
    fun bindPon(body: PONBindVO){
        viewModelScope.launch(Dispatchers.IO){
            rep.bindPonRep(listOf(body))
                .onSuccess {
                    bindResult.postValue(CommonModel(true, "Bind Success"))
                }.onServerError { code, msg ->
                    if(code == 200){
                        bindResult.postValue(CommonModel(true, "Bind Success"))
                    }else{
                        bindResult.postValue(CommonModel(false, msg))
                    }
                    LogUtil.d("code:$code, msg:$msg")
                }.onOtherError {
                    bindResult.postValue(CommonModel(false, it.message ?: ""))
                    LogUtil.d(it.message ?: "")
                }
        }
    }

    var siteList: List<UserSiteDTO> = emptyList()
    var currentSiteId: Int = -1
    var siteIdByUser: Int = -1
    fun getBoundAndInboundCount(success: (BoundAndInboundVO) -> Unit){
        viewModelScope.launch {
            rep.getBoundAndInboundCountRep(currentSiteId)
                .onSuccess {
                    success.invoke(this)
                }.onServerError { code, msg ->
                    LogUtil.d("code:$code, msg:$msg")
                }.onOtherError {
                    LogUtil.d(it.message ?: "")
                }
        }
    }
    fun getBoundDataBySiteId(success: (List<TrackingVO>) -> Unit){
        viewModelScope.launch {
            rep.getBoundDataBySiteIdRep(currentSiteId)
                .onSuccess {
                    success.invoke(this)
                }.onServerError { code, msg ->
                    LogUtil.d("code:$code, msg:$msg")
                }.onOtherError {
                    LogUtil.d(it.message ?: "")
                }
        }
    }
    fun getPermsByUserId(success: (Boolean) -> Unit){
        viewModelScope.launch {
            rep.getPermsByUserIdRep()
                .onSuccess {
                    success.invoke(this)
                }.onServerError { code, msg ->
                    LogUtil.d("code:$code, msg:$msg")
                }.onOtherError {
                    LogUtil.d(it.message ?: "")
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

    fun checkEpcInDatabase(epc: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            rep.getTrackingIdByEPCRep(listOf(epc))
                .onSuccess {
                    callback.invoke(this.isNotEmpty())
                }.onServerError { _, msg ->
                    showWarn(msg)
                    callback.invoke(false)
                }.onOtherError {
                    showWarn(it.message ?: "")
                    callback.invoke(false)
                }
        }
    }

}