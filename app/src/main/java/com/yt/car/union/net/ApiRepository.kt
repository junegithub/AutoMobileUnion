package com.yt.car.union.net

import com.yt.car.union.net.bean.BaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApiRepository {
    // 单例
    companion object {
        val instance by lazy { ApiRepository() }
    }

    // 通用 GET 请求封装
    suspend fun <T> getRequest(
        call: suspend () -> BaseResponse<T>
    ): BaseResponse<T> {
        return withContext(Dispatchers.IO) {
            call.invoke()
        }
    }

    // 通用 POST 请求封装
    suspend fun <T> postRequest(
        call: suspend () -> BaseResponse<T>
    ): BaseResponse<T> {
        return withContext(Dispatchers.IO) {
            call.invoke()
        }
    }
}