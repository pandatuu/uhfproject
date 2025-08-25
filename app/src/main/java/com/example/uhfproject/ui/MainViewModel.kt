package com.example.uhfproject.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.uhfproject.utils.LogUtil
import com.example.uhfproject.utils.RetrofitClient
import com.example.uhfproject.utils.SingleLiveEvent
import com.seuic.uhf.EPC
import com.seuic.uhf.UHFService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
        if(UHFService.getInstance().inventoryOnce(epc, 100)){
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
        if(UHFService.getInstance().inventoryStart()){
            LogUtil.d("UHF盘点开启")
            mInventoryStart = true
            //开始盘点
            if(stockListener == null){
                stockListener = viewModelScope.launch {
                    while(mInventoryStart){
                        delay(100)
                        val time1 = System.currentTimeMillis()
                        val tagIds = UHFService.getInstance().tagIDs.toSet()
                        LogUtil.d("tagIds耗时-${System.currentTimeMillis()-time1}")
                        _epcList.postValue(tagIds.map { it.getId() })
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
        if(UHFService.getInstance().inventoryStop()){
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

}

class MainRep(){
    private val mainService = RetrofitClient.createService<MainService>()
//    private val database = AppDatabase.getInstance(appContext)


}