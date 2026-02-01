package com.yt.car.union.net

import com.yt.car.union.bean.MeetingItem
import com.yt.car.union.bean.TodoItem
import com.yt.car.union.bean.TrainingCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * 数据仓库：模拟接口请求、数据缓存
 */
class SafetyRepository {

    // 模拟获取培训卡片数据
    suspend fun getTrainingCard(): TrainingCard {
        return withContext(Dispatchers.IO) {
            delay(800) // 模拟网络延迟
            TrainingCard(
                title = "日常安全培训",
                info = "江西6月\n九江市\n道路普通货物运输",
                unfinishedCount = 3
            )
        }
    }

    // 模拟获取待办列表
    suspend fun getTodoList(): List<TodoItem> {
        return withContext(Dispatchers.IO) {
            delay(800) // 模拟网络延迟
            listOf(
                TodoItem(
                    id = "1",
                    title = "安全隐患整改通知书",
                    desc = "鲁H303G7的安全隐患排查报告于2023年6月12日审核不通过，点击这里签收告知书。",
                    overdueTime = "已过去1天10小时"
                ),
                TodoItem(
                    id = "2",
                    title = "驾驶员责任书",
                    desc = "《驾驶员安全生产目标责任书》，点击这里立即签署。",
                    overdueTime = "已过去1天10小时"
                ),
                TodoItem(
                    id = "3",
                    title = "驾驶员承诺书",
                    desc = "《安全文明驾驶承诺书》，点击这里立即签署。",
                    overdueTime = "已过去1天10小时"
                ),
                TodoItem(
                    id = "4",
                    title = "岗前培训",
                    desc = "德州市华兴物流有限公司 鲁NE9392 安全员培训课件",
                    overdueTime = "已过去1天10小时"
                ),
                TodoItem(
                    id = "5",
                    title = "法律法规试卷",
                    desc = "《岗前培训法律法规3号安全卷》",
                    overdueTime = ""
                )
            )
        }
    }

    // 模拟获取会议数据（对接服务端时间）
    suspend fun getMeetingInfo(): MeetingItem {
        return withContext(Dispatchers.IO) {
            delay(800) // 模拟网络延迟
            // 模拟服务端返回的会议开始时间（3天2小时23分钟后）
            val serverCurrentTime = System.currentTimeMillis()
            val meetingStartTime =
                serverCurrentTime + 3 * 24 * 60 * 60 * 1000L + 2 * 60 * 60 * 1000L + 23 * 60 * 1000L
            MeetingItem(
                title = "安全会议",
                content = "嘉祥全顺运输有限公司 嘉祥全顺运输有限公司晨会 2023-06-11 00:00:00到嘉祥全顺运输有限公司参会。2023-06-11 00:00:00签签到，2023-06-11 00:00:00签退。",
                startTime = meetingStartTime
            )
        }
    }

    // 模拟标记待办项为完成
    suspend fun markTodoCompleted(todoId: String): Boolean {
        return withContext(Dispatchers.IO) {
            delay(500) // 模拟接口延迟
            true // 模拟标记成功
        }
    }

    // 模拟刷新所有数据
    suspend fun refreshAllData(): Boolean {
        return withContext(Dispatchers.IO) {
            delay(1000) // 模拟刷新延迟
            true
        }
    }
}