package com.yt.car.union.net

import com.yt.car.union.util.SPUtils
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Token请求头拦截器：所有请求自动添加token
 */
class TokenInterceptor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // 构建新的请求，添加token头
        val newRequest = chain.request().newBuilder()
            .addHeader("token", SPUtils.getToken()) // 与前端请求头key一致
            .addHeader("Content-Type", "application/json;charset=UTF-8") // 默认JSON格式
            .build()
        return chain.proceed(newRequest)
    }
}