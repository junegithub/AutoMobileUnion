package com.yt.car.union.pages.training.adapter

// 基础Item类型（区分不同Tab的展示类型）
sealed class StudyProveItem {
    // 安全教育Item
    data class SafetyEducationItem(
        val month: String,
        val trainingProject: String,
        val getTime: String,
        val certificateId: Int
    ) : StudyProveItem()

    // 继续教育Item
    data class ContinueEducationItem(
        val date: String,
        val trainingProject: String,
        val getTime: String,
        val certificateId: Int
    ) : StudyProveItem()

    // 岗前培训Item
    data class BeforeEducationItem(
        val month: String,
        val totalHours: Int,
        val getTime: String,
        val certificateId: Int
    ) : StudyProveItem()
}