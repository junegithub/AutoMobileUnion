package com.fx.zfcar.util

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.fx.zfcar.R
import com.fx.zfcar.car.VideoFullActivity
import com.fx.zfcar.pages.MainActivity
import com.fx.zfcar.training.notice.SignatureActivity

object WindowInsetHelper {

    fun applyBottomInset(activity: Activity) {
        if (!shouldApplyBottomInset(activity)) {
            return
        }
        val content = activity.findViewById<ViewGroup>(android.R.id.content) ?: return
        val root = content.getChildAt(0) ?: return
        val originalBottom = (root.getTag(R.id.tag_original_padding_bottom) as? Int) ?: root.paddingBottom
        root.setTag(R.id.tag_original_padding_bottom, originalBottom)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = originalBottom + systemBars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(root)
    }

    private fun shouldApplyBottomInset(activity: Activity): Boolean {
        return activity !is MainActivity &&
            activity !is VideoFullActivity &&
            activity !is SignatureActivity
    }
}
