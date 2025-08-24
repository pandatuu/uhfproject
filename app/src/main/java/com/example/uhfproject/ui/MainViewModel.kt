package com.example.uhfproject.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.uhfproject.app.MyApplication.Companion.appContext
import com.example.uhfproject.room.AppDatabase
import com.example.uhfproject.utils.RetrofitClient

class MainViewModel(application: Application): AndroidViewModel(application) {
    private val rep = MainRep()
}

class MainRep(){
    private val mainService = RetrofitClient.createService<MainService>()
    private val database = AppDatabase.getInstance(appContext)


}