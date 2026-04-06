package com.fx.zfcar.pages

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.fx.zfcar.MyApp
import com.fx.zfcar.ad.TbAdConfig
import com.fx.zfcar.ad.TbAdSdkManager
import com.fx.zfcar.databinding.ActivitySplashAdBinding
import com.fx.zfcar.util.SPUtils
import com.tb.mob.TbManager
import com.tb.mob.bean.Position
import com.tb.mob.config.TbSplashConfig

class SplashAdActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashAdBinding
    private val mainHandler = Handler(Looper.getMainLooper())
    private var hasNavigated = false
    private val initTimeoutTask = Runnable {
        Log.w(TAG, "TB splash init timeout, fallback route")
        routeNext()
    }
    private val loadTimeoutTask = Runnable {
        Log.w(TAG, "TB splash load timeout, fallback route")
        routeNext()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashAdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!TbAdConfig.isConfigured(this)) {
            Log.d(TAG, "TB splash skipped: appId/codeId not configured")
            routeNext()
            return
        }

        Log.d(TAG, "TB splash start loading, sdkReady=${TbAdSdkManager.isReady()}")
        if (TbAdSdkManager.isReady()) {
            Log.d(TAG, "TB splash sdk already ready")
            loadSplash()
            return
        }

        mainHandler.postDelayed(initTimeoutTask, SDK_INIT_TIMEOUT_MS)
        TbAdSdkManager.ensureInit(application as MyApp) { success ->
            runOnUiThread {
                mainHandler.removeCallbacks(initTimeoutTask)
                if (!success || isFinishing || isDestroyed) {
                    Log.w(TAG, "TB splash sdk init failed, fallback route")
                    routeNext()
                    return@runOnUiThread
                }
                Log.d(TAG, "TB splash sdk init success")
                loadSplash()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadSplash() {
        Log.d(TAG, "TB splash load with timeout=${SPLASH_LOAD_TIMEOUT_MS}ms")
        mainHandler.postDelayed(loadTimeoutTask, SPLASH_LOAD_TIMEOUT_MS)
        val config = TbSplashConfig.Builder()
            .codeId(TbAdConfig.splashCodeId(this))
            .container(binding.splashContainer)
            .build()
        TbManager.loadSplash(config, this, object : TbManager.SplashLoadListener {
            override fun onFail(message: String) {
                Log.w(TAG, "TB splash load failed: $message")
                routeNext()
            }

            override fun onTick(timeLeft: Long) {
                Log.d(TAG, "TB splash tick: $timeLeft")
            }

            override fun onClicked() {
                Log.d(TAG, "TB splash clicked")
            }

            override fun onDismiss() {
                Log.d(TAG, "TB splash dismissed")
                routeNext()
            }

            override fun onExposure(position: Position) {
                Log.d(TAG, "TB splash exposed: $position")
                mainHandler.removeCallbacks(loadTimeoutTask)
            }
        })
    }

    private fun routeNext() {
        if (hasNavigated) {
            return
        }
        hasNavigated = true
        mainHandler.removeCallbacks(initTimeoutTask)
        mainHandler.removeCallbacks(loadTimeoutTask)
        Log.d(TAG, "TB splash route next")
        val target = if (SPUtils.isPolicyAccepted()) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, AgreementConsentActivity::class.java)
        }
        startActivity(target)
        finish()
    }

    override fun onDestroy() {
        mainHandler.removeCallbacks(initTimeoutTask)
        mainHandler.removeCallbacks(loadTimeoutTask)
        super.onDestroy()
    }

    companion object {
        private const val TAG = "SplashAdActivity"
        private const val SDK_INIT_TIMEOUT_MS = 8000L
        private const val SPLASH_LOAD_TIMEOUT_MS = 15000L
    }
}
