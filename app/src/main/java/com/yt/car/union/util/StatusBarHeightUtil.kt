package com.yt.car.union.util

import android.content.Context

object StatusBarHeightUtil {
    /**
     * Kotlin版获取状态栏高度（px）
     */
    fun getStatusBarHeight(context: Context): Int {
        var statusBarHeight = 0
        val res = context.resources
        val resourceId = res.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = res.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight
    }
}

// 使用方式
//val statusBarHeight = StatusBarHeightUtil.getStatusBarHeight(this) // Activity中
//val statusBarHeight = StatusBarHeightUtil.getStatusBarHeight(requireContext()) // Fragment中