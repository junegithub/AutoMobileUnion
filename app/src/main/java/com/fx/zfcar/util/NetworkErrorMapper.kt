package com.fx.zfcar.util

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

object NetworkErrorMapper {

    fun fromThrowable(throwable: Throwable): String {
        return when (throwable) {
            is UnknownHostException -> "网络异常，请检查网络连接后重试"
            is ConnectException -> "网络连接失败，请稍后重试"
            is SocketTimeoutException -> "网络请求超时，请稍后重试"
            is SSLException -> "网络证书校验失败，请稍后重试"
            is IOException -> "网络异常，请稍后重试"
            else -> throwable.message?.takeIf { it.isNotBlank() } ?: "请求失败，请稍后重试"
        }
    }

    fun fromHttp(code: Int, message: String?): String {
        return when (code) {
            408 -> "请求超时，请稍后重试"
            502, 503, 504 -> "服务暂时不可用，请稍后重试"
            in 500..599 -> "服务器异常，请稍后重试"
            else -> {
                val suffix = message?.takeIf { it.isNotBlank() }?.let { "：$it" }.orEmpty()
                "请求失败($code)$suffix"
            }
        }
    }
}
