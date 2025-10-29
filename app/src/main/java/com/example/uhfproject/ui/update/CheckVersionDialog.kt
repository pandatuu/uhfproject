package com.example.uhfproject.ui.update

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.uhfproject.databinding.DialogCheckversionBinding
import com.example.uhfproject.utils.BaseDialogFragment
import com.example.uhfproject.utils.Const.ip
import com.example.uhfproject.utils.Const.port
import com.example.uhfproject.utils.retrofit.LiveDataCallAdapterFactory
import com.example.uhfproject.utils.retrofit.MainService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CheckVersionDialog() : BaseDialogFragment<DialogCheckversionBinding>() {

    private var versionCode = 0
    private var versionName = ""
    lateinit var latestVersionDto: LatestVersionDto

    override fun onResume() {
        super.onResume()

        val widthDP =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 275f, resources.displayMetrics)
                .toInt()
        val heightDP =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 255f, resources.displayMetrics)
                .toInt()

        val params: ViewGroup.LayoutParams = dialog!!.window!!.attributes
        params.width = widthDP
        params.height = heightDP
        dialog!!.window!!.attributes = params as WindowManager.LayoutParams
    }

    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogCheckversionBinding =
        DialogCheckversionBinding.inflate(layoutInflater, container, false)

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.checkVersionBtn.isEnabled = false
        mBinding.checkVersionBtn.setOnClickListener {
            // showDownloadDialog(latestVersionDto) // Disabled download dialog feature
        }
        try {
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            val packageInfo: PackageInfo =
                requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
            // 获取versionCode
            versionCode = packageInfo.versionCode
            versionName = packageInfo.versionName
            mBinding.checkCurrentVersionTx.text = "Current Version: $versionName"
        } catch (e: PackageManager.NameNotFoundException) {
            // 应用的包名未找到，这通常不会发生
            e.printStackTrace()
        }
        initVersion()
    }

    override fun initListener() {

    }

    override fun initResume() {

    }

    private fun initVersion() {
        // 构建Retrofit实例
        val retrofit = Retrofit.Builder() //设置网络请求BaseUrl地址
            .baseUrl("http://${ip}:9002/") //设置数据解析器
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(MainService::class.java).getVersionNet()
            .observe(requireActivity()) {
                if (it.code == 200 && it.data != null) {
                    val latestVersion = it.data!!.version.toInt()
                    if (latestVersion > versionCode) {
                        latestVersionDto = it.data!!
                        mBinding.checkVersionBtn.text = "Check for Updates"
                        mBinding.checkVersionBtn.isEnabled = true
                    }else{
                        mBinding.checkVersionBtn.text = "No updates"
                    }
                }
            }
    }
}

fun AppCompatActivity.showCheckVersionDialog() {
    CheckVersionDialog().show(supportFragmentManager, "CheckVersionDialog")
}
