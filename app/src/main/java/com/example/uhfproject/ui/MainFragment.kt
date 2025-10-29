package com.example.uhfproject.ui

import android.content.Intent
import android.graphics.Color
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

        if(mainViewModel.mode == 0){
            mBinding.features4Layout.visibility = View.GONE
            mBinding.features5Layout.visibility = View.GONE
            mBinding.features6Layout.visibility = View.GONE
            mBinding.features7Layout.visibility = View.GONE
            mBinding.features8Layout.visibility = View.GONE
            mBinding.features9Layout.visibility = View.GONE

            mBinding.imgCard1.setImageResource(R.drawable.ic_binding)
            mBinding.tvCard1.text = "Binding"
            mBinding.imgCard2.setImageResource(R.drawable.ic_stockcount)
            mBinding.tvCard2.text = "Stockcount"
            mBinding.imgCard3.setImageResource(R.drawable.ic_inbound)
            mBinding.tvCard3.text = "Inbound"

            mBinding.imgFeatures1.setImageResource(R.drawable.ic_find_rfid)
            mBinding.tvFeatures1.text = "Find RFID"
            mBinding.tvFeatures1.setTextColor(Color.parseColor("#000a7b"))
            mBinding.imgFeatures2.setImageResource(R.drawable.ic_rfid_query)
            mBinding.tvFeatures2.text = "RFID Query"
            mBinding.tvFeatures2.setTextColor(Color.parseColor("#000a7b"))
            mBinding.imgFeatures3.setImageResource(R.drawable.ic_bound_list)
            mBinding.tvFeatures3.text = "Bound List"
            mBinding.tvFeatures3.setTextColor(Color.parseColor("#000a7b"))

            mBinding.card1Layout.setOnClickListener {

            }
            mBinding.card2Layout.setOnClickListener {

            }
            mBinding.card3Layout.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_fragmentPonInbound)
            }

            mBinding.features1Layout.setOnClickListener {

            }
            mBinding.features2Layout.setOnClickListener {

            }
            mBinding.features3Layout.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_fragmentPonBoundList)
            }
        }else{
            mBinding.card1Layout.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_inBoundFragment)
            }
            mBinding.card3Layout.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_outBoundFragment)
            }
            mBinding.card2Layout.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_inventoryFragment)
            }
            mBinding.features8Layout.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_findItemFragment)
            }
            mBinding.dashboardCardClickable.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_dashboardFragment)
            }
            mBinding.features7Layout.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_itemQueryFragment)
            }
            mBinding.features4Layout.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_OBVerifyFragment)
            }
            mBinding.features1Layout.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_ibVerifyFragment)
            }
            mBinding.features2Layout.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_rfidBindingFragment)
            }
            mBinding.features9Layout.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_debugScanFragment)
            }
        }

        mBinding.logoutBtn.setOnClickListener { 
            Const.simpleAlert(requireContext(), "Log out?"){
                RetrofitClient.updateTokenAndRefreshToken("")
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finish()
            }
        }
        mBinding.mainClusterCard.findViewById<View>(R.id.img_refresh).setOnClickListener { 
            it.startAnimation(rotateAnimation)
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