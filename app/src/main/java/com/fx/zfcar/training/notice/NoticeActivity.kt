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
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.training.viewmodel.NoticeViewModel
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

    private val noticeViewModel by viewModels<NoticeViewModel>()
    private var noticeStateFlow = MutableStateFlow<ApiState<NoticeData>>(ApiState.Idle)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initListener()
        loadInfo()
    }

    /**
     * 初始化视图
     */
    private fun initView() {
        sectionList.forEach {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(it.name))
        }
        binding.tabLayout.getTabAt(noticeParams.index)?.select()

        initRecyclerView()

        lifecycleScope.launch {
            noticeStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                    }

                    is ApiState.Success -> {
                        uiState.data?.let {
                            totalPages = uiState.data.total
                            noticeList.addAll(uiState.data.rows)
                            noticeAdapter.notifyDataSetChanged()
                        }
                    }

                    is ApiState.Error -> {
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
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val index = tab.position
                noticeParams.index = index
                noticeParams.type = index + 1
                noticeParams.page = 1
                noticeList.clear()
                loadInfo() // 切换tab重新加载数据
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun loadInfo() {
        if (noticeParams.type == 3) {
            loadWarningNoticeRequest(noticeParams)
        } else {
            loadNoticeInfoRequest(noticeParams)
        }
    }

    private fun loadMore() {
        if (noticeParams.page < totalPages) {
            noticeParams.page++
            loadInfo()
        } else {
            binding.tvLoadMore.visibility = View.VISIBLE
            showToast("没有更多消息了")
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
}