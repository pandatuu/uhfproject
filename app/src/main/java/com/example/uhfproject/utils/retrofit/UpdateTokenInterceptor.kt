package com.example.uhfproject.utils.retrofit

import android.os.Handler
import android.util.Log
import androidx.navigation.NavController
import com.example.uhfproject.utils.Const.ACCESS_TOKEN
import com.example.uhfproject.utils.SPUtils
import okhttp3.*
import org.json.JSONObject
import java.nio.charset.Charset
import android.os.Looper
import android.content.Intent
import com.example.uhfproject.app.MyApplication.Companion.appContext

import com.example.uhfproject.ui.LoginActivity








class UpdateTokenInterceptor(
    @Volatile
    var token: String
) : Interceptor {

    companion object {
        const val ACCESS_TOKEN_NAME = "authorization"
        const val CODE_NAME = "code"
        const val TAG = "UpdateTokenInterceptor"
        const val TOKEN_EXPIRE_CODE = 401
    }

    fun updateTokenAndRefreshToken(token: String) {
        ACCESS_TOKEN = token
        SPUtils.putString(ACCESS_TOKEN_NAME, token)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        Log.d(TAG, "intercept token:$ACCESS_TOKEN")
        val builder = chain.request().newBuilder()
        builder.header(ACCESS_TOKEN_NAME, ACCESS_TOKEN)

        try {
            val response = chain.proceed(builder.build())

            val code = response.code
            if (code != 200) {
                Log.d(TAG, "intercept:  = $code")
                return response
            }


            if (isTokenExpired(response)) {
                //TODo 跳转到登陆界面
                Handler(Looper.getMainLooper()).post {
                    val intent = Intent(appContext, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) // 清除现有任务栈并创建新任务
                    appContext.startActivity(intent)
                }
            }
            return response
        } catch (e: Exception) {
            e.printStackTrace()

            // 发生异常时返回一个表示网络请求失败的响应
            return Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(500)
                .message("Internal Server Error")
                .body(ResponseBody.create(null, ""))
                .build()
        }


    }

    private fun isTokenExpired(response: Response): Boolean {
        val string = copyBuffer(response.body)
        Log.d(TAG, "isTokenExpired: string = $string")
        return getCode(string) == TOKEN_EXPIRE_CODE
    }

    private fun getCode(string: String?): Int? {
        return string?.let {
            JSONObject(it).getInt(CODE_NAME)
        }
    }

    private fun copyBuffer(body: ResponseBody?): String? {
        val source = body?.source()
        source?.request(Long.MAX_VALUE)
        val buffer = source?.buffer()
        return buffer?.clone()?.readString(Charset.defaultCharset())
    }

    @Synchronized
    private fun refreshToken(outDateToken: String): String {
        if (token.isEmpty() || outDateToken == token) {
            Log.d(TAG, "refreshToken: start token = $token")
            val client = OkHttpClient()
            val builder = Request.Builder()
            val call = client.newCall(
                builder.get()
//                    .url("$refreshTokenUrl?refreshToken=$refreshToken")
                    .build()
            )
            updateTokenByResponse(call.execute())
            Log.d(TAG, "refreshToken: end token = $token")
        }
        return token
    }

    private fun updateTokenByResponse(response: Response) {
        response.code.also {
            if (it == 200) {
                val string = response.body?.string()
                if (string != null) {
                    JSONObject(string).let { json ->
                        val code = json.getInt(CODE_NAME)
                        if (code == 200) {
                            val data = json.getJSONObject("data")
                            updateTokenAndRefreshToken(
                                data.getString(ACCESS_TOKEN_NAME),
                            )
                            return
                        }
                    }
                } else {
                    Log.d(TAG, "updateTokenByResponse: string = $string")
                }
            }
            //todo new login
        }
    }
}