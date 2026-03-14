package com.fx.zfcar.training.exam

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.fx.zfcar.R
import com.fx.zfcar.car.base.WeChatShareHelper
import com.fx.zfcar.databinding.ActivityExamPracticeBinding
import com.fx.zfcar.databinding.DialogRoleSheetBinding
import com.fx.zfcar.databinding.ItemCategoryBinding
import com.fx.zfcar.databinding.ItemRoleBinding
import com.fx.zfcar.databinding.LayoutTopTipsBinding
import com.fx.zfcar.net.QuestionListData
import com.fx.zfcar.net.QuestionOrderPayData
import com.fx.zfcar.net.TwoListData
import com.fx.zfcar.net.TwoOrderPayData
import com.fx.zfcar.net.WxPayParams
import com.fx.zfcar.pages.EventData
import com.fx.zfcar.training.viewmodel.ExamViewModel
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.JsonUtils
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.viewmodel.ApiState
import com.fx.zfcar.wxapi.WXEntryActivity.WxPayResult
import com.tencent.mm.opensdk.modelpay.PayReq
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.collections.forEachIndexed
import kotlin.getValue

class ExamPracticeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExamPracticeBinding

    // 弹窗Binding
    private lateinit var roleSheetBinding: DialogRoleSheetBinding

    private val examViewModel by viewModels<ExamViewModel>()
    private val trainingViewModel by viewModels<SafetyTrainingViewModel>()

    private val twoListState = MutableStateFlow<ApiState<TwoListData>>(ApiState.Idle)
    private val questionListState = MutableStateFlow<ApiState<QuestionListData>>(ApiState.Idle)
    private val orderIsPayState = MutableStateFlow<ApiState<QuestionOrderPayData>>(ApiState.Idle)
    private val twoOrderIsPayState = MutableStateFlow<ApiState<TwoOrderPayData>>(ApiState.Idle)
    private val twoOrderState = MutableStateFlow<ApiState<String>>(ApiState.Idle)
    private val orderState = MutableStateFlow<ApiState<String>>(ApiState.Idle)

    // 数据
    private var activeId: String = ""
    private var userType: String = ""
    private var from: String? = null
    private var payId = 0
    private var payNum = 0
    private var payShow: Boolean = false
    private var loadingShow: Boolean = false
    private var role: MutableList<RoleModel> = mutableListOf()
    private var title: String = ""
    private var allTestNum = 0
    private var answerCount = 0
    private var roleId: String = ""
    private var categoryList: MutableList<CategoryModel> = mutableListOf()
    private var nickName: String = ""

    private var categoryModel: CategoryModel? = null

    private var mAlertDialog: AlertDialog? = null

    private lateinit var wechat: WeChatShareHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化ViewBinding
        binding = ActivityExamPracticeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wechat = WeChatShareHelper(this, lifecycleScope)

        // 获取传参
        from = intent.getStringExtra("from")

        // 设置Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // 初始化
        initViewProperties()
        initEventListeners()
        collectStateFlows()
        init()
        EventBus.getDefault().register(this)
    }

    /**
     * 初始化View属性
     */
    private fun initViewProperties() {
        // 初始隐藏付费弹窗
        binding.flPayDialog.visibility = View.GONE

        // 设置Toolbar返回按钮点击事件
        binding.toolbar.setNavigationOnClickListener {
            goBack()
        }
    }

    /**
     * 初始化事件监听（优化：使用binding访问控件）
     */
    private fun initEventListeners() {
        // 角色切换
        PressEffectUtils.setCommonPressEffect(binding.llRoleSwitch)
        PressEffectUtils.setCommonPressEffect(binding.btnPay)
        PressEffectUtils.setCommonPressEffect(binding.btnCancelPay)
        PressEffectUtils.setCommonPressEffect(binding.flPayDialog)
        PressEffectUtils.setCommonPressEffect(binding.llPayContent)

        binding.llRoleSwitch.setOnClickListener {
            changeRole()
        }

        // 付费按钮
        binding.btnPay.setOnClickListener {
            payMoney()
        }

        // 取消付费
        binding.btnCancelPay.setOnClickListener {
            hidePayDialog()
        }

        // 点击空白处关闭付费弹窗
        binding.flPayDialog.setOnClickListener {
            hidePayDialog()
        }

        // 防止点击弹窗内容区域关闭
        binding.llPayContent.setOnClickListener {
            // 消费点击事件
        }
    }

    /**
     * 初始化数据
     */
    private fun init() {
        userType = ""
        loadingShow = false
        categoryList.clear()

        if (from == "twoList") {
            // 两类人员
            getQuestionList(mapOf("user_category_id" to "1"))
        } else {
            // 底部真题
            getQuestionList(emptyMap())
            this.from = null
        }
    }

    private fun collectStateFlows() {
        lifecycleScope.launch {
            twoListState.drop(1)
                .collect { state ->
                    when (state) {
                        is ApiState.Error -> {
                            showToast(state.msg)
                            loadingShow = true
                            updateUI()
                        }

                        is ApiState.Success -> {
                            handlTwoQueRes(state.data)
                        }

                        else -> {}
                    }
                }
        }

        lifecycleScope.launch {
            questionListState.drop(1)
                .collect { state ->
                    when (state) {
                        is ApiState.Error -> {
                            showToast(state.msg)
                            loadingShow = true
                            updateUI()
                        }

                        is ApiState.Success -> {
                            handlQueRes(state.data)
                        }

                        else -> {}
                    }
                }
        }

        lifecycleScope.launch {
            orderState.drop(1)
                .collect { state ->
                    when (state) {
                        is ApiState.Error -> {
                            showToast("支付订单创建失败${state.msg}")
                            hidePayDialog()
                        }

                        is ApiState.Success -> {
                            if (state.data != null) {
                                wechat.goPay(JsonUtils.fromJson(state.data))
                            } else {
                                showToast("支付失败")
                                hidePayDialog()
                            }
                        }

                        else -> {}
                    }
                }
        }

        lifecycleScope.launch {
            twoOrderState.drop(1)
                .collect { state ->
                    when (state) {
                        is ApiState.Error -> {
                            showToast("支付订单创建失败${state.msg}")
                            hidePayDialog()
                        }

                        is ApiState.Success -> {
                            if (state.data != null) {
                                wechat.goPay(JsonUtils.fromJson(state.data))
                            } else {
                                showToast("支付失败")
                                hidePayDialog()
                            }
                        }

                        else -> {}
                    }
                }
        }

        lifecycleScope.launch {
            orderIsPayState.drop(1)
                .collect { state ->
                    when (state) {
                        is ApiState.Error -> {
                            showToast("不需要支付")
                            goQuestion(categoryModel)
                        }

                        is ApiState.Success -> {
                            state.data?.let { payData ->
                                payId = state.data.question_category.id
                                payNum = payData.money

                                // 更新支付金额文本
                                binding.tvPayAmount.text = String.format("您当前余额0元，需付费%s元/类", payNum)

                                // 显示付费弹窗
                                showPayDialog()
                            }
                        }

                        else -> {}
                    }
                }
        }

        lifecycleScope.launch {
            twoOrderIsPayState.drop(1)
                .collect { state ->
                    when (state) {
                        is ApiState.Error -> {
                            showToast("不需要支付")
                            goQuestion(categoryModel)
                        }

                        is ApiState.Success -> {
                            state.data?.let { payData ->
                                payId = state.data.question_category.id
                                payNum = payData.money

                                // 更新支付金额文本
                                binding.tvPayAmount.text = String.format("您当前余额0元，需付费%s元/类", payNum)

                                // 显示付费弹窗
                                showPayDialog()
                            }
                        }

                        else -> {}
                    }
                }
        }
    }

    /**
     * 获取题目列表
     */
    private fun getQuestionList(params: Map<String, String>) {
        categoryList.clear()
        role.clear()

        if (from == "twoList") {
            examViewModel.getTwoList(params, twoListState)
        } else {
            examViewModel.getQuestionList(questionListState)
        }
    }

    /**
     * 处理题目列表响应
     */
    private fun handlQueRes(resInfo: QuestionListData?) {
        if (resInfo != null) {
            nickName = resInfo.nickname
            role.clear()

            // 普通角色
            val roleArr = listOf("驾驶员", "安全员", "押运员", "安全负责人")
            val resRole = resInfo.stype.split(",")

            resRole.forEach { item ->
                val index = item.toIntOrNull() ?: 0
                if (index < roleArr.size) {
                    role.add(RoleModel(roleArr[index], item))
                }
            }

            if (title.isEmpty() && role.isNotEmpty()) {
                title = role[0].text
                roleId = role[0].value
            }

            // 更新总数
            allTestNum = resInfo.question_count
            answerCount = resInfo.answer_count

            // 处理分类列表
            val resObj = resInfo.category_list
            categoryList.clear()

            if (resObj.isNotEmpty()) {
                resObj.forEach { value ->
                    categoryList.add(
                        CategoryModel(
                            category_name = value.category_name,
                            answer_count = value.answer_count,
                            question_count = value.question_count,
                            question_category_id = 97,
                            user_exam_id = value.user_exam_id
                        )
                    )
                }
            }

            // 更新标题（优化：使用String.format）
            binding.tvTitle.text = String.format("%s:%s", title, nickName)
            // 显示/隐藏下拉箭头
            binding.ivArrowDown.visibility = if (role.size > 1) View.VISIBLE else View.GONE
        }

        loadingShow = true
        updateUI()
    }

    private fun handlTwoQueRes(resInfo: TwoListData?) {
        if (resInfo != null) {
            nickName = resInfo.nickname
            role.clear()

            if (from == "twoList") {
                // 两类人员角色
                role.add(RoleModel("企业安全主要负责人", "3"))
                role.add(RoleModel("安全生产管理人员", "1"))

                title = if (activeId == role[0].value) role[0].text else role[1].text
                roleId = if (activeId == role[0].value) role[0].value else role[1].value
            }

            // 更新总数
            allTestNum = resInfo.question_count
            answerCount = resInfo.answer_count

            // 处理分类列表
            var key = 107
            var resObj = resInfo.category_list.`107`
            if (resObj == null) {
                key = 106
                resObj = resInfo.category_list.`106`
            }

            categoryList.clear()

            categoryList.add(
                CategoryModel(
                    category_name = resObj.category_name,
                    answer_count = resObj.answer_count,
                    question_count = resObj.question_count,
                    question_category_id = key,
                    user_exam_id = resObj.user_exam_id
                )
            )

            // 更新标题（优化：使用String.format）
            binding.tvTitle.text = String.format("%s:%s", title, nickName)
            // 显示/隐藏下拉箭头
            binding.ivArrowDown.visibility = if (role.size > 1) View.VISIBLE else View.GONE
        }

        loadingShow = true
        updateUI()
    }

    /**
     * 更新UI显示（优化：使用binding访问所有控件）
     */
    private fun updateUI() {
        // 更新分类列表
        updateCategoryList()

        // 显示/隐藏空数据和分类内容
        binding.llNoContent.visibility = if (categoryList.isEmpty() && loadingShow) View.VISIBLE else View.GONE
        binding.llSortContent.visibility = if (categoryList.isNotEmpty() && loadingShow) View.VISIBLE else View.GONE
    }

    /**
     * 更新分类列表（优化：使用ItemCategoryBinding）
     */
    private fun updateCategoryList() {
        binding.llCategoryList.removeAllViews()

        categoryList.forEach { item ->
            // 使用ItemCategoryBinding创建列表项
            val itemBinding = ItemCategoryBinding.inflate(layoutInflater)

            // 设置数据（优化：直接使用binding）
            itemBinding.tvCategoryName.text = item.category_name
            itemBinding.tvCount.text = String.format("%s/%s", item.answer_count, item.question_count)

            // 点击事件
            PressEffectUtils.setCommonPressEffect(itemBinding.root)
            itemBinding.root.setOnClickListener {
                categoryModel = item
                ifPay(item)
            }

            binding.llCategoryList.addView(itemBinding.root)
        }
    }

    /**
     * 检查是否需要支付
     */
    private fun ifPay(data: CategoryModel) {
        if (from == "twoList") {
            examViewModel.twoOrderPay(mapOf("question_category_id" to data.question_category_id.toString()), twoOrderIsPayState)
        } else {
            trainingViewModel.orderIsPay(data.question_category_id.toString(), orderIsPayState)
        }
    }

    /**
     * 支付
     */
    private fun payMoney() {
        val params = mapOf(
            "type" to "wechat",
            "method" to "app",
            "question_category_id" to payId.toString()
        )

        if (from == "twoList") {
            examViewModel.createTwoOrder(params, twoOrderState)
        } else {
            examViewModel.createQuestionOrder(params, orderState)
        }
    }

    /**
     * 跳转到答题页面
     */
    private fun goQuestion(data: CategoryModel?) {
        hidePayDialog()

        try {
            val intent = Intent(this, AnswerQuestionActivity::class.java).apply {
                putExtra("question_category_id", data?.question_category_id)
                putExtra("user_exam_id", data?.user_exam_id)
                putExtra("user_category_id", roleId)
                putExtra("from", from)
            }

            startActivity(intent)
            finish()
        } catch (e: Exception) {
            // 跳转失败处理
            init()
            showTopTips("请重新点击", "error")
        }
    }

    /**
     * 切换角色（优化：使用DialogRoleSheetBinding）
     */
    private fun changeRole() {
        if (role.size > 1) {
            // 初始化角色弹窗Binding
            roleSheetBinding = DialogRoleSheetBinding.inflate(layoutInflater)

            // 添加角色选项
            role.forEachIndexed { index, roleModel ->
                // 使用ItemRoleBinding创建角色项
                val roleItemBinding = ItemRoleBinding.inflate(layoutInflater)
                roleItemBinding.tvRoleName.text = roleModel.text

                // 角色点击事件
                PressEffectUtils.setCommonPressEffect(roleItemBinding.root)
                roleItemBinding.root.setOnClickListener {
                    clickRole(index)
                    // 关闭弹窗
                    mAlertDialog?.dismiss()
                }

                roleSheetBinding.llRoleList.addView(roleItemBinding.root)
            }

            // 取消按钮点击事件
            PressEffectUtils.setCommonPressEffect(roleSheetBinding.tvCancel)
            roleSheetBinding.tvCancel.setOnClickListener {
                mAlertDialog?.dismiss()
            }

            // 显示角色弹窗
            AlertDialog.Builder(this).apply {
                setView(roleSheetBinding.root)
                mAlertDialog = create()
                mAlertDialog?.window?.setGravity(Gravity.BOTTOM)
                mAlertDialog?.show()
            }
        }
    }

    /**
     * 选择角色
     */
    private fun clickRole(key: Int) {
        if (key < role.size) {
            categoryList.clear()
            val selectedRole = role[key]

            title = selectedRole.text
            roleId = selectedRole.value
            activeId = selectedRole.value

            // 更新标题
            binding.tvTitle.text = String.format("%s:%s", title, nickName)

            // 获取对应角色的题目列表
            getQuestionList(mapOf("user_category_id" to activeId))
        }
    }

    /**
     * 返回
     */
    private fun goBack() {
        finish()
    }

    // ========== 工具方法优化 ==========
    /**
     * 显示付费弹窗
     */
    private fun showPayDialog() {
        payShow = true
        binding.flPayDialog.visibility = View.VISIBLE
    }

    /**
     * 隐藏付费弹窗
     */
    private fun hidePayDialog() {
        payShow = false
        binding.flPayDialog.visibility = View.GONE
    }

    /**
     * 显示Toast（优化：封装为函数）
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * 显示顶部提示（优化：使用LayoutTopTipsBinding）
     */
    private fun showTopTips(title: String, type: String) {
        // 使用ViewBinding创建提示条
        val tipBinding = LayoutTopTipsBinding.inflate(layoutInflater)

        // 设置提示文本和颜色
        tipBinding.tvTip.text = title
        tipBinding.tvTip.setTextColor(
            if (type == "error") ContextCompat.getColor(this, R.color.error)
            else ContextCompat.getColor(this, R.color.success)
        )

        // 添加到布局并显示
        binding.llTopTips.removeAllViews()
        binding.llTopTips.addView(tipBinding.root)
        binding.llTopTips.visibility = View.VISIBLE

        // 2秒后隐藏
        binding.llTopTips.postDelayed({
            binding.llTopTips.visibility = View.GONE
            binding.llTopTips.removeAllViews()
        }, 2000)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EventData) {
        when (event.eventType) {
            EventData.EVENT_WXPAY_SUCCESS -> {
                showToast("支付成功，即将进入答题")
                hidePayDialog()

                // 跳转到答题页面
                categoryList.find { it.question_category_id == payId }?.let {
                    goQuestion(it)
                }
            }
            EventData.EVENT_WXPAY_CANCEL -> {
                hidePayDialog()
                AlertDialog.Builder(this@ExamPracticeActivity)
                    .setTitle("温馨提示")
                    .setMessage("订单尚未支付")
                    .setCancelable(false)
                    .setPositiveButton("确定", null)
                    .show()
            }
            EventData.EVENT_WXPAY_FAIL -> {
                val payResult = event.data as WxPayResult
                showToast("支付失败:${payResult.errMsg}")
                hidePayDialog()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}
data class RoleModel(
    val text: String,
    val value: String
)
data class CategoryModel(
    val category_name: String,
    val answer_count: Int,
    val question_count: Int,
    val question_category_id: Int,
    val user_exam_id: Int
)
// 扩展函数：转换为微信支付SDK的PayReq对象
fun WxPayParams.toPayReq(): PayReq {
    return PayReq().apply {
        appId = this@toPayReq.appId
        partnerId = this@toPayReq.partnerId
        prepayId = this@toPayReq.prepayId
        packageValue = this@toPayReq.packageValue
        nonceStr = this@toPayReq.nonceStr
        timeStamp = this@toPayReq.timeStamp
        sign = this@toPayReq.sign
    }
}