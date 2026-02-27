package com.fx.zfcar.training

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fx.zfcar.databinding.ActivityNoticeBinding
import com.fx.zfcar.net.NoticeData
import com.fx.zfcar.net.NoticeItem
import com.fx.zfcar.net.NoticeParams
import com.fx.zfcar.net.NoticeResponse
import com.fx.zfcar.net.SectionItem
import com.fx.zfcar.training.adapter.NoticeAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoticeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoticeBinding

    // 分段选择器数据（对应小程序的list）
    private val sectionList = listOf(
        SectionItem("企业通知"),
        SectionItem("公文公告"),
        SectionItem("违章公告")
    )

    // 请求参数（对应小程序的params）
    private val noticeParams = NoticeParams()

    // 公告列表数据
    private var noticeList = mutableListOf<NoticeItem>()
    private lateinit var noticeAdapter: NoticeAdapter

    // 总页数（对应小程序的pagesAllNum）
    private var totalPages = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initListener()
        getInfo() // 页面显示时加载数据（对应onShow）
    }

    /**
     * 初始化视图
     */
    private fun initView() {
        // 初始化分段选择器（TabLayout）
        sectionList.forEach {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(it.name))
        }
        // 默认选中第一个tab
        binding.tabLayout.getTabAt(noticeParams.index)?.select()

        // 初始化RecyclerView
        initRecyclerView()
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        noticeAdapter = NoticeAdapter(noticeParams.type) { item ->
            // 列表项点击事件（对应goDetail/goDetails）
            handleItemClick(item)
        }
        binding.rvNoticeList.apply {
            layoutManager = LinearLayoutManager(this@NoticeActivity)
            adapter = noticeAdapter
            setHasFixedSize(true)

            // 监听滑动到底部（对应@scrolltolower='getMore'）
            addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    // 滑动到底部且还有更多数据
                    if (lastVisibleItemPosition == totalItemCount - 1 && totalItemCount > 0) {
                        getMore()
                    }
                }
            })
        }
    }

    /**
     * 初始化事件监听
     */
    private fun initListener() {
        // 返回按钮点击（对应goBack）
        binding.ivBack.setOnClickListener {
            // 跳转到train/index（这里替换为实际的TabActivity）
            finish() // 示例：返回上一页，实际需替换为switchTab逻辑
        }

        // 分段选择器切换（对应sectionChange）
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                val index = tab.position
                noticeParams.index = index
                noticeParams.type = index + 1
                noticeParams.page = 1
                noticeList.clear()
                getInfo() // 切换tab重新加载数据
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
        })
    }

    /**
     * 加载公告数据（对应getInfo）
     */
    private fun getInfo() {
        // 模拟接口请求（实际替换为 Retrofit/OkHttp）
        CoroutineScope(Dispatchers.IO).launch {
            // 区分接口地址（对应小程序的url判断）
            val response = if (noticeParams.type == 3) {
                // 违章公告接口：warningNotice
                mockWarningNoticeRequest(noticeParams)
            } else {
                // 普通公告接口：getNoticeInfo
                mockNoticeInfoRequest(noticeParams)
            }

            withContext(Dispatchers.Main) {
                totalPages = response.data.total
                // 拼接列表数据（对应...this.noticeList,...res.data.data.rows）
                noticeList.addAll(response.data.rows)
                noticeAdapter.submitList(noticeList)
            }
        }
    }

    /**
     * 加载更多（对应getMore）
     */
    private fun getMore() {
        if (noticeParams.page < totalPages) {
            noticeParams.page++
            getInfo()
        } else {
            // 显示“没有更多消息了”
            binding.tvLoadMore.visibility = View.VISIBLE
            Toast.makeText(this, "没有更多消息了", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 处理列表项点击（对应goDetail/goDetails）
     */
    private fun handleItemClick(item: NoticeItem) {
        if (noticeParams.type == 3) {
            // 违章公告：跳转到warningDetail
            val intent = android.content.Intent(this, WarningDetailActivity::class.java)
            intent.putExtra("notice", item)
            startActivity(intent)
        } else {
            // 企业/公文公告：跳转到detail
            // 清除缓存（对应uni.removeStorageSync）
            getSharedPreferences("notice", MODE_PRIVATE).edit()
                .remove("noticeSign")
                .remove("noticeId")
                .apply()
            // 存储公告信息（对应uni.setStorageSync）
            getSharedPreferences("notice", MODE_PRIVATE).edit()
                .putString("noticeInfo", Gson().toJson(item))
                .apply()
            // 跳转详情页
            val intent = android.content.Intent(this, NoticeDetailActivity::class.java)
            intent.putExtra("noticeId", item.notice_id)
            startActivity(intent)
        }
    }

    /**
     * 模拟公告接口请求（实际替换为真实接口）
     */
    private suspend fun mockNoticeInfoRequest(params: NoticeParams): NoticeResponse {
        // 模拟网络延迟
        kotlinx.coroutines.delay(500)
        // 模拟返回数据
        return NoticeResponse(
            data = NoticeData(
                rows = listOf(
                    NoticeItem(
                        notice_id = "1",
                        title = "2026年企业安全培训通知",
                        content = "请全体员工于3月1日前完成安全培训学习",
                        status = 0,
                        type = params.type
                    ),
                    NoticeItem(
                        notice_id = "2",
                        title = "关于春节放假安排的通知",
                        content = "2026年春节放假时间为2月10日-2月17日",
                        status = 1,
                        type = params.type
                    )
                ),
                total = 3 // 总页数
            )
        )
    }

    /**
     * 模拟违章公告接口请求
     */
    private suspend fun mockWarningNoticeRequest(params: NoticeParams): NoticeResponse {
        kotlinx.coroutines.delay(500)
        return NoticeResponse(
            data = NoticeData(
                rows = listOf(
                    NoticeItem(
                        notice_id = "3",
                        title = "违章提醒：鲁F726SH超速",
                        content = "您的车辆于2026-02-26在S329超速，记6分罚款200元",
                        status = 0,
                        type = 3
                    )
                ),
                total = 1
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        // 取消所有协程（避免内存泄漏）
        CoroutineScope(Dispatchers.Main).cancel()
    }
}