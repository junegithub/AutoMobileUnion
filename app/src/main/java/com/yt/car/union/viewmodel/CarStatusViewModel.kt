package com.yt.car.union.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import android.util.Log
import com.yt.car.union.net.RetrofitClient
import com.yt.car.union.net.bean.CarStatisticsResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * UI状态密封类（区分加载中、成功、失败）
 */
sealed class CarStatusUiState {
    object Loading : CarStatusUiState()                // 加载中
    data class Success(val data: CarStatisticsResponse) : CarStatusUiState() // 成功
    data class Error(val msg: String) : CarStatusUiState() // 失败
}

/**
 * 车辆状态ViewModel
 */
class CarStatusViewModel : ViewModel() {

    // 私有可变状态（仅ViewModel内部修改）
    private val _uiState = MutableStateFlow<CarStatusUiState>(CarStatusUiState.Loading)
    // 公开不可变状态（UI层观察）
    val uiState: StateFlow<CarStatusUiState> = _uiState.asStateFlow()

    fun getCarStatusList() {
        // 发起请求前先置为加载状态
        _uiState.value = CarStatusUiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val params = mapOf(
                    "search" to "",
                    "type" to "all",
                    "page" to "1"
                )
                val response = RetrofitClient.getApiService().getAllcars()
                if (response.code == 1 && response.data != null) {
                    // 业务码成功 + 数据非空 → 发送成功状态
                    _uiState.value = CarStatusUiState.Success(response.data)
                } else {
                    // 业务码失败（如参数错误、无权限）
                    _uiState.value = CarStatusUiState.Error(response.msg ?: "请求失败，业务码异常")
                }
            } catch (e: Exception) {
                // 异常处理（网络错误、解析错误等）
                // 捕获网络异常（超时、无网络、解析失败等）
                val errorMsg = when (e) {
                    is java.net.ConnectException -> "网络连接失败，请检查网络"
                    is java.net.SocketTimeoutException -> "请求超时，请稍后重试"
                    else -> "请求异常：${e.message ?: "未知错误"}"
                }
                _uiState.value = CarStatusUiState.Error(errorMsg)
            }
        }
    }

    fun getCarTreeData() {
        viewModelScope.launch(Dispatchers.IO) {
            var response = RetrofitClient.getApiService().getCarTreeData(emptyMap())
        }
    }
}
