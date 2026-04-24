package com.fx.zfcar

import android.app.Application
import android.content.Context
import android.os.Bundle
import com.amap.api.maps.MapsInitializer
import com.fx.zfcar.net.CarUser
import com.fx.zfcar.net.CarUserInfo
import com.fx.zfcar.net.RetrofitClient
import com.fx.zfcar.net.UserLoginData
import com.fx.zfcar.ad.TbAdSdkManager
import com.fx.zfcar.util.SPUtils
import com.fx.zfcar.util.WindowInsetHelper
import com.kongzue.dialogx.DialogX
import com.google.gson.Gson
import java.lang.ref.WeakReference

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        mInstance = this
        // 初始化Retrofit单例
        RetrofitClient.init()
        // 高德地图隐私合规初始化（必须在初始化地图之前调用）
        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)
        DialogX.init(this)
        restoreNormalLoginState()
        TbAdSdkManager.warmUp(this)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: android.app.Activity, savedInstanceState: Bundle?) {
                activity.window?.decorView?.post {
                    WindowInsetHelper.applyBottomInset(activity)
                }
            }

            override fun onActivityStarted(activity: android.app.Activity) = Unit
            override fun onActivityResumed(activity: android.app.Activity) {
                currentActivityRef = WeakReference(activity)
            }
            override fun onActivityPaused(activity: android.app.Activity) = Unit
            override fun onActivityStopped(activity: android.app.Activity) = Unit
            override fun onActivitySaveInstanceState(activity: android.app.Activity, outState: Bundle) = Unit
            override fun onActivityDestroyed(activity: android.app.Activity) {
                if (currentActivityRef?.get() === activity) {
                    currentActivityRef = null
                }
            }
        })
    }

    private fun restoreNormalLoginState() {
        val token = SPUtils.getToken()
        if (token.isBlank()) {
            isLogin = false
            userInfo = null
            return
        }

        isLogin = true
        val cachedCarInfo = SPUtils.get(KEY_CAR_USER_INFO)
        if (cachedCarInfo.isBlank()) {
            userInfo = null
            return
        }

        userInfo = runCatching {
            Gson().fromJson(cachedCarInfo, CarUserInfo::class.java)?.info
        }.getOrNull()
    }

    // 3. 公共静态方法，全局获取MyApp实例
    companion object {
        private const val KEY_CAR_USER_INFO = "carUserInfo"
        @Volatile
        private var mInstance: MyApp? = null
        /**
         * 获取MyApp静态实例
         * @return 非空的MyApp实例
         * @throws IllegalStateException 未初始化（未在Manifest注册MyApp时抛出）
         */
        fun getInstance(): MyApp {
            return mInstance ?: throw IllegalStateException(
                "MyApp未初始化！请检查AndroidManifest.xml中是否注册了MyApp"
            )
        }

        // 拓展：直接获取ApplicationContext（更常用，避免外部重复调用getInstance().applicationContext）
        fun getAppContext(): Context {
            return getInstance().applicationContext
        }

        var isLogin: Boolean? = null
        var userInfo: CarUser? = null
        var isLYBH: Boolean? = null

        var isTrainingLogin: Boolean? = null
        var trainingUserInfo: UserLoginData? = null

        private var currentActivityRef: WeakReference<android.app.Activity>? = null

        fun getCurrentActivity(): android.app.Activity? {
            return currentActivityRef?.get()
        }
    }
}
