package com.fx.zfcar.training.drivelog

object DriveLogKeyboardScrollPolicy {
    fun targetScrollY(focusedTopInContent: Int, topOffsetPx: Int): Int {
        return (focusedTopInContent - topOffsetPx).coerceAtLeast(0)
    }
}
