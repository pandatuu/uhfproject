package com.example.uhfproject.utils

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.example.uhfproject.ui.MainViewModel


abstract class BaseDialogFragment<V : ViewBinding> : DialogFragment() {

    protected lateinit var mBinding: V

    protected var mainViewModel: MainViewModel? = null

    //布局id
    protected abstract fun bindLayout(inflater: LayoutInflater, container: ViewGroup?): V

    //初始化
    protected abstract fun initView(savedInstanceState: Bundle?)

    protected abstract fun initListener()

    protected abstract fun initResume()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (mainViewModel == null) {
            mainViewModel = ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
            )[MainViewModel::class.java]
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mainViewModel = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = bindLayout(inflater, container)
        dialog?.let { it ->
            it.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        hideBottomNav()

        initView(savedInstanceState)
        initListener()
    }

    override fun onResume() {
        super.onResume()
        //设置dialog的大小
        initResume()
    }

    /**
     * 隐藏虚拟按键
     * (dialog打开时，下面隐藏了的虚拟按键也跟着弹出来了)
     */
    private fun hideBottomNav() {
        dialog?.let { dialog ->
            val window = dialog.window
            if (window?.decorView == null) {
                return
            }
            //不加FLAG_NOT_FOCUSABLE，dialog显示时就会显示虚拟按键
            window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            dialog.setOnShowListener {
                //dialog显示之后，要清除FLAG_NOT_FOCUSABLE，否则不会弹出软键盘
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            }
            //隐藏虚拟按键
            hideBottomNavInner()
            window.decorView.setOnSystemUiVisibilityChangeListener {
                //从后台重新进入时，要再次隐藏虚拟按键
                hideBottomNavInner()
            }
        }
    }

    private fun hideBottomNavInner() {
        dialog?.let { dialog ->
            val decorView = dialog.window?.decorView
            val vis = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            decorView?.setSystemUiVisibility(vis)
        }
    }

    lateinit var audioManager: AudioManager
    fun View.click(onClick: (View) -> Unit) {
        setOnClickListener { v ->
            audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK)
            onClick(v)
        }
    }
}