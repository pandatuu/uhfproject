package com.example.uhfproject.utils

import android.util.Log

object LogUtil {
    
    private const val TAG = "UHF_APP"
    private const val LOG_LEVEL_NONE = 0 //不输出任和log
    private const val LOG_LEVEL_DEBUG = 1 //调试 蓝色
    private const val LOG_LEVEL_INFO = 2 //提现 绿色
    private const val LOG_LEVEL_WARN = 3 //警告 橙色
    private const val LOG_LEVEL_ERROR = 4 //错误 红色
    private const val LOG_LEVEL_ALL = 5 //输出所有等级

    /**
     * 允许输出的log日志等级
     * 当出正式版时,把mLogLevel的值改为 LOG_LEVEL_NONE,
     * 就不会输出任何的Log日志了.
     */
    var logLevel = LOG_LEVEL_ALL

    /**
     * 以级别为 d 的形式输出LOG,输出debug调试信息
     */
    fun d(msg: String) {
        if (logLevel >= LOG_LEVEL_DEBUG) {
            Log.d(TAG, msg)
        }
    }

    /**
     * 以级别为 i 的形式输出LOG,一般提示性的消息information
     */
    fun i(msg: String) {
        if (logLevel >= LOG_LEVEL_INFO) {
            Log.i(TAG, msg)
        }
    }

    /**
     * 以级别为 w 的形式输出LOG,显示warning警告，一般是需要我们注意优化Android代码
     */
    fun w(msg: String) {
        if (logLevel >= LOG_LEVEL_WARN) {
            Log.w(TAG, msg)
        }
    }

    /**
     * 以级别为 e 的形式输出LOG ，红色的错误信息，查看错误源的关键
     */
    fun e(msg: String) {
        if (logLevel >= LOG_LEVEL_ERROR) {
            Log.e(TAG, msg)
        }
    }

    /**
     * 以级别为 v 的形式输出LOG ，verbose啰嗦的意思
     *
     */
    fun v(msg: String) {
        if (logLevel >= LOG_LEVEL_ALL) {
            Log.v(TAG, msg)
        }
    }
}