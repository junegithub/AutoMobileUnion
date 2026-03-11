package com.fx.zfcar.training.safetytraining

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.fx.zfcar.R
import com.fx.zfcar.databinding.ActivityScoreDetailBinding
import com.fx.zfcar.net.ExamQuestion
import com.fx.zfcar.net.ExamResultData
import com.fx.zfcar.net.ExamResultQuestion
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.training.viewmodel.ExamViewModel
import com.fx.zfcar.viewmodel.ApiState
import com.google.android.flexbox.FlexboxLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ScoreDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScoreDetailBinding
    private val viewModel by viewModels<ExamViewModel>()

    // 页面参数
    private var id = 0
    private var trainingSafetyPlanId = 0
    private var type: String = ""

    private val _scoreDetailFlow = MutableStateFlow<ApiState<ExamResultData>>(ApiState.Idle)
    val scoreDetailFlow: StateFlow<ApiState<ExamResultData>> = _scoreDetailFlow.asStateFlow()

    private val _questionDetailFlow = MutableStateFlow<ApiState<ExamQuestion>>(ApiState.Idle)
    val questionDetailFlow: StateFlow<ApiState<ExamQuestion>> = _questionDetailFlow.asStateFlow()

    // 页面状态
    private var questionList: List<ExamResultQuestion> = emptyList()
    private var currentQuestionDetail: ExamQuestion? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScoreDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取参数
        getIntentParams()

        // 初始化视图
        initView()

        // 监听StateFlow
        observeStates()

        // 获取成绩单数据
        getScoreDetail()
    }

    // 获取页面参数
    private fun getIntentParams() {
        intent?.let {
            id = it.getIntExtra("id", 0)
            trainingSafetyPlanId = it.getIntExtra("training_safetyplan_id", 0)
            type = it.getStringExtra("type") ?: ""
        }
    }

    // 初始化视图
    private fun initView() {
        // 标题栏返回按钮
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // 遮罩层点击隐藏
        binding.maskLayout.setOnClickListener {
            hideQuestionDetail()
        }

        // 初始化ExamWidget（隐藏按钮）
        binding.examWidgetDetail.apply {
            setShowIndexText(false)
            setNumBoxShow(false)
            // 隐藏提交/下一题按钮（通过反射或修改ExamWidget添加隐藏按钮的方法）
            btnNext().visibility = View.GONE
            btnSubmit().visibility = View.GONE
        }
    }

    // 获取成绩单详情
    private fun getScoreDetail() {
        viewModel.getExamResult(buildScoreDetailParams(), _scoreDetailFlow)
    }

    // 监听状态流
    private fun observeStates() {
        // 监听成绩单状态
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                scoreDetailFlow.collectLatest { state ->
                    when (state) {
                        is ApiState.Idle -> {}
                        is ApiState.Loading -> {}
                        is ApiState.Success -> {
                            if (state.data != null) {
                                val scoreData = state.data

                                // 更新UI
                                binding.tvExamTitle.text = scoreData.row.exam_name
                                binding.tvScore.text = scoreData.info.score.toString()
                                binding.tvFullScore.text = "满分：${scoreData.row.score}分"

                                // 加载签名图片
                                scoreData.info.imgurl?.let {
                                    Glide.with(this@ScoreDetailActivity)
                                        .load(scoreData.info.imgurl)
                                        .into(binding.ivSign)
                                }

                                // 保存题目列表
                                questionList = scoreData.questions

                                // 构建答题卡
                                buildAnswerCards()
                            } else {
                                showToast("获取成绩单失败")
                                // 跳转到答题页面
                                goToExamPage()
                            }
                        }
                        is ApiState.Error -> showToast(state.msg)
                    }
                }
            }
        }

        // 监听题目详情状态
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                questionDetailFlow.collectLatest { state ->
                    when (state) {
                        is ApiState.Idle -> {}
                        is ApiState.Loading -> {}
                        is ApiState.Success -> {
                            state.data?.let {
                                currentQuestionDetail = state.data

                                // 更新ExamWidget
                                binding.examWidgetDetail.setDataList(listOf(state.data))
                                binding.examWidgetDetail.setIndex(0)

                                // 显示遮罩层
                                showQuestionDetail()
                            }
                        }
                        is ApiState.Error -> {
                            showToast(state.msg ?: "获取题目详情失败")
                            hideQuestionDetail()
                        }
                    }
                }
            }
        }
    }

    // 构建答题卡
    private fun buildAnswerCards() {
        binding.flowAnswerCards.removeAllViews()

        questionList.forEachIndexed { index, question ->
            // 创建题目卡片
            val cardView = TextView(this).apply {
                text = "${index + 1}"
                textSize = 20f
                gravity = Gravity.CENTER
                width = 40.dp
                height = 40.dp
                layoutParams = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    // 每5个元素清除右边距（可选，根据需求调整）
                    if ((index + 1) % 5 == 0) {
                        marginEnd = 0
                    } else {
                        marginEnd = 10.dp
                    }
                }

                // 设置背景颜色（答对/答错）
                if (question.uanswercolor == "green") {
                    setBackgroundResource(R.drawable.bg_green_answer)
                    setTextColor(resources.getColor(R.color.green_17a83e, null))
                } else {
                    setBackgroundResource(R.drawable.bg_red_answer)
                    setTextColor(resources.getColor(R.color.red_ff445a, null))
                }

                // 点击事件
                setOnClickListener {
                    getQuestionDetail(question.id.toString(), question.uanswer ?: "")
                }
            }

            binding.flowAnswerCards.addView(cardView)
        }
    }

    // 获取题目详情
    private fun getQuestionDetail(questionId: String, userAnswer: String) {
        viewModel.getQuestionView(buildQuestionDetailParams(questionId, userAnswer),
            _questionDetailFlow)
    }

    // 显示题目详情遮罩层
    private fun showQuestionDetail() {
        binding.maskLayout.visibility = View.VISIBLE
    }

    // 隐藏题目详情遮罩层
    private fun hideQuestionDetail() {
        binding.maskLayout.visibility = View.GONE
    }

    // 跳转到答题页面
    private fun goToExamPage() {
        val intent = Intent(this, ExamManagerActivity::class.java).apply {
            putExtra("id", id)
            putExtra("training_safetyplan_id", trainingSafetyPlanId)
            putExtra("type", type)
        }
        startActivity(intent)
        finish()
    }

    // 构建成绩单请求参数
    private fun buildScoreDetailParams(): Map<String, String> {
        return when (type) {
            "subject" -> mapOf(
                "exam_id" to id.toString(),
                "subject_id" to trainingSafetyPlanId.toString(),
                "type" to type
            )
            "daily" -> mapOf(
                "exam_id" to id.toString(),
                "training_publicplan_id" to trainingSafetyPlanId.toString(),
                "type" to type
            )
            "before" -> mapOf(
                "exam_id" to id.toString(),
                "training_before_id" to trainingSafetyPlanId.toString(),
                "type" to type
            )
            else -> mapOf(
                "exam_id" to id.toString(),
                "training_safetyplan_id" to trainingSafetyPlanId.toString(),
                "type" to type
            )
        }
    }

    // 构建题目详情请求参数
    private fun buildQuestionDetailParams(questionId: String, userAnswer: String): Map<String, String> {
        return when (type) {
            "daily" -> mapOf(
                "exam_id" to id.toString(),
                "question_id" to questionId,
                "uanswer" to userAnswer,
                "training_publicplan_id" to trainingSafetyPlanId.toString()
            )
            "before" -> mapOf(
                "exam_id" to id.toString(),
                "question_id" to questionId,
                "uanswer" to userAnswer,
                "training_before_id" to trainingSafetyPlanId.toString()
            )
            "subject" -> mapOf(
                "exam_id" to id.toString(),
                "question_id" to questionId,
                "uanswer" to userAnswer,
                "subject_id" to trainingSafetyPlanId.toString()
            )
            else -> mapOf(
                "exam_id" to id.toString(),
                "question_id" to questionId,
                "uanswer" to userAnswer,
                "training_safetyplan_id" to trainingSafetyPlanId.toString()
            )
        }
    }

    // 标题栏返回
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    // dp转px扩展函数
    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}
