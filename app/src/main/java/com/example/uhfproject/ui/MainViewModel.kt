package com.example.uhfproject.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.uhfproject.app.MyApplication.Companion.appContext
import com.example.uhfproject.model.ExcelDownloadVO
import com.example.uhfproject.utils.LogUtil
import com.example.uhfproject.utils.RetrofitClient
import com.seuic.uhf.EPC
import com.seuic.uhf.UHFService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(application: Application): AndroidViewModel(application) {
    private val rep = MainRep()


    private val _epcList: MutableLiveData<List<String>> = MutableLiveData()
    val epcList: LiveData<List<String>> = _epcList
    /**
     * 单次扫描
     */
    fun onceScan(){
        val epc = EPC()
        if(UHFService.getInstance(appContext).inventoryOnce(epc, 100)){
            val id = epc.getId()
            if (id != null && "" != id) {
                val currentList = _epcList.value?.toMutableList() ?: mutableListOf()
                if(currentList.all { it != id }){
                    currentList.add(epc.getId())
                    _epcList.postValue(currentList)
                }
            }
        }
    }

    /**
     * 盘点监听
     */
    private var stockListener: Job? = null
    private var mInventoryStart = true
    fun startStock(){
        if(UHFService.getInstance(appContext).inventoryStart()){
            LogUtil.d("UHF盘点开启")
            mInventoryStart = true
            //开始盘点
            if(stockListener == null){
                stockListener = viewModelScope.launch {
                    while(mInventoryStart){
                        delay(100)
                        val tagIds = UHFService.getInstance(appContext).tagIDs.toSet()
                        LogUtil.d("tagIds-${tagIds.map { it.getId() }}")
//                        _epcList.postValue(tagIds.map { it.getId() })
                        getExcelDownloadByEmp(tagIds.map { it.getId() })
                    }
                }
            }else{
                if(!stockListener!!.isActive){
                    stockListener!!.start()
                }
            }
        } else {
            //盘点失败
            LogUtil.d("scan-盘点失败")
        }
    }

    fun stopStock(){
        if(UHFService.getInstance(appContext).inventoryStop()){
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
//    private val _inboundExcel: MutableLiveData<List<ExcelDownloadVO>> = MutableLiveData()
//    val inboundExcel: LiveData<List<ExcelDownloadVO>> = _inboundExcel

    private fun getExcelDownloadByEmp(map: List<String>) {

    }

    fun submitInBound(){

    }


    //-----------------------OutBound--------------------------------
//
//    private val _outboundExcel: MutableLiveData<List<ExcelDownloadVO>> = MutableLiveData()
//    val outboundExcel: LiveData<List<ExcelDownloadVO>> = _outboundExcel
//

    fun submitOutBound(){

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

    private val _inventoryList: MutableLiveData<List<ExcelDownloadVO>> = MutableLiveData()
    val inventoryList: LiveData<List<ExcelDownloadVO>> = _inventoryList

    var getListBySortTimer = 0
    fun getListBySort(){
        viewModelScope.launch(Dispatchers.IO){
            val nowTimer = System.currentTimeMillis()
            if(nowTimer - getListBySortTimer < 500){
                //点击过快，避免请求接口太多
                //手动添加一个延时效果
                delay(1000)
            }
            //继续查询排序结果
        }
    }


}

class MainRep(){
    private val mainService = RetrofitClient.createService<MainService>()
//    private val database = AppDatabase.getInstance(appContext)


}