package com.fx.zfcar.training.safetytraining

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fx.zfcar.R
import com.fx.zfcar.databinding.ActivityTrainCourseBinding
import com.fx.zfcar.net.BeforeCoursewareItem
import com.fx.zfcar.net.BeforePlanDetail
import com.fx.zfcar.net.BeforeSubCoursewareListData
import com.fx.zfcar.training.adapter.TrainCourseAdapter
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.DateUtil
import com.fx.zfcar.util.SPUtils
import com.fx.zfcar.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue

/**
 * 培训课程列表页面
 * 3. 培训概览展示（总课时、学习进度）
 * 4. 课程状态展示、开始学习跳转
 * 5. 签到处理、定时重启逻辑
 */
class TrainCourseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTrainCourseBinding
    private val viewModel by viewModels<SafetyTrainingViewModel>()
    private lateinit var courseAdapter: TrainCourseAdapter

    // 页面参数
    private var name: String = ""
    private var id: String = ""
    private var fromUrl: String = ""

    // 分页参数
    private var page = 1
    private var totalPage = 1
    private var showLoadMore = 1
    private var dataList = 0

    // 加载状态
    private enum class LoadMoreState { LOAD_MORE, LOADING, NO_MORE }
    private var loadMoreState = LoadMoreState.LOAD_MORE

    // 数据缓存
    private val courseList = mutableListOf<BeforeCoursewareItem>()
    private var trainAbout: BeforePlanDetail? = null

    // StateFlow状态管理
    private val courseListState = MutableStateFlow<ApiState<BeforeSubCoursewareListData>>(ApiState.Loading)
    private val signState = MutableStateFlow<ApiState<Boolean>>(ApiState.Loading)
    private val timeConfigState = MutableStateFlow<ApiState<Int>>(ApiState.Loading)

    // 定时器
    private var timer5: android.os.CountDownTimer? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainCourseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取页面参数
        getIntentParams()

        // 设置标题
        binding.tvTitle.text = name

        // 初始化列表
        initRecyclerView()

        // 初始化刷新控件
        initSwipeRefresh()

        // 监听状态变化
        observeStates()

        // 处理从签字页面返回的逻辑
        handleSignReturn()

        // 初始化课程列表
        loadCourseList()
    }

    /**
     * 获取页面参数
     */
    private fun getIntentParams() {
        intent?.apply {
            fromUrl = getStringExtra("fromUrl") ?: ""
            name = getStringExtra("name") ?: ""
            id = getStringExtra("id") ?: ""
        }

        // 从SharedPreferences获取参数（签字返回场景）
        if (fromUrl == "sign") {
            name = SPUtils.get("beforeName")
            id = SPUtils.get("id")
        } else {
            // 保存页面滚动位置
            SPUtils.save("pageScoll", 0)
        }
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        courseAdapter = TrainCourseAdapter(this) { course ->
            // 开始学习点击事件
            startStudy(course)
        }

        binding.rvCourseList.apply {
            layoutManager = LinearLayoutManager(this@TrainCourseActivity)
            adapter = courseAdapter

            // 上拉加载更多监听
            addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    if (lastVisibleItem == totalItemCount - 1
                        && dy > 0
                        && totalItemCount > 0
                        && loadMoreState == LoadMoreState.LOAD_MORE
                        && page < totalPage) {

                        loadMoreData()
                    }
                }
            })
        }
    }

    /**
     * 初始化下拉刷新
     */
    private fun initSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            // 下拉刷新
            page = 1
            courseList.clear()
            loadCourseList()
        }

        // 设置刷新颜色
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary)
    }

    /**
     * 监听所有StateFlow状态
     */
    private fun observeStates() {
        // 监听课程列表状态
        lifecycleScope.launch {
            courseListState.collectLatest { apiState ->
                when (apiState) {
                    is ApiState.Loading -> {
                        binding.swipeRefreshLayout.isRefreshing = true
                    }

                    is ApiState.Success -> {
                        apiState.data?.let {
                            handleCourseListSuccess(apiState.data)
                        }
                    }

                    is ApiState.Error -> {
                        handleCourseListError(apiState.msg)
                    }
                    else -> {}
                }
            }
        }

        // 监听签到状态
        lifecycleScope.launch {
            signState.collectLatest { apiState ->
                when (apiState) {
                    is ApiState.Success -> {
                        if (apiState.data == false) {
                            finish()
                        } else {
                            // 检查是否需要跳转到考试页面
                            checkExamRedirect()
                        }
                    }

                    is ApiState.Error -> {
                        showToast(apiState.msg)
                        finish()
                    }

                    else -> {}
                }
            }
        }

        // 监听时间配置状态
        lifecycleScope.launch {
            timeConfigState.collectLatest { apiState ->
                when (apiState) {
                    is ApiState.Success -> {
                        val timeValue = apiState.data ?: 0
                        setupAutoRestartTimer(timeValue * 1000L)
                    }

                    is ApiState.Error -> {
                        showToast(apiState.msg)
                    }

                    else -> {}
                }
            }
        }
    }

    /**
     * 处理从签字页面返回的逻辑
     */
    private fun handleSignReturn() {
        if (fromUrl == "sign") {
            // 提交签到
            val signFile = SPUtils.get("beforeSign")
            viewModel.postBeforeSign(id, signFile, signState)
        }
    }

    /**
     * 加载课程列表
     */
    private fun loadCourseList(isLoadMore: Boolean = false) {
        if (isLoadMore) {
            loadMoreState = LoadMoreState.LOADING
            updateLoadMoreText()
        }

        viewModel.getBeforeSubCoursewareList(page.toString(), id, "", courseListState)
    }

    /**
     * 加载更多数据
     */
    private fun loadMoreData() {
        if (page >= totalPage) {
            loadMoreState = LoadMoreState.NO_MORE
            updateLoadMoreText()
            showToast("没有更多的数据了")
            return
        }

        page++
        loadCourseList(isLoadMore = true)
    }

    /**
     * 处理课程列表加载成功
     */
    private fun handleCourseListSuccess(data: BeforeSubCoursewareListData) {
        binding.swipeRefreshLayout.isRefreshing = false

        // 更新培训概览
        trainAbout = data.row
        updateTrainAboutUI()

        // 保存是否需要签字
        SPUtils.save("needBeforeSign", data.row.issign)

        // 更新分页信息
        totalPage = data.total

        if (data.list.isNotEmpty()) {
            // 格式化时长并添加数据
            val formattedList = data.list.map { course ->
                course.time = DateUtil.secondToDate(course.longtime)
                course.studytime_text = DateUtil.secondToDate(course.studytime)
                course
            }

            // 更新列表数据
            if (page == 1) {
                courseList.clear()
            }
            courseList.addAll(formattedList)
            courseAdapter.submitList(courseList.toList())

            // 控制加载更多显示
            if (data.list.size < 8) {
                showLoadMore = 0
                binding.tvLoadMore.visibility = View.GONE
            } else {
                showLoadMore = 1
                binding.tvLoadMore.visibility = View.VISIBLE
            }

            // 更新加载状态
            loadMoreState = if (page < totalPage) {
                LoadMoreState.LOAD_MORE
            } else {
                LoadMoreState.NO_MORE
            }

            updateLoadMoreText()
            dataList = 0
            binding.llEmptyView.visibility = View.GONE
        } else {
            // 无数据
            showLoadMore = 0
            binding.tvLoadMore.visibility = View.GONE
            dataList = 1

            if (page == 1) {
                courseAdapter.submitList(emptyList())
                binding.llEmptyView.visibility = View.VISIBLE
            }
        }
    }

    /**
     * 处理课程列表加载失败
     */
    private fun handleCourseListError(message: String) {
        binding.swipeRefreshLayout.isRefreshing = false
        showToast(message)

        // 恢复加载状态
        if (loadMoreState == LoadMoreState.LOADING) {
            loadMoreState = LoadMoreState.LOAD_MORE
            updateLoadMoreText()
        }

        // 空数据处理
        if (page == 1) {
            binding.llEmptyView.visibility = View.VISIBLE
        }
    }

    /**
     * 更新培训概览UI
     */
    private fun updateTrainAboutUI() {
        trainAbout?.apply {
            binding.tvTotalCourse.text = "${ksnum}课时"
            binding.tvProgress.text = "${progress}%"
        }
    }

    /**
     * 更新加载更多提示文字
     */
    private fun updateLoadMoreText() {
        val loadText = when (loadMoreState) {
            LoadMoreState.LOAD_MORE -> "上拉加载更多"
            LoadMoreState.LOADING -> "正在加载"
            LoadMoreState.NO_MORE -> "没有更多了"
        }
        binding.tvLoadMore.text = loadText
    }

    /**
     * 开始学习跳转
     */
    private fun startStudy(course: BeforeCoursewareItem) {
        val intent = Intent(this, CourseDetailActivity::class.java).apply {
            putExtra("safetyPlanId", id)
            putExtra("subjectId", course.id)
            putExtra("name", course.name)
            // 可添加next参数逻辑
        }
        startActivity(intent)
    }

    /**
     * 检查考试跳转
     */
    private fun checkExamRedirect() {
        val beforeExamsId = SPUtils.getInt("beforeExamsId")
        if (beforeExamsId > 0) {
            val intent = Intent(this, ExamManagerActivity::class.java).apply {
                putExtra("id", beforeExamsId.toString())
                putExtra("type", "before")
                putExtra("training_safetyplan_id", id)
                putExtra("name", SPUtils.get("beforeName"))
            }
            startActivity(intent)
        }
    }

    /**
     * 停止定时器并获取时间配置
     */
    fun stopTime(view: View) {
        timer5?.cancel()
        viewModel.getConfigTime(timeConfigState)
    }

    /**
     * 设置自动重启定时器
     */
    private fun setupAutoRestartTimer(delayMillis: Long) {
        timer5?.cancel()

        timer5 = object : android.os.CountDownTimer(delayMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                // 清空存储并重启应用
                SPUtils.clear()
                // 重启应用逻辑
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer5?.cancel()
        handler.removeCallbacksAndMessages(null)
    }
}