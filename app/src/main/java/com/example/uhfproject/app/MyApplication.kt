package com.example.uhfproject.app

import android.app.Application
import android.content.Context
import com.example.uhfproject.room.AppDatabase

class MyApplication: Application() {

    companion object{
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        AppDatabase.getInstance(this)
    }


}