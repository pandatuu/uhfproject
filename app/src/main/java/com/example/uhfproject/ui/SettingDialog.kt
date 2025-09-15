package com.example.uhfproject.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.uhfproject.R
import com.example.uhfproject.utils.Const
import com.example.uhfproject.utils.Const.ip
import com.example.uhfproject.utils.Const.mode
import com.example.uhfproject.utils.Const.port
import com.example.uhfproject.utils.retrofit.RetrofitClient
import es.dmoral.toasty.Toasty

class SettingDialog(private val mContext: Context) : AlertDialog(mContext) {

    private lateinit var tittle: TextView
    private lateinit var edtIp: EditText
    private lateinit var edtPort: EditText

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.dialog_setting)
        tittle = findViewById(R.id.setting_tittle)!!
        edtIp = findViewById(R.id.edt_ip)!!
        edtPort = findViewById(R.id.edt_port)!!
        edtIp.setText(ip)
        edtPort.setText(port)

        setOnShowListener {
            //清楚flags,获取焦点
            this@SettingDialog.window!!.clearFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
            )
            //弹出输入法
            this@SettingDialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
        val submit = findViewById<TextView>(R.id.btn_setting_submit)
        submit!!.setOnClickListener {
            val ip: String = edtIp.text.toString()
            val port: String = edtPort.text.toString()
            if (ip.isEmpty()) {
                Toasty.warning(mContext, "输入不能为空").show()
                dismiss()
                return@setOnClickListener
            }
            Const.ip = ip
            Const.port = port
//            Toasty.success(mContext, "修改成功，请退出软件重新登陆！").show()
            RetrofitClient.urlInterceptor.setBaseUrl(getUrl())
            dismiss()
        }

        val debug = findViewById<TextView>(R.id.btn_setting_debug)
        debug!!.setOnClickListener {
            edtIp.setText("101.43.218.72")
            edtPort.setText("9010")
            mode = false
        }
        val release = findViewById<TextView>(R.id.btn_setting_release)
        release!!.setOnClickListener {
            edtIp.setText("singpost.jwctsg.com")
            edtPort.setText("9010")
            mode = true
        }
    }

    private fun getUrl() = "http://$ip:$port/prod-api/"
}