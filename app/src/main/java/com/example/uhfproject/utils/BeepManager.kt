package com.example.uhfproject.utils

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import com.example.uhfproject.R
import com.example.uhfproject.app.MyApplication.Companion.appContext

object BeepSound {
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0

    fun init() {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool(3, AudioManager.STREAM_MUSIC, 20)

        // 加载 raw/beep.ogg
        soundId = soundPool!!.load(appContext, R.raw.scan, 1)
    }

    fun play() {
        soundPool?.play(soundId, 0.5f, 0.5f, 0, 0, 1f)
    }

    fun controlPlay(rssi: Float) {
        soundPool?.play(soundId, rssi, rssi, 0, 0, 1f)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}