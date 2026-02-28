package com.fx.zfcar.training.safetytraining

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fx.zfcar.databinding.ActivityTrainHomeBinding
import com.fx.zfcar.net.CheckSafeData
import com.fx.zfcar.net.CompanyPayData
import com.fx.zfcar.net.ExamsListData
import com.fx.zfcar.net.MeetingListData
import com.fx.zfcar.net.OldSafetyListData
import com.fx.zfcar.net.BeforeSubjectListData
import com.fx.zfcar.net.ExamItem
import com.fx.zfcar.net.OrderIsPayData
import com.fx.zfcar.net.PayInfo
import com.fx.zfcar.net.PostSignImgData
import com.fx.zfcar.net.QuestionOrderPayData
import com.fx.zfcar.net.SafetyListData
import com.fx.zfcar.net.SubjectListData
import com.fx.zfcar.net.SubjectOrderData
import com.fx.zfcar.net.SubjectPayData
import com.fx.zfcar.net.TrainItem
import com.fx.zfcar.net.TrainingOtherInfo
import com.fx.zfcar.training.adapter.TrainListAdapter
import com.fx.zfcar.training.adapter.TrainListItem
import com.fx.zfcar.training.notice.SignatureActivity
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.training.widget.ExamTicketGenerator
import com.fx.zfcar.util.SPUtils
import com.fx.zfcar.viewmodel.ApiState
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.*
import kotlin.getValue

class TrainHomeActivity : AppCompatActivity(), TrainListAdapter.OnItemClickListener {
    private lateinit var binding: ActivityTrainHomeBinding
    private var trainListAdapter: TrainListAdapter? = null
    private var trainDataList: MutableList<TrainListItem>? = mutableListOf()
    private val viewModel by viewModels<SafetyTrainingViewModel>()

    // 页面状态（仅从Intent获取）
    private var currentType = 0 // 0-安全 1-岗前 2-在线测验 3-安全会议 4-继续教育
    private var trainType = 0 // 0-进行中 1-历史（仅安全培训用）
    private var meetingType = 0 // 0-进行中 1-历史（仅安全会议用）
    private var currentPage = 1
    private var totalPage = 1
    private var categoryId = ""

    // 在线测验相关
    private var currentExamTab = 0 // 0-审核中 1-已审核
    private var starttime = ""
    private var endtime = ""

    // 防抖点击（1秒内只响应一次）
    private var lastClickTime = 0L
    // 加载状态锁（防止重复加载）
    private var isLoading = false

    // StateFlow 定义（对应各网络请求）
    private val userOtherInfoState = MutableStateFlow<ApiState<TrainingOtherInfo>>(ApiState.Idle)
    private val safetyListState = MutableStateFlow<ApiState<SafetyListData>>(ApiState.Idle)
    private val oldSafetyListState = MutableStateFlow<ApiState<OldSafetyListData>>(ApiState.Idle)
    private val beforeListState = MutableStateFlow<ApiState<BeforeSubjectListData>>(ApiState.Idle)
    private val examsListState = MutableStateFlow<ApiState<ExamsListData>>(ApiState.Idle)
    private val meetingListState = MutableStateFlow<ApiState<MeetingListData>>(ApiState.Idle)
    private val subjectListState = MutableStateFlow<ApiState<SubjectListData>>(ApiState.Idle)
    private val checkSafeState = MutableStateFlow<ApiState<CheckSafeData>>(ApiState.Idle)
    private val orderIsPayState = MutableStateFlow<ApiState<QuestionOrderPayData>>(ApiState.Idle)
    private val subjectPayState = MutableStateFlow<ApiState<SubjectOrderData>>(ApiState.Idle)
    private val companyPayState = MutableStateFlow<ApiState<CompanyPayData>>(ApiState.Idle)
    private val postSignImgState = MutableStateFlow<ApiState<PostSignImgData>>(ApiState.Idle)

    private lateinit var examTicketGenerator: ExamTicketGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        parseIntentParams() // 解析Intent参数
        collectStateFlows() // 收集StateFlow数据
        checkUserStatus() // 检查用户状态
    }

    /**
     * 解析Intent参数（核心：type完全由外部传入）
     */
    private fun parseIntentParams() {
        // 必传参数：type
        currentType = intent.getIntExtra("type", 0)
        // 可选参数：title（页面标题）
        binding.tvTitle.text = intent.getStringExtra("title") ?: getDefaultTitle(currentType)

        // 从签名页面返回的特殊处理
        if (intent.getStringExtra("fromUrl") == "sign" && currentType == 0) {
            val id = SPUtils.get("id")
            val signfile = SPUtils.get("dailySign")
            if (id.isNotEmpty() && signfile.isNotEmpty()) {
                postSignImg(id, signfile)
            }
        }

        // 根据type控制子Tab显示/隐藏
        when (currentType) {
            0, 3 -> binding.tabSub.visibility = View.VISIBLE // 安全培训/安全会议显示子Tab
            else -> binding.tabSub.visibility = View.GONE // 其他类型隐藏子Tab
        }
    }

    /**
     * 根据type获取默认标题
     */
    private fun getDefaultTitle(type: Int): String {
        return when (type) {
            0 -> "安全培训"
            1 -> "岗前培训"
            2 -> "在线测验"
            3 -> "安全会议"
            4 -> "继续教育"
            else -> "培训中心"
        }
    }

    /**
     * 初始化视图（移除所有下拉刷新相关代码）
     */
    private fun initView() {
        // 子Tab（进行中/历史）初始化
        val subTitles = arrayOf("进行中", "历史")
        subTitles.forEach { binding.tabSub.addTab(binding.tabSub.newTab().setText(it)) }
        binding.tabSub.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (currentType) {
                    0 -> { // 安全培训切换进行中/历史
                        trainType = tab.position
                        trainDataList?.clear()
                        trainListAdapter?.updateDynamicType(trainType)
                    }
                    3 -> meetingType = tab.position // 安全会议切换进行中/历史
                }
                resetPageStatus()
                loadData()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // 列表初始化
        trainListAdapter = TrainListAdapter(currentType)
        trainListAdapter!!.submitList(trainDataList)
        binding.rvTrainList.adapter = trainListAdapter
        binding.rvTrainList.layoutManager = LinearLayoutManager(this)

        // 上拉加载更多（仅保留此逻辑，移除下拉刷新）
        binding.rvTrainList.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                // 上拉且滑到最后一项、未在加载、还有更多数据时触发加载
                if (dy > 0
                    && lastVisibleItem == trainListAdapter!!.itemCount - 1
                    && !isLoading
                    && currentPage < totalPage) {
                    currentPage++
                    loadData(isLoadMore = true)
                }
            }
        })
    }

    /**
     * 收集所有StateFlow数据，处理网络请求结果
     */
    private fun collectStateFlows() {
        // 收集用户其他信息结果
        lifecycleScope.launch {
            userOtherInfoState.collect { state ->
                handleUserOtherInfoState(state)
            }
        }

        // 收集安全培训列表结果
        lifecycleScope.launch {
            safetyListState.collect { state ->
                handleBaseListState(state, isLoadMore = false)
            }
        }

        // 收集历史安全培训列表结果
        lifecycleScope.launch {
            oldSafetyListState.collect { state ->
                handleBaseListState(state, isLoadMore = false)
            }
        }

        // 收集岗前培训列表结果
        lifecycleScope.launch {
            beforeListState.collect { state ->
                handleBaseListState(state, isLoadMore = false)
            }
        }

        // 收集在线测验列表结果
        lifecycleScope.launch {
            examsListState.collect { state ->
                handleBaseListState(state, isLoadMore = false)
            }
        }

        // 收集安全会议列表结果
        lifecycleScope.launch {
            meetingListState.collect { state ->
                handleBaseListState(state, isLoadMore = false)
            }
        }

        // 收集继续教育列表结果
        lifecycleScope.launch {
            subjectListState.collect { state ->
                handleBaseListState(state, isLoadMore = false)
            }
        }

        // 收集支付检查结果
        lifecycleScope.launch {
            checkSafeState.collect { state ->
                when (state) {
                    is ApiState.Success -> {
                        state.data?.let { handlePayCheckResult(state.data, "train") }
                    }
                    is ApiState.Error -> {
                        showToast("支付检查失败：${state.msg}")
                    }
                    else -> {}
                }
            }
        }

        // 收集订单支付状态结果
        lifecycleScope.launch {
            orderIsPayState.collect { state ->
                when (state) {
                    is ApiState.Success -> {
                        state.data?.let { handlePayCheckResult(state.data, "daily") }
                    }
                    is ApiState.Error -> {
                        showToast("订单支付检查失败：${state.msg}")
                    }
                    else -> {}
                }
            }
        }

        // 收集继续教育支付检查结果
        lifecycleScope.launch {
            subjectPayState.collect { state ->
                when (state) {
                    is ApiState.Success -> {
                        state.data?.let { handlePayCheckResult(state.data, "subject") }
                    }
                    is ApiState.Error -> {
                        showToast("继续教育支付检查失败：${state.msg}")
                    }
                    else -> {}
                }
            }
        }

        // 收集企业支付结果
        lifecycleScope.launch {
            companyPayState.collect { state ->
                when (state) {
                    is ApiState.Success -> {
                        showToast("企业支付成功")
//                        gotoFaceCheck(TrainItem(id = SPUtils.get("tempTrainItemId"),
//                            name = SPUtils.get("tempTrainItemName")),
//                            "daily")
                    }
                    is ApiState.Error -> {
                        showToast("企业支付失败：${state.msg}")
                    }
                    else -> {}
                }
            }
        }

        // 收集提交签名结果
        lifecycleScope.launch {
            postSignImgState.collect { state ->
                finish() // 无论成功失败都关闭页面
            }
        }
    }

    /**
     * 处理用户其他信息请求状态
     */
    private fun handleUserOtherInfoState(state: ApiState<TrainingOtherInfo>) {
        when (state) {
            is ApiState.Success -> {
                state.data?.let {
                    SPUtils.save("testCheck", it.yzstatus)
                    categoryId = it.category_id

                    // 未认证则跳转到头像认证页面
                    if (it.yzstatus != 1) {
//                        startActivity(Intent(this@TrainHomeActivity, AvatarActivity::class.java))
//                        finish()
                        return
                    }

                    // 初始化加载数据
                    resetPageStatus()
                    loadData()
                }
            }
            is ApiState.Error -> {
                showToast(state.msg)
            }
            else -> {}
        }
    }

    /**
     * 处理通用列表请求状态
     */
    private fun <T> handleBaseListState(state: ApiState<T>, isLoadMore: Boolean) {
        isLoading = false // 释放加载锁
        when (state) {
            is ApiState.Success -> {
                val data = state.data
                var dataList: List<TrainListItem>? = emptyList()
                when (data) {
                    is SafetyListData -> {
                        totalPage = data.total
                        dataList = data?.publicList?.map { TrainListItem.TypeSafeItem(it) }
                    }
                    is OldSafetyListData -> {
//                        totalPage = data.total
//                        dataList = data?.rows?.map { TrainListItem.TypeSafeItem(it) }
                    }
                    is BeforeSubjectListData -> {
                        totalPage = data.total
                        dataList = data?.rows?.map { TrainListItem.TypePreJobItem(it) }
                    }
                    is ExamsListData -> {
                        totalPage = data.total
                        dataList = data?.rows?.map { TrainListItem.TypeExamItem(it) }
                    }
                    is MeetingListData -> {
                        totalPage = data.total
                        dataList = data?.rows?.map { TrainListItem.TypeMeetingItem(it) }
                    }
                    is SubjectListData -> {
                        totalPage = data.total
                        dataList = data?.rows?.map { TrainListItem.TypeContinueItem(it) }
                    }
                    else -> {
                        totalPage = 1
                    }
                }

                trainDataList?.addAll(dataList!!)

                // 更新列表

                // 空数据处理
                if (dataList?.isEmpty() == true) {
                    binding.tvEmpty.visibility = View.VISIBLE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                }

                // 加载更多状态
                if (currentPage >= totalPage) {
                    binding.tvLoadMore.text = "没有更多了"
                } else {
                    binding.tvLoadMore.text = "上拉加载更多"
                }
            }
            is ApiState.Error -> {
                showToast("加载失败：${state.msg}")
                binding.tvLoadMore.text = "加载失败，上拉重试"
            }
            else -> {}
        }
    }

    /**
     * 处理支付检查结果
     */
    private fun handlePayCheckResult(data: Any, type: String) {
        val payInfo = when (data) {
            is CheckSafeData -> PayInfo(
                money = data.money,
                usualpaytype = data.usualpaytype
            )
            is OrderIsPayData -> PayInfo(
                money = data.money,
                usualpaytype = data.usualpaytype
            )
            is SubjectPayData -> PayInfo(
                money = data.money,
                usualpaytype = data.usualpaytype
            )
            else -> PayInfo()
        }

        val itemId = SPUtils.get("tempTrainItemId")
        val itemName = SPUtils.get("tempTrainItemName")
        val item = TrainItem(id = itemId, name = itemName)

        if (type == "before") {
            // 岗前培训直接跳人脸识别
//            gotoFaceCheck(item, type)
            return
        }

        when {
            (data as? CheckSafeData)?.msg == "已过期" || payInfo.money.isNotEmpty() -> {
                // 跳支付页面
//                gotoPayPage(item, payInfo, type)
            }
            else -> {
                // 无需支付，跳人脸识别
//                gotoFaceCheck(item, type)
            }
        }
    }

    /**
     * 检查用户状态
     */
    private fun checkUserStatus() {
        viewModel.getUserOtherInfo(userOtherInfoState)
    }

    /**
     * 重置页面状态（移除下拉刷新相关）
     */
    private fun resetPageStatus() {
        currentPage = 1
        totalPage = 1
        isLoading = false
        binding.tvEmpty.visibility = View.GONE
        binding.tvLoadMore.text = "上拉加载更多"
    }

    /**
     * 加载数据（基于ViewModel + StateFlow）
     */
    private fun loadData(isLoadMore: Boolean = false) {
        // 加载中则直接返回，防止重复请求
        if (isLoading) return
        isLoading = true

        // 显示加载状态
        if (isLoadMore) {
            binding.tvLoadMore.text = "正在加载..."
        }

        // 根据type调用不同的ViewModel方法
        when (currentType) {
            0 -> { // 安全培训
                if (trainType == 0) {
                    viewModel.getSafetyList(currentPage, currentType, safetyListState)
                } else {
                    viewModel.getOldSafetyList(currentPage, oldSafetyListState)
                }
            }
            1 -> { // 岗前培训
                viewModel.getBeforeList(beforeListState)
            }
            2 -> { // 在线测验
                viewModel.getExamsList(currentPage, currentExamTab, starttime, endtime, examsListState)
            }
            3 -> { // 安全会议
                viewModel.getMeetingList(currentPage, meetingType, meetingListState)
            }
            4 -> { // 继续教育
                viewModel.getSubjectList(currentPage, subjectListState)
            }
            else -> {
                viewModel.getSafetyList(currentPage, currentType, safetyListState)
            }
        }
    }

    /**
     * 防抖点击处理
     */
    private fun isFastClick(): Boolean {
        val currentTime = System.currentTimeMillis()
        val interval = currentTime - lastClickTime
        lastClickTime = currentTime
        return interval < 1000
    }

    /**
     * 学习按钮点击
     */
    override fun onStudyClick(item: TrainListItem) {
        if (isFastClick()) return

        // 日常培训：检查是否需要签名
//        if (type == "daily" && item.progress >= 100 && item.issign == "1" && item.imgurl.isNullOrEmpty()) {
//            goSignPage(item.name, item.id)
//            return
//        }

        // 保存临时item信息，用于支付检查回调
//        SPUtils.save("tempTrainItemId", item.id)
//        SPUtils.save("tempTrainItemName", item.name)

        // 检查支付状态
//        checkPayStatus(item, type)
    }

    /**
     * 检查支付状态（调用ViewModel方法）
     */
    private fun checkPayStatus(item: TrainItem, type: String) {
        when (type) {
            "train" -> viewModel.checkSafe(item.id, checkSafeState)
            "daily" -> viewModel.orderIsPay(item.id, orderIsPayState)
            "subject" -> viewModel.subjectPay(item.id, subjectPayState)
            "before" -> handlePayCheckResult(Any(), type) // 岗前培训无需支付检查
        }
    }

    /**
     * 跳转到签名页面
     */
    private fun goSignPage(name: String, id: String) {
        SPUtils.save("dailyName", name)
        SPUtils.save("dailyId", id)

        val intent = Intent(this, SignatureActivity::class.java)
        intent.putExtra("from", "Training")
        intent.putExtra("fill", "dailySign")
        intent.putExtra("type", currentType.toString())
        startActivity(intent)
    }

    /**
     * 跳转到支付页面
     */
    private fun gotoPayPage(item: TrainListItem, payInfo: PayInfo, type: String) {
//        val intent = Intent(this, PayActivity::class.java)
//        intent.putExtra("name", item.name)
//        intent.putExtra("id", item.id)
//        intent.putExtra("money", payInfo.money)
//        intent.putExtra("type", type)
//        intent.putExtra("usualpaytype", payInfo.usualpaytype)
//        startActivity(intent)
    }

    /**
     * 跳转到人脸识别页面
     */
    private fun gotoFaceCheck(item: TrainListItem, type: String) {
//        val intent = Intent(this, FaceCheckActivity::class.java)
//        intent.putExtra("safetyPlanId", item.id)
//        intent.putExtra("name", item.name)
//        intent.putExtra("number", item.number ?: "")
//        intent.putExtra("type", type)
//        intent.putExtra("faceType", "start")
//        startActivity(intent)
    }

    /**
     * 考试按钮点击
     */
    override fun onExamClick(item: TrainListItem) {
//        val intent = Intent(this, ExamActivity::class.java)
//        intent.putExtra("id", item.training_exams_id)
//        intent.putExtra("name", item.name)
//        intent.putExtra("type", type)
//        intent.putExtra("training_safetyplan_id", item.id)
//        startActivity(intent)
    }

    /**
     * 认证按钮点击
     */
    override fun onAuthClick(item: TrainListItem) {
//        val intent = Intent(this, AuthenticationActivity::class.java)
//        intent.putExtra("id", item.id)
//        intent.putExtra("name", item.name)
//        startActivity(intent)
    }

    /**
     * 会议项点击
     */
    override fun onMeetingClick(item: TrainListItem) {
//        val intent = Intent(this, MeetingActivity::class.java)
//        intent.putExtra("id", item.id)
//        startActivity(intent)
    }

    /**
     * 提交签名
     */
    private fun postSignImg(id: String, signfile: String) {
        viewModel.postSignImg(id, signfile, postSignImgState)
    }

    override fun onResume() {
        super.onResume()
        // 页面恢复时重新加载数据
        resetPageStatus()
        loadData()
    }
}