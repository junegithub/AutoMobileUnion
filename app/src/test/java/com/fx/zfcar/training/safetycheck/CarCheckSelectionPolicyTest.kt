package com.fx.zfcar.training.safetycheck

import org.junit.Assert.assertEquals
import org.junit.Test

class CarCheckSelectionPolicyTest {
    @Test
    fun resolveCarNumUsesPrimaryCarNum() {
        assertEquals("鲁A12345", CarCheckSelectionPolicy.resolveCarNum("鲁A12345", "鲁B12345"))
    }

    @Test
    fun resolveCarNumFallsBackToLegacyCarnum() {
        assertEquals("鲁B12345", CarCheckSelectionPolicy.resolveCarNum("", "鲁B12345"))
    }
}
