package com.fx.zfcar.pages

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fx.zfcar.R
import com.fx.zfcar.databinding.ActivityPolicyContentBinding
import com.fx.zfcar.util.AssetFileReader

class PolicyContentActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_POLICY_TYPE = "policy_type"
        const val TYPE_TERMS = "terms"
        const val TYPE_PRIVACY = "privacy"

        fun open(context: Context, policyType: String) {
            context.startActivity(
                Intent(context, PolicyContentActivity::class.java).apply {
                    putExtra(EXTRA_POLICY_TYPE, policyType)
                }
            )
        }
    }

    private lateinit var binding: ActivityPolicyContentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPolicyContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { finish() }

        val policyType = intent.getStringExtra(EXTRA_POLICY_TYPE) ?: TYPE_PRIVACY
        val (title, fileName) = if (policyType == TYPE_TERMS) {
            getString(R.string.service_terms) to "terms.txt"
        } else {
            getString(R.string.privacy_policy) to "privacy.html"
        }

        binding.tvTitle.text = title
        renderContent(fileName)
    }

    private fun renderContent(fileName: String) {
        val rawContent = AssetFileReader.readTxtFile(this, fileName)
        val html = """
            <html>
            <head>
              <meta charset="utf-8" />
              <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no" />
              <style>
                body { padding: 16px; color: #222222; font-size: 16px; line-height: 1.75; word-break: break-word; }
              </style>
            </head>
            <body>$rawContent</body>
            </html>
        """.trimIndent()

        binding.webView.settings.apply {
            javaScriptEnabled = false
            domStorageEnabled = false
            builtInZoomControls = false
            displayZoomControls = false
        }
        binding.webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
    }
}
