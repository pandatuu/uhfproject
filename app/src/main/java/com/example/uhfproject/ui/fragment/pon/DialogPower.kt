package com.example.uhfproject.ui.fragment.pon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.Fragment
import com.example.uhfproject.databinding.DialogPowerBinding
import com.example.uhfproject.utils.BaseDialogFragment1


class DialogPower(private var power: Int, private val success:(Int) -> Unit): BaseDialogFragment1<DialogPowerBinding>() {

    override fun initBinding(inflater: LayoutInflater, container: ViewGroup?): DialogPowerBinding =
        DialogPowerBinding.inflate(inflater,container, false)

    override fun initView() {
        mBinding.seekBar.progress = power
        mBinding.tvPower.text = power.toString()
    }

    override fun initData() {

    }

    override fun initListener() {
        mBinding.root.setOnClickListener {
            success.invoke(power)
            dismiss()
        }
        mBinding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // 进度改变时调用
                // 在这里控制其他组件
                power = progress
                mBinding.tvPower.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // 开始滑动时调用
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // 结束滑动时调用
            }
        })
    }
}

fun Fragment.showDialogPower(power: Int, success:(Int) -> Unit){
    DialogPower(power, success).show(childFragmentManager, "DialogPower")
}