package com.yt.car.union.util

import java.util.Calendar

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
}