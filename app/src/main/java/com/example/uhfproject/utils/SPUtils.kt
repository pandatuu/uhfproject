package com.example.uhfproject.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.uhfproject.app.MyApplication.Companion.appContext
import com.google.gson.Gson

/**
 * SharedPreferences 缓存工具类
 * 支持基本数据类型、对象、集合等数据的存储和读取
 */
object SPUtils {

    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()

    /**
     * 初始化工具类，必须在 Application 或启动 Activity 中调用
     * @param context 上下文对象
     * @param name SharedPreferences 文件名，默认为 "app_cache"
     */
    fun init(context: Context, name: String = "app_cache") {
        sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    /**
     * 存储字符串数据
     * @param key 键
     * @param value 值
     * @param commit 是否同步提交（默认异步 apply）
     */
    fun putString(key: String, value: String, commit: Boolean = false) {
        if (checkInit()) {
            sharedPreferences.edit(commit) {
                putString(key, value)
            }
        }
    }

    /**
     * 获取字符串数据
     * @param key 键
     * @param defaultValue 默认值
     * @return 存储的值或默认值
     */
    fun getString(key: String, defaultValue: String = ""): String {
        return if (checkInit()) {
            sharedPreferences.getString(key, defaultValue) ?: defaultValue
        } else {
            defaultValue
        }
    }

    /**
     * 存储整型数据
     * @param key 键
     * @param value 值
     * @param commit 是否同步提交
     */
    fun putInt(key: String, value: Int, commit: Boolean = false) {
        if (checkInit()) {
            sharedPreferences.edit(commit) {
                putInt(key, value)
            }
        }
    }

    /**
     * 获取整型数据
     * @param key 键
     * @param defaultValue 默认值
     * @return 存储的值或默认值
     */
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return if (checkInit()) {
            sharedPreferences.getInt(key, defaultValue)
        } else {
            defaultValue
        }
    }

    /**
     * 存储长整型数据
     * @param key 键
     * @param value 值
     * @param commit 是否同步提交
     */
    fun putLong(key: String, value: Long, commit: Boolean = false) {
        if (checkInit()) {
            sharedPreferences.edit(commit) {
                putLong(key, value)
            }
        }
    }

    /**
     * 获取长整型数据
     * @param key 键
     * @param defaultValue 默认值
     * @return 存储的值或默认值
     */
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return if (checkInit()) {
            sharedPreferences.getLong(key, defaultValue)
        } else {
            defaultValue
        }
    }

    /**
     * 存储浮点型数据
     * @param key 键
     * @param value 值
     * @param commit 是否同步提交
     */
    fun putFloat(key: String, value: Float, commit: Boolean = false) {
        if (checkInit()) {
            sharedPreferences.edit(commit) {
                putFloat(key, value)
            }
        }
    }

    /**
     * 获取浮点型数据
     * @param key 键
     * @param defaultValue 默认值
     * @return 存储的值或默认值
     */
    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return if (checkInit()) {
            sharedPreferences.getFloat(key, defaultValue)
        } else {
            defaultValue
        }
    }

    /**
     * 存储布尔型数据
     * @param key 键
     * @param value 值
     * @param commit 是否同步提交
     */
    fun putBoolean(key: String, value: Boolean, commit: Boolean = false) {
        if (checkInit()) {
            sharedPreferences.edit(commit) {
                putBoolean(key, value)
            }
        }
    }

    /**
     * 获取布尔型数据
     * @param key 键
     * @param defaultValue 默认值
     * @return 存储的值或默认值
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return if (checkInit()) {
            sharedPreferences.getBoolean(key, defaultValue)
        } else {
            defaultValue
        }
    }

    /**
     * 存储对象数据（使用 Gson 序列化）
     * @param key 键
     * @param value 对象
     * @param commit 是否同步提交
     */
    fun <T> putObject(key: String, value: T, commit: Boolean = false) {
        if (checkInit()) {
            val json = gson.toJson(value)
            putString(key, json, commit)
        }
    }

    /**
     * 获取对象数据
     * @param key 键
     * @param clazz 对象类类型
     * @return 对象或 null
     */
    fun <T> getObject(key: String, clazz: Class<T>): T? {
        return try {
            val json = getString(key)
            if (json.isNotEmpty()) {
                gson.fromJson(json, clazz)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 移除指定键的数据
     * @param key 键
     * @param commit 是否同步提交
     */
    fun remove(key: String, commit: Boolean = false) {
        if (checkInit()) {
            sharedPreferences.edit(commit) {
                remove(key)
            }
        }
    }

    /**
     * 清空所有数据
     * @param commit 是否同步提交
     */
    fun clear(commit: Boolean = false) {
        if (checkInit()) {
            sharedPreferences.edit(commit) {
                clear()
            }
        }
    }

    /**
     * 检查是否包含某个键
     * @param key 键
     * @return 是否包含
     */
    fun contains(key: String): Boolean {
        return if (checkInit()) {
            sharedPreferences.contains(key)
        } else {
            false
        }
    }

    /**
     * 检查是否初始化
     * @return 是否已初始化
     */
    private fun checkInit(): Boolean {
        if (!::sharedPreferences.isInitialized) {
            throw IllegalStateException("SPUtils must be initialized before use. Call SPUtils.init(context) first.")
        }
        return true
    }
}