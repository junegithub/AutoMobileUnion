package com.fx.zfcar.car

import com.google.gson.Gson

object VideoWebMessagePolicy {
    private val gson = Gson()

    fun extractActionUrl(message: String): String {
        val trimmed = message.trim()
        if (trimmed.isBlank()) return ""
        if (trimmed.startsWith("http://") ||
            trimmed.startsWith("https://") ||
            trimmed.startsWith("rtmp://") ||
            trimmed.startsWith("rtsp://")
        ) {
            return trimmed
        }
        return runCatching {
            gson.fromJson(trimmed, VideoAction::class.java)?.action.orEmpty()
        }.getOrDefault("")
    }

    private data class VideoAction(
        val action: String = ""
    )
}
