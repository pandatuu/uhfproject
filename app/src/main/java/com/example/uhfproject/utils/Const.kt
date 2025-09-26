package com.example.uhfproject.utils

import android.content.Context
import android.text.InputType
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.uhfproject.R
import java.math.BigDecimal
import java.math.RoundingMode

object Const {

    var ACCESS_TOKEN: String = ""
    const val SCAN_ACTION: String = "com.android.server.scannerservice.broadcast"

    private const val IP_VALUE = "singpost.jwctsg.com"
    var ip: String
        set(value) = SPUtils.putString("IP_KEY", value)
        get() = SPUtils.getString("IP_KEY", IP_VALUE)

    private const val PORT_VALUE = "9010"
    var port: String
        set(value) = SPUtils.putString("PORT_KEY", value)
        get() = SPUtils.getString("PORT_KEY", PORT_VALUE)

    private const val MODE_VALUE = true
    var mode: Boolean
        set(value) = SPUtils.putBoolean("MODE_KEY", value)
        get() = SPUtils.getBoolean("MODE_KEY", MODE_VALUE)

    private const val READ_ME_VALUE = false
    var isReadMe: Boolean
        set(value) = SPUtils.putBoolean("READ_ME", value)
        get() = SPUtils.getBoolean("READ_ME", READ_ME_VALUE)

    private const val READ_ME_NAME = ""
    var readMeName: String
        set(value) = SPUtils.putString("READ_ME_NAME", value)
        get() = SPUtils.getString("READ_ME_NAME", READ_ME_NAME)

    private const val READ_ME_PASSWORD = ""
    var readMePassword: String
        set(value) = SPUtils.putString("READ_ME_PASSWORD", value)
        get() = SPUtils.getString("READ_ME_PASSWORD", READ_ME_PASSWORD)

    private const val INBOUND_POWER = 30
    var inBoundPower: Int
        get() = SPUtils.getInt("INBOUND_POWER", INBOUND_POWER)
        set(value) = SPUtils.putInt("INBOUND_POWER", value)

    private const val OUTBOUND_POWER = 30
    var outBoundPower: Int
        get() = SPUtils.getInt("OUTBOUND_POWER", OUTBOUND_POWER)
        set(value) = SPUtils.putInt("OUTBOUND_POWER", value)

    private const val INVENTORY_POWER = 30
    var inventoryPower: Int
        get() = SPUtils.getInt("INVENTORY_POWER", INVENTORY_POWER)
        set(value) = SPUtils.putInt("INVENTORY_POWER", value)

    private const val FIND_ITEM_POWER = 30
    var findItemPower: Int
        get() = SPUtils.getInt("FIND_ITEM_POWER", FIND_ITEM_POWER)
        set(value) = SPUtils.putInt("FIND_ITEM_POWER", value)

    private const val OB_VERIFY_POWER = 30
    var obVerifyPower: Int
        get() = SPUtils.getInt("OB_VERIFY_POWER", OB_VERIFY_POWER)
        set(value) = SPUtils.putInt("OB_VERIFY_POWER", value)

    private const val ITEM_QUERY_POWER = 5
    var itemQueryPower: Int
        get() = SPUtils.getInt("ITEM_QUERY_POWER", ITEM_QUERY_POWER)
        set(value) = SPUtils.putInt("ITEM_QUERY_POWER", value)

    private const val RFID_BIND_POWER = 5
    var rfidBindingPower: Int
        get() = SPUtils.getInt("RFID_BIND_POWER", RFID_BIND_POWER)
        set(value) = SPUtils.putInt("RFID_BIND_POWER", value)

    fun simpleAlert(context: Context, title: String, confirm: ()->Unit ){
        AlertDialog.Builder(context)
            .setTitle(title)
            .setPositiveButton(context.getString(R.string.alert_confirm)){ dialog, _ ->
                confirm.invoke()
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.alert_cancel)){ dialog,_ ->
                dialog.dismiss()
            }
            .show()
    }
    fun simpleAlert(context: Context, title: String, content: String, confirm: ()->Unit ){
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(content)
            .setPositiveButton(context.getString(R.string.alert_confirm)){ dialog,_ ->
                confirm.invoke()
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.alert_cancel)){ dialog,_ ->
                dialog.dismiss()
            }
            .show()
    }
    fun simpleEditAlert(context: Context, title: String, text: String = "", confirm: (Int)->Unit ){
        val edit = EditText(context)
        edit.setText(text)
        edit.inputType = InputType.TYPE_CLASS_NUMBER
        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(edit)
            .setPositiveButton(context.getString(R.string.alert_confirm)){ dialog,_ ->
                val num = edit.text.toString()
                if(num.toIntOrNull() == null){
                    return@setPositiveButton
                }
                confirm.invoke(num.toInt())
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.alert_cancel)){ dialog,_ ->
                dialog.dismiss()
            }
            .show()
    }

    fun Fragment.hideKeyboard() {
        activity?.let {
            val imm = it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            // 使用 Fragment 的根视图的 windowToken
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
        }
    }

    fun intToPercent(rssi: Int): BigDecimal {
        return BigDecimal(rssi)
            .divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
        // 除以 100，保留 2 位小数，四舍五入
    }
}