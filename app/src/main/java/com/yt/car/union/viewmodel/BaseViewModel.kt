package com.yt.car.union.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yt.car.union.net.IBaseResponse
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * 基础ViewModel，封装通用逻辑
 */
open class BaseViewModel : ViewModel() {

    // 通用异常处理器
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onRequestError(throwable)
    }



    /**
     * 通用网络请求协程封装
     * @param block 具体的请求逻辑
     * @param onSuccess 请求成功后的回调（可选）
     */
    protected fun <T, R:IBaseResponse<T>> launchRequest(
        block: suspend () -> Response<R>,
        stateFlow: MutableStateFlow<ApiState<T>>?
    ) {
        viewModelScope.launch(exceptionHandler) {
            stateFlow?.value = ApiState.Loading
            runCatching {
                block()
            }.onSuccess { response ->
                handleResponse(response, stateFlow)
            }.onFailure { exception ->
                stateFlow?.value = ApiState.Error(
                    msg = "请求异常：${exception.message ?: "未知错误"}",
                    throwable = exception
                )
            }
        }
    }

    private fun <T, R : IBaseResponse<T>> handleResponse(
        response: Response<R>,
        stateFlow: MutableStateFlow<ApiState<T>>?
    ) {
        when {
            !response.isSuccessful -> {
                stateFlow?.value = ApiState.Error("网络请求失败：${response.code()} ${response.message()}")
            }
            response.body() == null -> {
                stateFlow?.value = ApiState.Error("响应体为空")
            }
            response.body()?.isSuccess() == true -> {
                stateFlow?.value = ApiState.Success(response.body()!!.data)
            }
            else -> {
                val errorBody = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                val errorMsg = response.body()?.msg ?: errorBody ?: "请求失败"
                stateFlow?.value = ApiState.Error("接口返回错误：$errorMsg")
            }
        }
    }

    /**
     * 请求失败的通用回调（子类可重写）
     */
    protected open fun onRequestError(throwable: Throwable) {}
}