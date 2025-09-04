package com.example.uhfproject.ui

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.uhfproject.databinding.ActivityLoginBinding
import com.example.uhfproject.model.LoginBody
import com.example.uhfproject.ui.update.showCheckVersionDialog
import com.example.uhfproject.ui.update.showDownloadDialog
import com.example.uhfproject.utils.Const.isReadMe
import com.example.uhfproject.utils.Const.readMeName
import com.example.uhfproject.utils.Const.readMePassword
import com.example.uhfproject.utils.LogUtil
import com.example.uhfproject.utils.retrofit.LiveDataCallAdapterFactory
import com.example.uhfproject.utils.retrofit.MainService
import com.example.uhfproject.utils.retrofit.RetrofitClient
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity: AppCompatActivity() {

    private lateinit var mBinding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.checkReadMe.isChecked = isReadMe
        if(isReadMe){
            mBinding.edtUsername.setText(readMeName)
            mBinding.edtPassword.setText(readMePassword)
        }

        initVersion()
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
        // 获取versionCode
        mBinding.tvVersion.text = "Current Version: ${packageInfo.versionName}"

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
            val username = mBinding.edtUsername.text.toString()
            val password = mBinding.edtPassword.text.toString()
            if(username.isEmpty()){
                Toasty.warning(this, "Please enter your username.", Toasty.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(password.isEmpty()){
                Toasty.warning(this, "Please enter your password.", Toasty.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(isReadMe){
                readMeName = username
                readMePassword = password
            }
            login(username,password){
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
            }
        }
        mBinding.mainImg.setOnLongClickListener {
            SettingDialog(this).show()
            true
        }
        mBinding.tvVersion.setOnClickListener {
            showCheckVersionDialog()
        }
    }

    private fun login(userName: String, password: String, success: () -> Unit){
        lifecycleScope.launch(Dispatchers.IO){
            val retrofit = RetrofitClient.createService<MainService>()
            val result = retrofit.loginNet(LoginBody(userName, password))
            if (result.code == 200 && result.token != null) {
                LogUtil.d("login:${result}")
                RetrofitClient.updateTokenAndRefreshToken(result.token!!)
                withContext(Dispatchers.Main){
                    Toasty.success(this@LoginActivity, "Login Success", Toasty.LENGTH_SHORT).show()
                    success.invoke()
                }
            }
        }
    }
    private fun initVersion(){
        // 构建Retrofit实例
        val retrofit = Retrofit.Builder() //设置网络请求BaseUrl地址
            .baseUrl("http://49.233.245.14:8010/") //设置数据解析器
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
                            showDownloadDialog(data)
                        }
                    }
                } catch (e: PackageManager.NameNotFoundException) {
                    // 应用的包名未找到，这通常不会发生
                    e.printStackTrace()
                }
            }
        }
    }
}