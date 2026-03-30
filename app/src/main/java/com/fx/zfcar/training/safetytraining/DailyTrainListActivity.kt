package com.fx.zfcar.training.safetytraining

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fx.zfcar.databinding.ActivityDailyTrainListBinding
import com.fx.zfcar.net.CoursewareItem
import com.fx.zfcar.net.CoursewareListData
import com.fx.zfcar.net.PostSignImgData
import com.fx.zfcar.net.TrainingPublicPlan
import com.fx.zfcar.training.adapter.DailyCourseAdapter
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.DateUtil.secondToDate
import com.fx.zfcar.util.SPUtils
import com.fx.zfcar.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DailyTrainListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDailyTrainListBinding
    private val viewModel by viewModels<SafetyTrainingViewModel>()

    // ====================== 所有 StateFlow 都在 Activity ======================
    private val courseListState = MutableStateFlow<ApiState<CoursewareListData>>(ApiState.Loading)
    private val signState = MutableStateFlow<ApiState<PostSignImgData>>(ApiState.Loading)
    private val configTimeState = MutableStateFlow<ApiState<Int>>(ApiState.Loading)

    // ====================== 页面参数 ======================
    private var id = ""
    private var name = ""
    private var fromUrl = ""

    // ====================== 分页 ======================
    private var page = 1
    private var totalPage = 1
    private var showLoadMore = 1
    private var dataList = 0
    private val trainList = mutableListOf<CoursewareItem>()
    private var trainAbout: TrainingPublicPlan? = null

    // ====================== 定时器 ======================
    private var timer5: CountDownTimer? = null
    private lateinit var adapter: DailyCourseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyTrainListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initAdapter()
        parseIntent()
        observeAllStateFlow()
    }

    private fun initView() {
        binding.ivBack.setOnClickListener {
            finish()
            finish() // 返回两层
        }
    }

    private fun initAdapter() {
        adapter = DailyCourseAdapter(this) { item ->
            val intent = Intent(this@DailyTrainListActivity, StudyDetailActivity::class.java)
            intent.putExtra("safetyPlanId", trainAbout?.id.toString())
            intent.putExtra("subjectId", item.id.toString())
            intent.putExtra("name", item.name)
            intent.putExtra("content", item.content)
            startActivity(intent)
        }
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        // 上拉加载更多
        binding.recycler.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lm = recyclerView.layoutManager as LinearLayoutManager
                val last = lm.findLastCompletelyVisibleItemPosition()
                val total = adapter.itemCount

                if (last >= total - 2 && dy > 0 && showLoadMore == 1 && page < totalPage) {
                    page++
                    loadList()
                }
            }
        })
    }

    private fun parseIntent() {
        fromUrl = intent.getStringExtra("fromUrl") ?: ""
        if (fromUrl == "sign") {
            name = SPUtils.get("dailyName")
            id = SPUtils.get("id")
            val signFile = SPUtils.get("dailySign")
            viewModel.postSignImg(id, signFile, signState)
        } else {
            name = intent.getStringExtra("name") ?: ""
            id = intent.getStringExtra("id") ?: ""
        }
        binding.tvTitle.text = name
    }

    private fun observeAllStateFlow() {
        // 列表
        lifecycleScope.launch {
            courseListState.collectLatest { state ->
                when (state) {
                    is ApiState.Success -> {
                        state.data?.let {
                            handleListSuccess(state.data)
                        }
                    }
                    is ApiState.Error -> handleListError(state.msg)
                    else -> {}
                }
            }
        }

        // 签名
        lifecycleScope.launch {
            signState.collectLatest { state ->
                if (state is ApiState.Error) {
                    showToast(state.msg)
                    finish()
                }
            }
        }

        // 超时时间
        lifecycleScope.launch {
            configTimeState.collectLatest { state ->
                if (state is ApiState.Success) {
                    state.data?.let {
                        setupAutoRestart((state.data * 1000).toLong())
                    }
                }
            }
        }
    }

    private fun loadList() {
        viewModel.getCoursewareList(page.toString(), id, courseListState)
    }

    private fun handleListSuccess(data: CoursewareListData) {
        trainAbout = data.row
        totalPage = data.total

        binding.tvTotalClass.text = "${data.row.sublongtime}课时"
        binding.tvProgress.text = "${getProgress(data.list)}%"

        if (page == 1) trainList.clear()

        data.list.forEach { item ->
            item.time = secondToDate(item.longtime)
            item.studytime_text = secondToDate(item.studytime)
            trainList.add(item)
        }

        adapter.submitList(trainList.toList())

        showLoadMore = if (data.list.size < 8) 0 else 1
        binding.tvLoadMore.visibility = if (showLoadMore == 1 && page < totalPage) View.VISIBLE else View.GONE

        dataList = if (trainList.isEmpty()) 1 else 0
        binding.emptyLayout.visibility = if (dataList == 1) View.VISIBLE else View.GONE
    }

    private fun handleListError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun getProgress(list: List<CoursewareItem>): Int {
        if (list.isEmpty()) return 0
        val total = list.sumOf { it.longtime }
        val study = list.sumOf { it.studytime }
        return if (total == 0) 0 else (study * 100 / total)
    }

    fun stopTime(view: View) {
        timer5?.cancel()
        viewModel.getConfigTime(configTimeState)
    }

    private fun setupAutoRestart(millis: Long) {
        timer5?.cancel()
        timer5 = object : CountDownTimer(millis, 1000) {
            override fun onTick(p0: Long) {}
            override fun onFinish() {
                SPUtils.clear()
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        page = 1
        trainList.clear()
        loadList()
        viewModel.getConfigTime(configTimeState)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer5?.cancel()
    }
}