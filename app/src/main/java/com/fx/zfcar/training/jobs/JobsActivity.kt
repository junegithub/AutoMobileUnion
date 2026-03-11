package com.fx.zfcar.training.jobs

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fx.zfcar.R
import com.fx.zfcar.databinding.ActivityJobsBinding
import com.fx.zfcar.net.CompanyListData
import com.fx.zfcar.net.CompanyListRow
import com.fx.zfcar.net.MyJobItem
import com.fx.zfcar.net.MyJobListData
import com.fx.zfcar.training.adapter.JobInfoAdapter
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class InfoListItem {
    data class CompanyItem(val data: CompanyListRow) : InfoListItem()
    data class JobItem(val data: MyJobItem) : InfoListItem()
}

// 页面参数模型
data class Params(
    var page: Int = 1,
    var type: Int = 0 // 0=信息广场 1=我的
)

class JobsActivity : AppCompatActivity() {
    // 视图绑定
    private lateinit var binding: ActivityJobsBinding

    // ViewModel
    private val infoViewModel by viewModels<SafetyTrainingViewModel>()

    // 页面参数
    private val params = Params(page = 1, type = 0)

    // 统一列表数据（适配两种类型）
    private val infoList = mutableListOf<InfoListItem>()
    private lateinit var infoAdapter: JobInfoAdapter

    // 分页控制
    private var totalPage = 0
    private var isLoading = false

    // 标签列表
    private val tabList = listOf("信息广场", "我的")

    // 时间格式化工具
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    private val originalFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA)

    // 两个不同的 StateFlow（对应不同返回类型）
    private val companyListStateFlow = MutableStateFlow<ApiState<CompanyListData>>(ApiState.Loading)
    private val myJobListStateFlow = MutableStateFlow<ApiState<MyJobListData>>(ApiState.Loading)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        observeStateFlows()
        getInfo()
    }

    // 初始化视图
    private fun initView() {
        PressEffectUtils.setCommonPressEffect(binding.ivBack)
        PressEffectUtils.setCommonPressEffect(binding.btnRelease)
        // 1. 返回按钮
        binding.ivBack.setOnClickListener { goBack() }

        // 2. 标题
        binding.tvTitle.text = "综合信息"

        // 3. 标签栏
        initTabLayout()

        // 4. 列表
        initRecyclerView()

        // 5. 发布按钮
        binding.btnRelease.setOnClickListener { release() }

        // 6. 发布按钮显示控制
        updateReleaseButtonVisibility()
    }

    // 初始化标签栏
    private fun initTabLayout() {
        binding.tvTab1.text = tabList[0]
        binding.tvTab2.text = tabList[1]
        updateTabSelection(params.type)

        binding.tvTab1.setOnClickListener {
            if (params.type != 0) {
                sectionChange(0)
            }
        }

        binding.tvTab2.setOnClickListener {
            if (params.type != 1) {
                sectionChange(1)
            }
        }
    }

    // 初始化列表（使用 ViewBinding 优化适配器）
    private fun initRecyclerView() {
        infoAdapter = JobInfoAdapter(infoList) { item ->
            detail(item)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@JobsActivity)
            adapter = infoAdapter

            // 加载更多监听
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastPos = layoutManager.findLastVisibleItemPosition()

                    if (lastPos == infoList.size - 1 && !isLoading) {
                        getMore()
                    }
                }
            })
        }
    }

    // 监听两个 StateFlow
    private fun observeStateFlows() {
        // 监听信息广场列表状态
        lifecycleScope.launch {
            companyListStateFlow.drop(1)
                .collect { state ->
                handleCommonState(state) { data ->
                    // 处理信息广场数据
                    totalPage = data.total
                    data.rows?.let {
                        val formattedItems = data.rows.map { row ->
                            // 格式化时间
                            val formattedTime = try {
                                val date = originalFormat.parse(row.createtime)
                                timeFormat.format(date ?: Date())
                            } catch (e: Exception) {
                                row.createtime
                            }
                            // 封装为统一类型
                            InfoListItem.CompanyItem(row.copy(createtime = formattedTime))
                        }
                        infoList.addAll(formattedItems)
                        infoAdapter.notifyDataSetChanged()
                    }
                }
            }
        }

        // 监听我的信息列表状态
        lifecycleScope.launch {
            myJobListStateFlow.drop(1)
                .collect { state ->
                handleCommonState(state) { data ->
                    // 处理我的信息数据
                    totalPage = data.total
                    data.rows?.let {
                        val items = data.rows.map { row ->
                            InfoListItem.JobItem(row)
                        }
                        infoList.addAll(items)
                        infoAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    // 通用状态处理方法
    private fun <T> handleCommonState(
        state: ApiState<T>,
        onSuccess: (T) -> Unit
    ) {
        when (state) {
            is ApiState.Loading -> {
                isLoading = true
                binding.loadingView.visibility = View.VISIBLE
            }
            is ApiState.Success -> {
                isLoading = false
                binding.loadingView.visibility = View.GONE
                state.data?.let {
                    onSuccess(state.data)
                }
            }
            is ApiState.Error -> {
                isLoading = false
                binding.loadingView.visibility = View.GONE
                showToast(state.msg)
            }
            is ApiState.Idle -> {}
        }
    }

    // 标签切换
    private fun sectionChange(index: Int) {
        updateTabSelection(index)
        params.type = index
        params.page = 1

        // 清空列表
        infoList.clear()
        infoAdapter.notifyDataSetChanged()

        // 重新加载数据
        getInfo()

        // 更新发布按钮
        updateReleaseButtonVisibility()
    }

    // 更新标签选中状态
    private fun updateTabSelection(selectedIndex: Int) {
        when (selectedIndex) {
            0 -> {
                binding.tvTab1.setTextColor(resources.getColor(R.color.colorPrimary, theme))
                binding.tvTab1.setBackgroundResource(R.drawable.tab_selected_bg)
                binding.tvTab2.setTextColor(resources.getColor(R.color.text_gray, theme))
                binding.tvTab2.setBackgroundResource(R.drawable.bg_tab_unselected)
            }
            1 -> {
                binding.tvTab1.setTextColor(resources.getColor(R.color.text_gray, theme))
                binding.tvTab1.setBackgroundResource(R.drawable.bg_tab_unselected)
                binding.tvTab2.setTextColor(resources.getColor(R.color.colorPrimary, theme))
                binding.tvTab2.setBackgroundResource(R.drawable.tab_selected_bg)
            }
        }
    }

    // 更新发布按钮显示
    private fun updateReleaseButtonVisibility() {
        binding.btnRelease.visibility = if (params.type == 1) View.VISIBLE else View.GONE
    }

    // 获取数据（区分不同请求）
    private fun getInfo() {
        if (isLoading) return

        if (params.type == 0) {
            // 信息广场
            infoViewModel.getCompanyList(params.page, params.type, companyListStateFlow)
        } else {
            // 我的信息
            infoViewModel.getMyJobList(params.page, params.type, myJobListStateFlow)
        }
    }

    // 加载更多
    private fun getMore() {
        if (params.page < totalPage) {
            params.page++
            getInfo()
        } else {
            Toast.makeText(this, "没有更多消息了", Toast.LENGTH_SHORT).show()
        }
    }

    // 发布信息
    private fun release() {
        startActivity(Intent(this, PublishJobActivity::class.java))
    }

    // 详情跳转（区分不同类型）
    private fun detail(item: InfoListItem) {
        val intent = when (item) {
            is InfoListItem.CompanyItem -> {
                // 信息广场详情
//                Intent(this, JobDetailActivity::class.java).apply {
//                    putExtra("data", Gson().toJson(item.data))
//                }
            }
            is InfoListItem.JobItem -> {
                // 我的信息详情
//                Intent(this, JobMyDetailActivity::class.java).apply {
//                    putExtra("data", Gson().toJson(item.data))
//                }
            }
        }
//        startActivity(intent)
    }

    private fun goBack() {
        finish()
    }
}