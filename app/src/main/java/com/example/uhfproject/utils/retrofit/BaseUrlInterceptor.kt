package com.example.uhfproject.utils.retrofit

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

class BaseUrlInterceptor(private var baseUrl: String) : Interceptor {

    fun setBaseUrl(newBaseUrl: String) {
        baseUrl = newBaseUrl.ensureValidBaseUrl()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val newBaseHttpUrl = baseUrl.toHttpUrlOrNull()
            ?: throw IllegalArgumentException("Invalid base URL: $baseUrl")

        val newUrl = originalRequest.url.newBuilder()
            .scheme(newBaseHttpUrl.scheme)
            .host(newBaseHttpUrl.host)
            .port(newBaseHttpUrl.port)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }

    private fun String.ensureValidBaseUrl(): String {
        val url = this.trim()
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw IllegalArgumentException("Base URL must start with http:// or https://")
        }
        return if (url.endsWith("/")) url else "$url/"
    }
}