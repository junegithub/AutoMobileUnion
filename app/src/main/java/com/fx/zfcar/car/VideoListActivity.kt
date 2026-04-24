package com.fx.zfcar.car

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebViewClient
import android.util.Log
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fx.zfcar.car.adapter.VideoRecordAdapter
import com.fx.zfcar.databinding.ActivityVideoListBinding
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.util.PressEffectUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VideoListActivity : AppCompatActivity() {

    companion object {
        const val KEY_CAR_NUM = "key_car_num"
        const val KEY_SIM = "key_sim"
        const val KEY_VIDEO_LIST = "key_video_list"
    }

    private lateinit var binding: ActivityVideoListBinding
    private val gson = Gson()
    private lateinit var adapter: VideoRecordAdapter
    private var carNum = ""
    private var sim = ""
    private var selectedItem: VideoPlaybackActivity.VideoRecordItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carNum = intent.getStringExtra(KEY_CAR_NUM).orEmpty()
        sim = intent.getStringExtra(KEY_SIM).orEmpty()

        initView()
        initWebView()
        loadList()
    }

    private fun initView() {
        binding.tvTitle.text = carNum.ifEmpty { "视频列表" }
        PressEffectUtils.setCommonPressEffect(binding.ivBack)
        binding.ivBack.setOnClickListener { finish() }

        adapter = VideoRecordAdapter { item ->
            selectedItem = item
            binding.progressBar.visibility = View.VISIBLE
            playHistory(item)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.settings.allowFileAccess = true
        binding.webView.settings.allowContentAccess = true
        binding.webView.settings.mediaPlaybackRequiresUserGesture = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        binding.webView.addJavascriptInterface(WebAppInterface(), "AndroidInterface")
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: android.webkit.WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    Log.w(
                        "VideoListWebView",
                        "page load error code=${error?.errorCode}, desc=${error?.description}, url=${request.url}"
                    )
                    showToast("网络异常，页面加载失败，请稍后重试")
                }
            }

            override fun onReceivedHttpError(
                view: android.webkit.WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                if (request?.isForMainFrame == true) {
                    Log.w(
                        "VideoListWebView",
                        "page http error code=${errorResponse?.statusCode}, reason=${errorResponse?.reasonPhrase}, url=${request.url}"
                    )
                    showToast("网络异常，页面加载失败，请稍后重试")
                }
            }
        }
        binding.webView.loadUrl("file:///android_asset/hybrid/html/rtvsVideoHistory.html")
    }

    private fun loadList() {
        val videoListJson = intent.getStringExtra(KEY_VIDEO_LIST).orEmpty()
        val listType = object : TypeToken<List<VideoPlaybackActivity.VideoRecordItem>>() {}.type
        val list = runCatching {
            gson.fromJson<List<VideoPlaybackActivity.VideoRecordItem>>(videoListJson, listType)
        }.getOrDefault(emptyList())

        adapter.submitList(list.sortedByDescending { it.StartTime })
        binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun playHistory(item: VideoPlaybackActivity.VideoRecordItem) {
        val simNum = ("0$sim").toLongOrNull() ?: sim.toLongOrNull() ?: 0L
        val startTime = formatVideoTime(item.StartTime)
        val endTime = formatVideoTime(item.EndTime)
        val jsCode = "Connectt($simNum,${item.Channel},\"$startTime\",\"$endTime\",0,0,0,1,'www.ezbeidou.com',17001,0);"

        lifecycleScope.launch {
            delay(300)
            binding.webView.evaluateJavascript(jsCode, null)
        }
    }

    private fun formatVideoTime(time: String): String {
        if (time.length < 12) return time
        return "20${time.substring(0, 2)}-${time.substring(2, 4)}-${time.substring(4, 6)} " +
            "${time.substring(6, 8)}:${time.substring(8, 10)}:${time.substring(10, 12)}"
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun getMessage(data: String) {
            runOnUiThread {
                binding.progressBar.visibility = View.GONE
                val videoUrl = runCatching {
                    gson.fromJson(data, HistoryVideoAction::class.java)?.action
                }.getOrNull().orEmpty()

                if (videoUrl.isEmpty()) {
                    showToast("回放地址获取失败")
                    return@runOnUiThread
                }

                startActivity(
                    Intent(this@VideoListActivity, VideoFullActivity::class.java).apply {
                        putExtra(VideoFullActivity.KEY_VIDEO_URL, videoUrl)
                        putExtra(VideoFullActivity.KEY_TITLE, carNum)
                    }
                )
            }
        }
    }

    data class HistoryVideoAction(
        val action: String = ""
    )
}
