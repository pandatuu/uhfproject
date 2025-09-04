package com.example.uhfproject.utils.retrofit

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

class BaseUrlInterceptor(private var baseUrl: String) : Interceptor {

    fun setBaseUrl(newBaseUrl: String) {
        baseUrl = newBaseUrl.ensureValidBaseUrl()
    }

    override fun intercept(chain: Interceptor.Chain): Response {

        val originalRequest = chain.request()

        // 将 BaseUrl 转换为 HttpUrl 对象
        val newBaseHttpUrl = baseUrl.toHttpUrlOrNull()
            ?: throw IllegalArgumentException("Invalid base URL: $baseUrl")

        // 替换请求中的 URL
        val originalUrl = originalRequest.url
        val newUrl = newBaseHttpUrl.newBuilder()
            .encodedPath(originalUrl.encodedPath) // 保留原始路径
            .query(originalUrl.query) // 保留原始查询参数
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }

    // 扩展函数：确保 Base URL 格式合法并以 `/` 结尾
    private fun String.ensureValidBaseUrl(): String {
        val url = this.trim()
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw IllegalArgumentException("Base URL must start with http:// or https://")
        }
        return if (url.endsWith("/")) url else "$url/"
    }
}
