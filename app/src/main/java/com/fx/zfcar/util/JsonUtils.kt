package com.fx.zfcar.util

import com.google.gson.Gson

/**
 * JSON解析工具类
 */
object JsonUtils {
    val gson = Gson()

    /**
     * 将JSON字符串解析为指定类型的对象
     */
    inline fun <reified T> fromJson(json: String): T {
        return gson.fromJson(json, T::class.java)
    }

    /**
     * 将对象转换为JSON字符串
     */
    fun toJson(any: Any): String {
        return gson.toJson(any)
    }
}