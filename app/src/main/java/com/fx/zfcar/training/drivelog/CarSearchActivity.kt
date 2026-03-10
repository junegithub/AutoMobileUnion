package com.fx.zfcar.training.drivelog

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fx.zfcar.databinding.ActivityCarSearchBinding
import com.fx.zfcar.net.CarNumSearchData
import com.fx.zfcar.net.CarNumSearchItem
import com.fx.zfcar.training.adapter.CarSearchAdapter
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlin.getValue

class CarSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCarSearchBinding

    // 表单数据
    private val form = FormData()

    // 车辆列表（适配新数据模型）
    private val carsList = mutableListOf<CarNumSearchItem>()

    private val trainingViewModel by viewModels<SafetyTrainingViewModel>()
    private var searchStateFlow = MutableStateFlow<ApiState<CarNumSearchData>>(ApiState.Idle)

    // 适配器（适配新数据模型）
    private val carAdapter by lazy {
        CarSearchAdapter { car ->
            confirmCar(car)
        }
    }

    // 来源页面
    private var fromUrl: String? = null

    // 总页数
    private var totalPage = 0

    // 是否正在加载
    private var isLoading = false
    private var mIsLoadMore = false

    // 每页条数
    private val pageSize = 10

    // 表单数据类
    data class FormData(
        var carnum: String = "",
        var page: Int = 1
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取来源页面参数
        fromUrl = intent.getStringExtra("from")

        binding.layoutTitle.tvTitle.text="车辆搜索"
        PressEffectUtils.setCommonPressEffect(binding.layoutTitle.tvTitle)
        binding.layoutTitle.tvTitle.setOnClickListener {
            finish()
        }
        initRecyclerView()
        initListeners()
    }

    /**
     * 初始化列表
     */
    private fun initRecyclerView() {
        binding.rvCars.apply {
            layoutManager = LinearLayoutManager(this@CarSearchActivity)
            adapter = carAdapter

            // 滑动监听（触底加载）
            addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                    // 触底加载更多（优化：增加加载状态判断）
                    if (!isLoading && lastVisibleItemPosition == carsList.size - 1 && carsList.isNotEmpty()) {
                        lowerBottom()
                    }
                }
            })
        }
    }

    /**
     * 初始化监听
     */
    private fun initListeners() {
        // 输入框监听
        binding.etCarNum.addTextChangedListener { text ->
            form.carnum = text.toString().trim()
        }

        // 搜索按钮监听
        binding.btnSearch.setOnClickListener {
            searchConfirm()
        }

        lifecycleScope.launch {
            searchStateFlow.drop(1)
                .collect { state ->
                    when (state) {
                        is ApiState.Loading -> {
                        }
                        is ApiState.Success -> {
                            if (state.data != null) {
                                val data = state.data
                                // 计算总页数（适配新数据模型）
                                totalPage = data.calculateTotalPage(pageSize)

                                if (mIsLoadMore) {
                                    // 加载更多，追加数据
                                    carsList.addAll(data.rows)
                                } else {
                                    // 重新搜索，清空原有数据
                                    carsList.clear()
                                    carsList.addAll(data.rows)
                                    binding.tvLoadMore.visibility = View.GONE
                                }

                                // 更新列表（使用新数据模型）
                                carAdapter.submitList(carsList.toList())

                                // 处理空数据情况
                                if (carsList.isEmpty()) {
                                    showToast("未找到相关车辆")
                                    binding.tvLoadMore.text = "暂无数据"
                                    binding.tvLoadMore.visibility = View.VISIBLE
                                }
                            } else {
                                showToast("搜索失败，请重试")
                                binding.tvLoadMore.text = "加载失败"
                            }
                        }
                        is ApiState.Error -> {
                            showToast("搜索失败，请重试")
                            binding.tvLoadMore.text = "加载失败"
                        }
                        else -> {}
                    }
                }
        }
    }

    /**
     * 确认选择车辆（适配新数据模型）
     */
    private fun confirmCar(car: CarNumSearchItem) {
        val carNumResult = car.carnum
        // 可选：返回车辆ID
        val carId = car.id

        // 构建返回数据
        val intent = Intent().apply {
            putExtra("carNum", carNumResult)
            putExtra("carId", carId) // 新增返回车辆ID
        }

        // 设置返回结果
        setResult(RESULT_OK, intent)

        // 关闭当前页面
        finish()
    }

    /**
     * 触底加载更多
     */
    private fun lowerBottom() {
        if (form.page < totalPage && !isLoading) {
            form.page++
            search(isLoadMore = true)
        } else {
            showToast("没有更多的数据了")
            binding.tvLoadMore.visibility = View.VISIBLE
        }
    }

    /**
     * 搜索车辆（适配新数据模型）
     */
    private fun search(isLoadMore: Boolean = false) {
        if (form.carnum.isBlank() && !isLoadMore) {
            showToast("请输入车牌号")
            return
        }
        mIsLoadMore = isLoadMore

        isLoading = true
        // 显示加载中状态（可选）
        binding.tvLoadMore.text = "加载中..."
        binding.tvLoadMore.visibility = View.VISIBLE

        // 协程发起网络请求
        trainingViewModel.carnumSearch(form.carnum, form.page.toString(), searchStateFlow)
    }

    /**
     * 搜索确认
     */
    private fun searchConfirm() {
        if (form.carnum.isBlank()) {
            showToast("请输入车牌号")
            return
        }

        // 重置分页
        form.page = 1
        carsList.clear()

        // 执行搜索
        search()
    }
}