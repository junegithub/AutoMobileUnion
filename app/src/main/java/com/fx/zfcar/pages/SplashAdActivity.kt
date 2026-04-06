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
    private val fallbackTask = Runnable { routeNext() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashAdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!TbAdConfig.isConfigured(this)) {
            routeNext()
            return
        }

        mainHandler.postDelayed(fallbackTask, SPLASH_LOAD_TIMEOUT_MS)
        TbAdSdkManager.ensureInit(application as MyApp) { success ->
            runOnUiThread {
                if (!success || isFinishing || isDestroyed) {
                    routeNext()
                    return@runOnUiThread
                }
                loadSplash()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadSplash() {
        val config = TbSplashConfig.Builder()
            .codeId(TbAdConfig.splashCodeId(this))
            .container(binding.splashContainer)
            .build()
        TbManager.loadSplash(config, this, object : TbManager.SplashLoadListener {
            override fun onFail(message: String) {
                Log.w(TAG, "TB splash load failed: $message")
                routeNext()
            }

            override fun onTick(timeLeft: Long) = Unit

            override fun onClicked() = Unit

            override fun onDismiss() {
                routeNext()
            }

            override fun onExposure(position: Position) {
                mainHandler.removeCallbacks(fallbackTask)
            }
        })
    }

    private fun routeNext() {
        if (hasNavigated) {
            return
        }
        hasNavigated = true
        mainHandler.removeCallbacks(fallbackTask)
        val target = if (SPUtils.isPolicyAccepted()) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, AgreementConsentActivity::class.java)
        }
        startActivity(target)
        finish()
    }

    override fun onDestroy() {
        mainHandler.removeCallbacks(fallbackTask)
        super.onDestroy()
    }

    companion object {
        private const val TAG = "SplashAdActivity"
        private const val SPLASH_LOAD_TIMEOUT_MS = 4500L
    }
}
