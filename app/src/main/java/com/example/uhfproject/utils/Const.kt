package com.example.uhfproject.utils

import android.content.Context
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.example.uhfproject.R

object Const {

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
}