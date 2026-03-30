package com.fx.zfcar.training.safetytraining

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.fx.zfcar.databinding.ActivityStudyDetailBinding
import com.fx.zfcar.net.ApiConfig
import com.fx.zfcar.net.CoursewareItem
import com.fx.zfcar.net.CoursewareViewData
import com.fx.zfcar.net.SafeStudyData
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.training.viewmodel.ExamViewModel
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.DateUtil.secondToDate
import com.fx.zfcar.util.SPUtils
import com.fx.zfcar.viewmodel.ApiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue

class StudyDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudyDetailBinding
    private lateinit var exoPlayer: ExoPlayer

    private val viewModel by viewModels<SafetyTrainingViewModel>()
    private val examViewModel by viewModels<ExamViewModel>()

    // StateFlow 全在 Activity
    private val detailState = MutableStateFlow<ApiState<CoursewareViewData>>(ApiState.Loading)
    private val studyState = MutableStateFlow<ApiState<SafeStudyData>>(ApiState.Loading)
    private val timeConfigState = MutableStateFlow<ApiState<Int>>(ApiState.Loading)

    // 参数
    private var subjectId = ""
    private var safetyPlanId = ""
    private var name = ""
    private var content = ""

    // 播放与计时
    private var videoStartTime = 0
    private var currentSecond = 0
    private var totalDuration = 0
    private var studySeconds = 0
    private var checkFaceInterval = 0
    private var definition = 0

    // 状态
    private var hasVideo = false
    private var hasImage = false
    private var isFaceChecking = false

    // 定时器
    private var imageTimer: CountDownTimer? = null
    private var restartTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudyDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.extras?.let {
            subjectId = it.getString("subjectId", "")
            safetyPlanId = it.getString("safetyPlanId", "")
            name = it.getString("name", "")
            content = it.getString("content", "")
        }

        initPlayer()
        initClick()
        observeData()
    }

    private fun initPlayer() {
        exoPlayer = ExoPlayer.Builder(this).build()
        binding.playerView.player = exoPlayer
        binding.playerView.visibility = View.GONE
        binding.ivBack.visibility = View.GONE
    }

    private fun initClick() {
        binding.ivBack.setOnClickListener { finish() }
        binding.ivBackContent.setOnClickListener { finish() }
    }

    private fun observeData() {
        lifecycleScope.launch {
            detailState.collectLatest { state ->
                when (state) {
                    is ApiState.Success -> {
                        state.data?.let {
                            handleCourseData(state.data)
                        }
                    }
                    is ApiState.Error -> showToast(state.msg)
                    else -> Unit
                }
            }
        }

        lifecycleScope.launch {
            studyState.collectLatest { state ->
                if (state is ApiState.Success) {
                    state.data?.let {
                        handleStudyFinish(state.data)
                    }
                }
            }
        }

        lifecycleScope.launch {
            timeConfigState.collectLatest { state ->
                if (state is ApiState.Success) {
                    state.data?.let {
                        startRestartTimer(state.data)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadCourseDetail()
        getTimeConfig()
    }

    // ==================== 加载课程详情 ====================
    private fun loadCourseDetail() {
        val params = mapOf(
            "subject_id" to subjectId,
            "training_publicplan_id" to safetyPlanId
        )
        examViewModel.getCoursewareView(params, detailState)
    }

    // ==================== 解析数据 ====================
    private fun handleCourseData(data: CoursewareViewData) {
        val item = data.courrow
        val plan = data.row

        binding.tvTitle.text = name
        binding.tvCourseName.text = item.name
        binding.tvContent.text = content

        // 时长
        totalDuration = item.longtime
        videoStartTime = data.longtime
        checkFaceInterval = plan.checktime

        // 格式化
        item.time = secondToDate(item.longtime)
        item.studytime_text = secondToDate(data.longtime)

        binding.tvTotalTime.text = "课程时长：${item.time}"
        binding.tvStudyTime.text = "已学习：${item.studytime_text}"

        // 图片
        hasImage = item.imgurl.isNotEmpty()
        if (hasImage) {
            binding.ivCourse.visibility = View.VISIBLE
            Glide.with(this)
                .load("${ApiConfig.BASE_URL_TRAINING}${item.imgurl}")
                .into(binding.ivCourse)
            startImageCountTimer()
        }

        // 视频
        hasVideo = item.videourl.isNotEmpty()
        if (hasVideo) {
            binding.playerView.visibility = View.VISIBLE
            binding.ivBack.visibility = View.VISIBLE
            startVideoPlay(item)
        }
    }

    // ==================== 视频播放 ====================
    private fun startVideoPlay(item: CoursewareItem) {
        val videoUrl = when (definition) {
            0 -> item.videourl
            1 -> item.standardvideourl
            else -> item.highvideourl
        }

        val mediaItem = MediaItem.fromUri(videoUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.seekTo(videoStartTime * 1000L)
        exoPlayer.playWhenReady = true
        exoPlayer.prepare()

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    monitorVideoProgress()
                }
            }
        })
    }

    // ==================== 视频进度监听 ====================
    private fun monitorVideoProgress() {
        lifecycleScope.launch {
            while (exoPlayer.isPlaying) {
                delay(1000)
                currentSecond = (exoPlayer.currentPosition / 1000).toInt()
                onTimeTick()
            }
        }
    }

    // ==================== 图片计时 ====================
    private fun startImageCountTimer() {
        imageTimer = object : CountDownTimer(1000, 1000) {
            override fun onTick(millis: Long) {}
            override fun onFinish() {
                currentSecond++
                onTimeTick()
                startImageCountTimer()
            }
        }.start()
    }

    // ==================== 每秒回调：计时 + 人脸检测 ====================
    private fun onTimeTick() {
        studySeconds = currentSecond - videoStartTime
        binding.tvStudyTime.text = "已学习：${secondToDate(currentSecond)}"

        // 定时人脸检测
        if (checkFaceInterval > 0 && currentSecond % (checkFaceInterval * 60) == 0 && studySeconds > 0) {
            if (!isFaceChecking) {
                isFaceChecking = true
                exoPlayer.pause()
                submitStudyRecord()
            }
        }

        // 播放完成
        if (currentSecond >= totalDuration) {
            exoPlayer.pause()
            submitStudyRecord()
        }
    }

    // ==================== 提交学习记录 ====================
    private fun submitStudyRecord() {
        viewModel.safeStudy(subjectId, safetyPlanId,
            studySeconds.toString(), studyState)
    }

    // ==================== 学习完成：跳课或人脸 ====================
    private fun handleStudyFinish(data: SafeStudyData) {
        if (data.nextsubject_id != 0) {
            Toast.makeText(this, "即将播放下一课", Toast.LENGTH_SHORT).show()
            lifecycleScope.launch {
                delay(1000)
                val intent = Intent(this@StudyDetailActivity, StudyDetailActivity::class.java)
                intent.putExtra("subjectId", data.nextsubject_id.toString())
                intent.putExtra("safetyPlanId", safetyPlanId)
                startActivity(intent)
                finish()
            }
        }

        if (data.isend == 0) {
            val intent = Intent(this, FaceCheckActivity::class.java)
            intent.putExtra("safetyPlanId", safetyPlanId)
            intent.putExtra("type", "daily")
            intent.putExtra("faceType", "end")
            startActivity(intent)
        }

        isFaceChecking = false
    }

    // ==================== 超时重启 ====================
    fun stopTime(view: View) {
        restartTimer?.cancel()
        getTimeConfig()
    }

    private fun getTimeConfig() {
        viewModel.getConfigTime(timeConfigState)
    }

    private fun startRestartTimer(seconds: Int) {
        restartTimer?.cancel()
        restartTimer = object : CountDownTimer(seconds * 1000L, 1000) {
            override fun onTick(millis: Long) {}
            override fun onFinish() {
                SPUtils.clear()
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
        }.start()
    }

    override fun onPause() {
        super.onPause()
        exoPlayer.pause()
        imageTimer?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
        restartTimer?.cancel()
    }
}