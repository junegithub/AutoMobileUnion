package com.fx.zfcar.training.base

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.fx.zfcar.R
import com.fx.zfcar.databinding.ViewExamWidgetBinding
import com.fx.zfcar.net.ExamQuestion
import com.fx.zfcar.net.QuestionOption

/**
 * 考试答题组件（使用ViewBinding重构）
 * 替代原Vue中的exam-widght组件
 */
class ExamWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewExamWidgetBinding =
        ViewExamWidgetBinding.inflate(LayoutInflater.from(context), this, true)

    // 回调接口（保持不变）
    interface OnExamInteractionListener {
        fun onSelect(answer: String, questionId: String)
        fun onSelectFinish(newIndex: Int, totalCount: Int)
        fun onFinish(questions: List<ExamQuestion>)
    }

    private var listener: OnExamInteractionListener? = null

    // 组件属性（保持不变）
    private var dataList: List<ExamQuestion> = emptyList()
    private var currentIndex: Int = 0
    private var numBoxType: Int = 1
    private var showIndexText: Boolean = true
    private var numBoxShow: Boolean = true

    init {
        orientation = VERTICAL
        initListener()
    }

    /**
     * 初始化事件监听（使用binding访问控件）
     */
    private fun initListener() {
        // 下一题按钮
        binding.btnNext.setOnClickListener {
            handleNextQuestion()
        }

        // 提交按钮
        binding.btnSubmit.setOnClickListener {
            handleSubmitExam()
        }

        // 初始状态：隐藏提交按钮
        binding.btnSubmit.visibility = GONE
    }

    fun btnNext() : View {
        return binding.btnNext
    }

    fun btnSubmit() : View {
        return binding.btnSubmit
    }

    /**
     * 处理下一题逻辑
     */
    private fun handleNextQuestion() {
        if (currentIndex < dataList.size - 1) {
            // 获取选中的答案
            val selectedId = binding.rgOptions.checkedRadioButtonId
            if (selectedId != -1) {
                val selectedOption = findViewById<RadioButton>(selectedId)
                val answer = selectedOption.tag.toString()

                // 保存答案
                dataList[currentIndex].fldAnswer = answer

                // 回调选择事件
                listener?.onSelect(answer, dataList[currentIndex].questionID.toString())

                // 切换到下一题
                currentIndex++
                updateQuestion()

                // 回调进度更新
                listener?.onSelectFinish(currentIndex, dataList.size)

                // 最后一题显示提交按钮
                if (currentIndex == dataList.size - 1) {
                    binding.btnNext.visibility = GONE
                    binding.btnSubmit.visibility = VISIBLE
                }
            } else {
                Toast.makeText(context, "请选择答案", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 处理提交试卷逻辑
     */
    private fun handleSubmitExam() {
        // 保存最后一题答案
        val selectedId = binding.rgOptions.checkedRadioButtonId
        if (selectedId != -1) {
            val selectedOption = findViewById<RadioButton>(selectedId)
            val answer = selectedOption.tag.toString()
            dataList[currentIndex].fldAnswer = answer

            // 回调选择事件
            listener?.onSelect(answer, dataList[currentIndex].questionID.toString())

            // 检查是否所有题目都已作答
            val allAnswered = dataList.none { it.fldAnswer == null }
            if (allAnswered) {
                listener?.onFinish(dataList)
            } else {
                Toast.makeText(context, "请先答完全部题目再提交", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "请选择答案", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 更新当前题目显示（核心逻辑，使用binding访问控件）
     */
    private fun updateQuestion() {
        if (dataList.isEmpty() || currentIndex >= dataList.size) return

        val currentQuestion = dataList[currentIndex]

        // 1. 更新题目内容
        binding.tvQuestionTitle.text = currentQuestion.fldName

        // 2. 更新进度文本
        binding.tvProgress.text = "总进度${currentIndex + 1}/${dataList.size}"

        // 3. 清空选项组（避免重复添加）
        binding.rgOptions.removeAllViews()

        // 4. 动态添加选项
        currentQuestion.QuestionOptionList.forEach { option ->
            val radioButton = createRadioButton(option)
            binding.rgOptions.addView(radioButton)

            // 如果已有答案，自动选中
            if (currentQuestion.fldAnswer == option.fldOptionIndex) {
                radioButton.isChecked = true
            }
        }

        // 5. 控制进度文本显示
        binding.tvProgress.visibility = if (showIndexText && numBoxShow) VISIBLE else GONE
    }

    /**
     * 创建选项RadioButton（封装为独立方法，提升可读性）
     */
    private fun createRadioButton(option: QuestionOption): RadioButton {
        return RadioButton(context).apply {
            text = option.fldOptionText
            tag = option.fldOptionIndex // 存储选项索引
            textSize = 16f
            setPadding(0, 16.dp, 0, 16.dp)
            setTextColor(ContextCompat.getColor(context, R.color.text_black))
        }
    }

    // ==================== 对外暴露的方法（保持不变） ====================
    fun setDataList(questions: List<ExamQuestion>) {
        this.dataList = questions
        currentIndex = 0
        updateQuestion()
    }

    fun setIndex(index: Int) {
        this.currentIndex = index
        updateQuestion()
    }

    fun setShowIndexText(show: Boolean) {
        this.showIndexText = show
        binding.tvProgress.visibility = if (show && numBoxShow) VISIBLE else GONE
    }

    fun setNumBoxType(type: Int) {
        this.numBoxType = type
        // 可根据type定制不同的题号栏样式
    }

    fun setNumBoxShow(show: Boolean) {
        this.numBoxShow = show
        binding.tvProgress.visibility = if (show && showIndexText) VISIBLE else GONE
    }

    fun setOnExamInteractionListener(listener: OnExamInteractionListener) {
        this.listener = listener
    }

    // ==================== 扩展函数 ====================
    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}