package com.example.uhfproject.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    protected lateinit var mBinding: VB
    protected val mainViewModel: MainViewModel by activityViewModels()

    abstract fun initView()

    abstract fun initData()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 使用反射创建Binding实例
        val bindingClass = getBindingClass()
        val method = bindingClass.getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
        mBinding = method.invoke(null, inflater, container, false) as VB
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
        observeData()
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

    abstract fun observeData()
}