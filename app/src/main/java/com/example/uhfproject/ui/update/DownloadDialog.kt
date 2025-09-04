package com.example.uhfproject.ui.update

import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.uhfproject.databinding.DialogDownloadBinding
import com.example.uhfproject.utils.BaseDialogFragment
import com.example.uhfproject.utils.LogUtil
import java.io.File

class DownloadDialog(private val latestVersion: LatestVersionDto) :
    BaseDialogFragment<DialogDownloadBinding>() {

    private var versionCode = 0
    private var versionName = ""

    override fun onResume() {
        super.onResume()
        //修改dialog的固定宽高
        val widthDP =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 265f, resources.displayMetrics)
                .toInt()
        val heightDP =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 465f, resources.displayMetrics)
                .toInt()

        val params: ViewGroup.LayoutParams = dialog!!.window!!.attributes
        params.width = widthDP
        params.height = heightDP
        dialog!!.window!!.attributes = params as WindowManager.LayoutParams
    }

    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogDownloadBinding = DialogDownloadBinding.inflate(layoutInflater, container, false)

    override fun initView(savedInstanceState: Bundle?) {
        try {
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            val packageInfo: PackageInfo =
                requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
            // 获取versionCode
            versionCode = packageInfo.versionCode
            versionName = packageInfo.versionName
            LogUtil.d("remark:"+latestVersion.remark?:"")
            mBinding.updateCurrentVersionTx.text = "Current Version: $versionName"
            mBinding.updateLatestVersionTx.text = "Latest Version: ${latestVersion.versionName}"
            mBinding.updateVersionDescTx.text = "Release Notes:\n ${latestVersion.remark?.replace("\\n", "\n")}"
        } catch (e: PackageManager.NameNotFoundException) {
            // 应用的包名未找到，这通常不会发生
            e.printStackTrace()
        }
        mBinding.updateVersionBtn.isEnabled = latestVersion.version.toInt() > versionCode
        mBinding.updateVersionBtn.setOnClickListener {
            mBinding.updateVersionBtn.isEnabled = false
            startDownLoad()
        }
    }

    override fun initListener() {
    }

    override fun initResume() {
    }

    private fun startDownLoad() {
        val downloadPath =
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.path
        LogUtil.d("downloadPath$downloadPath")
        val newFilePath = "$downloadPath/${latestVersion.apkName}"
        LogUtil.d("newFilePath$newFilePath")
        if (File(newFilePath).exists()) {
            installApk(newFilePath)
            return
        }
        Toast.makeText(requireContext(), "Downloading latest version, please be patient...", Toast.LENGTH_LONG).show()
        val downloadManager =
            requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        //注册广播，监听下载状态
        val intentFilter = IntentFilter()
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED)
        requireActivity().registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                    Log.d("download", "下载完毕")
                    installApk(newFilePath)
                } else if (intent.action.equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
                    Log.d("download", "点击了通知栏的下载框")
                }
            }
        }, intentFilter)

        //http://49.233.245.14/temp/haiyou_4.0.5.apk
        val downloadRequest =
            DownloadManager.Request(Uri.parse("http://49.233.245.14/temp/${latestVersion.apkName}"))
                //允许移动网络下载
                .setAllowedOverMetered(true)
                //当处于下载中状态和下载完成时状态，均在通知栏中显示
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                //设置保存路径  storage/emulated/0/Android/data/com.lzc.downloaddemo/files/Download/haiyou_4.0.5.apk
                .setDestinationInExternalFilesDir(
                    requireActivity(),
                    Environment.DIRECTORY_DOWNLOADS,
                    latestVersion.apkName
                )
                //设置标题和描述
                .setTitle(latestVersion.apkName)
                .setMimeType("application/vnd.android.package-archive")
        downloadRequest.allowScanningByMediaScanner()
        downloadManager.enqueue(downloadRequest)
    }

    private fun installApk(newFilePath: String) {
        // 检查 Fragment 是否已附加到 Activity
        if (!isAdded || context == null || activity == null) {
            return
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                val apkFile = File(newFilePath)
                val uri = FileProvider.getUriForFile(
                    requireContext(), // 此时已通过 isAdded 检查，可安全使用 requireContext()
                    "${requireActivity().packageName}.fileProvider",
                    apkFile
                )
                setDataAndType(uri, "application/vnd.android.package-archive")
            } catch (e: Exception) {
                Toast.makeText(context, "Installation failed：${e.message}", Toast.LENGTH_SHORT).show()
                return@apply
            }
        }

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Installer not found", Toast.LENGTH_SHORT).show()
        }
    }
}

fun Fragment.showDownloadDialog(
    latestVersion: LatestVersionDto
) {
    DownloadDialog(latestVersion).show(
        childFragmentManager,
        "DownloadDialog"
    )
}

fun AppCompatActivity.showDownloadDialog(
    latestVersion: LatestVersionDto
) {
    DownloadDialog(latestVersion).show(
        supportFragmentManager,
        "DownloadDialog"
    )
}

data class LatestVersionDto(
    val apkName: String,
    val version: String,
    val versionName: String,
    val id: Int,
    val serverName: String,
    val serverCode: String,
    val status: Int,
    val createTime: String,
    val createUserId: String,
    val updateTime: String?,
    val updateUserId: Int?,
    val remark: String?,
)