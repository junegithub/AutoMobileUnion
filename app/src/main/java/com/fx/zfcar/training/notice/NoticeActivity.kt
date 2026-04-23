package com.fx.zfcar.training.notice

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fx.zfcar.databinding.ActivityNoticeBinding
import com.fx.zfcar.net.NoticeData
import com.fx.zfcar.net.NoticeItem
import com.fx.zfcar.net.NoticeParams
import com.fx.zfcar.net.SectionItem
import com.fx.zfcar.training.adapter.NoticeAdapter
import com.fx.zfcar.training.viewmodel.NoticeViewModel
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.util.SPUtils
import com.fx.zfcar.viewmodel.ApiState
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class NoticeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoticeBinding

    private val sectionList = listOf(
        SectionItem("企业通知"),
        SectionItem("公文公告"),
        SectionItem("违章公告")
    )

    private val noticeParams = NoticeParams()

    private var noticeList = mutableListOf<NoticeItem>()
    private lateinit var noticeAdapter: NoticeAdapter

    private var totalPages = 0
    private var isLoading = false
    private var isLoadMore = false

    private val noticeViewModel by viewModels<NoticeViewModel>()
    private var noticeStateFlow = MutableStateFlow<ApiState<NoticeData>>(ApiState.Idle)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initListener()
        renderLoading()
        loadInfo()
    }

    /**
     * 初始化视图
     */
    private fun initView() {
        binding.titleLayout.tvTitle.text = "消息公告"
        PressEffectUtils.setCommonPressEffect(binding.titleLayout.tvTitle)
        sectionList.forEach {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(it.name))
        }
        binding.tabLayout.getTabAt(noticeParams.index)?.select()

        initRecyclerView()

        lifecycleScope.launch {
            noticeStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                        isLoading = true
                        if (!isLoadMore) {
                            renderLoading()
                            binding.tvLoadMore.visibility = View.GONE
                        }
                    }

                    is ApiState.Success -> {
                        isLoading = false
                        isLoadMore = false
                        uiState.data?.let {
                            totalPages = uiState.data.totalPage()
                            if (noticeParams.page == 1) {
                                noticeList.clear()
                            }
                            noticeList.addAll(uiState.data.rows)
                            noticeAdapter.submitList(noticeList.toList())
                            updatePageState()
                            binding.tvLoadMore.visibility =
                                if (noticeList.isNotEmpty() && noticeParams.page >= totalPages) {
                                    View.VISIBLE
                                } else {
                                    View.GONE
                                }
                        }
                    }

                    is ApiState.Error -> {
                        isLoading = false
                        if (isLoadMore && noticeParams.page > 1) {
                            noticeParams.page--
                            binding.tvLoadMore.text = "加载失败，上拉重试"
                            binding.tvLoadMore.visibility = View.VISIBLE
                        } else if (noticeList.isEmpty()) {
                            renderEmpty(uiState.msg.ifBlank { "加载失败，请重试" }, true)
                        }
                        isLoadMore = false
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }
    }

    private fun initRecyclerView() {
        noticeAdapter = NoticeAdapter(noticeParams.type) { item ->
            handleItemClick(item)
        }
        binding.rvNoticeList.apply {
            layoutManager = LinearLayoutManager(this@NoticeActivity)
            adapter = noticeAdapter
            setHasFixedSize(true)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    // 滑动到底部且还有更多数据
                    if (lastVisibleItemPosition == totalItemCount - 1 && totalItemCount > 0) {
                        loadMore()
                    }
                }
            })
        }
    }

    private fun initListener() {
        binding.titleLayout.tvTitle.setOnClickListener {
            finish()
        }
        PressEffectUtils.setCommonPressEffect(binding.btnRetry)
        binding.btnRetry.setOnClickListener {
            resetListState()
            renderLoading()
            loadInfo()
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val index = tab.position
                noticeParams.index = index
                noticeParams.type = index + 1
                noticeAdapter.updateType(noticeParams.type)
                resetListState()
                renderLoading()
                loadInfo() // 切换tab重新加载数据
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun loadInfo() {
        if (isLoading) return
        if (noticeParams.type == 3) {
            loadWarningNoticeRequest(noticeParams)
        } else {
            loadNoticeInfoRequest(noticeParams)
        }
    }

    private fun loadMore() {
        if (isLoading) return
        if (noticeParams.page < totalPages) {
            noticeParams.page++
            isLoadMore = true
            binding.tvLoadMore.text = "正在加载"
            binding.tvLoadMore.visibility = View.VISIBLE
            loadInfo()
        } else {
            if (noticeList.isNotEmpty()) {
                binding.tvLoadMore.text = "没有更多消息了"
                binding.tvLoadMore.visibility = View.VISIBLE
            }
        }
    }

    private fun handleItemClick(item: NoticeItem) {
        if (noticeParams.type == 3) {
            val intent = Intent(this, WarningDetailActivity::class.java)
            intent.putExtra("notice", Gson().toJson(item))
            startActivity(intent)
        } else {
            SPUtils.remove("noticeSign")
            SPUtils.remove("noticeId")
            SPUtils.save("noticeInfo", Gson().toJson(item))

            val intent = Intent(this, NoticeDetailActivity::class.java)
            intent.putExtra("noticeId", item.id)
            startActivity(intent)
        }
    }

    private fun loadNoticeInfoRequest(params: NoticeParams) {
        noticeViewModel.getNoticeInfo(params.page, params.index, params.type, noticeStateFlow)
    }

    private fun loadWarningNoticeRequest(params: NoticeParams) {
        noticeViewModel.warningNotice(params.page, params.index, params.type, noticeStateFlow)
    }

    private fun resetListState() {
        noticeParams.page = 1
        totalPages = 0
        isLoading = false
        isLoadMore = false
        noticeList.clear()
        noticeAdapter.submitList(emptyList())
        binding.tvLoadMore.text = "没有更多消息了"
        binding.tvLoadMore.visibility = View.GONE
    }

    private fun updatePageState() {
        if (noticeList.isEmpty()) {
            renderEmpty("暂无公告", false)
        } else {
            renderContent()
        }
    }

    private fun renderLoading() {
        binding.pbLoading.visibility = View.VISIBLE
        binding.rvNoticeList.visibility = View.GONE
        binding.llEmptyView.visibility = View.GONE
    }

    private fun renderContent() {
        binding.pbLoading.visibility = View.GONE
        binding.rvNoticeList.visibility = View.VISIBLE
        binding.llEmptyView.visibility = View.GONE
    }

    private fun renderEmpty(message: String, showRetry: Boolean) {
        binding.pbLoading.visibility = View.GONE
        binding.rvNoticeList.visibility = View.GONE
        binding.llEmptyView.visibility = View.VISIBLE
        binding.tvEmptyTip.text = message
        binding.btnRetry.visibility = if (showRetry) View.VISIBLE else View.GONE
    }
}
