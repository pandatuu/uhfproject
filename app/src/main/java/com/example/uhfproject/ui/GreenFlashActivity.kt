package com.example.uhfproject.ui

import android.graphics.Color
import android.view.WindowManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class GreenFlashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置全屏
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // 创建绿色视图
        val greenView = View(this)
        greenView.setBackgroundColor(Color.GREEN)
        setContentView(greenView)

        // 延迟后关闭
        Handler().postDelayed({
            finish()
            // 禁用关闭动画
            overridePendingTransition(0, 0)
        }, 300) // 显示100毫秒
    }
}