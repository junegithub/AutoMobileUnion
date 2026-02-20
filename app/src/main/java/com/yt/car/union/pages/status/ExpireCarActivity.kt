package com.yt.car.union.pages.status

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.loadState.LoadState
import com.chad.library.adapter4.loadState.trailing.TrailingLoadStateAdapter
import com.yt.car.union.databinding.ActivityExpireCarBinding
import com.yt.car.union.net.CarExpireItem
import com.yt.car.union.net.CarExpireResponse
import com.yt.car.union.pages.adapter.ExpireCarAdapter
import com.yt.car.union.viewmodel.ApiState
import com.yt.car.union.viewmodel.CarInfoViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.getValue

/**
 * 车辆到期页面
 */
class ExpireCarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExpireCarBinding
    private lateinit var adapter: ExpireCarAdapter
    private lateinit var adapterHelper: QuickAdapterHelper

    private val statusList = mutableListOf<CarExpireItem>()

    private var pageNum = 1
    private val pageSize = 50 // 每页条数
    private var loadFromMore: Boolean = false

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
                        // 隐藏进度框，关闭输入框，提示成功
                        state.data?.let {
                            statusList.addAll(state.data.list)
                            adapter.notifyDataSetChanged()
                        }
                        if (loadFromMore) {
                            updateLoadState()
                            loadFromMore = false
                        }
                    }
                    is ApiState.Error -> {
                        Toast.makeText(this@ExpireCarActivity, "获取数据失败：${state.msg}", Toast.LENGTH_SHORT).show()
                        // 重置状态
                        statusListStateFlow.value = ApiState.Idle
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
        binding.rvStatusList.apply {
            layoutManager = LinearLayoutManager(this@ExpireCarActivity)
        }
        adapterHelper = QuickAdapterHelper.Builder(adapter)
            .setTrailingLoadStateAdapter(object : TrailingLoadStateAdapter.OnTrailingListener {
                override fun onLoad() {
                    pageNum++
                    loadFromMore = true
                    loadCarList()
                }

                override fun onFailRetry() {
                }

            })
            .setTrailPreloadSize(1)
            .attachTo(binding.rvStatusList)
        updateLoadState()
    }

    private fun updateLoadState() {
        adapterHelper.trailingLoadState = LoadState.NotLoading(false)
    }

    /**
     * 初始化TabLayout
     */
    private fun initTabLayout() {
        binding.customTabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                tab?.position?.let { position ->
                    pageNum = 1 // 切换Tab重置页码
                    statusList.clear()
                    adapter.notifyDataSetChanged()
                    loadCarList(position == 1)
                }
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun loadCarList(expired: Boolean = false) {
        carInfoViewModel.getOutdate(expired, pageNum, pageSize, statusListStateFlow)
    }
}