package com.fx.zfcar.training.pay

object YearPayRequestPolicy {
    fun buildWechatAppParams(yearId: Int): Map<String, Any> {
        return mapOf(
            "type" to "wechat",
            "method" to "app",
            "year_id" to yearId
        )
    }

    fun buildAlipayAppParams(yearId: Int): Map<String, Any> {
        return mapOf(
            "type" to "alipay",
            "method" to "app",
            "year_id" to yearId
        )
    }
}
