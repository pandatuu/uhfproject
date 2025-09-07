package com.example.uhfproject.utils

import android.content.Context
import android.media.AudioManager
import com.example.uhfproject.app.MyApplication.Companion.appContext

class VolumeController {

    private val audioManager: AudioManager by lazy {
        appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    /**
     * 获取当前媒体音量
     */
    fun getCurrentVolume(): Int {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    /**
     * 获取最大媒体音量
     */
    private fun getMaxVolume(): Int {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }

    /**
     * 设置媒体音量到特定值
     * @param volume 0到最大音量之间的值
     */
    private fun setVolume(volume: Int) {
        // 确保音量值在有效范围内
        val maxVolume = getMaxVolume()
        val adjustedVolume = volume.coerceIn(0, maxVolume)

        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            adjustedVolume,
            0 // 不显示UI变化
        )
    }

    /**
     * 设置媒体音量百分比
     * @param percent 0到100之间的百分比值
     */
    fun setVolumePercent(percent: Int) {
        val adjustedPercent = percent.coerceIn(0, 100)
        val maxVolume = getMaxVolume()
        val targetVolume = (maxVolume * adjustedPercent / 100).coerceAtLeast(1)

        setVolume(targetVolume)
    }
}