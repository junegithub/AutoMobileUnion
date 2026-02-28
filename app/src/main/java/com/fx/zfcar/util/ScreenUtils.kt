package com.fx.zfcar.util

import androidx.appcompat.app.AppCompatActivity

object ScreenUtils {
    fun getScreenWidth(context: AppCompatActivity): Int {
        return context.resources.displayMetrics.widthPixels
    }

    fun getScreenHeight(context: AppCompatActivity): Int {
        return context.resources.displayMetrics.heightPixels
    }

    fun vw2px(context: AppCompatActivity, vw: Float): Int {
        return (getScreenWidth(context) * vw * 0.01).toInt()
    }

    fun vh2px(context: AppCompatActivity, vh: Float): Int {
        return (getScreenHeight(context) * vh * 0.01).toInt()
    }

    fun dp2px(context: AppCompatActivity, dp: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

    fun sp2px(context: AppCompatActivity, sp: Float): Int {
        val scaledDensity = context.resources.displayMetrics.scaledDensity
        return (sp * scaledDensity + 0.5f).toInt()
    }
}