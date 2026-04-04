package com.fx.zfcar.car.status

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fx.zfcar.car.adapter.ExpireCarAdapter
import com.fx.zfcar.car.viewmodel.CarInfoViewModel
import com.fx.zfcar.net.CarExpireItem
import com.fx.zfcar.net.CarExpireResponse
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.viewmodel.ApiState
import com.google.android.material.tabs.TabLayout
import com.fx.zfcar.databinding.ActivityExpireCarBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.getValue

/**
 * 车辆到期页面
 */
class ExpireCarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExpireCarBinding
    private lateinit var adapter: ExpireCarAdapter

    private val statusList = mutableListOf<CarExpireItem>()

    private var pageNum = 1
    private val pageSize = 50 // 每页条数
    private var loadFromMore: Boolean = false
    private var currentTotal: Int = 0
    private var reachedEnd: Boolean = false
    private lateinit var layoutManager: LinearLayoutManager

    private val carInfoViewModel by viewModels<CarInfoViewModel>()
    private val statusListStateFlow = MutableStateFlow<ApiState<CarExpireResponse>>(ApiState.Idle)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpireCarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 初始化列表
        initRecyclerView()

        // 2. 初始化Tab
        initTabLayout()

        lifecycleScope.launch {
            statusListStateFlow.collect { state ->
                when (state) {
                    is ApiState.Loading -> {
                        // 显示进度框
                    }
                    is ApiState.Success -> {
                        val data = state.data ?: return@collect
                        currentTotal = data.total
                        if (!loadFromMore) {
                            statusList.clear()
                        }
                        statusList.addAll(data.list)
                        reachedEnd = statusList.size >= currentTotal || data.list.size < pageSize
                        adapter.submitList(statusList.toList())
                        loadFromMore = false
                    }
                    is ApiState.Error -> {

                        showToast("获取数据失败：${state.msg}")
                        // 重置状态
                        statusListStateFlow.value = ApiState.Idle
                        if (loadFromMore && pageNum > 1) pageNum--
                        loadFromMore = false
                    }
                    is ApiState.Idle -> {
                        // 初始状态，无需处理
                    }
                }
            }
        }

        loadCarList()

        // 4. 返回按钮点击
        binding.ivBack.setOnClickListener { finish() }
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        adapter = ExpireCarAdapter()
        adapter.submitList(statusList)
        layoutManager = LinearLayoutManager(this@ExpireCarActivity)
        binding.rvStatusList.apply {
            layoutManager = this@ExpireCarActivity.layoutManager
            adapter = this@ExpireCarActivity.adapter
        }
        initScrollPagination()
    }

    private fun initScrollPagination() {
        binding.rvStatusList.clearOnScrollListeners()
        binding.rvStatusList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0 || loadFromMore || reachedEnd) return
                if (layoutManager.findLastVisibleItemPosition() >= adapter.itemCount - 2 && adapter.itemCount > 0) {
                    pageNum++
                    loadFromMore = true
                    loadCarList(binding.customTabLayout.selectedTabPosition == 1)
                }
            }
        })
    }

    /**
     * 初始化TabLayout
     */
    private fun initTabLayout() {
        binding.customTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { position ->
                    pageNum = 1 // 切换Tab重置页码
                    currentTotal = 0
                    reachedEnd = false
                    loadFromMore = false
                    statusList.clear()
                    adapter.submitList(emptyList())
                    loadCarList(position == 1)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadCarList(expired: Boolean = false) {
        carInfoViewModel.getOutdate(expired, pageNum, pageSize, statusListStateFlow)
    }
}
