package com.fx.zfcar.ad

import android.app.Application
import android.util.Log
import com.tb.mob.TbManager
import com.tb.mob.config.TbInitConfig

object TbAdSdkManager {
    private const val TAG = "TbAdSdkManager"

    @Volatile
    private var initialized = false
    @Volatile
    private var initializing = false
    private val pendingCallbacks = mutableListOf<(Boolean) -> Unit>()

    fun ensureInit(application: Application, callback: (Boolean) -> Unit) {
        if (!TbAdConfig.isConfigured(application)) {
            callback(false)
            return
        }
        if (initialized) {
            callback(true)
            return
        }
        synchronized(this) {
            if (initialized) {
                callback(true)
                return
            }
            pendingCallbacks += callback
            if (initializing) {
                return
            }
            initializing = true
        }

        val config = TbInitConfig.Builder()
            .appId(TbAdConfig.appId(application))
            .build()
        TbManager.init(application, config, object : TbManager.IsInitListener {
            override fun onFail(message: String?) {
                Log.w(TAG, "TB SDK init failed: $message")
                complete(false)
            }

            override fun onSuccess() {
                Log.d(TAG, "TB SDK init success")
                complete(true)
            }

            override fun onDpSuccess() {
                Log.d(TAG, "TB SDK dp success")
            }
        })
    }

    private fun complete(success: Boolean) {
        val callbacks = synchronized(this) {
            initializing = false
            initialized = success
            pendingCallbacks.toList().also { pendingCallbacks.clear() }
        }
        callbacks.forEach { it(success) }
    }
}
