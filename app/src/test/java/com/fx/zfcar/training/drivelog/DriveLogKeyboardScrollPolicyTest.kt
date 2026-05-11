package com.fx.zfcar.training.drivelog

import org.junit.Assert.assertEquals
import org.junit.Test

class DriveLogKeyboardScrollPolicyTest {
    @Test
    fun targetScrollYKeepsFocusedInputBelowTopOffset() {
        val target = DriveLogKeyboardScrollPolicy.targetScrollY(
            focusedTopInContent = 360,
            topOffsetPx = 96
        )

        assertEquals(264, target)
    }

    @Test
    fun targetScrollYNeverGoesBelowZero() {
        val target = DriveLogKeyboardScrollPolicy.targetScrollY(
            focusedTopInContent = 48,
            topOffsetPx = 96
        )

        assertEquals(0, target)
    }
}
