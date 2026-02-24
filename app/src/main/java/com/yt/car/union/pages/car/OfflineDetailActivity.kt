package com.yt.car.union.pages.car

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.yt.car.union.databinding.ActivityOfflineDetailBinding
import com.yt.car.union.pages.adapter.OfflineDateAdapter
import com.yt.car.union.util.PressEffectUtils
import com.yt.car.union.viewmodel.ApiState
import com.yt.car.union.viewmodel.car.ReportViewModel
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

        binding.tvTitle.text = "离线提醒" + intent.getIntExtra(KEY_CAR_NUM, 0)

        lifecycleScope.launch {
            offlineDetailStateFlow.collect {state ->
                when (state) {
                    is ApiState.Loading -> {
                        // 加载中：显示进度条，隐藏其他视图
                    }
                    is ApiState.Success -> {
                        // 成功：隐藏进度条，显示数据
                        state.data?.let {
                            setupRecyclerView(state.data)
                        }
                    }
                    is ApiState.Error -> {
                        // 失败：显示错误信息，隐藏其他视图
                    }
                    is ApiState.Idle -> {
                        // 初始状态，无需处理
                    }
                }
            }
        }

        val carId = intent.getIntExtra(KEY_CAR_ID, 0)
        val start = intent.getStringExtra(KEY_START).toString()
        val end = intent.getStringExtra(KEY_END).toString()
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