package com.fx.zfcar.car.status

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fx.zfcar.car.viewmodel.CarInfoViewModel
import com.fx.zfcar.net.CarStatusListData
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.util.ProgressDialogUtils
import com.fx.zfcar.viewmodel.ApiState
import com.fx.zfcar.databinding.ActivityDeviceStatusBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.getValue

class DeviceStatusActivity : AppCompatActivity() {
    companion object {
        private const val REFRESH_INTERVAL_MS = 30_000L
    }

    private lateinit var binding: ActivityDeviceStatusBinding

    private val carStatusListStateFlow = MutableStateFlow<ApiState<CarStatusListData>>(ApiState.Idle)
    private val carInfoViewModel by viewModels<CarInfoViewModel>()
    private var lastNavigateAt: Long = 0L
    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            initData()
            refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initData()
        initListener()
    }

    private fun initView() {
        renderStatusCounts(null)
        lifecycleScope.launch {
            carStatusListStateFlow.collect { state ->
                when (state) {
                    is ApiState.Loading -> {
                        ProgressDialogUtils.show(this@DeviceStatusActivity)
                    }
                    is ApiState.Success -> {
                        ProgressDialogUtils.dismiss()
                        renderStatusCounts(state.data)
                    }
                    is ApiState.Error -> {
                        ProgressDialogUtils.dismiss()
                        renderStatusCounts(null)
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
            handleStatusClick { switchToDetail("行驶中", "driving") }
        }
        // 静止条目点击
        binding.itemStop.setOnClickListener {
            handleStatusClick { switchToDetail("静止", "stop") }
        }
        // 离线条目点击
        binding.itemOffline.setOnClickListener {
            handleStatusClick { switchToDetail("离线", "offline") }
        }
        // 超速条目点击
        binding.itemOverSpeed.setOnClickListener {
            handleStatusClick { switchToDetail("超速", "overSpeed") }
        }
        // 疲劳条目点击
        binding.itemTired.setOnClickListener {
            handleStatusClick { switchToDetail("疲劳", "tired") }
        }
        // 到期条目点击
        binding.itemExpired.setOnClickListener {
            handleStatusClick {
                startActivity(Intent(this@DeviceStatusActivity, ExpireCarActivity::class.java))
            }
        }
    }

    private fun renderStatusCounts(data: CarStatusListData?) {
        binding.tvDriveCount.text = (data?.drive ?: 0).toString()
        binding.tvStopCount.text = (data?.stop ?: 0).toString()
        binding.tvOfflineCount.text = (data?.offline ?: 0).toString()
        binding.tvOverSpeedCount.text = (data?.overSpeed ?: 0).toString()
        binding.tvTiredCount.text = (data?.tired ?: 0).toString()
        binding.tvExpiredCount.text = (data?.expired ?: 0).toString()
    }

    private fun handleStatusClick(action: () -> Unit) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastNavigateAt < 500) return
        lastNavigateAt = now
        action()
    }

    private fun switchToDetail(title: String, type: String) {
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
        refreshHandler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        super.onResume()
        refreshHandler.removeCallbacks(refreshRunnable)
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS)
    }

    override fun onPause() {
        super.onPause()
        refreshHandler.removeCallbacks(refreshRunnable)
    }
}
