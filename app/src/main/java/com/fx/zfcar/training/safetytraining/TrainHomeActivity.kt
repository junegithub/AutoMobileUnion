package com.fx.zfcar.training.safetytraining

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fx.zfcar.databinding.ActivityTrainListBinding
import com.fx.zfcar.net.BeforeSubjectListData
import com.fx.zfcar.net.CheckSafeData
import com.fx.zfcar.net.MeetingItem
import com.fx.zfcar.net.MeetingListData
import com.fx.zfcar.net.OldSafetyListData
import com.fx.zfcar.net.OrderIsPayData
import com.fx.zfcar.net.PayInfo
import com.fx.zfcar.net.PostSignImgData
import com.fx.zfcar.net.QuestionOrderPayData
import com.fx.zfcar.net.SafetyListData
import com.fx.zfcar.net.SubjectItem
import com.fx.zfcar.net.SubjectListData
import com.fx.zfcar.net.SubjectOrderData
import com.fx.zfcar.net.SubjectPayData
import com.fx.zfcar.net.TrainItem
import com.fx.zfcar.net.TrainingOtherInfo
import com.fx.zfcar.training.adapter.TrainListAdapter
import com.fx.zfcar.training.adapter.TrainListItem
import com.fx.zfcar.training.notice.SignatureActivity
import com.fx.zfcar.training.user.UserCenterActivity
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.util.SPUtils
import com.fx.zfcar.viewmodel.ApiState
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.getValue

class TrainListActivity : AppCompatActivity(), TrainListAdapter.OnItemClickListener {
    private lateinit var binding: ActivityTrainListBinding


    private val trainingViewModel by viewModels<SafetyTrainingViewModel>()
    private val safetyListState = MutableStateFlow<ApiState<SafetyListData>>(ApiState.Idle)
    private val oldSafetyListState = MutableStateFlow<ApiState<OldSafetyListData>>(ApiState.Idle)
    private val beforeListState = MutableStateFlow<ApiState<BeforeSubjectListData>>(ApiState.Idle)
    private val meetingListState = MutableStateFlow<ApiState<MeetingListData>>(ApiState.Idle)
    private val subjectListState = MutableStateFlow<ApiState<SubjectListData>>(ApiState.Idle)
    private val otherUserInfoState = MutableStateFlow<ApiState<TrainingOtherInfo>>(ApiState.Idle)
    private val checkSafeState = MutableStateFlow<ApiState<CheckSafeData>>(ApiState.Idle)
    private val orderIsPayState = MutableStateFlow<ApiState<QuestionOrderPayData>>(ApiState.Idle)
    private val subjectPayState = MutableStateFlow<ApiState<SubjectOrderData>>(ApiState.Idle)
    private val postSignImgState = MutableStateFlow<ApiState<PostSignImgData>>(ApiState.Idle)

    private val trainAdapter by lazy { TrainListAdapter() }
    private var trainDataList: MutableList<TrainListItem>? = mutableListOf()

    // Intent传入参数
    private var currentType = 0 // 0-安全 1-岗前 3-会议 4-继续教育
    private var pageTitle = ""
    private var meetingStatus = 0 // 安全培训：0-进行中 1-历史
    private var trainStatus = 0 // 会议子状态：0-进行中 1-历史
    private var category_id = ""

    // 分页参数
    private var currentPage = 1
    private var totalPage = 1
    // 标记是否正在加载
    private var isLoading = false

    // 支付状态常量
    companion object {
        private const val PAY_STATUS_UNPAID = 0 // 未支付
        private const val PAY_STATUS_PAID = 1   // 已支付
        private const val PAY_STATUS_FREE = 2   // 免费
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 获取Intent参数（过滤type=2，默认走安全培训）
        currentType = intent.getIntExtra("type", 0).takeIf { it != 2 } ?: 0
        pageTitle = intent.getStringExtra("title") ?: when (currentType) {
            0 -> "安全培训"
            1 -> "岗前培训"
            3 -> "安全会议"
            4 -> "继续教育"
            else -> "培训列表"
        }
        // 从签名页面返回的特殊处理
        if (intent.getStringExtra("fromUrl") == "sign" && currentType == 0) {
            val id = SPUtils.get("id")
            val signfile = SPUtils.get("dailySign")
            if (id.isNotEmpty() && signfile.isNotEmpty()) {
                postSignImg(id, signfile)
            }
        }

        initView()
        collectStateFlows()
        getUserInfo()
        loadData()
    }

    private fun initView() {
        // 设置标题
        supportActionBar?.title = pageTitle

        // 控制subTabLayout显示：仅会议类型（type=3）显示
        binding.subTabLayout.visibility = if (currentType == 3) View.VISIBLE else View.GONE

        // 初始化会议子Tab
        if (currentType == 0 || currentType == 3) {
            binding.subTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    when (currentType) {
                        0 -> { // 安全培训切换进行中/历史
                            trainStatus = tab.position
                            trainDataList?.clear()
                            trainAdapter.updateDynamicType(trainStatus)
                        }
                        3 -> meetingStatus = tab.position // 安全会议切换进行中/历史
                    }
                    resetPageAndLoad()
                }
                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
        }

        // 列表初始化
        binding.rvTrainList.adapter = trainAdapter
        binding.rvTrainList.layoutManager = LinearLayoutManager(this)
        trainAdapter.setCurrentType(currentType)
        trainAdapter.submitList(trainDataList)

        // 仅保留上拉加载更多（移除下拉刷新）
        binding.rvTrainList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (isLoading || currentPage >= totalPage) return

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                // 滑动到最后1个item时加载更多
                if (lastVisibleItem >= totalItemCount - 1 && dy > 0) {
                    isLoading = true
                    currentPage++
                    loadData()
                }
            }
        })

        // 个人中心点击
        PressEffectUtils.setCommonPressEffect(binding.ivUser)
        binding.ivUser.setOnClickListener {
            startActivity(Intent(this, UserCenterActivity::class.java))
        }
    }

    /**
     * 提交签名
     */
    private fun postSignImg(id: String, signfile: String) {
        trainingViewModel.postSignImg(id, signfile, postSignImgState)
    }

    // 获取用户信息
    private fun getUserInfo() {
        trainingViewModel.getUserOtherInfo(otherUserInfoState)
    }

    // 重置分页并加载
    private fun resetPageAndLoad() {
        currentPage = 1
        totalPage = 1
        loadData()
    }

    // 根据type获取标识
    private fun getTypeTag(): String {
        return when (currentType) {
            0 -> "daily"
            4 -> "subject"
            else -> "daily"
        }
    }

    // 加载数据（移除测验分支）
    private fun loadData() {
        when (currentType) {
            0 -> {
                if (trainStatus == 0) {
                    if (category_id == "3031") {
                        trainingViewModel.getSubjectList(currentPage, subjectListState)
                    } else {
                        trainingViewModel.getSafetyList(currentPage, currentType, safetyListState)
                    }
                } else {
                    trainingViewModel.getOldSafetyList(currentPage, oldSafetyListState)
                }

            }
            1 -> trainingViewModel.getBeforeList(beforeListState)
            3 -> trainingViewModel.getMeetingList(currentPage, meetingStatus, meetingListState)
            4 -> trainingViewModel.getSubjectList(currentPage, subjectListState)
            else -> trainingViewModel.getSafetyList(currentPage, currentType, safetyListState)
        }
    }

    /**
     * 收集所有StateFlow数据，处理网络请求结果
     */
    private fun collectStateFlows() {
        lifecycleScope.launch {
            otherUserInfoState.collect { state ->
                when(state) {
                    is ApiState.Idle -> {}
                    is ApiState.Error -> {
                        showToast(state.msg)
                    }
                    is ApiState.Success -> {
                        state.data?.let {
                            category_id = state.data.category_id
                            // 检查认证状态
                            if (state.data.yzstatus != 1) {
                                startActivity(Intent(this@TrainListActivity, AvatarActivity::class.java))
                                finish()
                            }
                        }
                    }
                    is ApiState.Loading -> {

                    }
                }
            }
        }
        // 收集安全培训列表结果
        lifecycleScope.launch {
            safetyListState.collect { state ->
                handleBaseListState(state)
            }
        }

        // 收集历史安全培训列表结果
        lifecycleScope.launch {
            oldSafetyListState.collect { state ->
                handleBaseListState(state)
            }
        }

        // 收集岗前培训列表结果
        lifecycleScope.launch {
            beforeListState.collect { state ->
                handleBaseListState(state)
            }
        }

        // 收集安全会议列表结果
        lifecycleScope.launch {
            meetingListState.collect { state ->
                handleBaseListState(state)
            }
        }

        // 收集继续教育列表结果
        lifecycleScope.launch {
            subjectListState.collect { state ->
                handleBaseListState(state)
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

        lifecycleScope.launch {
            postSignImgState.collect { state ->
                finish() // 无论成功失败都关闭页面
            }
        }
    }

    /**
     * 处理通用列表请求状态
     */
    private fun <T> handleBaseListState(state: ApiState<T>) {
        isLoading = false // 释放加载锁
        when (state) {
            is ApiState.Loading -> {
                if (currentPage == 1) { // 仅第一页显示加载中
                    binding.tvLoadMore.visibility = View.VISIBLE
                    binding.emptyView.visibility = View.GONE
                }
            }
            is ApiState.Success -> {
                binding.tvLoadMore.visibility = View.GONE
                binding.emptyView.visibility = if (trainAdapter.itemCount == 0) View.VISIBLE else View.GONE

                val data = state.data
                var dataList: List<TrainListItem>? = emptyList()
                when (data) {
                    is SafetyListData -> {
                        totalPage = data.total
                        dataList = data?.publicList?.map { TrainListItem.TypeSafeItem(it) }
                    }
                    is OldSafetyListData -> {
                        totalPage = data.total
                        dataList = data?.rows?.map { TrainListItem.TypeSafeOldItem(it) }
                    }
                    is BeforeSubjectListData -> {
                        totalPage = data.total
                        dataList = data?.rows?.map { TrainListItem.TypePreJobItem(it) }
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
                trainAdapter.notifyDataSetChanged()
                currentPage++

                // 空数据
                if (trainAdapter.itemCount == 0) {
                    binding.emptyView.visibility = View.VISIBLE
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

        if (type == "before") {
            // 岗前培训直接跳人脸识别
            gotoFaceCheck(itemId, itemName, type)
            return
        }

        when {
            (data as? CheckSafeData)?.msg == "已过期" || payInfo.money.isNotEmpty() -> {
                // 跳支付页面
                gotoPayPage(itemId, itemName, payInfo, type)
            }
            else -> {
                // 无需支付，跳人脸识别
                gotoFaceCheck(itemId, itemName, type)
            }
        }
    }

    // ==================== 点击事件实现 ====================
    override fun onStudyClick(item: TrainListItem, typeTag: String) {
        // 检查是否需要签字
        if (typeTag == "daily") {
            val data = (item as TrainListItem.TypeSafeItem).data
            if (data.progress >= 100 && data.issign == "1" && data.imgurl.isEmpty()) {
                goSign(data.name, data.id.toString())
                return
            }
        }

        // 支付检查
        checkPay(item, typeTag)
    }

    override fun onExamClick(item: TrainListItem) {
        val data = (item as TrainListItem.TypeSafeItem).data
        // 跳考试页面
        val intent = Intent(this, ExamActivity::class.java)
        intent.putExtra("id", data.training_exams_id)
        intent.putExtra("name", data.name)
        intent.putExtra("type", if (currentType == 0) "daily" else "train")
        intent.putExtra("training_safetyplan_id", data.id)
        startActivity(intent)
    }

    override fun onAuthClick(item: SubjectItem) {
        // 跳认证页面
        val intent = Intent(this, AuthActivity::class.java)
        intent.putExtra("id", item.id)
        intent.putExtra("name", item.name)
        startActivity(intent)
    }

    override fun onMeetingClick(item: MeetingItem) {
        // 跳会议页面
        val intent = Intent(this, MeetingActivity::class.java)
        intent.putExtra("id", item.id)
        intent.putExtra("name", item.name)
        intent.putExtra("address", item.address)
        intent.putExtra("starttime", item.starttime)
        intent.putExtra("status", item.studytype)
        startActivity(intent)
    }

    /**
     * 支付检查逻辑
     * @param item 培训项
     * @param type 类型标识（daily/subject）
     */
    private fun checkPay(item: TrainListItem, typeTag: String) {
        when (typeTag) {
            "train" -> {
                trainingViewModel.checkSafe((item as TrainListItem.TypeSafeItem).data.id.toString(),
                    checkSafeState)
            }
            "daily" -> {
                trainingViewModel.orderIsPay((item as TrainListItem.TypeSafeItem).data.id.toString(), orderIsPayState)
            }
            "subject" -> {
                trainingViewModel.subjectPay((item as TrainListItem.TypeContinueItem).data.id.toString(), subjectPayState)
            }
            "before" -> handlePayCheckResult(Any(), typeTag)
        }

        lifecycleScope.launch {
            try {

                if (payResponse.code == 1) {
                    val payData = payResponse.data
                    if (payData != null) {
                        // 需要支付
                        val intent = Intent(this@TrainListActivity, PayActivity::class.java)
                        intent.putExtra("name", item.name)
                        intent.putExtra("id", item.id)
                        intent.putExtra("money", payData.money)
                        intent.putExtra("type", typeTag)
                        intent.putExtra("usualpaytype", payData.usualpaytype)
                        startActivity(intent)
                    }
                } else {
                    if (payResponse.msg == "不需要支付") {
                        // 跳人脸识别
                        val intent = Intent(this@TrainListActivity, FaceCheckActivity::class.java)
                        intent.putExtra("safetyPlanId", item.id)
                        intent.putExtra("name", item.name)
                        intent.putExtra("number", item.number)
                        intent.putExtra("type", typeTag)
                        intent.putExtra("faceType", "start")
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@TrainListActivity, payResponse.msg, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@TrainListActivity, "支付检查失败:${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 跳签字页面
    private fun goSign(name: String, id: String) {
        SPUtils.save("dailyName", name)
        SPUtils.save("dailyId", id)

        val intent = Intent(this, SignatureActivity::class.java)
        intent.putExtra("from", "Train")
        intent.putExtra("fill", "dailySign")
        intent.putExtra("type", currentType)
        startActivity(intent)
    }

    /**
     * 跳转到支付页面
     */
    private fun gotoPayPage(id: String, name: String, payInfo: PayInfo, type: String) {
        val intent = Intent(this, PayActivity::class.java)
        intent.putExtra("name", name)
        intent.putExtra("id", id)
        intent.putExtra("money", payInfo.money)
        intent.putExtra("type", type)
        intent.putExtra("usualpaytype", payInfo.usualpaytype)
        startActivity(intent)
    }

    /**
     * 跳转到人脸识别页面
     */
    private fun gotoFaceCheck(id: String, name: String, type: String) {
        val intent = Intent(this, FaceCheckActivity::class.java)
        intent.putExtra("safetyPlanId", id)
        intent.putExtra("name", name)
        intent.putExtra("number", "")
        intent.putExtra("type", type)
        intent.putExtra("faceType", "start")
        startActivity(intent)
    }
}