package com.fx.zfcar.training.exam

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.fx.zfcar.R
import com.fx.zfcar.databinding.ActivityAnswerQuestionBinding
import com.fx.zfcar.databinding.BsdQuestionListBinding
import com.fx.zfcar.databinding.ItemOptionBinding
import com.fx.zfcar.databinding.ItemQuestionNumBinding
import com.fx.zfcar.net.AnswerData
import com.fx.zfcar.net.AnswerOption
import com.fx.zfcar.net.AnswerRequest
import com.fx.zfcar.net.QuestionItem
import com.fx.zfcar.net.SelectTwoQuestionListData
import com.fx.zfcar.net.StartAnswerData
import com.fx.zfcar.net.StartAnswerQuestion
import com.fx.zfcar.net.StartAnswerRequest
import com.fx.zfcar.net.StartTwoAnswerRequest
import com.fx.zfcar.net.UpdateTwoQuestionRequest
import com.fx.zfcar.training.viewmodel.ExamViewModel
import com.fx.zfcar.viewmodel.ApiState
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlin.getValue

class AnswerQuestionActivity : AppCompatActivity() {
    // ViewBinding
    private lateinit var binding: ActivityAnswerQuestionBinding

    private val viewModel by viewModels<ExamViewModel>()

    private val startAnswerStateFlow = MutableStateFlow<ApiState<StartAnswerData>>(ApiState.Idle)
    private val questionListStateFlow = MutableStateFlow<ApiState<SelectTwoQuestionListData>>(ApiState.Idle)
    private val answerStateFlow = MutableStateFlow<ApiState<AnswerData>>(ApiState.Idle)
    private val updateQuestionStateFlow = MutableStateFlow<ApiState<String>>(ApiState.Idle)

    private val startTwoAnswerStateFlow = MutableStateFlow<ApiState<StartAnswerData>>(ApiState.Idle)
    private val twoQuestionListStateFlow = MutableStateFlow<ApiState<SelectTwoQuestionListData>>(ApiState.Idle)
    private val answerTwoStateFlow = MutableStateFlow<ApiState<AnswerData>>(ApiState.Idle)
    private val updateTwoQuestionStateFlow = MutableStateFlow<ApiState<String>>(ApiState.Idle)
    private val selectTwoQuestionStateFlow = MutableStateFlow<ApiState<StartAnswerData>>(ApiState.Idle)
    // 数据
    private var activeNum = ""
    private var selectMoreShow = false
    private var resultShow = false
    private var activeKeyArr = mutableListOf<String>()
    private var buttonBottomShow = false
    private var selectTestList = SelectTwoQuestionListData()
    private val listTitle = listOf("答题模式", "看题模式")
    private val examTypeArr = listOf("单选题", "多选题", "判断题")
    private var current = "0"
    private var resData = StartAnswerData(
        question = StartAnswerQuestion()
    )
    private var startX = 0f
    private var maskShow = true
    private var from = ""
    private var question_category_id = ""
    private var user_exam_id = ""
    private var user_category_id = ""
    private var question_id = 0

    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bsdQuestionListBinding: BsdQuestionListBinding


    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnswerQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取传参
        from = intent.getStringExtra("from") ?: ""
        question_category_id = intent.getStringExtra("question_category_id") ?: ""
        user_exam_id = intent.getStringExtra("user_exam_id") ?: ""
        user_category_id = intent.getStringExtra("user_category_id") ?: ""

        // 初始化Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            goBack()
        }

        initTabLayout()
        initListeners()
        observeStates()
        initData()
        initBottomSheetDialog()
    }

    /**
     * 初始化TabLayout
     */
    private fun initTabLayout() {
        listTitle.forEach { title ->
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(title))
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.toString()?.let { titleChange(it) }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    /**
     * 初始化事件监听
     */
    private fun initListeners() {
        // 查看所有题目
        binding.tvViewAll.setOnClickListener {
            moreSelect()
        }

        // 提交按钮
        binding.btnSubmit.setOnClickListener {
            submitAns()
        }

        // 滑动切题
        binding.mainContent.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!selectMoreShow) {
                        val endX = event.x
                        val diff = endX - startX
                        if (Math.abs(diff) > 20) {
                            if (diff < 0) {
                                goNextTest()
                            }
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun observeStates() {
        lifecycleScope.launch {
            startAnswerStateFlow.drop(1)
                .collect { state ->
                    when (state) {
                        is ApiState.Idle -> {}
                        is ApiState.Loading -> {}
                        is ApiState.Success -> {
                            state.data?.let { handleExamData(it) }
                            getExamNumInfo()
                        }
                        is ApiState.Error -> {
                            showToast("获取题目失败：${state.msg}")
                        }
                    }
                }
        }

        lifecycleScope.launch {
            questionListStateFlow.drop(1)
                .collect { state ->
                    when (state) {
                        is ApiState.Idle -> {}
                        is ApiState.Loading -> {}
                        is ApiState.Success -> {
                            state.data?.let {
                                handleSelest(it)
                            }
                        }
                        is ApiState.Error -> {
                            showToast("获取题目列表失败：${state.msg}")
                        }
                    }
                }
        }

        lifecycleScope.launch {
            answerStateFlow.drop(1)
                .collect { state ->
                    when (state) {
                        is ApiState.Idle -> {}
                        is ApiState.Loading -> {}
                        is ApiState.Success -> {
                            state.data?.let { handleResAns(it) }
                        }
                        is ApiState.Error -> {
                            showToast("提交答案失败：${state.msg}")
                        }
                    }
                }
        }

        lifecycleScope.launch {
            updateQuestionStateFlow.drop(1)
                .collect { state ->
                    when (state) {
                        is ApiState.Idle -> {}
                        is ApiState.Loading -> {}
                        is ApiState.Success -> {
                            postStartAnswer()
                        }
                        is ApiState.Error -> {
                            showToast("更新内容失败：${state.msg}")
                        }
                    }
                }
        }

        lifecycleScope.launch {
            startTwoAnswerStateFlow.drop(1)
                .collect { state ->
                    when (state) {
                        is ApiState.Idle -> {}
                        is ApiState.Loading -> {}
                        is ApiState.Success -> {
                            state.data?.let { handleExamData(it) }
                            getExamNumInfo()
                        }
                        is ApiState.Error -> {
                            showToast("获取题目失败：${state.msg}")
                        }
                    }
                }
        }

        lifecycleScope.launch {
            twoQuestionListStateFlow.drop(1)
                .collect { state ->
                    when (state) {
                        is ApiState.Idle -> {}
                        is ApiState.Loading -> {}
                        is ApiState.Success -> {
                            state.data?.let {
                                handleSelest(it)
                            }
                        }
                        is ApiState.Error -> {
                            showToast("获取题目列表失败：${state.msg}")
                        }
                    }
                }
        }

        lifecycleScope.launch {
            answerTwoStateFlow.drop(1)
                .collect { state ->
                    when (state) {
                        is ApiState.Idle -> {}
                        is ApiState.Loading -> {}
                        is ApiState.Success -> {
                            state.data?.let { handleResAns(it) }
                        }
                        is ApiState.Error -> {
                            showToast("提交答案失败：${state.msg}")
                        }
                    }
                }
        }

        lifecycleScope.launch {
            updateTwoQuestionStateFlow.drop(1)
                .collect { state ->
                    when (state) {
                        is ApiState.Idle -> {}
                        is ApiState.Loading -> {}
                        is ApiState.Success -> {
                            postStartTwoAnswer()
                        }
                        is ApiState.Error -> {
                            showToast("更新内容失败：${state.msg}")
                        }
                    }
                }
        }

        lifecycleScope.launch {
            selectTwoQuestionStateFlow.drop(1)
                .collect { state ->
                    when (state) {
                        is ApiState.Idle -> {}
                        is ApiState.Loading -> {}
                        is ApiState.Success -> {
                            if (current == "0") {
                                resultShow = false
                                binding.llAnswerAnalysis.visibility = View.GONE
                            }
                            state.data?.let { handleExamData(it) }
                        }
                        is ApiState.Error -> {
                            showToast("加载题目失败：${state.msg}")
                        }
                    }
                }
        }
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        binding.vMask.visibility = View.VISIBLE

        if (from == "twoList") {
            viewModel.updateTwoQuestion(UpdateTwoQuestionRequest(from, user_category_id.toInt(), question_category_id.toInt(), question_id),
                updateTwoQuestionStateFlow)
        } else {
            viewModel.updateQuestion(UpdateTwoQuestionRequest(from, user_category_id.toInt(), question_category_id.toInt(), question_id), updateQuestionStateFlow)
        }
    }

    /**
     * 开始答题（普通）
     */
    private fun postStartAnswer() {
        viewModel.startAnswer(StartAnswerRequest(question_category_id, user_exam_id, user_category_id), startAnswerStateFlow)
    }

    /**
     * 开始答题（两类人员）
     */
    private fun postStartTwoAnswer() {
        viewModel.startTwoAnswer(StartTwoAnswerRequest(from, user_category_id.toInt(), question_category_id.toInt(), question_id), startTwoAnswerStateFlow)
    }

    /**
     * 处理题目数据
     */
    private fun handleExamData(data: StartAnswerData) {
        resData = data

        // 更新题目信息
        binding.tvQuestionNum.text = "【全部】第$activeNum/${data.question_count}题目"
        binding.tvRightCount.text = "对${data.right_count}"
        binding.tvWrongCount.text = "错${data.wrong_count}"

        // 更新题目类型和内容
        val questionType = data.question.type.toIntOrNull() ?: 1
        binding.tvQuestionType.text = examTypeArr[questionType - 1]
        binding.tvQuestionText.text = data.question.question

        // 更新选项列表
        updateOptionsList(data.question.selectdata, questionType)

        // 更新解析区域
        binding.tvCorrectAnswer.text = "正确答案:${data.question.answer}"
        binding.tvAnalysisText.text = "解析:${data.question.describe}"

        // 隐藏遮罩
        binding.vMask.visibility = View.GONE
    }

    /**
     * 更新选项列表
     */
    private fun updateOptionsList(options: List<AnswerOption>, type: Int) {
        binding.llOptions.removeAllViews()

        options.forEach { option ->
            option.type = type

            val optionBinding = ItemOptionBinding.inflate(layoutInflater)
            optionBinding.tvOptionKey.text = "${option.key}."
            optionBinding.tvOptionValue.text = option.value

            // 设置选项背景
            updateOptionBackground(optionBinding.root, option.key)

            // 选项点击事件
            optionBinding.root.setOnClickListener {
                selectAns(option)
            }

            binding.llOptions.addView(optionBinding.root)
        }
    }

    /**
     * 更新选项背景
     */
    private fun updateOptionBackground(view: View, key: String) {
        when {
            resultShow && resData.question.answer.split(",").contains(key) -> {
                // 正确答案
                view.setBackgroundResource(R.drawable.answer_right_bg)
            }
            resultShow && activeKeyArr.contains(key) && !resData.question.answer.split(",").contains(key) -> {
                // 错误选择
                view.setBackgroundResource(R.drawable.answer_error_bg)
            }
            activeKeyArr.contains(key) -> {
                // 选中状态
                view.setBackgroundResource(R.drawable.answer_active_bg)
            }
            else -> {
                // 默认状态
                view.setBackgroundResource(R.drawable.answer_bg)
            }
        }
    }

    /**
     * 返回上一页
     */
    private fun goBack() {
        finish()
    }

    /**
     * 切换到下一题
     */
    private fun goNextTest() {
        resultShow = false
        activeKeyArr.clear()
        binding.llAnswerAnalysis.visibility = View.GONE
        binding.btnSubmit.visibility = View.GONE

        if (from == "twoList") {
            postStartTwoAnswer()
        } else {
            // 普通答题
            val questionList = selectTestList.question_list
            for ((key, item) in questionList.withIndex()) {
                if (item.question_id == resData.question.id) {
                    val nextNum = key + 1
                    if (nextNum >= questionList.size) {
                        // 查找未答题目
                        for ((s, da) in questionList.withIndex()) {
                            if (da.is_right != "0" && da.is_right != "1") {
                                selectNum(da, s.toString())
                                return
                            }
                        }
                        showToast("已经完成全部答题")
                    } else {
                        selectNum(questionList[nextNum], nextNum.toString())
                    }
                    return
                }
            }
        }
    }

    /**
     * 处理答题结果
     */
    private fun handleResAns(res: AnswerData) {
        resultShow = true
        buttonBottomShow = false
        binding.llAnswerAnalysis.visibility = View.VISIBLE
        binding.btnSubmit.visibility = View.GONE

        // 更新选项背景
        resData.question.selectdata.forEach { option ->
            for (i in 0 until binding.llOptions.childCount) {
                val view = binding.llOptions.getChildAt(i)
                val keyTv = view.findViewById<TextView>(R.id.tv_option_key)
                if (keyTv.text.toString().startsWith(option.key)) {
                    updateOptionBackground(view, option.key)
                    break
                }
            }
        }

        when {
            res.has_next && res.is_right -> {
                // 回答正确且有下一题
                goNextTest()
            }
            !res.has_next -> {
                // 没有下一题
                showToast("已经完成全部答题")
            }
            res.has_next && !res.is_right -> {
                // 回答错误，4秒后跳下一题
                handler.postDelayed({
                    goNextTest()
                }, 4000)
            }
        }
    }

    /**
     * 提交答案
     */
    private fun submitAns() {
        val request = AnswerRequest(resData.user_exam_id, resData.question.id,
            activeKeyArr.joinToString(","))

        if (from == "twoList") {
            viewModel.twoAnswer(request, answerTwoStateFlow)
        } else {
            viewModel.answer(request, answerStateFlow)
        }
    }

    /**
     * 选择题目
     */
    private fun selectNum(data: QuestionItem, index: String) {
        activeNum = index
        question_id = data.question_id

        if (from == "twoList") {
            viewModel.selectTwoQuestion(user_exam_id, selectTwoQuestionStateFlow)
        } else {
            postStartAnswer()
        }

        // 关闭弹窗
        selectMoreShow = false
        bottomSheetDialog.dismiss()
    }

    /**
     * 处理题目列表数据
     */
    private fun handleSelest(data: SelectTwoQuestionListData) {
        selectTestList = data

        // 查找当前题目位置
        for ((index, item) in data.question_list.withIndex()) {
            if (item.question_id == resData.question.id) {
                activeNum = index.toString()
                binding.tvQuestionNum.text = "【全部】第$activeNum/${data.question_count}题目"
                break
            }
        }

        // 更新题目列表弹窗
        updateQuestionListDialog(data.question_list)

        // 隐藏遮罩
        binding.vMask.visibility = View.GONE
    }

    /**
     * 获取题目列表信息
     */
    private fun getExamNumInfo() {

        if (from == "twoList") {
            viewModel.selectTwoQuestionList(resData.user_exam_id.toString(), twoQuestionListStateFlow)
        } else {
            viewModel.selectQuestionList(resData.user_exam_id.toString(), questionListStateFlow)
        }
    }

    /**
     * 显示题目列表弹窗
     */
    private fun moreSelect() {
        selectMoreShow = true
        val dialog = BottomSheetDialog(this)
        val dialogBinding = ItemQuestionNumBinding.inflate(layoutInflater)

        // 设置弹窗内容
        dialog.setContentView(dialogBinding.root)
        dialog.show()

        // 更新题目列表
        updateQuestionListDialog(selectTestList.question_list)
    }

    /**
     * 更新题目列表弹窗
     */
    private fun updateQuestionListDialog(questionList: List<QuestionItem>) {
        val adapter = QuestionNumAdapter(questionList)
        bsdQuestionListBinding.gvQuestionList.adapter = adapter
        showQuestionListBottomSheet()
    }

    /**
     * 初始化BottomSheetDialog
     */
    private fun initBottomSheetDialog() {
        bottomSheetDialog = BottomSheetDialog(this)

        bsdQuestionListBinding = BsdQuestionListBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(bsdQuestionListBinding.root)

        // 初始化GridView数据
        bsdQuestionListBinding.gvQuestionList.numColumns = 3
        bsdQuestionListBinding.gvQuestionList.verticalSpacing = resources.getDimensionPixelSize(R.dimen.padding_1vh)
        bsdQuestionListBinding.gvQuestionList.horizontalSpacing = resources.getDimensionPixelSize(R.dimen.padding_2vw)
    }

    /**
     * 显示题目列表BottomSheet
     */
    private fun showQuestionListBottomSheet() {
        // 显示BottomSheet
        bottomSheetDialog.show()
    }

    /**
     * 模式切换
     */
    private fun titleChange(data: String) {
        current = data
        buttonBottomShow = false
        binding.btnSubmit.visibility = View.GONE

        if (current == "1") {
            // 看题模式
            resultShow = true
            binding.llAnswerAnalysis.visibility = View.VISIBLE
            activeKeyArr.clear()
        } else {
            // 答题模式
            resultShow = false
            binding.llAnswerAnalysis.visibility = View.GONE
            activeKeyArr.clear()
        }

        // 更新选项背景
        resData.question.selectdata.forEachIndexed { index, option ->
            val view = binding.llOptions.getChildAt(index)
            view?.let { updateOptionBackground(it, option.key) }
        }
    }

    /**
     * 选择答案
     */
    private fun selectAns(data: AnswerOption) {
        if (resultShow) return

        if (activeKeyArr.contains(data.key)) {
            // 取消选择
            activeKeyArr.remove(data.key)
        } else {
            // 添加选择
            activeKeyArr.add(data.key)
            activeKeyArr = activeKeyArr.distinct().toMutableList()
        }

        // 更新选项背景
        for (i in 0 until binding.llOptions.childCount) {
            val view = binding.llOptions.getChildAt(i)
            val keyTv = view.findViewById<TextView>(R.id.tv_option_key)
            if (keyTv.text.toString().startsWith(data.key)) {
                updateOptionBackground(view, data.key)
                break
            }
        }

        // 处理不同题型
        when (data.type) {
            2 -> {
                // 多选题
                buttonBottomShow = activeKeyArr.isNotEmpty()
                binding.btnSubmit.visibility = if (buttonBottomShow) View.VISIBLE else View.GONE
            }
            1, 3 -> {
                // 单选题/判断题
                resultShow = true
                binding.llAnswerAnalysis.visibility = View.VISIBLE
                submitAns()
            }
        }
    }

    /**
     * 显示Toast提示
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        if (message.contains("全部答题")) {
            handler.postDelayed({
                goBack()
            }, 2000)
        }
    }

    /**
     * 题目列表适配器
     */
    inner class QuestionNumAdapter(private val list: List<QuestionItem>) : android.widget.BaseAdapter() {
        override fun getCount(): Int = list.size

        override fun getItem(position: Int): Any = list[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val binding = ItemQuestionNumBinding.inflate(layoutInflater)
            binding.tvQuestionNumItem.text = "第${position+1}题"

            // 设置背景样式
            val item = list[position]
            when {
                position.toString() == activeNum -> {
                    // 当前题目
                    binding.tvQuestionNumItem.setBackgroundColor(ContextCompat.getColor(this@AnswerQuestionActivity, R.color.primary_light))
                }
                item.is_right == "0" -> {
                    // 错误
                    binding.tvQuestionNumItem.setBackgroundColor(ContextCompat.getColor(this@AnswerQuestionActivity, R.color.error_bg))
                    binding.tvQuestionNumItem.setTextColor(ContextCompat.getColor(this@AnswerQuestionActivity, R.color.error_red))
//                    binding.tvQuestionNumItem.setBorderColor(ContextCompat.getColor(this@AnswerQuestionActivity, R.color.error_red))
                }
                item.is_right == "1" -> {
                    // 正确
                    binding.tvQuestionNumItem.setBackgroundColor(ContextCompat.getColor(this@AnswerQuestionActivity, R.color.success_bg))
                    binding.tvQuestionNumItem.setTextColor(ContextCompat.getColor(this@AnswerQuestionActivity, R.color.success_border))
//                    binding.tvQuestionNumItem.setBorderColor(ContextCompat.getColor(this@AnswerQuestionActivity, R.color.success_border))
                }
            }

            // 点击事件
            binding.root.setOnClickListener {
                selectNum(item, position.toString())
            }

            return binding.root
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}