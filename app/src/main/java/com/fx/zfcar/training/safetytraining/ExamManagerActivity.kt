package com.fx.zfcar.training.safetytraining

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.fx.zfcar.databinding.ActivityExamManagerBinding
import com.fx.zfcar.net.ExamQuestion
import com.fx.zfcar.net.ExamResultData
import com.fx.zfcar.net.ExamViewData
import com.fx.zfcar.training.base.ExamWidget
import com.fx.zfcar.training.base.SelectData
import com.fx.zfcar.training.base.SelectFinishData
import com.fx.zfcar.training.notice.SignatureActivity
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.training.viewmodel.ExamViewModel
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.viewmodel.ApiState
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ExamManagerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExamManagerBinding
    private val viewModel by viewModels<ExamViewModel>()

    // 页面参数
    private var id = 0
    private var trainingSafetyPlanId = 0
    private var type: String = ""

    // 页面状态
    private var score: Int = 0
    private var isPass: Boolean = false
    private var isAnswer: Boolean = true
    private var showIndexText: Boolean = true
    private var numBoxShow: Boolean = true
    private var examsId: String = ""
    private var questionList: MutableList<ExamQuestion> = mutableListOf()

    // 检查考试状态的StateFlow
    private val _examStateFlow = MutableStateFlow<ApiState<ExamResultData>>(ApiState.Idle)
    val examStateFlow: StateFlow<ApiState<ExamResultData>> = _examStateFlow.asStateFlow()

    // 获取试题列表的StateFlow
    private val _questionListFlow = MutableStateFlow<ApiState<ExamViewData>>(ApiState.Idle)
    val questionListFlow: StateFlow<ApiState<ExamViewData>> = _questionListFlow.asStateFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExamManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getIntentParams()
        initView()
        observeStates()
        checkExamState()
    }

    /**
     * 检查考试状态
     */
    private fun checkExamState() {
        viewModel.getExamResult(buildRequestParams(), _examStateFlow)
    }

    /**
     * 获取试题列表
     */
    private fun getQuestionList() {
        viewModel.getExamView(buildRequestParams(), _questionListFlow)
    }

    private fun observeStates() {
        // 监听考试状态
        lifecycleScope.launch {
            examStateFlow.collectLatest { state ->
                when (state) {
                    is ApiState.Idle -> {}
                    is ApiState.Loading -> {}
                    is ApiState.Success -> {
                        if (state.data != null) {
                            // 考试已完成，显示结果
                            isAnswer = true
                            score = state.data.info.score
                            isPass = state.data.passtype
                            id = state.data.row.id
                            // 更新UI
                            updateUIState()
                            supportActionBar?.title = "考试管理"
                        } else {
                            goTest()
                        }
                    }
                    is ApiState.Error -> {
                        showToast(state.msg)
                    }
                }
            }
        }

        // 监听试题列表状态
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                questionListFlow.collectLatest { state ->
                    when (state) {
                        is ApiState.Idle -> {}
                        is ApiState.Loading -> {}
                        is ApiState.Success -> {
                            if (state.data != null) {
                                examsId = state.data.exams_id.toString()
                                questionList = state.data.questions.toMutableList()

                                // 更新考试组件数据
                                binding.examWidget.dataList = questionList

                                // 更新标题
                                supportActionBar?.title = "总进度1/${questionList.size}"

                                // 重置StateFlow
                                resetQuestionListState()
                            }
                        }
                        is ApiState.Error -> {
                            showToast(state.msg)
                            resetQuestionListState()
                        }
                    }
                }
            }
        }
    }

    private fun resetExamState() {
        _examStateFlow.value = ApiState.Idle
    }

    private fun resetQuestionListState() {
        _questionListFlow.value = ApiState.Idle
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
        // 设置标题栏
        binding.titleLayout.tvTitle.text = "考试管理"
        PressEffectUtils.setCommonPressEffect(binding.titleLayout.tvTitle)
        binding.titleLayout.tvTitle.setOnClickListener {
            finish()
        }

        PressEffectUtils.setCommonPressEffect(binding.btnCheckTest)
        PressEffectUtils.setCommonPressEffect(binding.btnGoTest)

        // 成绩单按钮点击
        binding.btnCheckTest.setOnClickListener {
            val intent = Intent(this, ScoreDetailActivity::class.java).apply {
                putExtra("id", id)
                putExtra("type", this@ExamManagerActivity.type)
                putExtra("training_safetyplan_id", trainingSafetyPlanId)
            }
            startActivity(intent)
        }

        // 开始答题按钮点击
        binding.btnGoTest.setOnClickListener {
            isAnswer = false
            updateUIState()
            getQuestionList()
        }

        // 初始化考试组件
        initExamWidget()
    }

    // 初始化考试组件
    private fun initExamWidget() {
        binding.examWidget.apply {
            showButton = true
            finishText = "提交试卷"
            lastText = "上一题"
            nextText = "下一题"
            indexText = "题目导航"
            showIndexText = false
            numBoxType = 1
            numBoxShow = false

            // 设置试题数据
            dataList = questionList // 你的QuestionModel列表

            // 设置回调监听
            setOnExamListener(object : ExamWidget.OnExamListener {
                override fun onSelect(data: SelectData) {
                    // 选择答案回调
                }

                override fun onSelectFinish(data: SelectFinishData) {
                    // 切换题目回调
                    supportActionBar?.title = "总进度${data.newItem.index + 1}/${data.newItem.total}"
                }

                override fun onFinish(questions: List<ExamQuestion>) {
                    // 完成答题回调
                    showSubmitConfirmDialog(questions)
                }
            })
        }
    }

    // 更新UI状态
    private fun updateUIState() {
        if (isAnswer) {
            binding.layoutResult.visibility = View.VISIBLE
            binding.examWidget.visibility = View.GONE
            binding.tvScore.text = "分数：$score 分"
            binding.tvIsPass.text = "是否通过：${if (isPass) "是" else "否"}"
        } else {
            binding.layoutResult.visibility = View.GONE
            binding.examWidget.visibility = View.VISIBLE
        }
    }

    // 进入答题界面
    private fun goTest() {
        isAnswer = false
        updateUIState()
        getQuestionList()
    }

    // 显示提交确认对话框
    private fun showSubmitConfirmDialog(questions: List<ExamQuestion>) {
        AlertDialog.Builder(this)
            .setTitle("提示")
            .setMessage("确认提交，完成考试？")
            .setPositiveButton("确认") { _, _ ->
                val answerList = questions.map { it.fldAnswer ?: "" }
                val answerJson = Gson().toJson(answerList)

                val intent = Intent(this, SignatureActivity::class.java).apply {
                    putExtra("id", id)
                    putExtra("training_safetyplan_id", trainingSafetyPlanId)
                    putExtra("type", type)
                    putExtra("answer", answerJson)
                }
                startActivity(intent)

                isAnswer = true
                updateUIState()
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // 构建请求参数
    private fun buildRequestParams(): Map<String, String> {
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

    // 标题栏返回按钮
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}