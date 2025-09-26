package com.example.uhfproject.ui.fragment

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.uhfproject.R
import com.example.uhfproject.app.MyApplication
import com.example.uhfproject.databinding.FragmentFindItemBinding
import com.example.uhfproject.model.ExcelDownloadVO
import com.example.uhfproject.ui.MainActivity
import com.example.uhfproject.utils.*
import com.example.uhfproject.utils.Const.findItemPower
import com.example.uhfproject.utils.Const.intToPercent
import com.seuic.uhf.EPC
import com.seuic.uhf.UHFService
import com.seuic.uhfutils.EpcSearch
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


class FindItemFragment : BaseFragment<FragmentFindItemBinding>(), SensorEventListener {

    private var item: ExcelDownloadVO? = null

    private var sensorManager: SensorManager? = null
    private var gyroscopeSensor: Sensor? = null
    private var rotationVectorSensor: Sensor? = null

    private var currentRfid = ""
    private var northDegree: Float? = null
    private var currentRssi = 0


    override fun initView() {
        mActivity = requireActivity() as MainActivity
        mBinding.vScanHint.setBackgroundColor(Color.GRAY)
        mBinding.tvScanHint.text = "Not Scanned"
        mainViewModel.startQuest = false
        item = arguments?.getParcelable("item") as ExcelDownloadVO?
        item?.let {
            mBinding.edtRfid.editText?.setText(it.trackingNumber)
            currentRfid = it.epc?:""
            mBinding.tvRfid.text = "EPC:$currentRfid"
        }
        LogUtil.d("")
        //获取陀螺仪传感器管理器和服务
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (sensorManager != null) {
            gyroscopeSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
            rotationVectorSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        }
        if (gyroscopeSensor == null) {
            Toasty.warning(requireContext(), "您的设备不支持陀螺仪", Toasty.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initData() {
        UHFService.getInstance(MyApplication.appContext).power = Const.inventoryPower

        mBinding.tvBack.setOnClickListener {
            mainViewModel.stopLoading()
            findNavController().popBackStack()
        }
        mBinding.imgPower.setOnClickListener {
            Const.simpleEditAlert(
                requireContext(),
                getString(R.string.finditem_click_power_hint),
                findItemPower.toString()
            ) {
                findItemPower = it
                UHFService.getInstance(MyApplication.appContext).power = findItemPower
            }
        }
        mBinding.btnQuery.setOnClickListener {
            LogUtil.d("btnUpload")
            mBinding.edtRfid.editText?.clearFocus()
            hideKeyboard(mBinding.edtRfid.editText!!)
            val tracking = mBinding.edtRfid.editText?.text.toString()
            if(tracking.isEmpty()){
                return@setOnClickListener
            }
            mainViewModel.getItemByTracking(tracking, success = {
                lifecycleScope.launch(Dispatchers.Main){
                    currentRfid = it.epc?:""
                    mBinding.tvRfid.text = "EPC:$currentRfid"
                }
            }, empty = {
                lifecycleScope.launch(Dispatchers.Main){
                    mBinding.tvRfid.text = "Tracking# Error"
                }
            })
        }
        mBinding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val v = requireActivity().currentFocus
                if (v is EditText) {
                    val outRect = Rect()
                    v.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                        v.clearFocus()
                        hideKeyboard(v)
                    }
                }
            }
            false
        }
    }

    override fun observeData() {
        mainViewModel.findList.observe(viewLifecycleOwner){
            val search = EpcSearch.search(it)
            search.find { it.getId() == currentRfid }?.let {
                currentRssi = it.rssi
                LogUtil.d("寻物rssi:${intToPercent(currentRssi).toFloat()}")
                BeepSound.controlPlay((currentRssi/100).toFloat())
                VolumeController().setVolumePercent(currentRssi)
                mBinding.tvProgress.text = "${currentRssi}%"
                onValueChanged(currentRssi)
            }
        }
        mainViewModel.startBtn.observe(viewLifecycleOwner){
            if(it){
                mBinding.imgPoint.visibility = View.VISIBLE
                //归位
                resetBView()
                startCircularMotion(mBinding.imgCompassShade,
                    mBinding.imgCompassShade.x + (mBinding.imgCompassShade.width / 2f),
                    mBinding.imgCompassShade.y + (mBinding.imgCompassShade.height / 2f))
            }
        }
        mainViewModel.stopBtn.observe(viewLifecycleOwner){
            if(it){
                stopCircularMotion()
                mBinding.imgPoint.visibility = View.GONE
            }
        }
    }

    private fun onValueChanged(value: Int) {
        // 1️⃣ 先除以100得到比例
        val ratio = intToPercent(value).toFloat()  // 注意要用 Float
        // 2️⃣ 根据 viewA 的宽度计算目标宽度
        val targetWidth = (mBinding.vProgressBackground.width * ratio).toInt()
        // 3️⃣ 更新 viewB 宽度
        lifecycleScope.launch(Dispatchers.Main){
            val layoutParams = mBinding.vProgress.layoutParams
            layoutParams.width = targetWidth
            mBinding.vProgress.layoutParams = layoutParams
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onResume() {
        super.onResume()
        // 注册监听器，设置采样速率（例如：SENSOR_DELAY_UI）
        gyroscopeSensor?.also { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        rotationVectorSensor?.also { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        mActivity?.onKeyDownCallback = { keyCode, event ->
            if (keyCode == 142 && event?.action == KeyEvent.ACTION_DOWN) {
                if(!keyStatus){
                    keyStatus = true
                    mBinding.vScanHint.setBackgroundColor(Color.parseColor("#0055A3"))
                    mBinding.tvScanHint.text = "Scanning"
                    mainViewModel.startStock()
                }else{
                    keyStatus = false
                    mBinding.vScanHint.setBackgroundColor(Color.GRAY)
                    mBinding.tvScanHint.text = "Not Scanned"
                    mainViewModel.stopStock()
                }
                true
            } else {
                false
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // 务必注销监听器以节省电量
//        mainViewModel.stopStock()
        sensorManager?.unregisterListener(this)
        gyroscopeSensor = null
        rotationVectorSensor = null
        sensorManager = null
    }

    private var centerX = 0f
    private var centerY = 0f
    private var radius = 150f
    private var currentAngle = -90f
    private var lastUpdateTime = 0L
    private var isFirstSensorUpdate = true // 标记是否是第一次传感器更新
    private var currentZ = 0f

    private var currentPercentage = 0.5f // 初始位置在中间 (50%)
    private var targetPercentage = 0.5f
    private val minRadius = 0f
    private val maxRadius = 275f

    override fun onSensorChanged(event: SensorEvent) {
        when {
            event.sensor.type == Sensor.TYPE_GYROSCOPE -> {
                val now = System.currentTimeMillis()
                // 如果是第一次传感器更新，直接使用重置后的位置，不进行角度计算
                if (isFirstSensorUpdate) {
                    isFirstSensorUpdate = false
                    lastUpdateTime = now
                    return
                }
                val dt = (now - lastUpdateTime) / 1000f // 转换为秒

                if (dt > 0) {
                    // 获取Z轴角速度并反向
                    val zRotation = -event.values[2] // 负号实现反向
                    // 更新角度
                    currentAngle += (-zRotation) * dt * 180f / Math.PI.toFloat() // 转换为角度
                    currentZ += zRotation * dt * 180f / Math.PI.toFloat() // 转换为角度
                    // 2. compassView 跟随旋转（反向）
                    mBinding.imgCompass.rotation = currentZ

                    updatePointPosition(intToPercent(currentRssi).toFloat())

                    lastUpdateTime = now
                }
            }
            northDegree==null && event.sensor?.type == Sensor.TYPE_ROTATION_VECTOR -> {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                val orientationAngles = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)

                val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()

                currentZ += (azimuth + 360) % 360  // 0° 表示正北
                northDegree = currentZ
                mBinding.imgCompass.rotation = currentZ

            }
        }
    }

    private fun updatePointPosition(rssi: Float){
        // 将百分比转换为实际半径 (圆心100%→半径最小，边缘0%→半径最大)
        val radiusRange = maxRadius - minRadius
        val currentRadius = minRadius + (1 - rssi) * radiusRange

        val rad = Math.toRadians(currentAngle.toDouble())
        mBinding.imgPoint.x = centerX + currentRadius * cos(rad).toFloat() - mBinding.imgPoint.width / 2
        mBinding.imgPoint.y = centerY + currentRadius * sin(rad).toFloat() - mBinding.imgPoint.height / 2
    }

    /** 重置：回到 AView 顶部 **/
    private fun resetBView() {
        // 重置时直接放回顶部 (不依赖 yaw)
        centerX = mBinding.imgCompass.x + (mBinding.imgCompass.width / 2f)
        centerY = mBinding.imgCompass.y + (mBinding.imgCompass.height / 2f)
        // 放置bView到圆心正上方位置
        currentAngle = -90f // 确保初始角度为-90度（正上方）
        val rad = Math.toRadians(-currentAngle.toDouble())
        mBinding.imgPoint.x = centerX + radius * cos(rad).toFloat() - mBinding.imgPoint.width / 2
        mBinding.imgPoint.y = centerY + radius * sin(rad).toFloat() - mBinding.imgPoint.height / 2
        lastUpdateTime = System.currentTimeMillis() // 重置时间戳
        isFirstSensorUpdate = true // 标记下一次传感器更新为第一次
    }

    // 动画控制变量
    private var animator: ValueAnimator? = null
    private var currentAnimatorAngle = 0f // 记录当前角度
    // 启动圆周运动
    private fun startCircularMotion(view: View, centerX1: Float, centerY1: Float, radius: Float = 0f) {
        animator?.cancel() // 取消之前的动画
        animator = ValueAnimator.ofFloat(currentAnimatorAngle, currentAnimatorAngle + 360f).apply {
            duration = 2000
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE

            addUpdateListener {
                val angle = it.animatedValue as Float
                currentAnimatorAngle = angle % 360f // 更新当前角度
                val rad = Math.toRadians(angle.toDouble())
                view.x = centerX1 + radius * cos(rad).toFloat() - view.width / 2
                view.y = centerY1 + radius * sin(rad).toFloat() - view.height / 2
                view.rotation = angle
            }

            start()
        }
    }
    // 停止圆周运动
    private fun stopCircularMotion() {
        animator?.cancel()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) { }
    private var keyStatus = false
    private var mActivity: MainActivity? = null

    override fun onStop() {
        mainViewModel.stopStock()
        mActivity?.onKeyDownCallback = null
        super.onStop()
    }

}