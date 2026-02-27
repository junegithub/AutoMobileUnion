package com.fx.zfcar.car.base

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.lifecycle.LifecycleCoroutineScope
import com.fx.zfcar.R
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.util.Constant
import com.fx.zfcar.viewmodel.ApiState
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tencent.mm.opensdk.utils.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.net.URLEncoder

class WeChatShareHelper(private val context: Context,
                        lifecycleScope: LifecycleCoroutineScope) {
    private var wxApi: IWXAPI =
        WXAPIFactory.createWXAPI(context.applicationContext, Constant.WX_APP_ID, true)
    val shareLocationStateFlow = MutableStateFlow<ApiState<String>>(ApiState.Idle)

    var carnum: String = ""

    init {
        val isRegisterSuccess = wxApi.registerApp(Constant.WX_APP_ID)

        Log.i("WXSDK", "注册结果: $isRegisterSuccess")
        Log.i("WXSDK", "微信是否安装: ${wxApi.isWXAppInstalled}")

        lifecycleScope.launch {
            shareLocationStateFlow.collect {
                when (it) {
                    is ApiState.Loading -> {
                        // 加载中：显示进度条，隐藏其他视图
                    }
                    is ApiState.Success -> {
                        it.data?.let { token -> shareToWeChat(token) }
                    }
                    is ApiState.Error -> {
                        // 失败：显示错误信息，隐藏其他视图
                    }
                    is ApiState.Idle -> {
                        // 初始状态，无需处理
                    }
                }
            }
        }
    }

    /**
     * 核心分享逻辑（对应原JS代码）
     * @param token 要分享的token
     */
    private fun shareToWeChat(token: String) {
        // 1. 编码token（对应JS的encodeURIComponent）
        val encodedToken = try {
            URLEncoder.encode(token, "UTF-8")
        } catch (e: Exception) {
            context?.showToast("Token编码失败")
            return
        }

        // 4. 检查微信是否安装
        if (!wxApi.isWXAppInstalled) {
            context?.showToast("未安装微信")
            return
        }

        val endTime = System.currentTimeMillis() + 5 * 60 * 1000 // 加5分钟

        // 3. 拼接分享链接
        val shareUrl = "https://www.ezbeidou.com/share?token=$encodedToken&endTime=$endTime"
        println("分享链接：$shareUrl")

        // 5. 构建微信分享对象
        // 5.1 创建网页对象
        val webpageObject = WXWebpageObject().apply {
            webpageUrl = shareUrl
        }

        // 5.2 创建媒体消息
        val msg = WXMediaMessage(webpageObject).apply {
            title = "实时位置" // 分享标题
            description = carnum // 分享摘要
            // 注意：微信分享图片不能直接用网络URL，需先下载为Bitmap
            thumbData = getValidWXThumbnail() // 缩略图（必填）
        }

        // 5.3 创建发送请求
        val req = SendMessageToWX.Req().apply {
            transaction = "webpage_share_${System.currentTimeMillis()}" // 唯一标识
            message = msg
            scene = SendMessageToWX.Req.WXSceneSession // 分享到微信会话（对应JS的WXSceneSession）
        }

        // 6. 发送分享请求
        val result = wxApi.sendReq(req)
        if (!result) {
            context?.showToast("分享请求发送失败")
        }
    }

    /**
     * 生成符合微信要求的缩略图（≤32KB，80x80px，PNG/JPG格式）
     * @return 合规的byte[]，null表示生成失败
     */
    private fun getValidWXThumbnail(): ByteArray? {
        return try {
            // 步骤1：加载图片（优先用本地资源，网络图片需先下载）
            val drawable = context.resources.getDrawable(R.drawable.icon_logo)
            var bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            // 步骤2：压缩分辨率（固定80x80px，微信推荐尺寸）
            bitmap = Bitmap.createScaledBitmap(bitmap, 80, 80, true)

            // 步骤3：逐级压缩质量，确保≤32KB
            val baos = ByteArrayOutputStream()
            var quality = 80 // 初始压缩质量
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)

            // 循环压缩直到≤32KB（32*1024=32768字节）
            while (baos.toByteArray().size > 32768 && quality > 10) {
                baos.reset() // 重置输出流
                quality -= 10 // 每次降低10%质量
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            }

            // 步骤4：释放Bitmap，避免内存泄漏
            val thumbData = baos.toByteArray()
            bitmap.recycle()
            baos.close()

            // 调试：打印缩略图大小
            Log.d("WXShare", "缩略图大小：${thumbData.size} 字节")
            thumbData
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}