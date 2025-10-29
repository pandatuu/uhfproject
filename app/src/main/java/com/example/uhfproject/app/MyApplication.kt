package com.example.uhfproject.app

import android.app.Application
import android.content.Context
import com.example.uhfproject.utils.SPUtils
import com.example.uhfproject.utils.retrofit.RetrofitClient

//import com.example.uhfproject.room.AppDatabase

class MyApplication: Application() {

    companion object{
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        SPUtils.init(this)
        RetrofitClient.init(this)
//        AppDatabase.getInstance(this)
    }


}