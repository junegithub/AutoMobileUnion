package com.fx.zfcar.util

import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkErrorMapperTest {
    @Test
    fun http401UsesLoginExpiredMessage() {
        assertEquals("登录状态已过期，请重新登录", NetworkErrorMapper.fromHttp(401, "Unauthorized"))
    }
}
