package com.fx.zfcar.analytics

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.fx.zfcar.MyApp
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

object UmengTracker {
    private const val TAG = "UmengTracker"
    private const val APP_KEY = "6a03e2b29a7f376488dcf890"
    private const val CHANNEL = "default"

    private const val EVENT_LOGIN_FAILED = "Um_Event_LoginFailed"
    private const val EVENT_LOGIN_SUCCESS = "Um_Event_LoginSuc"
    private const val EVENT_MODULAR_CLICK = "Um_Event_ModularClick"
    private const val EVENT_PAGE_VIEW = "Um_Event_PageView"
    private const val EVENT_PAY_SUCCESS = "Um_Event_PaySuc"
    private const val EVENT_SEARCH_CLICK = "Um_Event_SearchClick"
    private const val EVENT_SEARCH_SUCCESS = "Um_Event_SearchSuc"

    private const val KEY_REASONS = "Um_Key_Reasons"
    private const val KEY_LOGIN_TYPE = "Um_Key_LoginType"
    private const val KEY_USER_ID = "Um_Key_UserID"
    private const val KEY_BUTTON_NAME = "Um_Key_ButtonName"
    private const val KEY_SOURCE_PAGE = "Um_Key_SourcePage"
    private const val KEY_SOURCE_LOCATION = "Um_Key_SourceLocation"
    private const val KEY_USER_LEVEL = "Um_Key_UserLevel"
    private const val KEY_PAGE_NAME = "Um_Key_PageName"
    private const val KEY_PAGE_CATEGORY = "Um_Key_PageCategory"
    private const val KEY_DURATION = "Um_Key_Duration"
    private const val KEY_PAY_MONEY = "Um_Key_PayMoney"
    private const val KEY_DISCOUNT_TYPE = "Um_Key_DiscountType"
    private const val KEY_SEARCH_LOCATION = "Um_Key_SearchLocation"
    private const val KEY_SEARCH_KEYWORD = "Um_Key_SearchKeyword"
    private const val KEY_SEARCH_PORTAL = "Um_Key_SearchPortal"
    private const val KEY_SEARCH_RECOMMEND = "Um_key_SearchRecommend"

    private val initialized = AtomicBoolean(false)
    private val pageStartTimes = ConcurrentHashMap<String, Long>()

    fun init(context: Context) {
        if (!initialized.compareAndSet(false, true)) {
            return
        }
        runCatching {
            UMConfigure.init(context.applicationContext, APP_KEY, CHANNEL, UMConfigure.DEVICE_TYPE_PHONE, "")
            MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.MANUAL)
        }.onFailure {
            initialized.set(false)
            Log.w(TAG, "init failed", it)
        }
    }

    fun onActivityResumed(activity: Activity) {
        if (!initialized.get()) {
            return
        }
        val pageName = activity.javaClass.simpleName
        pageStartTimes[pageName] = SystemClock.elapsedRealtime()
        runCatching {
            MobclickAgent.onPageStart(pageName)
            MobclickAgent.onResume(activity)
        }.onFailure {
            Log.w(TAG, "activity resume track failed: $pageName", it)
        }
    }

    fun onActivityPaused(activity: Activity) {
        if (!initialized.get()) {
            return
        }
        val pageName = activity.javaClass.simpleName
        val duration = pageStartTimes.remove(pageName)?.let {
            ((SystemClock.elapsedRealtime() - it) / 1000).coerceAtLeast(0)
        } ?: 0L
        pageView(activity, pageName, "Activity", duration)
        runCatching {
            MobclickAgent.onPageEnd(pageName)
            MobclickAgent.onPause(activity)
        }.onFailure {
            Log.w(TAG, "activity pause track failed: $pageName", it)
        }
    }

    fun loginFailed(context: Context, reason: String?) {
        track(context, EVENT_LOGIN_FAILED, mapOf(KEY_REASONS to reason.orEmpty()))
    }

    fun loginSuccess(context: Context, loginType: String, userId: String?) {
        track(
            context,
            EVENT_LOGIN_SUCCESS,
            mapOf(
                KEY_LOGIN_TYPE to loginType,
                KEY_USER_ID to userId.orEmpty()
            )
        )
    }

    fun modularClick(
        context: Context,
        buttonName: String,
        sourcePage: String,
        sourceLocation: String = ""
    ) {
        track(
            context,
            EVENT_MODULAR_CLICK,
            mapOf(
                KEY_BUTTON_NAME to buttonName,
                KEY_SOURCE_PAGE to sourcePage,
                KEY_SOURCE_LOCATION to sourceLocation
            ).withUser()
        )
    }

    fun pageView(
        context: Context,
        pageName: String,
        pageCategory: String,
        durationSeconds: Long,
        sourcePage: String = "",
        sourceLocation: String = ""
    ) {
        track(
            context,
            EVENT_PAGE_VIEW,
            mapOf(
                KEY_PAGE_NAME to pageName,
                KEY_PAGE_CATEGORY to pageCategory,
                KEY_SOURCE_PAGE to sourcePage,
                KEY_SOURCE_LOCATION to sourceLocation,
                KEY_DURATION to durationSeconds.toString()
            ).withUser()
        )
    }

    fun paySuccess(context: Context, amount: Float, discountType: String = "") {
        track(
            context,
            EVENT_PAY_SUCCESS,
            mapOf(
                KEY_PAY_MONEY to amount.formatAmount(),
                KEY_DISCOUNT_TYPE to discountType
            ).withUser()
        )
    }

    fun searchClick(context: Context, searchLocation: String) {
        track(
            context,
            EVENT_SEARCH_CLICK,
            mapOf(KEY_SEARCH_LOCATION to searchLocation).withUser()
        )
    }

    fun searchSuccess(context: Context, keyword: String, portal: String, isRecommend: Boolean) {
        track(
            context,
            EVENT_SEARCH_SUCCESS,
            mapOf(
                KEY_SEARCH_KEYWORD to keyword,
                KEY_SEARCH_PORTAL to portal,
                KEY_SEARCH_RECOMMEND to if (isRecommend) "1" else "0"
            ).withUser()
        )
    }

    private fun track(context: Context, eventId: String, params: Map<String, String>) {
        if (!initialized.get()) {
            return
        }
        runCatching {
            MobclickAgent.onEventObject(context.applicationContext, eventId, params.filterValues { it.isNotBlank() })
        }.onFailure {
            Log.w(TAG, "track failed: $eventId", it)
        }
    }

    private fun Map<String, String>.withUser(): Map<String, String> {
        val userId = MyApp.trainingUserInfo?.userid?.takeIf { it.isNotBlank() }
            ?: MyApp.userInfo?.username.orEmpty()
        val userLevel = MyApp.trainingUserInfo?.otherinfo?.level?.toString().orEmpty()
        return this + mapOf(
            KEY_USER_ID to userId,
            KEY_USER_LEVEL to userLevel
        )
    }

    private fun Float.formatAmount(): String {
        return BigDecimal(this.toDouble()).setScale(2, RoundingMode.HALF_UP).toPlainString()
    }
}
