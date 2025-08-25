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

    private var keyStatus: Boolean = false

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == 142 && event?.action == KeyEvent.ACTION_UP) {
            if(!keyStatus){
                LogUtil.d("UHF扫描开启")
                mViewModel.startStock()
            }else{
                LogUtil.d("UHF扫描关闭")
                mViewModel.stopStock()
            }
            keyStatus = !keyStatus
            true
        }else{
            false
        }
    }

    override fun onPause() {
        super.onPause()
        uhfService?.close()
    }
}