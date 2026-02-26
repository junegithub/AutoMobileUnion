package com.fx.zfcar.training.safetytraining

import com.fx.zfcar.net.OldSafetyPlan
import com.fx.zfcar.net.SafetyPlan
import com.fx.zfcar.util.DateUtil

// 培训状态枚举（匹配按钮显示逻辑）
enum class SafetyTrainingStatus {
    COMPLETED_EXAM_PASSED, // 已完成+考试已通过
    NOT_STARTED, // 未开始（开始学习+开始考试）
}

// UI层数据模型（解耦接口数据，适配UI展示）
data class SafetyTrainingItem(
    val id: Int,
    val title: String, // 如“2026年2月日常安全培训”
    val progress: Int, // 学习进度（0-100）
    val status: SafetyTrainingStatus
)

// 转换接口数据到UI模型的扩展方法
fun SafetyPlan.toSafetyTrainingItem(): SafetyTrainingItem {
    // 解析createtime生成标题（yyyy年M月日常安全培训）
    val title = "${DateUtil.timestamp2Date(createtime)}日常安全培训"

    // 判断状态
    val status = if (this.progress == 100 && this.joinexams == 1) {
        SafetyTrainingStatus.COMPLETED_EXAM_PASSED
    } else {
        SafetyTrainingStatus.NOT_STARTED
    }

    return SafetyTrainingItem(
        id = this.id,
        title = title,
        progress = this.progress,
        status = status
    )
}

fun OldSafetyPlan.toSafetyTrainingItem(): SafetyTrainingItem {
    // 解析createtime生成标题（yyyy年M月日常安全培训）
    val title = "${DateUtil.timestamp2Date(starttime)}日常安全培训"

    // 判断状态
    val status = if (this.progress == 100 && this.joinexams == 1) {
        SafetyTrainingStatus.COMPLETED_EXAM_PASSED
    } else {
        SafetyTrainingStatus.NOT_STARTED
    }

    return SafetyTrainingItem(
        id = this.id,
        title = title,
        progress = this.progress,
        status = status
    )
}