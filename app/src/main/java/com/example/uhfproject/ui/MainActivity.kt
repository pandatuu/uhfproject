package com.example.uhfproject.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.uhfproject.databinding.ActivityMainBinding
import com.seuic.uhf.UHFService

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mViewModel: MainViewModel

    private var uhfService: UHFService? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        uhfService = UHFService.getInstance()

    }

    override fun onResume() {
        super.onResume()
        // open UHF
        if(uhfService == null){
            uhfService = UHFService.getInstance()
        }
        val ret: Boolean = uhfService!!.open()
        if (!ret) {
            //UHF扫描加载失败

        }
    }

    override fun onPause() {
        super.onPause()
        uhfService?.close()
    }
}