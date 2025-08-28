package com.example.uhfproject.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.uhfproject.R
import com.example.uhfproject.app.MyApplication
import com.example.uhfproject.databinding.FragmentFindItemBinding
import com.example.uhfproject.model.ExcelDownloadVO
import com.example.uhfproject.ui.BaseFragment
import com.example.uhfproject.utils.Const
import com.example.uhfproject.utils.Const.findItemPower
import com.example.uhfproject.utils.LogUtil
import com.seuic.uhf.UHFService
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Integer.min

class FindItemFragment : BaseFragment<FragmentFindItemBinding>(), SensorEventListener {

    var item: ExcelDownloadVO? = null


    private var sensorManager: SensorManager? = null
    private var gyroscopeSensor: Sensor? = null
    private var rotationVectorSensor: Sensor? = null

    private var lastUpdateTime: Long = 0
    private var deviceRotationAngle = 0f // 设备累计旋转角度

    private var northDegree: Float? = null
    private var firstCount = 0
    private var currentRssi = 0


    override fun initView() {
        mainViewModel.startQuest = false
        item = arguments?.getParcelable("item") as ExcelDownloadVO?
        item?.let {
            mBinding.edtRfid.editText?.setText(it.trackingNumber)
            mBinding.tvRfid.text = "EPC:${it.epc}"
        }
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
        mBinding.btnUpload.setOnClickListener {
            val tracking = mBinding.edtRfid.editText?.text.toString()
            mainViewModel.getItemByTracking(tracking, success = {
                mBinding.tvRfid.text = "EPC:${it.epc}"
            }, empty = {
                mBinding.tvRfid.text = "Tracking# Error"
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
        mBinding.btnUpload.setOnClickListener {
            mBinding.edtRfid.editText?.clearFocus()
            hideKeyboard(mBinding.edtRfid.editText!!)
        }
    }

    override fun observeData() {
        mainViewModel.findList.observe(viewLifecycleOwner){
            val rfid = mBinding.tvRfid.text.toString().substring(4)
            it.find { it.getId() == rfid }?.let {
                LogUtil.d("寻物rssi成功: ${it.rssi}")
                currentRssi = calculatePercentage(it.rssi)

                mBinding.tvProgress.text = "${currentRssi}%"

                onValueChanged(currentRssi)
            }
        }
        mainViewModel.startBtn.observe(viewLifecycleOwner){
            if(it){
                mBinding.cView.visibility = View.VISIBLE
                //归位
                resetBViewToTop(mBinding.layoutA, mBinding.cView)
            }
        }
        mainViewModel.stopBtn.observe(viewLifecycleOwner){
            if(it){
                mBinding.cView.visibility = View.GONE
            }
        }
    }

    private fun onValueChanged(value: Int) {
        // 1️⃣ 先除以100得到比例
        val ratio = value / 100f  // 注意要用 Float

        // 2️⃣ 根据 viewA 的宽度计算目标宽度
        val targetWidth = (mBinding.vProgressBackground.width * ratio).toInt()

        // 3️⃣ 更新 viewB 宽度
        lifecycleScope.launch(Dispatchers.Main){
            val layoutParams = mBinding.vProgress.layoutParams
            layoutParams.width = targetWidth
            mBinding.vProgress.layoutParams = layoutParams
        }
    }

    private fun calculatePercentage(value: Int): Int {
        require(value in -80..0) { "Value must be between -80 and 0" }
        // 计算百分比：((当前值 + 80) / 80) * 100
        val percentage = ((value + 80) / 80.0 * 100).toInt()
        return percentage.coerceIn(0, 100) // 确保百分比在 0-100 范围内
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
    }

    override fun onPause() {
        super.onPause()
        // 务必注销监听器以节省电量
        mainViewModel.stopStock()
        sensorManager?.unregisterListener(this)
        gyroscopeSensor = null
        rotationVectorSensor = null
        sensorManager = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when {
                event.sensor.type == Sensor.TYPE_GYROSCOPE -> {
                    // 绕Z轴的角速度（垂直于屏幕的旋转）
                    val axisZ = event.values[2]

                    // 获取当前时间
                    val currentTime = System.currentTimeMillis()

                    if (lastUpdateTime == 0L) {
                        lastUpdateTime = currentTime
                        return
                    }

                    // 计算时间间隔（转换为秒）
                    val deltaTime = (currentTime - lastUpdateTime) / 1000f
                    lastUpdateTime = currentTime

                    // 将角速度转换为角度变化（弧度转角度）
                    val angleChange = Math.toDegrees((axisZ * deltaTime).toDouble()).toFloat()

                    // 更新设备累计旋转角度
                    deviceRotationAngle += angleChange
                }
                northDegree==null && event.sensor?.type == Sensor.TYPE_ROTATION_VECTOR -> {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                    val orientationAngles = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)

                    val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()

                    northDegree = (azimuth + 360) % 360  // 0° 表示正北
                }
            }
            updateViewRotation()
        }
    }

    private fun updateViewRotation() {
        requireActivity().runOnUiThread {
            if(firstCount==0 && northDegree != null){
                mBinding.img.rotation = northDegree!!
                firstCount++
            }
            // 设置View的旋转角度（反向）
            mBinding.layoutA.rotation = deviceRotationAngle
//            mBinding.cView.rotation = deviceRotationAngle
            updateBViewPosition(mBinding.layoutA, mBinding.cView, currentRssi.toFloat(), deviceRotationAngle)
        }
    }
    private fun updateBViewPosition(aView: View, bView: View, value: Float, angleDegrees: Float) {
        val fraction = value.coerceIn(0f, 100f) / 100f

        val cx = aView.width / 2f
        val cy = aView.height / 2f
        val radius = minOf(cx, cy)
        val rPrime = radius * (1 - fraction)

        val theta = 0.0 // 转弧度

        val newX = (cx + rPrime * Math.sin(theta) - bView.width / 2).toFloat()
        val newY = (cy - rPrime * Math.cos(theta) - bView.height / 2).toFloat()

        bView.x = newX
        bView.y = newY
    }
    private fun resetBViewToTop(aView: View, bView: View) {
        val cx = aView.width / 2f
        val cy = aView.height / 2f
        val radius = minOf(cx, cy)

        // 顶部正Y轴 -> angle = 0, fraction = 0
        val fraction = 0f
        val rPrime = radius * (1 - fraction) // rPrime = radius

        val theta = 0.0 // 顶部正Y轴

        val newX = (cx + rPrime * Math.sin(theta) - bView.width / 2).toFloat()
        val newY = (cy - rPrime * Math.cos(theta) - bView.height / 2).toFloat()

        bView.x = newX
        bView.y = newY
    }




    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

}