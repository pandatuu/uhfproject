package com.example.uhfproject.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.example.uhfproject.R
import com.example.uhfproject.ui.MainViewModel
import java.lang.reflect.ParameterizedType

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: VB? = null
    val mBinding get() = _binding!!

    lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val bindingClass = getBindingClass()
        val method = bindingClass.getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
        _binding = method.invoke(null, inflater, container, false) as VB
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        initView()
        initData()
        observeData()
        observeScanStatus()
    }

    abstract fun initView()

    abstract fun initData()

    abstract fun observeData()

    private fun observeScanStatus() {
        mainViewModel.isScanning.observe(viewLifecycleOwner) { isScanning ->
            view?.post {
                val scanStatusView = view?.findViewById<TextView>(R.id.scan_status_bar)
                
                scanStatusView?.let {
                    if (isScanning) {
                        it.text = "Scanning"
                        it.setBackgroundResource(R.drawable.bg_scan_status_active)
                    } else {
                        it.text = "Not Scanning"
                        it.setBackgroundResource(R.drawable.bg_scan_status_inactive)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @Suppress("UNCHECKED_CAST")
    private fun getBindingClass(): Class<VB> {
        val type = javaClass.genericSuperclass
        return if (type is ParameterizedType) {
            type.actualTypeArguments[0] as Class<VB>
        } else {
            throw IllegalArgumentException("必须指定Binding类型")
        }
    }
}