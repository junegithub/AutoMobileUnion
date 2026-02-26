package com.fx.zfcar.training.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.fx.zfcar.net.StudyDetailData
import com.fx.zfcar.training.adapter.StudyDetailAdapter
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.DateUtil
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.viewmodel.ApiState
import com.kongzue.dialogx.datepicker.DatePickerDialog
import com.kongzue.dialogx.datepicker.interfaces.OnDateSelected
import com.fx.zfcar.R
import com.fx.zfcar.databinding.FragmentStudyDetailsBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.getValue


class StudyDetailsFragment : BaseUserFragment() {

    private lateinit var binding: FragmentStudyDetailsBinding

    private val trainingViewModel by viewModels<SafetyTrainingViewModel>()
    private var stateFlow = MutableStateFlow<ApiState<StudyDetailData>>(ApiState.Idle)

    // 三个Tab对应的Adapter
    private lateinit var studyAdapter: StudyDetailAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStudyDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        loadSafetyEducationData()
    }

    override fun getTitle(): String = getString(R.string.title_study_detail)

    override fun getTitleView(): TextView = binding.titleLayout.tvTitle

    // ------------------------ 初始化逻辑 ------------------------
    /**
     * 初始化基础View（RecyclerView、分割线、API服务等）
     */
    private fun initView() {
        PressEffectUtils.setCommonPressEffect(binding.tvSelectedMonth)
        binding.tvSelectedMonth.text = DateUtil.getCurrentYearMonthCompat()

        // 月份选择箭头点击事件
        binding.tvSelectedMonth.setOnClickListener {
            DatePickerDialog.build()
                .setMaxYear(DateUtil.getCurrentYear())
                .setDefaultSelect(
                    DateUtil.getCurrentYear(), DateUtil.getCurrentMonth(),
                    DateUtil.getCurrentDay())
                .show(object : OnDateSelected() {
                    override fun onSelect(text: String?, year: Int, month: Int, day: Int) {
                        binding.tvSelectedMonth.text = "$year-$month"
                        loadSafetyEducationData()
                    }
                })
        }

        // 初始化RecyclerView
        binding.rvStudyList.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            // 添加分割线（匹配截图视觉效果）
            addItemDecoration(
                DividerItemDecoration(context, LinearLayoutManager.VERTICAL).apply {
                    setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider_horizontal)!!)
                }
            )
        }
        studyAdapter = StudyDetailAdapter(requireActivity()) { itemId ->
//            val intent = Intent(this, StudyRecordActivity::class.java)
//            intent.putExtra("id", blockId)
//            startActivity(intent)
        }


        lifecycleScope.launch {
            stateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                    }

                    is ApiState.Success -> {
                        uiState.data?.let {
                            studyAdapter.submitList(uiState.data.rows)
                            updateEmptyState(uiState.data.rows.isEmpty())
                        }
                    }

                    is ApiState.Error -> {
                        context?.showToast("加载失败：${uiState.msg}")
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }
    }

    // ------------------------ 通用工具方法 ------------------------
    /**
     * 更新空状态显示
     */
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvStudyList.visibility = if (isEmpty) View.GONE else View.VISIBLE
        if (isEmpty) {
            context?.showToast("暂无数据")
        }
    }

    // ------------------------ 各Tab数据加载 ------------------------
    /**
     * 加载安全教育数据
     */
    private fun loadSafetyEducationData() {
        trainingViewModel.getStudySafetyList(binding.tvSelectedMonth.text.toString(), stateFlow)
    }

}