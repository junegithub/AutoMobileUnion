package com.fx.zfcar.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TrainingFileUrlPolicyTest {
    @Test
    fun buildKeepsAbsoluteUrl() {
        assertEquals(
            "https://cdn.example.com/a.png",
            TrainingFileUrlPolicy.build("https://safe.ezbeidou.com", "https://cdn.example.com/a.png")
        )
    }

    @Test
    fun buildJoinsBaseAndLeadingSlashPath() {
        assertEquals(
            "https://safe.ezbeidou.com/uploads/a.png",
            TrainingFileUrlPolicy.build("https://safe.ezbeidou.com/", "/uploads/a.png")
        )
    }
}
