package com.fx.zfcar.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.fx.zfcar.util.Constant
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory

class WXEntryActivity : Activity(), IWXAPIEventHandler {
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
}