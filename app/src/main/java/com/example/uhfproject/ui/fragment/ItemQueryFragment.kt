package com.example.uhfproject.ui.fragment

import android.graphics.Color
import android.view.KeyEvent
import com.example.uhfproject.databinding.FragmentItemQueryBinding
import com.example.uhfproject.ui.MainActivity
import com.example.uhfproject.utils.BaseFragment

class ItemQueryFragment: BaseFragment<FragmentItemQueryBinding>() {

    private var keyStatus = false
    private var mActivity: MainActivity? = null

    override fun initView() {
        mActivity = requireActivity() as MainActivity
    }

    override fun initData() {
        mainViewModel.startQuest = false
    }

    override fun observeData() {
        mainViewModel.findList.observe(viewLifecycleOwner){

        }
    }
    override fun onResume() {
        super.onResume()
        mActivity?.onKeyDownCallback = { keyCode, event ->
            if (keyCode == 142 && event?.action == KeyEvent.ACTION_DOWN) {
                if(!keyStatus){
                    keyStatus = true
                    mainViewModel.startStock()
                }
                true
            }else{
                false
            }
        }
        mActivity?.onKeyUpCallback = { keyCode, event ->
            if (keyCode == 142 && event?.action == KeyEvent.ACTION_DOWN) {
                keyStatus = false
                mainViewModel.stopStock()
                true
            }else{
                false
            }
        }
    }
}