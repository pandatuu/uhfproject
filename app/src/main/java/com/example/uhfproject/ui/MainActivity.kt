package com.example.uhfproject.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.uhfproject.R
import com.example.uhfproject.databinding.ActivityMainBinding
import com.example.uhfproject.utils.LogUtil
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

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
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
            LogUtil.d("UHF扫描加载失败")
        }else{
            LogUtil.d("UHF扫描加载成功")
        }
    }

    private var keyStatus = false

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        LogUtil.d("按键keyCode:$keyCode, action:${event?.action}")
        if (keyCode == 142 && event?.action == KeyEvent.ACTION_DOWN) {
            if(!keyStatus){
                keyStatus = true
                mViewModel.startStock()
            }else{
                keyStatus = false
                mViewModel.stopStock()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onPause() {
        super.onPause()
        uhfService?.close()
        uhfService = null
    }
}