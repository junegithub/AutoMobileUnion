package com.fx.zfcar.car.status

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.fx.zfcar.car.adapter.StatusAdapter
import com.fx.zfcar.car.viewmodel.CarInfoViewModel
import com.fx.zfcar.net.BaseCarInfo
import com.fx.zfcar.net.CarStatusDetailItem
import com.fx.zfcar.pages.EventData
import com.fx.zfcar.pages.MainActivity
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.viewmodel.ApiState
import com.fx.zfcar.databinding.ActivityDeviceStatusListBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import kotlin.getValue

class DeviceStatusListActivity : AppCompatActivity() {

    // 伴生对象：相当于Java的static静态成员，其他类可直接访问
    companion object {
        // const val 用于编译期确定的字符串常量（必须是字面量，不能是方法返回值）
        const val KEY_CAR_STATUS_TITLE = "key_car_status_title"
        const val KEY_CAR_STATUS_TYPE = "key_car_status_type"
        private const val REFRESH_INTERVAL_MS = 30_000L
    }

    private lateinit var binding: ActivityDeviceStatusListBinding
    private lateinit var statusAdapter: StatusAdapter
    private val statusList = mutableListOf<CarStatusDetailItem>()
    private val carInfoViewModel by viewModels<CarInfoViewModel>()
    private val statusListStateFlow = MutableStateFlow<ApiState<List<CarStatusDetailItem>>>(ApiState.Idle)
    private var pageNum: Int = 1
    private val pageSize = 50
    private var carType = ""
    private var loadFromMore: Boolean = false
    private var reachedEnd: Boolean = false
    private lateinit var layoutManager: LinearLayoutManager
    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            if (!loadFromMore) {
                pageNum = 1
                loadData()
            }
            refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceStatusListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carType = intent.getStringExtra(KEY_CAR_STATUS_TYPE).toString()
        initView()
        loadData()
        initListener()
    }

    private fun initView() {
        binding.titleLayout.tvTitle.text = intent.getStringExtra(KEY_CAR_STATUS_TITLE)
        // 初始化RecyclerView
        statusAdapter = StatusAdapter()
        statusAdapter.submitList(statusList)
        statusAdapter.setOnDebouncedItemClick { adapter, view, position ->
            val item = statusList[position]
            EventBus.getDefault().postSticky(
                EventData(
                    EventData.EVENT_CAR_DETAIL,
                    BaseCarInfo(
                        item.carId, item.carNum,
                        item.lon, item.lat, 0
                    )
                )
            )
            startActivity(Intent(this@DeviceStatusListActivity, MainActivity::class.java))
            finish()
        }
        layoutManager = LinearLayoutManager(this@DeviceStatusListActivity)
        binding.rvStatusList.apply {
            layoutManager = this@DeviceStatusListActivity.layoutManager
            adapter = statusAdapter
        }
        initScrollPagination()

        lifecycleScope.launch {
            statusListStateFlow.collect { state ->
                when (state) {
                    is ApiState.Loading -> {
                        // 显示进度框
                    }
                    is ApiState.Success -> {
                        val data = state.data.orEmpty()
                        if (!loadFromMore) {
                            statusList.clear()
                        }
                        statusList.addAll(data)
                        reachedEnd = data.size < pageSize
                        statusAdapter.submitList(statusList.toList())
                        binding.llEmptyView.visibility = if (statusList.isEmpty()) View.VISIBLE else View.GONE
                        binding.rvStatusList.visibility = if (statusList.isEmpty()) View.GONE else View.VISIBLE
                        loadFromMore = false
                    }
                    is ApiState.Error -> {
                        binding.llEmptyView.visibility = if (statusList.isEmpty()) View.VISIBLE else View.GONE
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
    }

    private fun initScrollPagination() {
        binding.rvStatusList.clearOnScrollListeners()
        binding.rvStatusList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0 || loadFromMore || reachedEnd) return
                if (layoutManager.findLastVisibleItemPosition() >= statusAdapter.itemCount - 2 && statusAdapter.itemCount > 0) {
                    pageNum++
                    loadFromMore = true
                    loadData()
                }
            }
        })
    }

    private fun loadData() {
        carInfoViewModel.getCarStatusByType(carType, pageNum, pageSize, statusListStateFlow)
    }


    private fun initListener() {
        PressEffectUtils.setCommonPressEffect(binding.titleLayout.tvTitle)
        // 返回按钮点击
        binding.titleLayout.tvTitle.setOnClickListener { finish() }
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

    override fun onDestroy() {
        super.onDestroy()
        refreshHandler.removeCallbacksAndMessages(null)
    }
}
