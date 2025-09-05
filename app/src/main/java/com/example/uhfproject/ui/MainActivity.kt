package com.example.uhfproject.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.uhfproject.R
import com.example.uhfproject.app.MyApplication.Companion.appContext
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

        mViewModel.username = intent.getStringExtra("username") ?: ""

        uhfService = UHFService.getInstance(appContext)

        mViewModel.loading.observe(this) {
            if (it) {
                mBinding.mainLoading.visibility = View.VISIBLE
            } else {
                mBinding.mainLoading.visibility = View.GONE
            }
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navHostFragment.navController
    }

    override fun onResume() {
        super.onResume()
        // open UHF
        if(uhfService == null){
            uhfService = UHFService.getInstance(appContext)
        }
        val ret: Boolean = uhfService!!.open()
        if (!ret) {
            //UHF扫描加载失败
            LogUtil.d("UHF扫描加载失败")
        }else{
            LogUtil.d("UHF扫描加载成功")
        }
    }

//    private var keyStatus = false
    var onKeyDownCallback: ((keyCode: Int, event: KeyEvent?) -> Boolean)? = null

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        LogUtil.d("按键keyCode:$keyCode, action:${event?.action}")
        if (onKeyDownCallback?.invoke(keyCode, event) == true) {
            return true // Fragment 消费掉
        }
//        if (keyCode == 142 && event?.action == KeyEvent.ACTION_DOWN) {
//            if(!keyStatus){
//                keyStatus = true
//                mViewModel.startStock()
//            }else{
//                keyStatus = false
//                mViewModel.stopStock()
//            }
//            return true
//        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onPause() {
        super.onPause()
        uhfService?.close()
        LogUtil.d("UHF扫描关闭")
        uhfService = null
    }
}