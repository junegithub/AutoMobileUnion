package com.fx.zfcar.viewmodel

/**
 * 统一的网络请求状态密封类
 * @param T 成功返回的数据类型（泛型，适配不同接口的response）
 */
sealed class ApiState<out T> {
    object Idle : ApiState<Nothing>() // 初始状态
    // 请求进行中
    object Loading : ApiState<Nothing>()

    // 请求成功，携带response数据
    data class Success<out T>(val data: T?) : ApiState<T>()

    // 请求失败，携带错误提示信息
    data class Error(val msg: String, val throwable: Throwable? = null) : ApiState<Nothing>() // 失败（带错误信息）
}