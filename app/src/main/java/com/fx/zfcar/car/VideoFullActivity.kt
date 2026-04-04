package com.fx.zfcar.car

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.fx.zfcar.databinding.ActivityVideoFullBinding
import com.fx.zfcar.util.PressEffectUtils

class VideoFullActivity : AppCompatActivity() {

    companion object {
        const val KEY_VIDEO_URL = "key_video_url"
        const val KEY_TITLE = "key_title"
    }

    private lateinit var binding: ActivityVideoFullBinding
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoFullBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvTitle.text = intent.getStringExtra(KEY_TITLE).orEmpty().ifEmpty { "视频回放" }
        PressEffectUtils.setCommonPressEffect(binding.ivBack)
        binding.ivBack.setOnClickListener { finish() }

        val videoUrl = intent.getStringExtra(KEY_VIDEO_URL).orEmpty()
        if (videoUrl.isNotEmpty()) {
            initPlayer(videoUrl)
        }
    }

    private fun initPlayer(videoUrl: String) {
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer
            exoPlayer.setMediaItem(MediaItem.fromUri(videoUrl))
            exoPlayer.playWhenReady = true
            exoPlayer.prepare()
        }
    }

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.playerView.player = null
        player?.release()
        player = null
    }
}
