package com.fx.zfcar.util

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.alipay.sdk.app.PayTask
import com.fx.zfcar.net.PayOrderData
import com.fx.zfcar.training.AlipayResult
import com.fx.zfcar.util.Constant.WX_APP_ID
import com.fx.zfcar.wxapi.WXEntryActivity
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object PayUtils {
    private val handler = Handler(Looper.getMainLooper())

    /**
     * 调起微信支付
     */
    fun callWeChatPay(
        activity: Activity,
        payData: PayOrderData,
        callback: (Boolean, String) -> Unit
    ) {
        // 设置全局回调
        WXEntryActivity.payResultCallback = callback

        val wxApi: IWXAPI = WXAPIFactory.createWXAPI(activity, WX_APP_ID)
        wxApi.registerApp(WX_APP_ID)

        // 检查微信是否安装
        if (!wxApi.isWXAppInstalled) {
            callback(false, "未安装微信，请先安装微信后再试")
            Toast.makeText(activity, "未安装微信", Toast.LENGTH_SHORT).show()
            return
        }

        // 构建支付请求
        val req = PayReq().apply {
            appId = WX_APP_ID
            partnerId = payData.partnerId ?: "" // 商户号
            prepayId = payData.prepayId ?: ""   // 预支付ID
            packageValue = payData.packageValue ?: "Sign=WXPay"
            nonceStr = payData.nonceStr ?: ""
            timeStamp = payData.timeStamp ?: ""
            sign = payData.paySign ?: ""
        }

        // 调起支付
        val isSuccess = wxApi.sendReq(req)
        if (!isSuccess) {
            callback(false, "调起微信支付失败")
            Toast.makeText(activity, "调起支付失败", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 调起支付宝支付
     */
    fun callAlipay(
        activity: Activity,
        orderInfo: String,
        callback: (Boolean, String) -> Unit
    ) {
        // 子线程处理支付
        GlobalScope.launch(Dispatchers.IO) {
            val payTask = PayTask(activity)
            val result = payTask.payV2(orderInfo, true)

            // 解析结果
            val alipayResult = parseAlipayResult(result)

            // 主线程回调
            withContext(Dispatchers.Main) {
                when (alipayResult.resultStatus) {
                    "9000" -> {
                        // 支付成功
                        callback(true, "支付成功")
                        Toast.makeText(activity, "支付成功", Toast.LENGTH_SHORT).show()
                    }
                    "8000" -> {
                        // 处理中
                        callback(false, "支付结果确认中")
                        Toast.makeText(activity, "支付结果确认中", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // 支付失败
                        val errorMsg = when (alipayResult.resultStatus) {
                            "4000" -> "支付失败"
                            "5000" -> "重复请求"
                            "6001" -> "用户取消支付"
                            "6002" -> "网络连接出错"
                            else -> "支付失败：${alipayResult.memo}"
                        }
                        callback(false, errorMsg)
                        Toast.makeText(activity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * 解析支付宝支付结果
     */
    private fun parseAlipayResult(result: Map<String, String>): AlipayResult {
        return AlipayResult(
            resultStatus = result["resultStatus"] ?: "",
            result = result["result"] ?: "",
            memo = result["memo"] ?: ""
        )
    }

    /**
     * 获取微信登录Code（实际项目需集成微信登录）
     */
    fun getWeChatLoginCode(activity: Activity, callback: (String) -> Unit) {
        // 这里简化处理，实际需调用微信登录API
        // 测试用Code
        handler.postDelayed({
            callback("test_wechat_code_${System.currentTimeMillis()}")
        }, 500)
    }
}