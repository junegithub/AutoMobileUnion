package com.yt.car.union.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yt.car.union.bean.MeetingItem
import com.yt.car.union.bean.TodoItem
import com.yt.car.union.bean.TrainingCard
import com.yt.car.union.net.SafetyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SafetyViewModel : ViewModel() {
    private val repository = SafetyRepository()

    // 培训卡片数据
    private val _trainingCard = MutableLiveData<TrainingCard?>()
    val trainingCard: LiveData<TrainingCard?> = _trainingCard

    // 待办列表数据
    private val _todoList = MutableLiveData<List<TodoItem>>()
    val todoList: LiveData<List<TodoItem>> = _todoList

    // 会议数据
    private val _meetingItem = MutableLiveData<MeetingItem?>()
    val meetingItem: LiveData<MeetingItem?> = _meetingItem

    // 加载状态
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // 倒计时Job（用于取消倒计时）
    private var countdownJob: Job? = null

    init {
        // 初始化加载数据
        loadAllData()
    }

    // 加载所有数据
    fun loadAllData() {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.Main) {
            try {
                // 并行加载数据
                val trainingCardDeferred = launch { _trainingCard.value = repository.getTrainingCard() }
                val todoListDeferred = launch { _todoList.value = repository.getTodoList() }
                val meetingItemDeferred = launch {
                    val meeting = repository.getMeetingInfo()
                    _meetingItem.value = meeting
                    startMeetingCountdown(meeting)
                }

                // 等待所有数据加载完成
                trainingCardDeferred.join()
                todoListDeferred.join()
                meetingItemDeferred.join()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 启动会议倒计时
    private fun startMeetingCountdown(meetingItem: MeetingItem) {
        // 取消之前的倒计时
        countdownJob?.cancel()

        countdownJob = viewModelScope.launch(Dispatchers.Main) {
            while (true) {
                val currentTime = System.currentTimeMillis()
                val remainingTime = meetingItem.startTime - currentTime

                if (remainingTime <= 0) {
                    // 倒计时结束
                    val updatedMeeting = meetingItem.copy(countdown = "会议已开始")
                    _meetingItem.value = updatedMeeting
                    break
                }

                // 格式化倒计时
                val days = TimeUnit.MILLISECONDS.toDays(remainingTime)
                val hours = TimeUnit.MILLISECONDS.toHours(remainingTime) - TimeUnit.DAYS.toHours(days)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingTime) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(remainingTime))
                val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(remainingTime))

                // 更新倒计时文本
                val countdownText = "${days}天${hours}小时${minutes}分钟${seconds}秒后开始"
                val updatedMeeting = meetingItem.copy(countdown = countdownText)
                _meetingItem.value = updatedMeeting

                // 每秒刷新一次
                delay(1000)
            }
        }
    }

    // 标记待办项为完成
    fun markTodoCompleted(todoId: String) {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                _isLoading.value = true
                val success = repository.markTodoCompleted(todoId)
                if (success) {
                    // 本地更新待办状态
                    val currentList = _todoList.value?.toMutableList()
                    currentList?.forEachIndexed { index, todoItem ->
                        if (todoItem.id == todoId) {
                            currentList[index] = todoItem.copy(isCompleted = true)
                        }
                    }
                    _todoList.value = currentList ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 下拉刷新数据
    fun refreshData() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                _isLoading.value = true
                val success = repository.refreshAllData()
                if (success) {
                    // 重新加载所有数据
                    loadAllData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 取消倒计时（ViewModel销毁时调用）
    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}