package com.fx.zfcar.util

object TrainingFileUrlPolicy {
    fun build(baseUrl: String, path: String): String {
        return if (path.startsWith("http://") || path.startsWith("https://")) {
            path
        } else {
            baseUrl.trimEnd('/') + "/" + path.trimStart('/')
        }
    }
}
