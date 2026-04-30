package com.fx.zfcar.ad

import android.content.Context
import android.os.Build
import com.fx.zfcar.R

object TbAdConfig {

    fun appId(context: Context): String {
        return context.getString(R.string.tb_ad_app_id).trim()
    }

    fun splashCodeId(context: Context): String {
        return context.getString(R.string.tb_ad_splash_code_id).trim()
    }

    fun isConfigured(context: Context): Boolean {
        return appId(context).isNotEmpty() && splashCodeId(context).isNotEmpty()
    }

    fun isRuntimeSupported(): Boolean {
        return Build.SUPPORTED_ABIS.none { abi ->
            abi.equals("x86", ignoreCase = true) || abi.equals("x86_64", ignoreCase = true)
        }
    }

    fun isAvailable(context: Context): Boolean {
        return isConfigured(context) && isRuntimeSupported()
    }
}
