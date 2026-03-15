package com.fx.zfcar.training

data class WXPayResult(
    val returnCode: String,      // SUCCESS/FAIL
    val returnMsg: String,       // 返回信息
    val resultCode: String,      // SUCCESS/FAIL
    val errCode: String,         // 错误码
    val errCodeDes: String       // 错误描述
)
data class AlipayResult(
    val resultStatus: String,    // 9000=成功 8000=处理中 4000=失败
    val result: String,
    val memo: String
)
