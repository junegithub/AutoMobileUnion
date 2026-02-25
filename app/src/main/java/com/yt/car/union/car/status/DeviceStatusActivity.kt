package com.yt.car.union.car.status

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.yt.car.union.databinding.ActivityDeviceStatusBinding
import com.yt.car.union.net.CarStatusListData
import com.yt.car.union.util.PressEffectUtils
import com.yt.car.union.util.ProgressDialogUtils
import com.yt.car.union.viewmodel.ApiState
import com.yt.car.union.car.viewmodel.CarInfoViewModel
import com.yt.car.union.training.user.showToast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.getValue

class DeviceStatusActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeviceStatusBinding

    private val carStatusListStateFlow = MutableStateFlow<ApiState<CarStatusListData>>(ApiState.Idle)
    private val carInfoViewModel by viewModels<CarInfoViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initData()
        initListener()
    }

    private fun initView() {
        lifecycleScope.launch {
            carStatusListStateFlow.collect { state ->
                when (state) {
                    is ApiState.Loading -> {
                        ProgressDialogUtils.show(this@DeviceStatusActivity)
                    }
                    is ApiState.Success -> {
                        // 隐藏进度框，关闭输入框，提示成功
                        ProgressDialogUtils.dismiss()
                        state.data?.let {
                            // 直接给每个TextView赋值
                            binding.tvDriveCount.text = it.drive.toString()
                            binding.tvStopCount.text = it.stop.toString()
                            binding.tvOfflineCount.text = it.offline.toString()
                            binding.tvOverSpeedCount.text = it.overSpeed.toString()
                            binding.tvTiredCount.text = it.tired.toString()
                            binding.tvExpiredCount.text = it.expired.toString()
                        }
                    }
                    is ApiState.Error -> {
                        ProgressDialogUtils.dismiss()
                        showToast("获取数据失败：${state.msg}")
                    }
                    is ApiState.Idle -> {
                        // 初始状态，无需处理
                    }
                }
            }
        }
    }

    /**
     * 初始化截图中的设备状态数据
     */
    private fun initData() {
        carInfoViewModel.getCarStatusList(carStatusListStateFlow)
    }

    private fun initListener() {
        PressEffectUtils.setCommonPressEffect(binding.ivBack)
        // 返回按钮点击
        binding.ivBack.setOnClickListener { finish() }

        // 行驶中条目点击
        binding.itemDrive.setOnClickListener {
            swithToDetail("行驶中", "driving")
        }
        // 静止条目点击
        binding.itemStop.setOnClickListener {
            swithToDetail("静止", "stop")
        }
        // 离线条目点击
        binding.itemOffline.setOnClickListener {
            swithToDetail("离线", "offline")
        }
        // 超速条目点击
        binding.itemOverSpeed.setOnClickListener {
            swithToDetail("超速", "overSpeed")
        }
        // 疲劳条目点击
        binding.itemTired.setOnClickListener {
            swithToDetail("疲劳", "tired")
        }
        // 到期条目点击
        binding.itemExpired.setOnClickListener {
            startActivity(Intent(this@DeviceStatusActivity, ExpireCarActivity::class.java))
        }
    }

    private fun swithToDetail(title: String, type: String) {
        val intent = Intent(this, DeviceStatusListActivity::class.java)
        intent.putExtra(DeviceStatusListActivity.KEY_CAR_STATUS_TITLE, title)
        intent.putExtra(DeviceStatusListActivity.KEY_CAR_STATUS_TYPE, type)
        startActivity(intent)
    }

    /**
     * 页面销毁时强制关闭弹窗，避免内存泄漏
     */
    override fun onDestroy() {
        super.onDestroy()
        ProgressDialogUtils.dismiss()
    }
}