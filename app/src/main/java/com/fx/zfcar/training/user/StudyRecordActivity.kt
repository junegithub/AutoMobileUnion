package com.fx.zfcar.training.user

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fx.zfcar.databinding.ActivityStudyRecordBinding
import com.fx.zfcar.net.StudyListData
import com.fx.zfcar.net.StudyRecord
import com.fx.zfcar.training.adapter.StudyRecordAdapter
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.DateUtil
import com.fx.zfcar.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlin.getValue

class StudyRecordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudyRecordBinding
    private val viewModel by viewModels<SafetyTrainingViewModel>()
    private lateinit var studyAdapter: StudyRecordAdapter

    // 页面参数
    private var safetyPlanId: String = ""
    private var searchVal: String = ""

    // 分页参数
    private var page = 1
    private var totalPage = 1

    // 加载更多状态
    private enum class LoadMoreState { LOAD_MORE, LOADING, NO_MORE }
    private var loadMoreState = LoadMoreState.LOAD_MORE

    // 数据缓存
    private val studyList = mutableListOf<StudyRecord>()

    // API请求状态Flow（完全由Activity托管）
    private val studyListState = MutableStateFlow<ApiState<StudyListData>>(ApiState.Loading)

    // 加载状态文字
    private val loadTextMap = mapOf(
        LoadMoreState.LOAD_MORE to "上拉加载更多",
        LoadMoreState.LOADING to "正在加载",
        LoadMoreState.NO_MORE to "没有更多了"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudyRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取页面参数
        safetyPlanId = intent.getStringExtra("id") ?: ""

        // 初始化列表
        initRecyclerView()

        // 初始化事件监听
        initListeners()

        // 监听请求状态
        observeStudyListState()

        // 首次加载数据
        loadStudyList()
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        studyAdapter = StudyRecordAdapter(this)

        binding.rvStudyList.apply {
            layoutManager = LinearLayoutManager(this@StudyRecordActivity)
            adapter = studyAdapter

            // 上拉加载更多监听
            addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    // 上拉到底部且可以加载更多
                    if (lastVisibleItemPosition == totalItemCount - 1
                        && dy > 0
                        && totalItemCount > 0
                        && loadMoreState == LoadMoreState.LOAD_MORE) {

                        loadMoreData()
                    }
                }
            })
        }
    }

    /**
     * 初始化事件监听
     */
    private fun initListeners() {
        // 返回按钮
        binding.ivBack.setOnClickListener { finish() }

        // 搜索按钮
        binding.tvSearch.setOnClickListener { performSearch() }

        // 搜索框回车
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    /**
     * 执行搜索
     */
    private fun performSearch() {
        searchVal = binding.etSearch.text.toString().trim()

        // 重置分页和数据
        page = 1
        totalPage = 1
        studyList.clear()
        loadMoreState = LoadMoreState.LOAD_MORE

        // 重新加载
        loadStudyList()
    }

    /**
     * 加载学习记录
     */
    private fun loadStudyList(isLoadMore: Boolean = false) {
        if (isLoadMore) {
            loadMoreState = LoadMoreState.LOADING
            binding.tvLoadMore.text = loadTextMap[loadMoreState]
        }

        // 调用ViewModel的极简函数
        viewModel.getStudyList(
            searchname = searchVal,
            training_safetyplan_id = safetyPlanId,
            page = page,
            stateFlow = studyListState
        )
    }

    /**
     * 加载更多
     */
    private fun loadMoreData() {
        if (page >= totalPage) {
            loadMoreState = LoadMoreState.NO_MORE
            binding.tvLoadMore.text = loadTextMap[loadMoreState]
            return
        }

        page++
        loadStudyList(isLoadMore = true)
    }

    /**
     * 监听请求状态
     */
    private fun observeStudyListState() {
        lifecycleScope.launch {
            studyListState.drop(1)
                .collect { apiState ->
                when (apiState) {
                    is ApiState.Loading -> {
                        // 加载中状态
                        binding.llEmptyView.visibility = View.GONE
                    }

                    is ApiState.Success -> {
                        apiState.data?.let {
                            handleLoadSuccess(apiState.data)
                        }
                    }

                    is ApiState.Error -> {
                        handleLoadError(apiState.msg)
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * 处理加载成功
     */
    private fun handleLoadSuccess(data: StudyListData) {
        // 更新总页数
        totalPage = data.total

        // 格式化时长并添加数据
        val formattedList = data.rows.map { record ->
            record.studytime = DateUtil.secondToDate(record.longtime.toInt())
            record
        }

        // 更新数据列表
        if (page == 1) {
            studyList.clear()
        }
        studyList.addAll(formattedList)

        // 更新适配器
        studyAdapter.updateData(studyList)

        // 处理空数据
        if (studyList.isEmpty()) {
            binding.llEmptyView.visibility = View.VISIBLE
            binding.tvLoadMore.visibility = View.GONE
        } else {
            binding.llEmptyView.visibility = View.GONE

            // 更新加载更多状态
            loadMoreState = if (page < totalPage && data.rows.size >= 6) {
                LoadMoreState.LOAD_MORE
            } else {
                LoadMoreState.NO_MORE
            }

            binding.tvLoadMore.visibility = View.VISIBLE
            binding.tvLoadMore.text = loadTextMap[loadMoreState]
        }
    }

    /**
     * 处理加载失败
     */
    private fun handleLoadError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        // 恢复加载更多状态
        if (loadMoreState == LoadMoreState.LOADING) {
            loadMoreState = LoadMoreState.LOAD_MORE
            binding.tvLoadMore.text = loadTextMap[loadMoreState]
        }

        // 空数据处理
        if (studyList.isEmpty()) {
            binding.llEmptyView.visibility = View.VISIBLE
        }
    }
}