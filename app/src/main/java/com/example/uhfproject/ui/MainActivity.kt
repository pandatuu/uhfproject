package com.example.uhfproject.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.uhfproject.R
import com.example.uhfproject.app.MyApplication.Companion.appContext
import com.example.uhfproject.databinding.ActivityMainBinding
import com.example.uhfproject.ui.fragment.DebugScanFragment
import com.example.uhfproject.utils.BeepSound
import com.example.uhfproject.utils.LogUtil
import com.seuic.uhf.UHFService

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel

    private var uhfService: UHFService? = null
    private var isScanKeyDown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        mainViewModel.username = intent.getStringExtra("username") ?: ""

        uhfService = UHFService.getInstance(appContext)

        mainViewModel.loading.observe(this) {
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
        val ret: Boolean = uhfService?.open() ?: false
        if (!ret) {
            LogUtil.d("UHF scan load failed")
        } else {
            LogUtil.d("UHF scan load success")
            BeepSound.init()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 142 && event?.action == KeyEvent.ACTION_DOWN) {
            if (!isScanKeyDown) {
                isScanKeyDown = true
                
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
                val currentFragment = navHostFragment.childFragmentManager.fragments.getOrNull(0)

                // Special handling for DebugScanFragment
                if (currentFragment is DebugScanFragment) {
                    currentFragment.onTriggerDown()
                } else {
                    val navController = findNavController(R.id.navHostFragment)
                    val currentDestinationId = navController.currentDestination?.id
                    // Exclude scanning on Dashboard/MainFragment
                    if (currentDestinationId != R.id.mainFragment && currentDestinationId != R.id.dashboardFragment) {
                        mainViewModel.startScanning()
                    }
                }
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 142 && event?.action == KeyEvent.ACTION_UP) {
            if (isScanKeyDown) {
                isScanKeyDown = false

                val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
                val currentFragment = navHostFragment.childFragmentManager.fragments.getOrNull(0)

                // Special handling for DebugScanFragment
                if (currentFragment is DebugScanFragment) {
                    currentFragment.onTriggerUp()
                } else {
                    mainViewModel.stopScanning()
                }
            }
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onPause() {
        super.onPause()
        if (isScanKeyDown) {
            isScanKeyDown = false
            mainViewModel.stopScanning()
        }
        uhfService?.close()
        LogUtil.d("UHF scan closed")
        BeepSound.release()
    }
}
