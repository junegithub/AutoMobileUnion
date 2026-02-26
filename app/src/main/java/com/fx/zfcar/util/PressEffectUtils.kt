package com.fx.zfcar.util

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.FloatRange

/**
 * 通用控件按压效果工具类
 * 支持：按压变暗/变亮、缩放、透明度变化，可组合使用
 */
object PressEffectUtils {
    // 默认按压透明度（正常1.0 → 按压0.7）
    private const val DEFAULT_PRESS_ALPHA = 0.7f
    // 默认按压缩放比例（正常1.0 → 按压0.95）
    private const val DEFAULT_PRESS_SCALE = 0.95f
    // 动画时长（毫秒）
    private const val ANIM_DURATION = 80L

    /**
     * 通用按压效果（默认：变暗+轻微缩放）
     * @param view 任意View（Button/TextView/ImageView等）
     * @param pressAlpha 按压时的透明度（0.0~1.0）
     * @param pressScale 按压时的缩放比例（0.0~1.0）
     */
    fun setCommonPressEffect(
        view: View,
        @FloatRange(from = 0.0, to = 1.0) pressAlpha: Float = DEFAULT_PRESS_ALPHA,
        @FloatRange(from = 0.0, to = 1.0) pressScale: Float = DEFAULT_PRESS_SCALE
    ) {
        // 初始化View状态
        view.isClickable = true // 确保View可点击
        view.alpha = 1.0f
        view.scaleX = 1.0f
        view.scaleY = 1.0f

        // 监听触摸事件，实现按压/抬起的视觉变化
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    // 按压/滑动到控件内：应用按压效果
                    if (isTouchInView(v, event)) {
                        startPressAnim(v, pressAlpha, pressScale)
                    } else {
                        // 滑动到控件外：恢复原状
                        startReleaseAnim(v)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // 抬起/取消触摸：恢复原状
                    startReleaseAnim(v)
                }
            }
            // 返回false：不拦截View原有点击事件（如OnClickListener）
            false
        }
    }

    /**
     * 仅按压变暗效果（无缩放）
     */
    fun setPressAlphaEffect(view: View, @FloatRange(from = 0.0, to = 1.0) pressAlpha: Float = DEFAULT_PRESS_ALPHA) {
        setCommonPressEffect(view, pressAlpha, 1.0f)
    }

    /**
     * 仅按压缩放效果（无透明度变化）
     */
    fun setPressScaleEffect(view: View, @FloatRange(from = 0.0, to = 1.0) pressScale: Float = DEFAULT_PRESS_SCALE) {
        setCommonPressEffect(view, 1.0f, pressScale)
    }

    /**
     * 按压动画（变暗+缩放）
     */
    private fun startPressAnim(view: View, pressAlpha: Float, pressScale: Float) {
        // 透明度动画
        ValueAnimator.ofFloat(view.alpha, pressAlpha).apply {
            duration = ANIM_DURATION
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                view.alpha = anim.animatedValue as Float
            }
            start()
        }
        // 缩放动画
        ValueAnimator.ofFloat(view.scaleX, pressScale).apply {
            duration = ANIM_DURATION
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                val scale = anim.animatedValue as Float
                view.scaleX = scale
                view.scaleY = scale
            }
            start()
        }
    }

    /**
     * 释放动画（恢复原状）
     */
    private fun startReleaseAnim(view: View) {
        // 透明度恢复
        ValueAnimator.ofFloat(view.alpha, 1.0f).apply {
            duration = ANIM_DURATION
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                view.alpha = anim.animatedValue as Float
            }
            start()
        }
        // 缩放恢复
        ValueAnimator.ofFloat(view.scaleX, 1.0f).apply {
            duration = ANIM_DURATION
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                val scale = anim.animatedValue as Float
                view.scaleX = scale
                view.scaleY = scale
            }
            // 动画结束后，确保状态完全恢复（避免精度问题）
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(anim: Animator) {}
                override fun onAnimationEnd(anim: Animator) {
                    view.scaleX = 1.0f
                    view.scaleY = 1.0f
                }
                override fun onAnimationCancel(anim: Animator) {}
                override fun onAnimationRepeat(anim: Animator) {}
            })
            start()
        }
    }

    /**
     * 判断触摸点是否在View范围内（解决滑动到控件外仍显示按压效果的问题）
     */
    private fun isTouchInView(view: View, event: MotionEvent): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = event.rawX
        val y = event.rawY
        return x >= location[0] && x <= location[0] + view.width &&
                y >= location[1] && y <= location[1] + view.height
    }
}