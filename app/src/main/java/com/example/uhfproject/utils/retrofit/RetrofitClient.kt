package com.example.uhfproject.utils.retrofit

import com.example.uhfproject.utils.Const.ACCESS_TOKEN
import com.example.uhfproject.utils.Const.ip
import com.example.uhfproject.utils.Const.port
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val BASE_URL = "http://$ip:$port"
    val urlInterceptor = BaseUrlInterceptor(BASE_URL)
    private val tokenInterceptor = UpdateTokenInterceptor(ACCESS_TOKEN)

    private const val TIMEOUT = 30L // 超时时间30秒

    private val okHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // 根据需求调整日志级别
        }

        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(urlInterceptor)
            .addInterceptor(tokenInterceptor)
            .addInterceptor(loggingInterceptor)
            // 可以添加其他拦截器，如认证拦截器
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }

    // 使用内联函数和reified类型参数简化API服务创建
    inline fun <reified T> createService(): T {
        return createService(T::class.java)
    }

    fun updateTokenAndRefreshToken(token: String) {
        if (token != null) {
            tokenInterceptor.updateTokenAndRefreshToken(token)
        }
    }
}