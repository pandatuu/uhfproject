package com.example.uhfproject.ui

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.uhfproject.R
import com.example.uhfproject.app.MyApplication.Companion.appContext
import com.example.uhfproject.databinding.ActivityLoginBinding
import com.example.uhfproject.model.LoginBody
import com.example.uhfproject.model.UserSiteDTO
import com.example.uhfproject.ui.update.showCheckVersionDialog
import com.example.uhfproject.ui.update.showDownloadDialog
import com.example.uhfproject.utils.Const.ip
import com.example.uhfproject.utils.Const.isReadMe
import com.example.uhfproject.utils.Const.readMeName
import com.example.uhfproject.utils.Const.readMePassword
import com.example.uhfproject.utils.LogUtil
import com.example.uhfproject.utils.retrofit.LiveDataCallAdapterFactory
import com.example.uhfproject.utils.retrofit.MainService
import com.example.uhfproject.utils.retrofit.RetrofitClient
import com.google.gson.Gson
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.ConnectException
import java.net.SocketTimeoutException

class LoginActivity: AppCompatActivity() {

    private lateinit var mBinding: ActivityLoginBinding

    private var eyeGone = false
    private var mode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.checkReadMe.isChecked = isReadMe
        if(isReadMe){
            mBinding.edtUsername.setText(readMeName)
            mBinding.edtPassword.setText(readMePassword)
        }

        mBinding.imgEye.setImageResource(R.drawable.ic_eye_gone)
        setMode()
        initVersion()
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
        // 获取versionCode
        mBinding.tvVersion.text = "Version ${packageInfo.versionName}"

        setListener()
    }

    private fun setListener(){
        mBinding.checkReadMe.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                isReadMe = true
                readMeName = mBinding.edtUsername.text.toString()
                readMePassword = mBinding.edtPassword.text.toString()
            }else{
                isReadMe = false
                readMeName = ""
                readMePassword = ""
            }
        }
        mBinding.btnLogin.setOnClickListener {
            mBinding.loginLoading.visibility = View.VISIBLE
            val username = mBinding.edtUsername.text.toString()
            val password = mBinding.edtPassword.text.toString()
            if(username.isEmpty()){
                Toasty.warning(this, "Please enter your username.", Toasty.LENGTH_SHORT).show()
                mBinding.loginLoading.visibility = View.GONE
                return@setOnClickListener
            }
            if(password.isEmpty()){
                Toasty.warning(this, "Please enter your password.", Toasty.LENGTH_SHORT).show()
                mBinding.loginLoading.visibility = View.GONE
                return@setOnClickListener
            }
            if(isReadMe){
                readMeName = username
                readMePassword = password
            }
            login(username,password, success = { permission, userSiteId, siteList ->
                mBinding.loginLoading.visibility = View.GONE
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("username", username)
                intent.putExtra("mode", mode)
                intent.putExtra("permission", permission)
                intent.putExtra("userSiteId", userSiteId)
                intent.putExtra("siteList", Gson().toJson(siteList))
                startActivity(intent)
            }, failed = {
                lifecycleScope.launch(Dispatchers.Main){
                    mBinding.loginLoading.visibility = View.GONE
                }
            })
        }
        mBinding.mainImg.setOnLongClickListener {
            SettingDialog(this).show()
            true
        }
        mBinding.tvVersion.setOnClickListener {
            showCheckVersionDialog()
        }
        mBinding.imgEye.setOnClickListener {
            mBinding.edtPassword.clearFocus()
            eyeGone = !eyeGone
            if(eyeGone){
                mBinding.imgEye.setImageResource(R.drawable.ic_eye_visibility)
                mBinding.edtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }else{
                mBinding.imgEye.setImageResource(R.drawable.ic_eye_gone)
                mBinding.edtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }
        mBinding.btnPon.setOnClickListener {
            mode = 0
            setMode()
        }
        mBinding.btnApple.setOnClickListener {
            mode = 1
            setMode()
        }
    }

    private fun login(userName: String, password: String, success: (Boolean, Int?, List<UserSiteDTO>) -> Unit, failed: () -> Unit){
        lifecycleScope.launch(Dispatchers.IO){
            try{
                //登录
                val retrofit = RetrofitClient.createService<MainService>()
                val result = retrofit.loginNet(LoginBody(userName, password))
                if (result.code == 200 && result.token != null) {
                    LogUtil.d("login:${result}")
                    RetrofitClient.updateTokenAndRefreshToken(result.token!!)
                    //获取用户权限
                    val permsByUserIdNet = retrofit.getPermsByUserIdNet()
                    if(permsByUserIdNet.code == 200 && permsByUserIdNet.data != null){
                        var getSiteByUserIdResult: UserSiteDTO? = null
                        if(permsByUserIdNet.data!!){
                            val getSiteByUserId = retrofit.getSiteByUserIdNet()
                            if(getSiteByUserId.code == 200 && getSiteByUserId.data != null){
                                getSiteByUserIdResult = getSiteByUserId.data!!
                            }else{
                                lifecycleScope.launch(Dispatchers.Main){
                                    Toasty.error(appContext, getSiteByUserId.msg.toString(), Toasty.LENGTH_SHORT).show()
                                }
                            }
                        }
                        val getSiteList = retrofit.getSiteListNet()
                        if(getSiteList.code == 200 && getSiteList.data != null){
                            withContext(Dispatchers.Main){
                                Toasty.success(this@LoginActivity, "Login Success", Toasty.LENGTH_SHORT).show()
                                success.invoke(permsByUserIdNet.data!!, getSiteByUserIdResult?.siteId?.toIntOrNull(), getSiteList.data!!)
                            }
                        }else{
                            lifecycleScope.launch(Dispatchers.Main){
                                Toasty.error(appContext, getSiteList.msg.toString(), Toasty.LENGTH_SHORT).show()
                            }
                        }
                    }else{
                        lifecycleScope.launch(Dispatchers.Main){
                            Toasty.error(appContext, permsByUserIdNet.msg.toString(), Toasty.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toasty.error(appContext, result.msg.toString(), Toasty.LENGTH_SHORT).show()
                    }
                }
                failed.invoke()
            } catch (e: Exception) {
                // 捕获异常，包括拦截器中抛出的异常和网络异常等
                e.printStackTrace()
                failed.invoke()
                withContext(Dispatchers.Main) {
                    // 根据异常类型显示错误信息
                    val errorMessage = when (e) {
                        is ConnectException -> "Connection failed. Check network settings."
                        is SocketTimeoutException -> "Time Out."
                        is HttpException -> "Server Error：${e.code()}."
                        else -> "Login Failed: ${e.message}."
                    }
                    Toasty.error(this@LoginActivity, errorMessage, Toasty.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun initVersion(){
        // 构建Retrofit实例
        val retrofit = Retrofit.Builder() //设置网络请求BaseUrl地址
            .baseUrl("http://${ip}:9002/") //设置数据解析器
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(MainService::class.java).getVersionNet().observe(this){
            if(it.code==200){
                try {
                    // getPackageName()是你当前类的包名，0代表是获取版本信息
                    val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
                    // 获取versionCode
                    it.data?.let { data ->
                        val versionCode: Int = packageInfo.versionCode
                        val latestVersion = data.version.toInt()
                        if(latestVersion > versionCode){
                            // showDownloadDialog(data) // Disabled download dialog feature
                        }
                    }
                } catch (e: PackageManager.NameNotFoundException) {
                    // 应用的包名未找到，这通常不会发生
                    e.printStackTrace()
                }
            }
        }
    }

    private fun setMode(){
        when(mode){
            0 -> {
                mBinding.btnPon.setBackgroundResource(R.drawable.bg_blue_5_1)
                mBinding.btnPon.setTextColor(Color.WHITE)
                mBinding.btnPon.textSize = 16f
                mBinding.btnApple.setBackgroundResource(R.drawable.bg_blue_border_5_1)
                mBinding.btnApple.setTextColor(Color.parseColor("#1684fc"))
                mBinding.btnApple.textSize = 12f
            }
            1 -> {
                mBinding.btnPon.setBackgroundResource(R.drawable.bg_blue_border_5_2)
                mBinding.btnPon.setTextColor(Color.parseColor("#1684fc"))
                mBinding.btnPon.textSize = 12f
                mBinding.btnApple.setBackgroundResource(R.drawable.bg_blue_5_2)
                mBinding.btnApple.setTextColor(Color.WHITE)
                mBinding.btnApple.textSize = 16f
            }
        }
    }
}
