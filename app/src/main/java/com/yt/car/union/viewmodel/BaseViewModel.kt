package com.yt.car.union.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

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
    protected fun <T> launchRequest(
        block: suspend () -> T,
        onSuccess: (T) -> Unit = {}
    ) {
        viewModelScope.launch(exceptionHandler) {
            val result = block()
            onSuccess(result)
            onRequestSuccess(result)
        }
    }

    /**
     * 请求成功的通用回调（子类可重写）
     */
    protected open fun <T> onRequestSuccess(result: T) {}

    /**
     * 请求失败的通用回调（子类可重写）
     */
    protected open fun onRequestError(throwable: Throwable) {}
}