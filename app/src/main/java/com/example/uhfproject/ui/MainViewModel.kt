package com.example.uhfproject.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.uhfproject.app.MyApplication.Companion.appContext
import com.example.uhfproject.room.AppDatabase
import com.example.uhfproject.utils.RetrofitClient
import com.seuic.uhf.EPC
import com.seuic.uhf.UHFService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(application: Application): AndroidViewModel(application) {
    private val rep = MainRep()


    private val _epcList: MutableLiveData<List<EPC>> = MutableLiveData()
    val epcList: LiveData<List<EPC>> = _epcList
    /**
     * 单次扫描
     */
    fun onceScan(){
        val epc = EPC()
        if(UHFService.getInstance().inventoryOnce(epc, 100)){
            val id = epc.getId()
            if (id != null && "" != id) {
                val currentList = _epcList.value?.toMutableList() ?: mutableListOf()
                if(currentList.all { it.getId() != id }){
                    currentList.add(epc)
                    _epcList.postValue(currentList)
                }
            }
        }
    }

    private var mInventoryStart = false

    /**
     * 盘点监听
     */
    private var stockListener: Job? = null

    fun startStock(){
        if(UHFService.getInstance().inventoryStart()){
            mInventoryStart = true
            //开始盘点
            if(stockListener == null){
                stockListener =  viewModelScope.launch {
                    while(mInventoryStart){
                        val tagIds = UHFService.getInstance().tagIDs
                        _epcList.postValue(tagIds)
                        delay(100)
                    }
                }
            }
            stockListener!!.start()
        } else {
            //盘点失败
        }
    }

    fun stopStock(){
        if(UHFService.getInstance().inventoryStop()){
            //停止盘点
            stockListener?.let {
                if(it.isActive){
                    mInventoryStart = false
                    it.cancel()
                    stockListener = null
                }
            }
        } else {
            //停止失败
        }
    }

}

class MainRep(){
    private val mainService = RetrofitClient.createService<MainService>()
    private val database = AppDatabase.getInstance(appContext)


}