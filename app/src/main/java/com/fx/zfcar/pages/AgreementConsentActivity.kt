package com.fx.zfcar.pages

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.fx.zfcar.R
import com.fx.zfcar.databinding.ActivityAgreementConsentBinding
import com.fx.zfcar.util.SPUtils

class AgreementConsentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgreementConsentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgreementConsentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initAgreementText()

        binding.btnAgree.setOnClickListener {
            binding.cbAgreement.isChecked = true
            SPUtils.savePolicyAccepted(true)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.btnReject.setOnClickListener {
            finishAffinity()
        }
    }

    private fun initAgreementText() {
        val fullText = getText(R.string.agreement_consent_content)
        val serviceText = getText(R.string.agreement_service)
        val privacyText = getText(R.string.agreement_privacy)
        val spannable = SpannableString(fullText)

        val serviceStart = fullText.indexOf(serviceText.toString())
        val serviceEnd = serviceStart + serviceText.length
        val privacyStart = fullText.indexOf(privacyText.toString())
        val privacyEnd = privacyStart + privacyText.length

        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                PolicyContentActivity.open(this@AgreementConsentActivity, PolicyContentActivity.TYPE_TERMS)
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = ContextCompat.getColor(this@AgreementConsentActivity, R.color.colorPrimary)
                ds.isUnderlineText = false
            }
        }, serviceStart, serviceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                PolicyContentActivity.open(this@AgreementConsentActivity, PolicyContentActivity.TYPE_PRIVACY)
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = ContextCompat.getColor(this@AgreementConsentActivity, R.color.colorPrimary)
                ds.isUnderlineText = false
            }
        }, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.tvAgreementContent.text = spannable
        binding.tvAgreementContent.movementMethod = LinkMovementMethod.getInstance()
        binding.tvAgreementContent.highlightColor = ContextCompat.getColor(this, android.R.color.transparent)
    }
}
