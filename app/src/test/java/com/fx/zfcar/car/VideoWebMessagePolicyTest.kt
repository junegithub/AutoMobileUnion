package com.fx.zfcar.car

import org.junit.Assert.assertEquals
import org.junit.Test

class VideoWebMessagePolicyTest {
    @Test
    fun extractsActionUrlFromJsonMessage() {
        val url = VideoWebMessagePolicy.extractActionUrl("""{"action":"rtmp://example/live"}""")

        assertEquals("rtmp://example/live", url)
    }

    @Test
    fun keepsRawUrlMessage() {
        val url = VideoWebMessagePolicy.extractActionUrl("http://example/live.m3u8")

        assertEquals("http://example/live.m3u8", url)
    }

    @Test
    fun returnsBlankForMalformedMessage() {
        val url = VideoWebMessagePolicy.extractActionUrl("""{"action":""}""")

        assertEquals("", url)
    }
}
