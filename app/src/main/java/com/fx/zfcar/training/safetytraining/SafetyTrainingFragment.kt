package com.fx.zfcar.training.safetytraining

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fx.zfcar.net.OldSafetyListData
import com.fx.zfcar.net.SafetyListData
import com.fx.zfcar.training.adapter.SafetyTrainingAdapter
import com.fx.zfcar.training.adapter.SafetyTrainingHistoryAdapter
import com.fx.zfcar.training.user.UserCenterActivity
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.viewmodel.ApiState
import com.google.android.material.tabs.TabLayout
import com.fx.zfcar.databinding.FragmentSafetyTrainingBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.getValue

// Tab类型枚举
enum class SafetyTabType {
    ONGOING, // 进行中
    HISTORY // 历史
}

class SafetyTrainingFragment : BaseSafetyFragment() {
    private var _binding: FragmentSafetyTrainingBinding? = null
    private val binding get() = _binding!!

    // 两个Tab对应的Adapter
    private lateinit var ongoingAdapter: SafetyTrainingAdapter
    private lateinit var historyAdapter: SafetyTrainingHistoryAdapter

    // 当前选中的Tab
    private var currentTabType: SafetyTabType = SafetyTabType.ONGOING

    private val trainingViewModel by viewModels<SafetyTrainingViewModel>()
    private var ongoingStateFlow = MutableStateFlow<ApiState<SafetyListData>>(ApiState.Idle)
    private var historyStateFlow = MutableStateFlow<ApiState<OldSafetyListData>>(ApiState.Idle)

    private var page = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSafetyTrainingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getTitle(): String = "安全培训"

    override fun getTitleView(): TextView = binding.titleLayout.tvTitle

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initAdapter()
        bindTabLayout()
        // 默认加载“进行中”Tab数据
        switchTab(SafetyTabType.ONGOING)

        PressEffectUtils.setCommonPressEffect(binding.toUserCenter)
        binding.toUserCenter.setOnClickListener {
            startActivity(Intent(requireActivity(), UserCenterActivity::class.java))
        }
    }

    /**
     * 初始化View
     */
    private fun initView() {
        // 初始化RecyclerView
        binding.rvSafetyTraining.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }
    }

    /**
     * 初始化Adapter
     */
    private fun initAdapter() {
        // 进行中Adapter
        ongoingAdapter = SafetyTrainingAdapter(
            onStartStudy = { id ->
                // 开始学习逻辑
            },
            onStartExam = { id ->
                // 开始考试逻辑
            },
            onItemClick = { id ->
                // 条目点击逻辑
            }
        )

        // 历史Adapter（复用核心Adapter）
        historyAdapter = SafetyTrainingHistoryAdapter(
            onStartStudy = { id -> },
            onStartExam = { id -> },
            onItemClick = { id -> }
        )
    }

    /**
     * 绑定TabLayout切换
     */
    private fun bindTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val tabType = when (tab?.position) {
                    0 -> SafetyTabType.ONGOING
                    1 -> SafetyTabType.HISTORY
                    else -> SafetyTabType.ONGOING
                }
                switchTab(tabType)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {
                onTabSelected(tab) // 重新选中刷新数据
            }
        })
    }

    /**
     * 切换Tab核心方法
     */
    private fun switchTab(tabType: SafetyTabType) {
        currentTabType = tabType
        when (tabType) {
            SafetyTabType.ONGOING -> {
                binding.rvSafetyTraining.adapter = ongoingAdapter
                loadOngoingData()
            }
            SafetyTabType.HISTORY -> {
                binding.rvSafetyTraining.adapter = historyAdapter
                loadHistoryData()
            }
        }
    }

    /**
     * 加载“进行中”数据
     */
    private fun loadOngoingData() {
        lifecycleScope.launch {
            ongoingStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                    }

                    is ApiState.Success -> {
                        uiState.data?.let {
                            val ongoingItems = uiState.data.publicList
                                .filter { it.progress <= 100 || it.joinexams == 0 } // 未完成/未考试
                                .map { it.toSafetyTrainingItem() }
                            ongoingAdapter.submitList(ongoingItems)
                        }
                    }

                    is ApiState.Error -> {
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }

        trainingViewModel.getSafetyList(page, 0, ongoingStateFlow)
    }

    /**
     * 加载“历史”数据
     */
    private fun loadHistoryData() {
        lifecycleScope.launch {
            historyStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                    }

                    is ApiState.Success -> {
                        uiState.data?.let {
                            val historyItems = uiState.data.rows
                                .filter { it.progress == 100 && it.joinexams == 1 } // 已完成+考试通过
                                .map { it.toSafetyTrainingItem() }
                            historyAdapter.submitList(historyItems)
                        }
                    }

                    is ApiState.Error -> {
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }

        trainingViewModel.getOldSafetyList(page, historyStateFlow)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}