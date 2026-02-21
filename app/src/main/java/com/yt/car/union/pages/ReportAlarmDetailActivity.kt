package com.yt.car.union.pages

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.loadState.LoadState
import com.chad.library.adapter4.loadState.trailing.TrailingLoadStateAdapter
import com.yt.car.union.databinding.ActivityReportWarningDetailBinding
import com.yt.car.union.net.WarningDetailData
import com.yt.car.union.net.WarningDetailItem
import com.yt.car.union.net.WarningReportItem
import com.yt.car.union.pages.adapter.WarningDetailAdapter
import com.yt.car.union.util.PressEffectUtils
import com.yt.car.union.viewmodel.ApiState
import com.yt.car.union.viewmodel.ReportViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.getValue

class ReportAlarmDetailActivity : AppCompatActivity() {

    // 伴生对象：相当于Java的static静态成员，其他类可直接访问
    companion object {
        // const val 用于编译期确定的字符串常量（必须是字面量，不能是方法返回值）
        const val KEY_WARN_REPORT_NUM = "key_warn_report_num"
        const val KEY_WARN_REPORT_NAME = "key_warn_report_name"
        const val KEY_WARN_REPORT_TYPE = "key_warn_report_type"
    }

    private lateinit var binding: ActivityReportWarningDetailBinding
    private lateinit var warningDetailAdapter: WarningDetailAdapter
    private val detailItems = mutableListOf<WarningDetailItem>()
    private val reportViewModel by viewModels<ReportViewModel>()
    private val warningDetailStateFlow = MutableStateFlow<ApiState<WarningDetailData>>(ApiState.Idle)
    private var pageNum: Int = 1
    private val pageSize = 20
    private val timetype = 1

    private var warnType = 0
    private var warnNum = 0
    private var warnName = ""
    private lateinit var adapterHelper: QuickAdapterHelper
    private var loadFromMore: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportWarningDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        warnType = intent.getIntExtra(KEY_WARN_REPORT_TYPE, 0)
        warnNum = intent.getIntExtra(KEY_WARN_REPORT_NUM, 0)
        warnName = intent.getStringExtra(KEY_WARN_REPORT_NAME).toString()
        initView()
        loadData()
        initListener()
    }

    private fun initView() {
        binding.tvTitle.text = "$warnName$warnNum"
        // 初始化RecyclerView
        warningDetailAdapter = WarningDetailAdapter()
        warningDetailAdapter.submitList(detailItems)
        adapterHelper = QuickAdapterHelper.Builder(warningDetailAdapter)
            .setTrailingLoadStateAdapter(object : TrailingLoadStateAdapter.OnTrailingListener {
                override fun onLoad() {
                    pageNum++
                    loadFromMore = true
                    loadData()
                }

                override fun onFailRetry() {
                }

            })
            .setTrailPreloadSize(1)
            .attachTo(binding.rvWarningList)
        updateLoadState()
        binding.rvWarningList.apply {
            layoutManager = LinearLayoutManager(this@ReportAlarmDetailActivity)
        }

        lifecycleScope.launch {
            warningDetailStateFlow.collect { state ->
                when (state) {
                    is ApiState.Loading -> {
                        // 显示进度框
                    }
                    is ApiState.Success -> {
                        // 隐藏进度框，关闭输入框，提示成功
                        state.data?.let {
                            detailItems.addAll(state.data.list)
                            warningDetailAdapter.notifyDataSetChanged()
                        }
                        if (loadFromMore) {
                            updateLoadState()
                            loadFromMore = false
                        }
                    }
                    is ApiState.Error -> {
                        Toast.makeText(this@ReportAlarmDetailActivity, "获取数据失败：${state.msg}", Toast.LENGTH_SHORT).show()
                        // 重置状态
                        warningDetailStateFlow.value = ApiState.Idle
                        if (loadFromMore) {
                            updateLoadState()
                            loadFromMore = false
                        }
                    }
                    is ApiState.Idle -> {
                        // 初始状态，无需处理
                    }
                }
            }
        }
    }

    private fun updateLoadState() {
        adapterHelper.trailingLoadState = LoadState.NotLoading(false)
    }

    private fun loadData() {
        reportViewModel.getWarningDetail(pageNum, pageSize, timetype, warnType , warningDetailStateFlow)
    }

    private fun initListener() {
        PressEffectUtils.setCommonPressEffect(binding.tvBack)
        // 返回按钮点击
        binding.tvBack.setOnClickListener { finish() }
    }
}