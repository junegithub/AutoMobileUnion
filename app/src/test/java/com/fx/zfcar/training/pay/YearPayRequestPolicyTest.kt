package com.fx.zfcar.training.pay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class YearPayRequestPolicyTest {
    @Test
    fun buildWechatAppParamsDoesNotRequireLoginCode() {
        val params = YearPayRequestPolicy.buildWechatAppParams(yearId = 18)

        assertEquals(
            mapOf(
                "type" to "wechat",
                "method" to "app",
                "year_id" to 18
            ),
            params
        )
        assertFalse(params.containsKey("code"))
    }

    @Test
    fun buildAlipayAppParamsUsesYearId() {
        val params = YearPayRequestPolicy.buildAlipayAppParams(yearId = 18)

        assertEquals(
            mapOf(
                "type" to "alipay",
                "method" to "app",
                "year_id" to 18
            ),
            params
        )
    }
}
