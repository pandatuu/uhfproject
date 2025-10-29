package com.example.uhfproject.utils.retrofit

import com.example.uhfproject.model.HttpResult
import com.example.uhfproject.model.HttpResultPager
import com.example.uhfproject.utils.LogUtil
import kotlinx.coroutines.CancellationException


sealed class APIResult<out T> {

    companion object {
        const val TAG = "APIResult"
    }

    data class Success<T>(var data: T) : APIResult<T>()
    data class SuccessOrNull<T>(var data: T?) : APIResult<T>()


    data class ServerError<T>(
        var code: Int,
        var msg: String,
    ) : APIResult<T>()

    data class OtherError<T>(
        var t: Throwable,
    ) : APIResult<T>()

    inline fun onSuccess(success: T.() -> Unit): APIResult<T> {
        if (this is Success<T>) {
            success.invoke(data)
        }
        return this
    }

    inline fun onSuccessOrNull(success: ((T)?) -> Unit): APIResult<T> {
        if (this is SuccessOrNull<T>) {
            success.invoke(data)
        }
        return this
    }

    inline fun onServerError(serverError: (code: Int, msg: String) -> Unit): APIResult<T> {
        if (this is ServerError<T>) {
            serverError.invoke(code, msg)
        }
        return this
    }

    inline fun onOtherError(onOtherError: (t: Throwable) -> Unit): APIResult<T> {
        if (this is OtherError<T>) {
            onOtherError.invoke(t)
        }
        return this
    }

    fun getOrNull(): T? {
        return if (this is Success<T>) data else null
    }
}

suspend inline fun <T> safeNetworkInvoke(
    crossinline block: suspend () -> HttpResult<T>,
): APIResult<T> {
    try {
        val result = block.invoke()
        if (result.code == 200) {
            if (result.data == null) {
                return APIResult.ServerError(result.code, "No data")
            }
            return APIResult.Success(result.data!!)
        }
        return APIResult.ServerError(result.code, result.msg ?: "")
    } catch (t: Throwable) {
        if (t is CancellationException) throw t
        t.printStackTrace()
        return APIResult.OtherError(t)
    }
}

suspend inline fun <T> safeNetWorkPagerInvoke(crossinline block: suspend () -> HttpResultPager<T>): APIResult<T> {
    try {
        val result = block.invoke()
        if (result.code == 200) {
            return APIResult.SuccessOrNull<T>(result.rows)
        }
        LogUtil.d("code:${result.code},msg:${result.msg ?: ""}")
        return APIResult.ServerError(result.code, result.msg ?: "")
    } catch (t: Throwable) {
        if (t is CancellationException) throw t
        t.printStackTrace()
        LogUtil.d("msg:${t.message ?: ""}")
        return APIResult.OtherError(t)
    }
}