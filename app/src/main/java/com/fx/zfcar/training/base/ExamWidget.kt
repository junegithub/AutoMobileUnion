package com.fx.zfcar.training.base

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.core.view.isVisible
import com.fx.zfcar.R
import com.fx.zfcar.databinding.ViewExamWidgetBinding
import com.fx.zfcar.net.ExamQuestion
import com.fx.zfcar.net.QuestionOption
import com.fx.zfcar.util.PressEffectUtils

/**
 * 支持：单选/多选/填空、题号导航、动画遮罩、答题状态管理
 */
class ExamWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewExamWidgetBinding by lazy {
        ViewExamWidgetBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    // 属性配置（对应props）
    var index: Int = 0
        set(value) {
            field = value
            currentIndex = value
            currentSelectFinish(currentIndex)
        }
    var dataList: List<ExamQuestion> = emptyList()
        set(value) {
            field = value
            init()
        }
    var showButton: Boolean = true
    var finishText: String = "提交"
    var lastText: String = "上一题"
    var nextText: String = "下一题"
    var indexText: String = "题号"
    var showIndexText: Boolean = true
    var numBoxType: Int = 0 // 0-内嵌 1-遮罩
    var numBoxShow: Boolean = false

    // 内部状态（对应data）
    private var windowHeight: Int = 500
    private var showIndexBox: Boolean = false
    private var currentHasChange: Boolean = false
    private var currentIndex: Int = 0
    private lateinit var currentItem: ExamQuestion
    private var currentCheck: String = "" // 单选答案
    private var currentCheckBoxCheck: MutableList<String> = mutableListOf() // 多选答案
    private var currentText: String = "" // 填空答案
    private var showMask: Boolean = false // 遮罩显示状态

    // 回调接口
    interface OnExamListener {
        fun onSelect(data: SelectData) // 选择答案
        fun onSelectFinish(data: SelectFinishData) // 切换题目
        fun onFinish(questions: List<ExamQuestion>) // 完成答题
    }

    private var examListener: OnExamListener? = null

    init {
        initSystemInfo()
        initViewProperties()
        initEventListeners()
        init()
    }

    /**
     * 初始化系统信息（屏幕高度）
     */
    private fun initSystemInfo() {
        val displayMetrics = resources.displayMetrics
        windowHeight = displayMetrics.heightPixels

        if (numBoxType == 1) {
            hideMask()
        }
    }

    /**
     * 初始化View属性（替代原initView）
     */
    private fun initViewProperties() {
        // 初始化按钮文本
        binding.btnLast.text = lastText
        binding.btnNext.text = nextText
        binding.tvIndexText.text = indexText
        binding.btnFinish.text = finishText

        // 初始化显示状态
        binding.tvIndexText.isVisible = showIndexText
        binding.btnFinish.isVisible = showButton
        binding.tvCorrectAnswerTitle.isVisible = !showButton

        // 设置文本大小（优化：使用资源引用）
        binding.tvQuestionTitle.textSize = resources.getDimension(R.dimen.exam_title_text_size) / resources.displayMetrics.scaledDensity
        binding.etTextarea.textSize = resources.getDimension(R.dimen.exam_option_text_size) / resources.displayMetrics.scaledDensity
    }

    /**
     * 初始化事件监听（优化：模块化）
     */
    private fun initEventListeners() {
        // 上一题
        PressEffectUtils.setCommonPressEffect(binding.btnLast)
        binding.btnLast.setOnClickListener {
            if (currentIndex > 0) {
                lastQuestion()
            }
        }

        // 下一题
        PressEffectUtils.setCommonPressEffect(binding.btnNext)
        binding.btnNext.setOnClickListener {
            if (currentIndex < dataList.size - 1) {
                nextQuestion()
            }
        }

        // 题号文字点击
        PressEffectUtils.setCommonPressEffect(binding.tvIndexText)
        binding.tvIndexText.setOnClickListener {
            switchIndexBox()
        }

        // 提交按钮
        PressEffectUtils.setCommonPressEffect(binding.btnFinish)
        binding.btnFinish.setOnClickListener {
            finish()
        }

        // 遮罩点击隐藏
        binding.vMask.setOnClickListener {
            hideMask()
        }

        // 填空题输入监听（优化：使用更简洁的写法）
        binding.etTextarea.doOnTextChanged { text, _, _, _ ->
            currentText = text.toString()
            currentHasChange = true
            if (dataList.isNotEmpty() && currentIndex < dataList.size) {
                dataList.toMutableList()[currentIndex].fldAnswer = currentText
            }
        }
    }

    /**
     * 初始化数据
     */
    private fun init() {
        if (dataList.isEmpty()) return

        currentItem = getCurrentItem()
        checkQuestionSelected()
        updateQuestionView()
        updateIndexBox()
        updateMaskIndexBox()
    }

    /**
     * 更新题目视图（核心优化：使用binding访问所有控件）
     */
    private fun updateQuestionView() {
        // 更新题目标题
        val titlePrefix = if (showButton) "${currentIndex + 1}." else ""
        binding.tvQuestionTitle.text = "$titlePrefix${currentItem.fldName ?: ""}"

        // 隐藏所有选项容器
        binding.llRadioOptions.isVisible = false
        binding.llCheckboxOptions.isVisible = false
        binding.llTextareaContainer.isVisible = false

        // 更新按钮状态
        binding.btnLast.isVisible = currentIndex != 0
        binding.btnNext.isVisible = currentIndex < dataList.size - 1

        // 根据题型显示对应选项
        when (currentItem.questionType) {
            0 -> updateRadioOptions() // 单选
            1 -> updateCheckboxOptions() // 多选
            2 -> updateTextarea() // 填空
        }
    }

    /**
     * 更新单选选项（优化：使用binding）
     */
    private fun updateRadioOptions() {
        binding.llRadioOptions.isVisible = true
        binding.llRadioOptions.removeAllViews()

        currentItem.QuestionOptionList.forEach { option ->
            val radioLayout = createOptionLayout()

            val radioButton = RadioButton(context).apply {
                isEnabled = showButton
                isChecked = option.fldOptionIndex == currentCheck
                layoutParams = LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                )
            }

            val optionText = createOptionTextView(option)

            radioLayout.addView(radioButton)
            radioLayout.addView(optionText)
            radioLayout.setOnClickListener {
                if (showButton) {
                    radioChange(option)
                    // 更新所有单选按钮状态
                    binding.llRadioOptions.forEachChild<LinearLayout> { child ->
                        child.forEachChild<RadioButton> { rb ->
                            rb.isChecked = rb == radioButton
                        }
                    }
                }
            }

            binding.llRadioOptions.addView(radioLayout)
        }
    }

    /**
     * 更新多选选项（优化：使用binding）
     */
    private fun updateCheckboxOptions() {
        binding.llCheckboxOptions.isVisible = true
        binding.llCheckboxOptions.removeAllViews()

        currentItem.QuestionOptionList.forEach { option ->
            val checkLayout = createOptionLayout()

            val checkBox = CheckBox(context).apply {
                isEnabled = showButton
                isChecked = checkboxCheck(option.fldOptionIndex)
                layoutParams = LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                )
            }

            val optionText = createOptionTextView(option)

            checkLayout.addView(checkBox)
            checkLayout.addView(optionText)
            checkLayout.setOnClickListener {
                if (showButton) {
                    checkBox.isChecked = !checkBox.isChecked
                    checkboxChange(option.fldOptionIndex, checkBox.isChecked)
                }
            }

            binding.llCheckboxOptions.addView(checkLayout)
        }
    }

    /**
     * 更新填空题（优化：使用binding）
     */
    private fun updateTextarea() {
        binding.llTextareaContainer.isVisible = true
        binding.etTextarea.setText(currentText)
        binding.etTextarea.isEnabled = showButton
    }

    /**
     * 更新题号盒子（优化：使用binding）
     */
    private fun updateIndexBox() {
        binding.llIndexBox.removeAllViews()
        if (dataList.isEmpty()) return

        dataList.forEachIndexed { quesIndex, question ->
            val indexView = createIndexItem(quesIndex, question, false)
            binding.llIndexBox.addView(indexView)
        }

        binding.llIndexBox.isVisible = showIndexBox && numBoxType == 0
    }

    /**
     * 更新遮罩题号盒子（优化：使用binding）
     */
    private fun updateMaskIndexBox() {
        binding.llMaskIndexBox.removeAllViews()
        if (dataList.isEmpty()) return

        dataList.forEachIndexed { quesIndex, question ->
            val indexView = createIndexItem(quesIndex, question, true)
            binding.llMaskIndexBox.addView(indexView)
        }
    }

    // ========== 工具方法优化 ==========
    /**
     * 创建选项布局（提取通用逻辑）
     */
    private fun createOptionLayout(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setPadding(0, 5.dpToPx(), 0, 5.dpToPx())
            }
        }
    }

    /**
     * 创建选项文本（提取通用逻辑）
     */
    private fun createOptionTextView(option: QuestionOption): TextView {
        return TextView(context).apply {
            text = "${option.fldOptionIndex}.${option.fldOptionText}"
            textSize = resources.getDimension(R.dimen.exam_option_text_size) / resources.displayMetrics.scaledDensity
            setTextColor(resources.getColor(R.color.black, context.theme))
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = 10.dpToPx()
            }
        }
    }

    /**
     * 创建题号项（优化：使用context.theme获取颜色）
     */
    private fun createIndexItem(
        quesIndex: Int,
        question: ExamQuestion,
        isMask: Boolean
    ): TextView {
        val indexSize = if (isMask) {
            resources.getDimensionPixelSize(R.dimen.exam_index_box_mask_size)
        } else {
            resources.getDimensionPixelSize(R.dimen.exam_index_box_size)
        }

        return TextView(context).apply {
            text = "${quesIndex + 1}"
            gravity = Gravity.CENTER
            textSize = 14f
            layoutParams = LayoutParams(indexSize, indexSize).apply {
                marginStart = 5.dpToPx()
                marginEnd = 5.dpToPx()
            }

            // 设置样式（优化：使用context.theme）
            setBackgroundResource(R.drawable.index_box_bg)
            setTextColor(
                if (checkItem(question)) resources.getColor(R.color.exam_primary, context.theme)
                else resources.getColor(R.color.black, context.theme)
            )
            setBackgroundColor(
                if (quesIndex == currentIndex)
                    resources.getColor(R.color.exam_light_gray, context.theme)
                else resources.getColor(R.color.white, context.theme)
            )

            // 点击事件
            setOnClickListener {
                currentSelectFinish(quesIndex)
                if (numBoxType == 1) {
                    hideMask()
                }
            }
        }
    }

    private fun radioChange(option: QuestionOption) {
        currentHasChange = true
        currentCheck = option.fldOptionIndex
        if (dataList.isNotEmpty() && currentIndex < dataList.size) {
            dataList.toMutableList()[currentIndex].fldAnswer = option.fldOptionIndex
        }

        examListener?.onSelect(
            SelectData(
                question = currentItem,
                answer = option
            )
        )

        updateRadioOptions()
        updateIndexBox()
        updateMaskIndexBox()
    }

    private fun checkboxChange(optionIndex: String, isChecked: Boolean) {
        currentHasChange = true

        if (isChecked) {
            if (!currentCheckBoxCheck.contains(optionIndex)) {
                currentCheckBoxCheck.add(optionIndex)
            }
        } else {
            currentCheckBoxCheck.remove(optionIndex)
        }

        if (dataList.isNotEmpty() && currentIndex < dataList.size) {
            val answerStr = currentCheckBoxCheck.joinToString(",")
            dataList.toMutableList()[currentIndex].fldAnswer = answerStr
        }

        examListener?.onSelect(
            SelectData(
                question = currentItem,
                answer = currentCheckBoxCheck.joinToString(",")
            )
        )

        updateCheckboxOptions()
        updateIndexBox()
        updateMaskIndexBox()
    }

    private fun lastQuestion() {
        currentSelectFinish(currentIndex - 1)
    }

    private fun nextQuestion() {
        currentSelectFinish(currentIndex + 1)
    }

    private fun currentSelectFinish(newIndex: Int) {
        if (newIndex < 0 || newIndex >= dataList.size) return

        val oldIndex = currentIndex
        val oldQuestion = dataList[oldIndex]
        val newQuestion = dataList[newIndex]

        val currentAnswer = when (oldQuestion.questionType) {
            0 -> currentCheck.toString()
            1 -> currentCheckBoxCheck.joinToString(",")
            2 -> currentText
            else -> ""
        }

        examListener?.onSelectFinish(
            SelectFinishData(
                currentItem = CurrentItemData(
                    question = oldQuestion,
                    answer = currentAnswer,
                    hasChange = currentHasChange,
                    index = oldIndex,
                    total = dataList.size
                ),
                newItem = NewItemData(
                    question = newQuestion,
                    index = newIndex,
                    total = dataList.size
                )
            )
        )

        currentIndex = newIndex
        currentItem = getCurrentItem()
        currentHasChange = false

        checkQuestionSelected()
        updateQuestionView()
        updateIndexBox()
        updateMaskIndexBox()
    }

    private fun checkItem(item: ExamQuestion): Boolean {
        if (item.fldAnswer.isNullOrEmpty()) return false

        return when (item.questionType) {
            0 -> true
            1 -> item.fldAnswer!!.split(",").isNotEmpty()
            2 -> item.fldAnswer!!.isNotEmpty()
            else -> false
        }
    }

    private fun checkboxCheck(num: String): Boolean {
        return currentCheckBoxCheck.contains(num)
    }

    private fun checkQuestionSelected() {
        when (currentItem.questionType) {
            0 -> {
                currentCheck = try {
                    currentItem.fldAnswer?: ""
                } catch (e: Exception) {
                    ""
                }
            }
            1 -> {
                currentCheckBoxCheck = try {
                    currentItem.fldAnswer?.split(",")?.toMutableList() ?: mutableListOf()
                } catch (e: Exception) {
                    mutableListOf()
                }
            }
            2 -> {
                currentText = currentItem.fldAnswer ?: ""
            }
        }
    }

    private fun finish() {
        examListener?.onFinish(dataList)
    }

    private fun getCurrentItem(): ExamQuestion {
        return if (dataList.isNotEmpty() && currentIndex < dataList.size) {
            dataList[currentIndex]
        } else {
            ExamQuestion(
                questionID = 0,
                fldName = "",
                fldAnswer = "",
                questionType = 0,
                QuestionOptionList = emptyList()
            )
        }
    }

    private fun hideMask() {
        val animator = ValueAnimator.ofInt(0, windowHeight / 2)
        animator.duration = 300
        animator.interpolator = AccelerateDecelerateInterpolator()

        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            binding.llMaskIndexContainer.translationY = value.toFloat()
        }

        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                showMask = false
                binding.vMask.isVisible = false
                binding.llMaskIndexContainer.isVisible = false
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        animator.start()
    }

    private fun showMaskFun() {
        showMask = true
        binding.vMask.isVisible = true
        binding.llMaskIndexContainer.isVisible = true
        binding.llMaskIndexContainer.translationY = (windowHeight / 2).toFloat()

        val animator = ValueAnimator.ofInt(windowHeight / 2, 0)
        animator.duration = 300
        animator.interpolator = AccelerateDecelerateInterpolator()

        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            binding.llMaskIndexContainer.translationY = value.toFloat()
        }

        animator.start()
    }

    private fun switchIndexBox() {
        if (numBoxType == 0) {
            showIndexBox = !showIndexBox
            binding.llIndexBox.isVisible = showIndexBox
        } else if (numBoxType == 1) {
            showMaskFun()
        }
    }

    // ========== 扩展函数优化 ==========
    /**
     * DP转PX扩展函数（优化：更简洁）
     */
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    /**
     * 遍历子View扩展函数（优化：泛型+高阶函数）
     */
    private inline fun <reified T : View> ViewGroup.forEachChild(action: (T) -> Unit) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is T) {
                action(child)
            }
        }
    }

    /**
     * 文本变化监听扩展函数（优化：更简洁）
     */
    private fun EditText.doOnTextChanged(action: (CharSequence?, Int, Int, Int) -> Unit) {
        addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                action(s, start, before, count)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    // ========== 对外接口 ==========
    fun setOnExamListener(listener: OnExamListener) {
        this.examListener = listener
    }
}

/**
 * 选择完成回调数据
 */
data class SelectFinishData(
    val currentItem: CurrentItemData,
    val newItem: NewItemData
)

data class CurrentItemData(
    val question: ExamQuestion,
    val answer: String,
    val hasChange: Boolean,
    val index: Int,
    val total: Int
)

data class NewItemData(
    val question: ExamQuestion,
    val index: Int,
    val total: Int
)

/**
 * 选择答案回调数据
 */
data class SelectData(
    val question: ExamQuestion,
    val answer: Any
)