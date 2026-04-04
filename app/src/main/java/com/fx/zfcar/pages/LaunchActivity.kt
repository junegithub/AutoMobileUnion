package com.fx.zfcar.pages

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fx.zfcar.util.SPUtils

class LaunchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val target = if (SPUtils.isPolicyAccepted()) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, AgreementConsentActivity::class.java)
        }

        startActivity(target)
        finish()
    }
}
