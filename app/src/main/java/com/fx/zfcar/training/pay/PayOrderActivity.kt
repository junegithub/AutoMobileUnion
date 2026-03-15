package com.fx.zfcar.training.pay

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fx.zfcar.databinding.ActivityPayOrderBinding
import com.fx.zfcar.net.OrderListData
import com.fx.zfcar.training.adapter.PayOrderAdapter
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 支付订单列表页面（ViewBinding完整版）
 * 特性：
 * 1. 完全使用ViewBinding，无findViewById
 * 2. StateFlow状态管理在Activity中
 * 3. 适配新OrderItem数据类
 * 4. 下拉刷新+上拉加载更多
 */
class PayOrderActivity : AppCompatActivity() {

    /**
     * 加载状态枚举
     */
    enum class LoadMoreStatus {
        LOADMORE, // 上拉加载更多
        LOADING,  // 正在加载
        NOMORE    // 没有更多了
    }

    private lateinit var binding: ActivityPayOrderBinding

    // ViewModel
    private val viewModel by viewModels<SafetyTrainingViewModel>()

    // 适配器
    private lateinit var orderAdapter: PayOrderAdapter

    private val _orderListState = MutableStateFlow<ApiState<OrderListData>>(ApiState.Idle)
    private val orderListState = _orderListState.asStateFlow()

    private var page = 1 // 当前页码
    private var totalPage = 1 // 总页数
    private var showLoadMore = 1 // 1-显示加载更多 0-隐藏
    private var dataList = 0 // 0-有数据 1-空数据
    private var loadMoreStatus: LoadMoreStatus = LoadMoreStatus.LOADMORE

    // 加载文字配置
    private val loadText = mapOf(
        LoadMoreStatus.LOADMORE to "上拉加载更多",
        LoadMoreStatus.LOADING to "正在加载",
        LoadMoreStatus.NOMORE to "没有更多了"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPayOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecyclerView()
        initListener()
        observeStateFlows()
        getList()
    }

    /**
     * 初始化RecyclerView（ViewBinding）
     */
    private fun initRecyclerView() {
        orderAdapter = PayOrderAdapter(this)

        // 使用ViewBinding设置RecyclerView
        binding.rvOrderList.apply {
            layoutManager = LinearLayoutManager(this@PayOrderActivity)
            adapter = orderAdapter

            // 上拉加载更多监听
            addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    // 上拉到底部且不是正在加载
                    if (lastVisibleItemPosition == totalItemCount - 1
                        && dy > 0
                        && loadMoreStatus != LoadMoreStatus.LOADING
                        && showLoadMore == 1) {

                        onReachBottom()
                    }
                }
            })
        }
    }

    /**
     * 初始化事件监听（ViewBinding）
     */
    private fun initListener() {
        PressEffectUtils.setCommonPressEffect(binding.ivBack)
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    /**
     * 监听StateFlow状态
     */
    private fun observeStateFlows() {
        lifecycleScope.launch {
            orderListState.collect { state ->
                handleOrderListState(state)
            }
        }
    }

    /**
     * 上拉加载更多
     */
    private fun onReachBottom() {
        if (page >= totalPage) {
            loadMoreStatus = LoadMoreStatus.NOMORE
            updateLoadMoreText()
            return
        }

        loadMoreStatus = LoadMoreStatus.LOADING
        updateLoadMoreText()

        page++
        getList()
    }

    /**
     * 获取订单列表
     */
    private fun getList() {
        dataList = 0
        viewModel.getOrderList(page, _orderListState)
    }

    /**
     * 处理订单列表状态（ViewBinding更新UI）
     */
    private fun handleOrderListState(state: ApiState<OrderListData>) {
        when (state) {
            is ApiState.Loading -> {
                // 更新加载更多文字
                binding.tvLoadMore.text = loadText[LoadMoreStatus.LOADING]
            }
            is ApiState.Success -> {
                state.data?.let {
                    val data = state.data
                    totalPage = data.total

                    if (data.list.isNotEmpty()) {
                        // 更新列表数据
                        orderAdapter.updateData(data.list, page > 1)

                        // 控制加载更多显示
                        if (data.list.size < 6) {
                            showLoadMore = 0
                            binding.tvLoadMore.visibility = View.GONE
                        } else {
                            showLoadMore = 1
                            binding.tvLoadMore.visibility = View.VISIBLE
                        }

                        // 隐藏空数据视图
                        binding.llEmptyView.visibility = View.GONE
                    } else {
                        // 无数据
                        showLoadMore = 0
                        binding.tvLoadMore.visibility = View.GONE
                        dataList = 1

                        // 显示空数据视图
                        if (page == 1) {
                            binding.llEmptyView.visibility = View.VISIBLE
                        }
                    }

                    // 恢复加载状态
                    loadMoreStatus = LoadMoreStatus.LOADMORE
                    updateLoadMoreText()
                }
            }
            is ApiState.Error -> {
                // 请求失败
                showToast(state.msg)

                // 恢复加载状态
                loadMoreStatus = LoadMoreStatus.LOADMORE
                updateLoadMoreText()
            }
            ApiState.Idle -> {}
        }
    }

    /**
     * 更新加载更多文字（ViewBinding）
     */
    private fun updateLoadMoreText() {
        binding.tvLoadMore.text = loadText[loadMoreStatus]
    }

    /**
     * 搜索功能
     */
    private fun search(value: String) {
        // 清空列表
        orderAdapter.clearData()
        // 重置分页
        totalPage = 0
        page = 1
        // 重新加载
        getList()
    }

    /**
     * 重置所有StateFlow状态
     */
    private fun resetAllStates() {
        _orderListState.update { ApiState.Idle }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 重置StateFlow状态
        resetAllStates()
    }
}