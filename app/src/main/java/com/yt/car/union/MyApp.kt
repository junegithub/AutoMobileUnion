package com.yt.car.union

import android.app.Application
import android.content.Context
import com.amap.api.maps.MapsInitializer
import com.kongzue.dialogx.DialogX
import com.yt.car.union.net.CarUser
import com.yt.car.union.net.RetrofitClient
import com.yt.car.union.net.UserLoginData

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
    }

    // 3. 公共静态方法，全局获取MyApp实例
    companion object {
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
    }
}