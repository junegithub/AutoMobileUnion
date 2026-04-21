package com.fx.zfcar.net

import com.fx.zfcar.util.SPUtils
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Token请求头拦截器：所有请求自动添加token
 */
class TokenInterceptor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()
            .header("Content-Type", "application/json;charset=UTF-8")

        val isTrainingRequest = request.url.host == "safe.ezbeidou.com"
        if (isTrainingRequest) {
            val trainingToken = SPUtils.getTrainingToken()
            if (trainingToken.isNotBlank()) {
                builder.header("token", trainingToken)
            }
            builder.removeHeader("Authorization")
        } else {
            val token = SPUtils.getToken()
            if (token.isNotBlank()) {
                builder.header("Authorization", token)
            }
            builder.removeHeader("token")
        }

        val newRequest = builder.build()
        return chain.proceed(newRequest)
    }
}
