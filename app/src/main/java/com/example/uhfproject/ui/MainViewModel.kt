package com.example.uhfproject.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.uhfproject.app.MyApplication.Companion.appContext
import com.example.uhfproject.model.*
import com.example.uhfproject.utils.BeepSound
import com.example.uhfproject.utils.LogUtil
import com.example.uhfproject.utils.SingleLiveEvent
import com.example.uhfproject.utils.retrofit.*
import com.seuic.uhf.EPC
import com.seuic.uhf.UHFService
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val rep = MainRep()

    var username = ""

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

    private val _findList: SingleLiveEvent<List<EPC>> = SingleLiveEvent()
    val findList: LiveData<List<EPC>> = _findList

    private val _startBtn: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val startBtn: LiveData<Boolean> = _startBtn
    private val _stopBtn: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val stopBtn: LiveData<Boolean> = _stopBtn

    private var sumBoundList: List<ExcelDownloadVO> = emptyList()

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

    var startQuest = false
    private val stringSet = mutableSetOf<String>()   // 用来存最新的 String 集合
    /**
     * 盘点监听
     */
    private var stockListener: Job? = null
    private var mInventoryStart = true
    fun startStock() {
        if (UHFService.getInstance(appContext).inventoryStart()) {
            UHFService.getInstance().registerReadTags {  }
//            LogUtil.d("UHF盘点开启")
            mInventoryStart = true
            _startBtn.postValue(true)
            //开始盘点
            if (stockListener == null) {
                stockListener = viewModelScope.launch(Dispatchers.IO) {
                    while (mInventoryStart) {
                        val epcList = UHFService.getInstance(appContext).tagIDs.toSet()
                        BeepSound.play()
                        if(startQuest){
                            val idList = epcList.map { it.getId() }
                            val change = synchronized(stringSet) {
                                val toAdd = idList - stringSet
                                val toRemove = stringSet - idList
                                stringSet.addAll(toAdd)
                                stringSet.removeAll(toRemove)
                                toAdd.isNotEmpty() || toRemove.isNotEmpty()
                            }
                            if(change){
                                val result = synchronized(stringSet) {
                                    sumBoundList.filter { it.epc in stringSet }
                                }
                                _boundExcel.postValue(result.reversed())
                            }
                            delay(500)
                        }else{
                            _findList.postValue(epcList.toList())
                            delay(100)
                        }
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
//            LogUtil.d("UHF盘点关闭")
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

    fun submitInBound(list: List<String>, success: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            rep.inboundRep(list)
                .onSuccess {
                    showSuccess(this@onSuccess.msg?:"Submission Success")
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
                    showSuccess(this@onSuccess.msg?:"Submission Success")
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

    //-----------------------Dashboard--------------------------------

    fun getHeadInfo(success: (DashboardNumberVO) -> Unit){
        viewModelScope.launch(Dispatchers.IO){
            rep.getStatisticsRep()
                .onSuccess {
                    success.invoke(this)
                }.onServerError { code, msg ->
                    showError(msg)
                }.onOtherError {
                    showError(it.message?:"")
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
                    showError(it.message?:"")
                }
        }
    }


    fun getBeatData(db: String, success: (OutboundVerifyVO) -> Unit){
        viewModelScope.launch(Dispatchers.IO){
            rep.getBeatDataRep(db)
                .onSuccess {
                    success.invoke(this)
                }.onServerError { code, msg ->
                    showError(msg)
                }.onOtherError {
                    showError(it.message?:"")
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

    fun bind(body: BindBody){
        viewModelScope.launch(Dispatchers.IO){
            rep.bindRep(body)
                .onSuccess {
                    showSuccess("Bind Success")
                }.onServerError { code, msg ->
                    showWarn(msg)
                    LogUtil.d("code:$code, msg:$msg")
                }.onOtherError {
                    showWarn(it.message ?: "")
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
            service.getItemByTrackingIdNet(tracking,pageNum, sortPageSize)
        }
    }

    suspend fun getStatisticsRep(): APIResult<DashboardNumberVO> {
        return safeNetworkInvoke {
            service.getStatisticsNet()
        }
    }

    suspend fun getTopRep(): APIResult<List<DashboardTopVO>> {
        return safeNetworkInvoke {
            service.getTopNet()
        }
    }

    suspend fun getBoundListRep(status: Int): APIResult<List<ExcelDownloadVO>> {
        return safeNetworkInvoke {
            service.getAllListNet(status)
        }
    }

    suspend fun getBeatDataRep(beatPrefix: String): APIResult<OutboundVerifyVO> {
        return safeNetworkInvoke {
            service.getBeatDataNet(beatPrefix)
        }
    }

    suspend fun getLifeCycleByEPCRep(epc: String): APIResult<LifeCycleVO> {
        return safeNetworkInvoke {
            service.getLifeCycleByEPCNet(epc)
        }
    }

    suspend fun bindRep(body: BindBody): APIResult<Any> {
        return safeNetworkInvoke {
            service.bindNet(body)
        }
    }
}