package com.fx.zfcar.training.safetytraining

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.fx.zfcar.R
import com.fx.zfcar.databinding.ActivityCourseDetailBinding
import com.fx.zfcar.net.CoursewareItem
import com.fx.zfcar.net.CoursewareViewData
import com.fx.zfcar.net.EvaluateClassRequest
import com.fx.zfcar.net.FaceData
import com.fx.zfcar.net.TrainingPublicPlan
import com.fx.zfcar.training.viewmodel.ExamViewModel
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.SPUtils
import com.fx.zfcar.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class CourseDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCourseDetailBinding
    private val viewModel by viewModels<ExamViewModel>()
    private val trainingViewModel by viewModels<SafetyTrainingViewModel>()

    // 页面参数
    private var subjectId: String = ""
    private var safetyPlanId: String = ""
    private var trainName: String = ""
    private var nextSubjectId: String = "" // 下一课ID

    // AndroidX Media3 播放器
    private var player: ExoPlayer? = null
    private var isPlayerReady = false
    private var playbackPosition = 0L
    private var playWhenReady = true
    private var definition = 0 // 0-流畅 1-标清 2-高清
    private var definitionShow = true // 清晰度弹窗显示标记

    // 核心业务状态
    private var courseInfo: CoursewareItem? = null
    private var trainAbout: TrainingPublicPlan? = null
    private var hasStudyTime = 0 // 已学习时长（秒）
    private var videoRealTime = 0 // 视频实时播放进度
    private var videotime = 0 // 初始播放位置
    private var alltime = 0 // 总时长
    private var checktime = 0 // 人脸识别间隔
    private var longtime = 0 // 本次学习时长
    private var clickNumber = 0
    private var ischeckface = false
    private var flag = true // 首次人脸识别标记
    private var pageScoll = 0 // 滚动位置
    private var evaluateInput = ""
    private var eveluateShow = false // 评价弹窗显示标记

    // 定时器（完整实现原逻辑）
    private var timer1: Timer? = null
    private var timer2: Timer? = null
    private var timer3: Timer? = null
    private var timer4: Timer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var urlFlag = true // 跳转标记

    // StateFlow
    private val _courseDetailFlow = MutableStateFlow<ApiState<CoursewareViewData>>(ApiState.Idle)
    val courseDetailFlow: StateFlow<ApiState<CoursewareViewData>> = _courseDetailFlow.asStateFlow()

    private val _studyTimeFlow = MutableStateFlow<ApiState<FaceData>>(ApiState.Idle)
    val studyTimeFlow: StateFlow<ApiState<FaceData>> = _studyTimeFlow.asStateFlow()

    private val _evaluateFlow = MutableStateFlow<ApiState<Any>>(ApiState.Idle)
    val evaluateFlow: StateFlow<ApiState<Any>> = _evaluateFlow.asStateFlow()

    // 生命周期观察者
    private val lifecycleObserver = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> {
                if (player == null) {
                    courseInfo?.videourl?.let { initPlayer(it) }
                } else {
                    player?.playWhenReady = playWhenReady
                }
                // 恢复定时器
                resumeTimers()
            }
            Lifecycle.Event.ON_STOP -> {
                player?.let {
                    playbackPosition = it.currentPosition
                    playWhenReady = it.playWhenReady
                    it.playWhenReady = false
                }
                // 暂停定时器
                pauseTimers()
            }
            Lifecycle.Event.ON_DESTROY -> {
                releasePlayer()
                cancelAllTimers()
                // 保存滚动位置
                saveScrollPosition()
            }
            else -> {}
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 添加生命周期观察者
        lifecycle.addObserver(lifecycleObserver)

        // 获取参数（完整）
        getIntentParams()

        // 初始化视图（补全所有交互）
        initView()

        // 监听StateFlow
        observeStates()

        // 获取课程详情
        getCourseDetail()
    }

    // 获取页面参数（完整）
    private fun getIntentParams() {
        intent?.let {
            subjectId = it.getStringExtra("subjectId") ?: ""
            safetyPlanId = it.getStringExtra("safetyPlanId") ?: ""
            trainName = it.getStringExtra("trainName") ?: ""
            nextSubjectId = it.getStringExtra("next") ?: ""
        }
    }

    // 初始化视图（补全所有交互逻辑）
    private fun initView() {
        // 返回按钮
        binding.ivBack.setOnClickListener { onBackPressed() }
        binding.ivTitleBack.setOnClickListener { onBackPressed() }

        // 评价按钮
        binding.tvEvaluate.setOnClickListener {
            eveluateShow = true
            showEvaluateDialog()
        }

        // 评价弹窗
        binding.llEvaluateMask.setOnClickListener {
            eveluateShow = false
            hideEvaluateDialog()
        }
        binding.tvCancelEvaluate.setOnClickListener {
            eveluateShow = false
            hideEvaluateDialog()
        }
        binding.tvConfirmEvaluate.setOnClickListener { submitEvaluate() }

        // 清晰度切换
        binding.tvDefinitionSmooth.setOnClickListener {
            changeDefinition(0)
            hideDefinitionDialog()
        }
        binding.tvDefinitionStandard.setOnClickListener {
            changeDefinition(1)
            hideDefinitionDialog()
        }
        binding.tvDefinitionHigh.setOnClickListener {
            changeDefinition(2)
            hideDefinitionDialog()
        }
        binding.llDefinitionMask.setOnClickListener { hideDefinitionDialog() }

        // 滚动监听（实时记录）
        binding.scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            pageScoll = scrollY
        }
    }

    /**
     * 初始化播放器（含清晰度切换逻辑）
     */
    private fun initPlayer(videoUrl: String) {
        releasePlayer() // 先释放旧播放器

        player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(10000)
            .build().also { exoPlayer ->

                binding.playerView.player = exoPlayer
                /*binding.playerView.setControllerVisibilityListener { visibility ->
                    // 播放器控制栏显示/隐藏时切换清晰度弹窗显示状态
                    definitionShow = visibility == View.VISIBLE
                    if (visibility == View.VISIBLE) {
                        showDefinitionDialog()
                    } else {
                        hideDefinitionDialog()
                    }
                }*/

                // 设置播放源
                val mediaItem = MediaItem.fromUri(videoUrl)
                exoPlayer.setMediaItem(mediaItem)

                // 恢复播放位置
                exoPlayer.seekTo(playbackPosition)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.prepare()

                // 播放器监听（完整）
                exoPlayer.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        isPlayerReady = playbackState == Player.STATE_READY

                        when (playbackState) {
                            Player.STATE_READY -> {
                                startProgressListener()
                                definitionShow = false // 播放时隐藏清晰度弹窗
                                hideDefinitionDialog()
                            }
                            Player.STATE_ENDED -> {
                                ischeckface = true
                                checkFace()
                                stopProgressListener()
                            }
                            Player.STATE_IDLE, Player.STATE_BUFFERING -> {}
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        showToast("视频播放出错：${error.message}")
                        stopProgressListener()
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        if (isPlaying) {
                            definitionShow = false
                            hideDefinitionDialog()
                            startProgressListener()
                        } else {
                            definitionShow = true
                            showDefinitionDialog()
                            stopProgressListener()
                        }
                    }
                })
            }

        binding.flVideoContainer.visibility = View.VISIBLE
    }

    /**
     * 切换清晰度
     */
    private fun changeDefinition(newDefinition: Int) {
        if (definition == newDefinition) return

        definition = newDefinition
        courseInfo?.let { course ->
            val playUrl = when (definition) {
                1 -> course.standardvideourl ?: course.videourl ?: return
                2 -> course.highvideourl ?: course.videourl ?: return
                else -> course.videourl ?: return
            }

            // 保存当前播放位置
            playbackPosition = player?.currentPosition ?: 0L
            playWhenReady = player?.playWhenReady ?: false

            // 重新初始化播放器
            initPlayer(playUrl)

            // 更新选中状态
            updateDefinitionSelectedState()
        }
    }

    /**
     * 更新清晰度选中状态
     */
    private fun updateDefinitionSelectedState() {
        binding.tvDefinitionSmooth.setTextColor(if (definition == 0) getColor(R.color.blue_3f83f7) else getColor(R.color.white))
        binding.tvDefinitionStandard.setTextColor(if (definition == 1) getColor(R.color.blue_3f83f7) else getColor(R.color.white))
        binding.tvDefinitionHigh.setTextColor(if (definition == 2) getColor(R.color.blue_3f83f7) else getColor(R.color.white))
    }

    /**
     * 显示/隐藏清晰度弹窗
     */
    private fun showDefinitionDialog() {
        if (definitionShow && courseInfo?.videourl != null) {
            binding.llDefinitionMask.visibility = View.VISIBLE
            updateDefinitionSelectedState()
        }
    }

    private fun hideDefinitionDialog() {
        binding.llDefinitionMask.visibility = View.GONE
    }

    /**
     * 显示/隐藏评价弹窗
     */
    private fun showEvaluateDialog() {
        binding.llEvaluateMask.visibility = View.VISIBLE
        binding.etEvaluateContent.setText("")
        evaluateInput = ""
    }

    private fun hideEvaluateDialog() {
        binding.llEvaluateMask.visibility = View.GONE
    }

/*    private fun initOtherCourseList(courseList: List<OtherCourse>) {
        binding.llOtherCourses.removeAllViews()

        courseList.forEachIndexed { index, course ->
            // 课程项容器
            val courseItem = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 16.dp
                }
                orientation = LinearLayout.VERTICAL
                background = getDrawable(R.drawable.bg_course_item)
                padding = 16.dp
            }

            // 课程类型+时长
            val typeTimeView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 8.dp
                }
                text = "${course.type_text}    ${course.time}"
                textSize = 12.sp
                setTextColor(getColor(R.color.text_666))
            }

            // 课程名称
            val nameView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 12.dp
                }
                text = course.name
                textSize = 16.sp
                setTextColor(getColor(R.color.text_333))
            }

            // 状态+操作栏
            val statusBar = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            // 学习状态
            val statusView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    rightMargin = 16.dp
                }
                val statusText = when {
                    course.studytype == 0 -> "未学习"
                    course.time == course.studytimeText -> "已完成"
                    else -> "已学习${course.studytimeText}"
                }
                text = statusText
                textSize = 14.sp
                setTextColor(if (course.studytype == 1) getColor(R.color.green_17a83e) else getColor(R.color.red_ff445a))
                background = getDrawable(if (course.studytype == 1) R.drawable.bg_green_tag else R.drawable.bg_red_tag)
                padding = 4.dp
                gravity = Gravity.CENTER
            }

            // 开始学习按钮
            val studyBtn = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                text = "开始学习 >"
                textSize = 14.sp
                setTextColor(getColor(R.color.blue_3f83f7))
                setOnClickListener {
                    goToCourseDetail(course, courseList, index)
                }
            }

            // 组装视图
            statusBar.addView(statusView)
            statusBar.addView(studyBtn)
            courseItem.addView(typeTimeView)
            courseItem.addView(nameView)
            courseItem.addView(statusBar)

            binding.llOtherCourses.addView(courseItem)
        }
    }*/

    /*private fun goToCourseDetail(course: OtherCourse, courseList: List<OtherCourse>, index: Int) {
        // 停止当前视频播放
        player?.pause()
        cancelAllTimers()

        // 计算下一课ID
        val nextId = when {
            courseList.size <= 1 -> subjectId
            index + 1 == courseList.size -> courseList[0].id
            else -> courseList[index + 1].id
        }

        // 跳转到课程详情
        val intent = Intent(this, CourseDetailActivity::class.java).apply {
            putExtra("safetyPlanId", safetyPlanId)
            putExtra("subjectId", course.id)
            putExtra("name", course.name)
            putExtra("next", nextId)
        }
        startActivity(intent)

        // 提交学习时长
        if (!ischeckface) {
            checkFace()
        }
    }*/

    /**
     * 启动所有定时器（还原原逻辑）
     */
    private fun startAllTimers() {
        cancelAllTimers()

        // 定时器1：1秒后恢复滚动位置（已在接口回调中实现）

        // 定时器2：20秒后首次人脸识别
        if (flag && hasStudyTime < alltime) {
            flag = false
            timer2 = Timer().apply {
                schedule(object : TimerTask() {
                    override fun run() {
                        handler.post {
                            val longtime = 20
                            urlFlag = false
                            navigateToFaceCheck(longtime)
                        }
                    }
                }, 20000)
            }
        }

        // 定时器3：每隔20分钟人脸识别
        timer3 = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    handler.post {
                        val longtime = 1200
                        urlFlag = false
                        navigateToFaceCheck(longtime)
                    }
                }
            }, 1200000, 1200000)
        }

        // 定时器4：学习时长累计
        if (hasStudyTime < alltime) {
            var time = hasStudyTime
            timer4 = Timer().apply {
                scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        handler.post {
                            longtime = time - videotime
                            if (time == alltime) {
                                cancel()
                                ischeckface = true
                                urlFlag = false
                                navigateToFaceCheck(longtime)
                                return@post
                            }
                            time++
                            hasStudyTime = time
                            binding.tvStudyTime.text = "已学习:${secondToDate(time)}"
                        }
                    }
                }, 0, 1000)
            }
        }
    }

    /**
     * 暂停所有定时器
     */
    private fun pauseTimers() {
        timer2?.cancel()
        timer3?.cancel()
        timer4?.cancel()
    }

    /**
     * 恢复所有定时器
     */
    private fun resumeTimers() {
        if (hasStudyTime < alltime) {
            startAllTimers()
        }
    }

    /**
     * 取消所有定时器
     */
    private fun cancelAllTimers() {
        timer1?.cancel()
        timer2?.cancel()
        timer3?.cancel()
        timer4?.cancel()

        timer1 = null
        timer2 = null
        timer3 = null
        timer4 = null
    }

    /**
     * 保存滚动位置到SP
     */
    private fun saveScrollPosition() {
        courseInfo?.id?.let { courseId ->
            SPUtils.save("course_scroll_$courseId", pageScoll)
        }
    }

    /**
     * 跳转到人脸识别页面（完整参数）
     */
    private fun navigateToFaceCheck(longtime: Int, isEnd: Boolean = false) {
        player?.pause()

        val intent = Intent(this, FaceCheckActivity::class.java).apply {
            putExtra("safetyPlanId", safetyPlanId)
            putExtra("subjectId", subjectId)
            putExtra("longtime", longtime.toString())
            putExtra("pageScoll", pageScoll.toString())
            if (isEnd) {
                putExtra("type", "newFace")
                putExtra("faceType", "end")
            }
        }
        startActivity(intent)
    }

    // 以下方法为核心逻辑（已优化，保留完整实现）
    private fun getCourseDetail() {
        val params = mapOf(
            "subject_id" to subjectId,
            "training_safetyplan_id" to safetyPlanId
        )

        viewModel.getCoursewareView(
            params = params,
            stateFlow = _courseDetailFlow
        )
    }

    private fun observeStates() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                courseDetailFlow.collectLatest { state ->
                    when (state) {
                        is ApiState.Idle -> {}
                        is ApiState.Loading -> {}
                        is ApiState.Success -> handleCourseDetailSuccess(state.data)
                        is ApiState.Error -> showToast(state.msg)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                studyTimeFlow.collectLatest { state ->
                    when (state) {
                        is ApiState.Idle -> {}
                        is ApiState.Loading -> {}
                        is ApiState.Success -> handleStudyTimeSuccess(state.data)
                        is ApiState.Error -> showToast(state.msg)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                evaluateFlow.collectLatest { state ->
                    when (state) {
                        is ApiState.Idle -> {}
                        is ApiState.Loading -> {}
                        is ApiState.Success -> {
                            showToast("评价成功")
                            hideEvaluateDialog()
                        }
                        is ApiState.Error -> showToast(state.msg)
                    }
                }
            }
        }
    }

    private fun handleCourseDetailSuccess(data: CoursewareViewData?) {
        if (data != null) {
            val courseData = data

            // 保存基础信息
            courseInfo = courseData.courrow
            trainAbout = courseData.row
            checktime = courseData.row.checktime
            alltime = courseData.courrow.longtime

            // 处理学习时长
            hasStudyTime = courseData.longtime
            videotime = if (hasStudyTime == alltime) {
                0 // 已完成则重置
            } else {
                courseData.longtime
            }

            // 更新UI
            binding.tvCourseName.text = courseData.courrow.name
            binding.tvCourseTitle.text = courseData.courrow.name
            binding.tvTotalTime.text = "课程时长${courseData.courrow.studytime}"
            binding.tvStudyTime.text = if (hasStudyTime == alltime) {
                "已完成"
            } else {
                "已学习:${secondToDate(hasStudyTime)}"
            }

            // 处理视频
            courseData.courrow.videourl?.let {
                val playUrl = when (definition) {
                    1 -> courseData.courrow.standardvideourl ?: it
                    2 -> courseData.courrow.highvideourl ?: it
                    else -> it
                }
                initPlayer(playUrl)
            }

            // 处理图片
            courseData.courrow.imgurl?.let { imgUrl ->
                if (imgUrl.isNotEmpty()) {
                    binding.vTitlePlaceholder.visibility = View.VISIBLE
                    binding.llTitleBar.visibility = View.VISIBLE
                    binding.ivCourseImage.visibility = View.VISIBLE

                    Glide.with(this)
                        .load(imgUrl)
                        .into(binding.ivCourseImage)

                    // 恢复滚动位置
//                    handler.postDelayed({
//                        binding.scrollView.scrollTo(0, courseData.courrow.pageScoll)
//                        pageScoll = courseData.courrow.pageScoll
//                    }, 1000)

                    // 启动定时器（原逻辑）
                    if (hasStudyTime < alltime) {
                        startAllTimers()
                    }
                }
            }

            // 处理其他课程列表
//            val formattedCourses = courseData.otherlist.map { course ->
//                course.copy(
//                    time = secondToDate(course.longtime),
//                    studytimeText = secondToDate(course.studytime)
//                )
//            }
//            initOtherCourseList(formattedCourses)

            // 已完成自动跳转下一课
            if (hasStudyTime == alltime && nextSubjectId.isNotEmpty() && nextSubjectId != "onlyone") {
                handler.postDelayed({
                    val intent = Intent(this, CourseDetailActivity::class.java).apply {
                        putExtra("safetyPlanId", safetyPlanId)
                        putExtra("subjectId", nextSubjectId)
                    }
                    startActivity(intent)
                    finish()
                }, 1000)
            }
        } else {
            showToast("获取课程详情失败")
        }
    }

    private fun handleStudyTimeSuccess(data: FaceData?) {
        if (data != null) {
            // 跳转到下一课
            if (data.nextsubject_id != 0) {
                showToast("即将为您跳转下一课")
                handler.postDelayed({
                    val intent = Intent(this, CourseDetailActivity::class.java).apply {
                        putExtra("safetyPlanId", safetyPlanId)
                        putExtra("subjectId", data.nextsubject_id.toString())
                    }
                    startActivity(intent)
                    finish()
                }, 1000)
            }
            // 人脸识别
//            else if (data.isend == 0) {
//                navigateToFaceCheck(longtime, isEnd = true)
//            }
        }
    }

    private fun submitEvaluate() {
        evaluateInput = binding.etEvaluateContent.text.toString().trim()
        if (evaluateInput.isEmpty()) {
            showToast("请输入评价内容")
            return
        }

        viewModel.evaluateClass(
            EvaluateClassRequest(subjectId, evaluateInput),
            stateFlow = _evaluateFlow
        )
    }

    private fun checkFace() {
        val params = mutableMapOf(
            "subject_id" to subjectId,
            "training_safetyplan_id" to safetyPlanId,
            "longtime" to longtime.toString()
        )

        // 添加滚动位置
        if (courseInfo?.imgurl?.isNotEmpty() == true) {
            params["pageScoll"] = pageScoll.toString()
        }

        trainingViewModel.safetyAdd(
            params = params,
            stateFlow = _studyTimeFlow
        )
    }

    // 工具方法
    private fun secondToDate(seconds: Int): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60

        return when {
            h > 0 -> String.format("%02d小时%02d分%02d秒", h, m, s)
            m > 0 -> String.format("%02d分%02d秒", m, s)
            else -> String.format("%02d秒", s)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // 播放器管理
    private fun releasePlayer() {
        player?.let {
            playbackPosition = it.currentPosition
            playWhenReady = it.playWhenReady
            it.release()
        }
        player = null
        stopProgressListener()
    }

    private fun startProgressListener() {
        stopProgressListener()
        timer4 = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    handler.post {
                        updatePlaybackProgress()
                    }
                }
            }, 0, 1000)
        }
    }

    private fun stopProgressListener() {
        timer4?.cancel()
        timer4 = null
    }

    private fun updatePlaybackProgress() {
        player?.let { player ->
            val currentTime = (player.currentPosition / 1000).toInt()
            clickNumber++

            // 更新已学习时长
            hasStudyTime = currentTime
            binding.tvStudyTime.text = if (hasStudyTime == alltime) {
                "已完成"
            } else {
                "已学习:${secondToDate(hasStudyTime)}"
            }

            // 计算本次学习时长
            longtime = currentTime - videotime

            // 人脸识别校验
            checkFaceDetection(currentTime)

            // 禁止快进
            checkFastForward(currentTime, player)

            // 更新实时进度
            videoRealTime = currentTime

            // 播放完成校验
            if (alltime - currentTime <= 1) {
                ischeckface = true
                player.pause()
                checkFace()
                stopProgressListener()
            }
        }
    }

    private fun checkFaceDetection(currentTime: Int) {
        if (clickNumber % 2 == 0 && clickNumber > 0) {
            // 首次20秒校验
            if (flag && (currentTime - videotime == 20 || currentTime - videotime == 21)) {
                flag = false
                ischeckface = true
                player?.pause()
                navigateToFaceCheck(longtime)
            }

            // 间隔时间校验
            if (checktime > 0 && (currentTime - videotime) % checktime == 0 && (currentTime - videotime) > 0) {
                ischeckface = true
                player?.pause()
                navigateToFaceCheck(longtime)
            }
        }
    }

    private fun checkFastForward(currentTime: Int, player: ExoPlayer) {
        val isReady = 1 // 1-禁止快进
        val jumpTime = if (videoRealTime == 0) videotime + videoRealTime else videoRealTime

        if (isReady == 1 && currentTime > jumpTime && currentTime - jumpTime > 2) {
            player.seekTo(jumpTime * 1000L)
            showToast("未完整看完该视频，不能快进")
            videoRealTime = jumpTime
        }
    }

    // 生命周期与返回逻辑
    override fun onBackPressed() {
        // 停止播放和定时器
        player?.pause()
        cancelAllTimers()

        // 保存滚动位置
        saveScrollPosition()

        // 提交学习时长
        if (!ischeckface) {
            checkFace()
        }

        super.onBackPressed()
    }

    override fun onDestroy() {
        lifecycle.removeObserver(lifecycleObserver)
        cancelAllTimers()
        saveScrollPosition()

        if (!ischeckface) {
            checkFace()
        }

        super.onDestroy()
    }

    // DP/SP 扩展属性
    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    private val Int.sp: Float
        get() = this * resources.displayMetrics.scaledDensity
}