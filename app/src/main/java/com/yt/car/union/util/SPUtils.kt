package com.yt.car.union.util

import android.content.Context
import android.content.SharedPreferences
import com.yt.car.union.MyApp
import com.yt.car.union.net.ApiConfig

object SPUtils {
    private const val SP_NAME = "carqilian_sp"
    private const val KEY_ACCOUNT = "account"
    private const val KEY_PASSWORD = "password"
    private const val KEY_REMEMBER = "remember_password"
    private const val KEY_AUTO_LOGIN = "auto_login"

    private fun getSP(): SharedPreferences {
        return MyApp.getAppContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    }

    // 保存登录信息
    fun saveLoginInfo(
        account: String,
        password: String,
        isRemember: Boolean,
        isAutoLogin: Boolean
    ) {
        getSP().edit()
            .putString(KEY_ACCOUNT, if (isRemember) account else "")
            .putString(KEY_PASSWORD, if (isRemember) password else "")
            .putBoolean(KEY_REMEMBER, isRemember)
            .putBoolean(KEY_AUTO_LOGIN, isAutoLogin)
            .apply()
    }

    fun saveToken(token: String?) {
        getSP().edit().putString(ApiConfig.SP_KEY_TOKEN, token).apply()
    }

    fun getToken(): String {
        return getSP().getString(ApiConfig.SP_KEY_TOKEN, "") ?: ""
    }

    // 获取账号
    fun getAccount(): String {
        return getSP().getString(KEY_ACCOUNT, "") ?: ""
    }

    // 获取密码
    fun getPassword(): String {
        return getSP().getString(KEY_PASSWORD, "") ?: ""
    }

    // 是否记住密码
    fun isRememberPassword(): Boolean {
        return getSP().getBoolean(KEY_REMEMBER, true)
    }

    // 是否自动登录
    fun isAutoLogin(): Boolean {
        return getSP().getBoolean(KEY_AUTO_LOGIN, true)
    }
}