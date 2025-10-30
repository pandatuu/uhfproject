package com.example.uhfproject.utils.retrofit

import android.content.Context
import com.example.uhfproject.utils.Const
import com.example.uhfproject.utils.Const.ACCESS_TOKEN
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val TIMEOUT = 30L // Timeout 30 seconds

    lateinit var urlInterceptor: BaseUrlInterceptor
    private lateinit var tokenInterceptor: UpdateTokenInterceptor
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var retrofit: Retrofit

    fun init(context: Context) {
//        val baseUrl = "http://${Const.ip}:${Const.port}/prod-api/"
        val baseUrl = "http://${Const.ip}:${Const.port}/"
        urlInterceptor = BaseUrlInterceptor(baseUrl)
        tokenInterceptor = UpdateTokenInterceptor(ACCESS_TOKEN)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        okHttpClient = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(urlInterceptor)
            .addInterceptor(tokenInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl) // BaseUrl must be set, but interceptor will override host/port
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }

    inline fun <reified T> createService(): T {
        return createService(T::class.java)
    }

    fun updateTokenAndRefreshToken(token: String) {
        if (::tokenInterceptor.isInitialized) {
            tokenInterceptor.updateTokenAndRefreshToken(token)
        }
    }
}