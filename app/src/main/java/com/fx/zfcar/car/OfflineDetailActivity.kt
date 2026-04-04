package com.fx.zfcar.car

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fx.zfcar.car.adapter.OfflineDateAdapter
import com.fx.zfcar.car.viewmodel.ReportViewModel
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.viewmodel.ApiState
import com.fx.zfcar.databinding.ActivityOfflineDetailBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.getValue

class OfflineDetailActivity : AppCompatActivity() {

    companion object {
        const val KEY_CAR_ID = "key_car_id"
        const val KEY_CAR_NUM = "key_car_num"
        const val KEY_START = "key_start"
        const val KEY_END = "key_end"
    }

    private lateinit var binding: ActivityOfflineDetailBinding
    private val reportViewModel by viewModels<ReportViewModel>()
    private var offlineDetailStateFlow = MutableStateFlow<ApiState<List<String>>>(ApiState.Idle)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfflineDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PressEffectUtils.setCommonPressEffect(binding.tvBack)
        binding.tvBack.setOnClickListener { finish() }

        val layoutManager = LinearLayoutManager(this)
        binding.rvList.layoutManager = layoutManager

        val carNum = intent.getStringExtra(KEY_CAR_NUM).orEmpty()
        binding.tvTitle.text = if (carNum.isNotEmpty()) "离线提醒 $carNum" else "离线提醒"

        lifecycleScope.launch {
            offlineDetailStateFlow.collect {state ->
                when (state) {
                    is ApiState.Loading -> {
                        // 加载中：显示进度条，隐藏其他视图
                    }
                    is ApiState.Success -> {
                        // 成功：隐藏进度条，显示数据
                        setupRecyclerView(state.data.orEmpty())
                    }
                    is ApiState.Error -> {
                        showToast(state.msg)
                    }
                    is ApiState.Idle -> {
                        // 初始状态，无需处理
                    }
                }
            }
        }

        val carId = intent.getStringExtra(KEY_CAR_ID)
            ?: intent.getIntExtra(KEY_CAR_ID, 0).takeIf { it > 0 }?.toString()
            .orEmpty()
        val start = intent.getStringExtra(KEY_START).orEmpty()
        val end = intent.getStringExtra(KEY_END).orEmpty()
        if (carId.isBlank()) {
            showToast("车辆信息异常")
            finish()
            return
        }
        reportViewModel.getOfflineDetailReport(carId, end, start, offlineDetailStateFlow)
    }

    /**
     * 配置RecyclerView：设置布局管理器和适配器
     */
    private fun setupRecyclerView(dateList: List<String>) {
        // 设置适配器
        val adapter = OfflineDateAdapter(dateList)
        binding.rvList.adapter = adapter
    }
}
