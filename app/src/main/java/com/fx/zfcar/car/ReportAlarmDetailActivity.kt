package com.fx.zfcar.car

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fx.zfcar.car.adapter.WarningDetailAdapter
import com.fx.zfcar.car.viewmodel.ReportViewModel
import com.fx.zfcar.net.WarningDetailData
import com.fx.zfcar.net.WarningDetailItem
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.viewmodel.ApiState
import com.fx.zfcar.databinding.ActivityReportWarningDetailBinding
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
    private var loadFromMore: Boolean = false
    private var currentTotal: Int = 0
    private var reachedEnd: Boolean = false
    private lateinit var layoutManager: LinearLayoutManager

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
        binding.tvTitle.text = "${warnName}${warnNum}"
        // 初始化RecyclerView
        warningDetailAdapter = WarningDetailAdapter()
        warningDetailAdapter.submitList(detailItems)
        layoutManager = LinearLayoutManager(this@ReportAlarmDetailActivity)
        binding.rvWarningList.apply {
            layoutManager = this@ReportAlarmDetailActivity.layoutManager
            adapter = warningDetailAdapter
        }
        initScrollPagination()

        lifecycleScope.launch {
            warningDetailStateFlow.collect { state ->
                when (state) {
                    is ApiState.Loading -> {
                        // 显示进度框
                    }
                    is ApiState.Success -> {
                        val data = state.data ?: return@collect
                        currentTotal = data.total
                        if (!loadFromMore) {
                            detailItems.clear()
                        }
                        detailItems.addAll(data.list)
                        reachedEnd = detailItems.size >= currentTotal || data.list.size < pageSize
                        warningDetailAdapter.submitList(detailItems.toList())
                        loadFromMore = false
                    }
                    is ApiState.Error -> {
                        showToast("获取数据失败：${state.msg}")
                        // 重置状态
                        warningDetailStateFlow.value = ApiState.Idle
                        if (loadFromMore && pageNum > 1) pageNum--
                        loadFromMore = false
                    }
                    is ApiState.Idle -> {
                        // 初始状态，无需处理
                    }
                }
            }
        }
    }

    private fun initScrollPagination() {
        binding.rvWarningList.clearOnScrollListeners()
        binding.rvWarningList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0 || loadFromMore || reachedEnd) return
                if (layoutManager.findLastVisibleItemPosition() >= warningDetailAdapter.itemCount - 2 && warningDetailAdapter.itemCount > 0) {
                    pageNum++
                    loadFromMore = true
                    loadData()
                }
            }
        })
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
