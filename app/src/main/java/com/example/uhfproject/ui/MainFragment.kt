package com.example.uhfproject.ui

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.navigation.fragment.findNavController
import com.example.uhfproject.R
import com.example.uhfproject.databinding.FragmentMainBinding
import com.example.uhfproject.ui.update.showCheckVersionDialog
import com.example.uhfproject.ui.update.showDownloadDialog
import com.example.uhfproject.utils.BaseFragment
import com.example.uhfproject.utils.LogUtil
import com.example.uhfproject.utils.retrofit.LiveDataCallAdapterFactory
import com.example.uhfproject.utils.retrofit.MainService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainFragment: BaseFragment<FragmentMainBinding>() {

    override fun initView() {
        initVersion()
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        val packageInfo: PackageInfo =
            requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
        // 获取versionCode
        mBinding.tvVersion.text = "Current Version: ${packageInfo.versionName}"
    }

    override fun initData() {
        mBinding.btnInbound.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_inBoundFragment)
        }
        mBinding.btnOutbound.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_outBoundFragment)
        }
        mBinding.btnInventory.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_inventoryFragment)
        }
        mBinding.btnFindItem.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_findItemFragment)
        }
        mBinding.tvVersion.setOnClickListener {
            showCheckVersionDialog()
        }
    }

    override fun observeData() {

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
                    val packageInfo: PackageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
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