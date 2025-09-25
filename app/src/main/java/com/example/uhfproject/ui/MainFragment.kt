package com.example.uhfproject.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.uhfproject.R
import com.example.uhfproject.databinding.FragmentMainBinding
import com.example.uhfproject.utils.Const
import com.example.uhfproject.utils.retrofit.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class MainFragment: Fragment() {

    private lateinit var mBinding: FragmentMainBinding

    private val mainViewModel: MainViewModel by activityViewModels()

    private val rotateAnimation = RotateAnimation(
        0f, 360f,                       // 从 0 度到 360 度
        Animation.RELATIVE_TO_SELF, 0.5f, // 旋转中心 X 轴为自身中心
        Animation.RELATIVE_TO_SELF, 0.5f  // 旋转中心 Y 轴为自身中心
    ).apply {
        duration = 800L                 // 动画持续时间为 1000 毫秒（1 秒）
        interpolator = LinearInterpolator() // 设置 interpolator 为匀速线性插值器，确保旋转速度均匀 :cite[2]:cite[5]
        fillAfter = true                 // 动画结束后保持最后状态（可选）
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentMainBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.loginUser.text = "Welcome, ${mainViewModel.username}"
        mainViewModel.getHeadInfo{
            lifecycleScope.launch(Dispatchers.Main){
                mBinding.tvPendingIb.text = formatNumberWithCommas(it.pendingInboundNumber?:0)
                mBinding.tvPendingOb.text = formatNumberWithCommas(it.outboundRemainingNumber?:0)
            }
        }

        mBinding.inboundLayout.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_inBoundFragment)
        }
        mBinding.outboundLayout.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_outBoundFragment)
        }
        mBinding.inventoryLayout.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_inventoryFragment)
        }
        mBinding.findItemLayout.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_findItemFragment)
        }
        mBinding.dashboardLayout.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_dashboardFragment)
        }
        mBinding.itemQueryLayout.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_itemQueryFragment)
        }
        mBinding.obVerifyLayout.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_OBVerifyFragment)
        }
        mBinding.logoutBtn.setOnClickListener {
            Const.simpleAlert(requireContext(), "Log out?"){
                RetrofitClient.updateTokenAndRefreshToken("")
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }
        }
        mBinding.refreshBtn.setOnClickListener {
            mBinding.imgRefresh.startAnimation(rotateAnimation)
            mainViewModel.getHeadInfo{
                lifecycleScope.launch(Dispatchers.Main){
                    mBinding.tvPendingIb.text = formatNumberWithCommas(it.pendingInboundNumber?:0)
                    mBinding.tvPendingOb.text = formatNumberWithCommas(it.outboundRemainingNumber?:0)
                }
            }
        }

    }

    private fun formatNumberWithCommas(number: Int): String {
        val numberFormat = NumberFormat.getNumberInstance(Locale.US) // 使用美国 locale（逗号分隔）
        return numberFormat.format(number)
    }
}