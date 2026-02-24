package com.yt.car.union.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

/**
 * 时间转换工具类
 * 处理秒级Long时间戳 -> 字符串格式
 */
object TimeUtils {

    // 常用时间格式：yyyy-MM-dd HH:mm:ss
    private const val FORMAT_DEFAULT = "yyyy-MM-dd HH:mm:ss"
    // 简化格式：yyyy-MM-dd
    private const val FORMAT_DATE_ONLY = "yyyy-MM-dd"

    /**
     * 将秒级时间戳转为指定格式的字符串
     * @param timestamp 秒级时间戳（Long）
     * @param format 时间格式，默认yyyy-MM-dd HH:mm:ss
     * @return 格式化后的时间字符串，异常时返回"--"
     */
    @SuppressLint("SimpleDateFormat")
    fun timestamp2String(timestamp: Long?, format: String = FORMAT_DEFAULT): String {
        // 处理空值/无效时间戳
        if (timestamp == null || timestamp <= 0) {
            return "--"
        }
        return try {
            // 秒级时间戳转毫秒（SimpleDateFormat需要毫秒级）
            val millis = timestamp * 1000
            val sdf = SimpleDateFormat(format, Locale.CHINA)
            sdf.format(Date(millis))
        } catch (e: Exception) {
            // 捕获格式化异常，返回占位符
            "--"
        }
    }

    /**
     * 简化版：仅转年月日（yyyy-MM-dd）
     */
    fun timestamp2Date(timestamp: Long?): String {
        return timestamp2String(timestamp, FORMAT_DATE_ONLY)
    }
}