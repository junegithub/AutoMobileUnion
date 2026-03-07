package com.fx.zfcar.training

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fx.zfcar.databinding.ActivityDangerCheckBinding
import com.fx.zfcar.net.DangerCheckHistoryItem
import com.fx.zfcar.net.DangerData
import com.fx.zfcar.net.DangerDetail
import com.fx.zfcar.training.adapter.DangerCheckHistoryAdapter
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.util.SPUtils
import com.fx.zfcar.viewmodel.ApiState
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.collections.isNotEmpty

class DangerCheckActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDangerCheckBinding

    private val dangerViewModel by viewModels<SafetyTrainingViewModel>()

    private lateinit var ingItem: DangerDetail
    private lateinit var historyAdapter: DangerCheckHistoryAdapter
    private val dangerCheckFlow = MutableStateFlow<ApiState<DangerData>>(ApiState.Loading)

    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDangerCheckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化
        initView()
        initViewModel()
        loadDangerCheckData()
    }

    // 初始化视图
    private fun initView() {
        PressEffectUtils.setCommonPressEffect(binding.ivBack)
        PressEffectUtils.setCommonPressEffect(binding.btnAdd)

        // 导航栏返回按钮
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 导航栏标题
        binding.tvTitle.text = "隐患排查"

        // 初始化历史记录列表
        historyAdapter = DangerCheckHistoryAdapter { item ->
            // 历史记录项点击事件
            goHistory(item)
        }
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = historyAdapter

        // 进行中的排查项点击事件
        binding.layoutIngCheck.setOnClickListener {
            if (::ingItem.isInitialized && ingItem.fbstaus == "1") {
                goDetail("edit")
            }
        }

        // 新增按钮点击事件
        binding.btnAdd.setOnClickListener {
            goDetail("add")
        }

        // 清除photos缓存
        SPUtils.remove("photos")
    }

    // 初始化ViewModel
    private fun initViewModel() {
        // 监听数据加载状态
        lifecycleScope.launch {
            dangerCheckFlow.collect { state ->
                when (state) {
                    is ApiState.Loading -> {
                        binding.loadingView.visibility = View.VISIBLE
                    }
                    is ApiState.Success -> {
                        binding.loadingView.visibility = View.GONE
                        val data = state.data
                        state.data?.let {
                            // 处理进行中的排查项
                            if (data.dslist.fbstaus == "1") {
                                ingItem = data.dslist
                                binding.layoutIngCheck.visibility = View.VISIBLE
                            } else {
                                binding.layoutIngCheck.visibility = View.GONE
                            }

                            // 处理历史记录
                            if (data.rows.isNotEmpty()) {
                                historyAdapter.setData(data.rows)
                                binding.rvHistory.visibility = View.VISIBLE
                            } else {
                                binding.rvHistory.visibility = View.GONE
                            }
                        }

                    }
                    is ApiState.Error -> {
                        binding.loadingView.visibility = View.GONE
                        // 可添加错误提示
                    }
                    is ApiState.Idle -> {}
                }
            }
        }
    }

    // 加载隐患排查数据
    private fun loadDangerCheckData() {
        dangerViewModel.getDanger(dangerCheckFlow)
    }

    // 跳转到历史记录详情
    private fun goHistory(item: DangerCheckHistoryItem) {
        // 存储历史记录项
        SPUtils.save("hisItem", gson.toJson(item))

        // 跳转页面
        val intent = Intent(this, DangerHistoryRecordActivity::class.java)
        startActivity(intent)
    }

    // 跳转到新增/编辑详情页
    private fun goDetail(type: String) {
        // 存储页面标题类型
        SPUtils.save("pageTitle", type)

        when (type) {
            "add" -> {
                // 清除缓存
                SPUtils.remove("photos")
                SPUtils.remove("inputForm")

                // 如果有历史记录，默认填充第一条
                val historyList = historyAdapter.getData()
                if (historyList.isNotEmpty()) {
                    SPUtils.save("inputForm", gson.toJson(historyList[0]))
                }
            }
            "edit" -> {
                // 存储进行中的排查项
                if (::ingItem.isInitialized) {
                    SPUtils.save("inputForm", gson.toJson(ingItem))
                }
                // 清除临时保存
                SPUtils.remove("tempSave")
            }
        }

        // 跳转到详情页
        val intent = Intent(this, DangerCheckDetailActivity::class.java)
        startActivity(intent)
    }
}