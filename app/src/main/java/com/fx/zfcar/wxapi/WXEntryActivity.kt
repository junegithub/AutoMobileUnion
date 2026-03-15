package com.fx.zfcar.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.fx.zfcar.pages.EventData
import com.fx.zfcar.util.Constant
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelpay.PayResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import org.greenrobot.eventbus.EventBus

class WXEntryActivity : Activity(), IWXAPIEventHandler {
    companion object {
        // 支付结果回调接口
        var payResultCallback: ((Boolean, String) -> Unit)? = null
    }
    // 微信API实例
    private lateinit var wxApi: IWXAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化微信API（替换成你的APP_ID）
        wxApi = WXAPIFactory.createWXAPI(this, Constant.WX_APP_ID, true)
        wxApi.handleIntent(intent, this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        wxApi.handleIntent(intent, this)
    }

    // 微信请求回调（无需处理）
    override fun onReq(req: BaseReq?) {}

    // 微信响应回调（分享结果）
    override fun onResp(resp: BaseResp?) {
        if (resp?.type == ConstantsAPI.COMMAND_PAY_BY_WX) {
            val result = when (resp.errCode) {
                BaseResp.ErrCode.ERR_OK -> {
                    "支付成功" to true
                }
                BaseResp.ErrCode.ERR_USER_CANCEL -> {
                    "用户取消支付" to false
                }
                else -> {
                    "支付失败：${resp.errStr}" to false
                }
            }

            // 回调结果
            payResultCallback?.invoke(result.second, result.first)

            handlePayResult(resp as PayResp)
        }
        when (resp?.errCode) {
            BaseResp.ErrCode.ERR_OK -> {
                // 分享成功
                Toast.makeText(this, "分享成功", Toast.LENGTH_SHORT).show()
            }
            BaseResp.ErrCode.ERR_USER_CANCEL -> {
                // 用户取消分享
                Toast.makeText(this, "取消分享", Toast.LENGTH_SHORT).show()
            }
            else -> {
                // 分享失败
                Toast.makeText(this, "分享失败：${resp?.errStr}", Toast.LENGTH_SHORT).show()
            }
        }
        finish() // 关闭回调页面
    }

    /**
     * 处理支付结果
     */
    private fun handlePayResult(payResp: PayResp) {
        when (payResp.errCode) {
            // 支付成功
            BaseResp.ErrCode.ERR_OK -> {
                // 1. 解析支付结果参数
                val payResult = WxPayResult(
                    returnCode = "SUCCESS",
                    resultCode = "SUCCESS",
                    prepayId = payResp.prepayId,
                    outTradeNo = payResp.extData, // 商户订单号（需在支付时传入extData）
                    errCode = payResp.errCode.toString(),
                    errMsg = payResp.errStr
                )

                // 2. 业务处理：通知支付页面、更新订单状态等
                EventBus.getDefault().post(EventData(EventData.EVENT_WXPAY_SUCCESS, payResult))
            }

            // 支付取消
            BaseResp.ErrCode.ERR_USER_CANCEL -> {
                val payResult = WxPayResult(
                    returnCode = "CANCEL",
                    resultCode = "CANCEL",
                    errCode = payResp.errCode.toString(),
                    errMsg = "用户取消支付"
                )
                EventBus.getDefault().post(EventData(EventData.EVENT_WXPAY_CANCEL, payResult))
            }

            // 支付失败
            else -> {
                val payResult = WxPayResult(
                    returnCode = "FAIL",
                    resultCode = "FAIL",
                    errCode = payResp.errCode.toString(),
                    errMsg = payResp.errStr ?: "支付失败"
                )
                EventBus.getDefault().post(EventData(EventData.EVENT_WXPAY_FAIL, payResult))
            }
        }

        // 关闭回调页面
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清空回调，防止内存泄漏
        payResultCallback = null
    }

    /**
     * 微信支付结果数据类（序列化支持）
     */
    data class WxPayResult(
        val returnCode: String,
        val resultCode: String,
        val prepayId: String = "",
        val outTradeNo: String = "",
        val errCode: String = "",
        val errMsg: String = ""
    ) : java.io.Serializable
}