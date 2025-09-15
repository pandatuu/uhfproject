package com.example.uhfproject.ui.update

import android.annotation.SuppressLint
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
import com.example.uhfproject.utils.Const.ip
import com.example.uhfproject.utils.LogUtil
import java.io.File

class DownloadDialog(private val latestVersion: LatestVersionDto) :
    BaseDialogFragment<DialogDownloadBinding>() {

    private var versionCode = 0
    private var versionName = ""

    override fun onResume() {
        super.onResume()
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

    @SuppressLint("SetTextI18n")
    override fun initView(savedInstanceState: Bundle?) {
        try {
            val packageInfo: PackageInfo =
                requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
            versionCode = packageInfo.versionCode
            versionName = packageInfo.versionName
            mBinding.updateCurrentVersionTx.text = "Current Version: $versionName"
            mBinding.updateLatestVersionTx.text = "Latest Version: ${latestVersion.versionName}"
            mBinding.updateVersionDescTx.text = "Release Notes:\n ${latestVersion.remark?.replace("\\n", "\n") ?: ""}"
        } catch (e: PackageManager.NameNotFoundException) {
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
        val intentFilter = IntentFilter()
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED)
        requireActivity().registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                    installApk(newFilePath)
                } else if (intent.action.equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {

                }
            }
        }, intentFilter)

        val downloadRequest =
            DownloadManager.Request(Uri.parse("https://www.jwctsg.com/files/${latestVersion.apkName}"))
                .setAllowedOverMetered(true)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(
                    requireActivity(),
                    Environment.DIRECTORY_DOWNLOADS,
                    latestVersion.apkName
                )
                .setTitle(latestVersion.apkName)
                .setMimeType("application/vnd.android.package-archive")
        downloadRequest.allowScanningByMediaScanner()
        downloadManager.enqueue(downloadRequest)
    }

    private fun installApk(newFilePath: String) {
        if (!isAdded || context == null || activity == null) {
            return
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                val apkFile = File(newFilePath)
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireActivity().packageName}.fileProvider",
                    apkFile
                )
                setDataAndType(uri, "application/vnd.android.package-archive")
            } catch (e: Exception) {
                Toast.makeText(context, "Installation failed ${e.message}", Toast.LENGTH_SHORT).show()
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