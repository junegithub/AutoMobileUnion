package com.fx.zfcar.util

import android.annotation.SuppressLint
import android.os.Build
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtil {
    fun getDaysInMonth(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun getFirstDayOfMonth(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1)
        return cal.get(Calendar.DAY_OF_WEEK) - 1
    }

    fun formatDate(cal: Calendar): String {
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return "$year-$month-$day"
    }

    fun isSameDay(c1: Calendar, c2: Calendar): Boolean {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
                c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH)
    }

    fun isInRange(date: Calendar, start: Calendar?, end: Calendar?): Boolean {
        if (start == null || end == null) return false
        val dateTime = date.timeInMillis
        val startTime = start.timeInMillis
        val endTime = end.timeInMillis
        return dateTime in startTime..endTime
    }

    fun isInMiddleRange(date: Calendar, start: Calendar?, end: Calendar?): Boolean {
        if (start == null || end == null) return false
        val dateTime = date.timeInMillis
        val startTime = start.timeInMillis
        val endTime = end.timeInMillis
        return dateTime > startTime && dateTime < endTime
    }

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

    /**
     * 获取当前年月，格式为 yyyy-MM
     * 兼容所有 Android 版本（包括低版本）
     */
    fun getCurrentYearMonthCompat(): String {
        val calendar = Calendar.getInstance(Locale.CHINA)
        val sdf = SimpleDateFormat("yyyy-MM", Locale.CHINA)
        return sdf.format(calendar.time)
    }

    /**
     * 获取当前年的int值（例如 2026）
     */
    fun getCurrentYear(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0+ 简洁实现
            LocalDate.now().year
        } else {
            // 兼容低版本
            Calendar.getInstance(Locale.CHINA).get(Calendar.YEAR)
        }
    }

    /**
     * 获取当前月的int值（真实月份，例如 2月返回2，而非0）
     */
    fun getCurrentMonth(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // LocalDate的月份是真实值（1-12），无需+1
            LocalDate.now().monthValue
        } else {
            // Calendar的月份从0开始（0=1月），必须+1
            Calendar.getInstance(Locale.CHINA).get(Calendar.MONTH) + 1
        }
    }

    /**
     * 获取当前日的int值（例如 24日返回24）
     */
    fun getCurrentDay(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.now().dayOfMonth
        } else {
            Calendar.getInstance(Locale.CHINA).get(Calendar.DAY_OF_MONTH)
        }
    }

    fun extractYMDFromDateStr(dateStr: String): Triple<Int, Int, Int> {
        return try {
            // 按 "-" 分割字符串
            val parts = dateStr.split("-")
            // 确保分割后有3个部分（年、月、日）
            if (parts.size != 3) {
                Triple(0, 0, 0)
            } else {
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                val day = parts[2].toInt()
                Triple(year, month, day)
            }
        } catch (e: Exception) {
            // 捕获转换异常（如非数字、格式错误）
            e.printStackTrace()
            Triple(0, 0, 0)
        }
    }
}