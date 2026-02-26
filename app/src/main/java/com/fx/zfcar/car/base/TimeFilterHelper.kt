package com.fx.zfcar.car.base

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fx.zfcar.databinding.TrackTimeChooserBinding
import com.fx.zfcar.util.DateUtil
import com.fx.zfcar.util.DialogUtils
import com.fx.zfcar.util.PressEffectUtils
import java.util.*

/**
 * 时间筛选全能工具类
 * 包含：时间计算、控件绑定、单选事件处理、跨容器（Fragment/Dialog/Activity）适配
 */
class TimeFilterHelper(
    private val context: Context,
    private val onTimeSelected: (startTime: Long, endTime: Long) -> Unit // 时间选中回调
) {

    // 快捷时间类型枚举
    enum class QuickTimeType {
        ONE_HOUR_AGO, TODAY, YESTERDAY, DAY_BEFORE_YESTERDAY
    }

    // 当前选中的时间类型
    private var currentType: QuickTimeType = QuickTimeType.ONE_HOUR_AGO
    // 当前时间区间
    private var currentStartTime = 0L
    private var currentEndTime = 0L

    private var binding: TrackTimeChooserBinding? = null

    // ========== 对外暴露的核心方法 ==========

    /**
     * 绑定时间筛选的View（核心：支持任意容器的View）
     * @param rootView 根布局View（Dialog/Fragment/Activity的根View）
     */
    fun bindView(rootView: ViewGroup?) {
        binding = TrackTimeChooserBinding.inflate(LayoutInflater.from(context))
        rootView?.let {
            rootView.removeAllViews()
            rootView.addView(binding?.root)
        }

        // 1. 初始化默认状态
        initDefaultState(binding!!)
        // 2. 绑定RadioGroup单选事件
        bindRadioGroupListener(binding!!)
        // 3. 绑定按钮事件
        bindButtonListener(binding!!)
    }

    fun getRootView(): View {
        return binding!!.root
    }

    /**
     * 获取当前选中的时间区间
     * @return Pair<开始时间戳, 结束时间戳>
     */
    fun getCurrentTimeRange(): Pair<Long, Long> {
        return Pair(currentStartTime, currentEndTime)
    }

    /**
     * 手动设置选中的时间类型（对外暴露）
     * @param type 时间类型
     * @param binding 可选：传入binding用于更新UI
     */
    fun setSelectedType(type: QuickTimeType, binding: TrackTimeChooserBinding? = null) {
        currentType = type
        // 更新时间戳
        updateTimeRangeByType(type)
        // 更新UI（如果传入binding）
        binding?.let { updateTimeDisplay(it) }
    }

    // ========== 内部核心逻辑 ==========

    /**
     * 初始化默认状态
     */
    private fun initDefaultState(binding: TrackTimeChooserBinding) {
        // 默认选中一小时前
        binding.rgQuickTime.check(binding.rbOneHourAgo.id)
        // 初始化时间区间
        setSelectedType(QuickTimeType.ONE_HOUR_AGO, binding)
    }

    /**
     * 绑定RadioGroup单选事件（原生单选逻辑）
     */
    private fun bindRadioGroupListener(binding: TrackTimeChooserBinding) {
        binding.rgQuickTime.setOnCheckedChangeListener { _, checkedId ->
            val type = when (checkedId) {
                binding.rbOneHourAgo.id -> QuickTimeType.ONE_HOUR_AGO
                binding.rbToday.id -> QuickTimeType.TODAY
                binding.rbYesterday.id -> QuickTimeType.YESTERDAY
                binding.rbDayBeforeYesterday.id -> QuickTimeType.DAY_BEFORE_YESTERDAY
                else -> QuickTimeType.ONE_HOUR_AGO
            }
            // 更新选中类型和UI
            setSelectedType(type, binding)
        }
    }

    /**
     * 绑定取消/确定按钮事件
     */
    private fun bindButtonListener(binding: TrackTimeChooserBinding) {
        PressEffectUtils.setCommonPressEffect(binding.tvStartTime)
        PressEffectUtils.setCommonPressEffect(binding.tvEndTime)
        PressEffectUtils.setCommonPressEffect(binding.tvCancel)
        PressEffectUtils.setCommonPressEffect(binding.tvConfirm)

        binding.tvStartTime.setOnClickListener {
            DialogUtils.showDateTimePicker(context, currentStartTime) {
                currentStartTime = it
                updateTimeDisplay(binding)
            }
        }
        binding.tvEndTime.setOnClickListener {
            DialogUtils.showDateTimePicker(context, currentEndTime) {
                currentEndTime = it
                updateTimeDisplay(binding)
            }
        }
        // 取消按钮：对外暴露空回调（由调用方处理关闭逻辑）
        binding.tvCancel.setOnClickListener {
            onTimeSelected(-1, -1) // 用-1标识取消操作
        }

        // 确定按钮：返回选中的时间区间
        binding.tvConfirm.setOnClickListener {
            onTimeSelected(currentStartTime, currentEndTime)
        }
    }

    /**
     * 根据时间类型更新时间区间
     */
    private fun updateTimeRangeByType(type: QuickTimeType) {
        val now = System.currentTimeMillis()
        currentStartTime = when (type) {
            QuickTimeType.ONE_HOUR_AGO -> now - 60 * 60 * 1000
            QuickTimeType.TODAY -> {
                Calendar.getInstance().apply {
                    timeInMillis = now // 基于当前时间初始化
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
            QuickTimeType.YESTERDAY -> {
                Calendar.getInstance().apply {
                    timeInMillis = now // 基于当前时间初始化
                    add(Calendar.DAY_OF_MONTH, -1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
            QuickTimeType.DAY_BEFORE_YESTERDAY -> {
                Calendar.getInstance().apply {
                    timeInMillis = now // 基于当前时间初始化
                    add(Calendar.DAY_OF_MONTH, -2)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
        }

        currentEndTime = when (type) {
            QuickTimeType.ONE_HOUR_AGO, QuickTimeType.TODAY -> now
            QuickTimeType.YESTERDAY -> {
                Calendar.getInstance().apply {
                    timeInMillis = now // 基于当前时间初始化
                    add(Calendar.DAY_OF_MONTH, -1)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis
            }
            QuickTimeType.DAY_BEFORE_YESTERDAY -> {
                Calendar.getInstance().apply {
                    timeInMillis = now // 基于当前时间初始化
                    add(Calendar.DAY_OF_MONTH, -2)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis
            }
        }
    }

    /**
     * 更新时间显示UI
     */
    private fun updateTimeDisplay(binding: TrackTimeChooserBinding) {
        binding.tvStartTime.text = "开始时间: ${DateUtil.timestamp2String(currentStartTime)}"
        binding.tvEndTime.text = "结束时间: ${DateUtil.timestamp2String(currentEndTime)}"
    }
}