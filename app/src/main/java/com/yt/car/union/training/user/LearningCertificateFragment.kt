package com.yt.car.union.training.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.kongzue.dialogx.datepicker.DatePickerDialog
import com.kongzue.dialogx.datepicker.interfaces.OnDateSelected
import com.yt.car.union.R
import com.yt.car.union.databinding.FragmentLearningCertificateBinding
import com.yt.car.union.net.BeforeEducationCertificateData
import com.yt.car.union.net.EducationCertificate
import com.yt.car.union.net.UserStudyProveListData
import com.yt.car.union.training.adapter.BeforeEducationAdapter
import com.yt.car.union.training.adapter.ContinueEducationAdapter
import com.yt.car.union.training.adapter.SafetyEducationAdapter
import com.yt.car.union.training.adapter.StudyProveItem
import com.yt.car.union.util.TimeUtils
import com.yt.car.union.viewmodel.ApiState
import com.yt.car.union.training.viewmodel.SafetyTrainingViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.getValue

// Tab类型枚举（统一管理Tab标识）
enum class StudyProveTabType {
    SAFETY_EDUCATION, // 安全教育
    CONTINUE_EDUCATION, // 继续教育
    BEFORE_EDUCATION // 岗前培训
}

class LearningCertificateFragment : BaseUserFragment() {

    private lateinit var binding: FragmentLearningCertificateBinding

    private val trainingViewModel by viewModels<SafetyTrainingViewModel>()
    private var stateFlow = MutableStateFlow<ApiState<UserStudyProveListData>>(ApiState.Idle)
    private var eduStateFlow = MutableStateFlow<ApiState<List<EducationCertificate>>>(ApiState.Idle)
    private var beforeEduStateFlow = MutableStateFlow<ApiState<BeforeEducationCertificateData>>(ApiState.Idle)

    // 三个Tab对应的Adapter
    private lateinit var safetyAdapter: SafetyEducationAdapter
    private lateinit var continueAdapter: ContinueEducationAdapter
    private lateinit var beforeAdapter: BeforeEducationAdapter

    // 当前选中的Tab类型
    private var currentTabType: StudyProveTabType = StudyProveTabType.SAFETY_EDUCATION

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLearningCertificateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initAdapter()
        bindTabLayout()
        switchTab(StudyProveTabType.SAFETY_EDUCATION)
    }

    override fun getTitle(): String = getString(R.string.title_learning_certificate)

    override fun getTitleView(): TextView = binding.titleLayout.tvTitle


    /**
     * 初始化所有Adapter
     */
    private fun initAdapter() {
        // 安全教育Adapter
        safetyAdapter = SafetyEducationAdapter { certificateId ->
            navigateToCertificateDetail(certificateId)
        }

        // 继续教育Adapter
        continueAdapter = ContinueEducationAdapter(
            onViewCertificate = { certificateId ->
                navigateToCertificateDetail(certificateId)
            },
            onCheckRecord = { certificateId ->
                navigateToCheckRecord(certificateId)
            }
        )

        // 岗前培训Adapter
        beforeAdapter = BeforeEducationAdapter { certificateId ->
            navigateToCertificateDetail(certificateId)
        }
    }

    // ------------------------ 初始化逻辑 ------------------------
    /**
     * 初始化基础View（RecyclerView、分割线、API服务等）
     */
    private fun initView() {

        binding.tvSelectedMonth.text = TimeUtils.getCurrentYearMonthCompat()
        // 初始化共用的RecyclerView
        binding.rvStudyProve.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            // 添加分割线（匹配截图视觉效果）
            addItemDecoration(
                DividerItemDecoration(context, LinearLayoutManager.VERTICAL).apply {
                    setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider_horizontal)!!)
                }
            )
        }

        // 月份选择箭头点击事件
        binding.tvSelectedMonth.setOnClickListener {
            if (currentTabType == StudyProveTabType.SAFETY_EDUCATION) {
                DatePickerDialog.build()
                    .setMaxYear(TimeUtils.getCurrentYear())
                    .setDefaultSelect(TimeUtils.getCurrentYear(), TimeUtils.getCurrentMonth(),
                        TimeUtils.getCurrentDay())
                    .show(object : OnDateSelected() {
                    override fun onSelect(text: String?, year: Int, month: Int, day: Int) {
                        binding.tvSelectedMonth.text = "$year-$month"
                        loadSafetyEducationData()
                    }
                })
            }
        }

        lifecycleScope.launch {
            stateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                    }

                    is ApiState.Success -> {
                        uiState.data?.let {
                            val items = uiState.data.plan.flatMap { plan ->
                                plan.list.map { certificate ->
                                    StudyProveItem.SafetyEducationItem(
                                        month = plan.month,
                                        trainingProject = certificate.title,
                                        getTime = certificate.getTime,
                                        certificateId = certificate.id
                                    )
                                }
                            }

                            binding.certificateContent.text = getString(R.string.certificate_content, items.size)
                            safetyAdapter.submitList(items)
                            updateEmptyState(items.isEmpty())
                        }
                    }

                    is ApiState.Error -> {
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }
        lifecycleScope.launch {
            eduStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                    }

                    is ApiState.Success -> {
                        uiState.data?.let {
                            val educationList = uiState.data
                            val items = educationList.mapIndexed { index, item ->
                                StudyProveItem.ContinueEducationItem(
                                    date = item.starttime,
                                    trainingProject = item.category,
                                    getTime = item.endtime,
                                    certificateId = index
                                )
                            }

                            binding.certificateContent.text = getString(R.string.certificate_content, items.size)
                            continueAdapter.submitList(items)
                            updateEmptyState(items.isEmpty())
                        }
                    }

                    is ApiState.Error -> {
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }
        lifecycleScope.launch {
            beforeEduStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                    }

                    is ApiState.Success -> {
                        uiState.data?.let {
                            val items = listOf(
                                StudyProveItem.BeforeEducationItem(
                                    month = uiState.data.date,
                                    totalHours = uiState.data.ksnum,
                                    getTime = uiState.data.addtime,
                                    certificateId = uiState.data.ksnum
                                )
                            )

                            binding.certificateContent.text = getString(R.string.certificate_content, items.size)
                            beforeAdapter.submitList(items)
                            updateEmptyState(items.isEmpty())
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

    /**
     * 绑定TabLayout切换事件（原Activity的核心逻辑）
     */
    private fun bindTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val tabType = when (tab?.position) {
                    0 -> StudyProveTabType.SAFETY_EDUCATION
                    1 -> StudyProveTabType.CONTINUE_EDUCATION
                    2 -> StudyProveTabType.BEFORE_EDUCATION
                    else -> StudyProveTabType.SAFETY_EDUCATION
                }
                switchTab(tabType) // 切换Tab（替换Adapter+加载数据）
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {
                // 重新选中Tab时刷新数据
                onTabSelected(tab)
            }
        })
    }

    // ------------------------ Tab切换核心逻辑 ------------------------
    /**
     * 切换Tab核心方法（仅替换Adapter+加载对应数据）
     */
    private fun switchTab(tabType: StudyProveTabType) {
        currentTabType = tabType
        // 1. 显示/隐藏月份选择栏（仅岗前培训显示）
        binding.tvSelectedMonth.visibility = if (tabType == StudyProveTabType.SAFETY_EDUCATION) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // 2. 替换RecyclerView的Adapter+加载对应数据
        when (tabType) {
            StudyProveTabType.SAFETY_EDUCATION -> {
                binding.rvStudyProve.adapter = safetyAdapter
                loadSafetyEducationData()
            }
            StudyProveTabType.CONTINUE_EDUCATION -> {
                binding.rvStudyProve.adapter = continueAdapter
                loadContinueEducationData()
            }
            StudyProveTabType.BEFORE_EDUCATION -> {
                binding.rvStudyProve.adapter = beforeAdapter
                loadBeforeEducationData()
            }
        }
    }

    // ------------------------ 通用工具方法 ------------------------
    /**
     * 更新空状态显示
     */
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvStudyProve.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    /**
     * 跳转证书详情页
     */
    private fun navigateToCertificateDetail(certificateId: Int) {
        Toast.makeText(context, "查看证书 ID: $certificateId", Toast.LENGTH_SHORT).show()
        // 补充证书详情页跳转逻辑
    }

    /**
     * 跳转打卡记录页
     */
    private fun navigateToCheckRecord(certificateId: Int) {
        Toast.makeText(context, "查看打卡记录 ID: $certificateId", Toast.LENGTH_SHORT).show()
        // 补充打卡记录页跳转逻辑
    }

    // ------------------------ 各Tab数据加载 ------------------------
    /**
     * 加载安全教育数据
     */
    private fun loadSafetyEducationData() {
        trainingViewModel.getUserStudyProveList(binding.tvSelectedMonth.text.toString(), stateFlow)
    }

    /**
     * 加载继续教育数据
     */
    private fun loadContinueEducationData() {
        trainingViewModel.getEducationCertificate(eduStateFlow)
    }

    /**
     * 加载岗前培训数据
     */
    private fun loadBeforeEducationData() {
        trainingViewModel.getBeforeEducationCertificate(beforeEduStateFlow)
    }
}