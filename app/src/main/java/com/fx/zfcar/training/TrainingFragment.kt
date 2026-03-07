package com.fx.zfcar.training

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fx.zfcar.MyApp
import com.fx.zfcar.training.user.UserCenterActivity
import com.fx.zfcar.util.DialogUtils
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.databinding.FragmentTrainingBinding
import com.fx.zfcar.net.BeforeExamInfoData
import com.fx.zfcar.net.EpidemicViewData
import com.fx.zfcar.net.QuestionOrderPayData
import com.fx.zfcar.net.SignViewData
import com.fx.zfcar.net.UserInfoData
import com.fx.zfcar.pages.LoginActivity
import com.fx.zfcar.training.notice.NoticeActivity
import com.fx.zfcar.training.safetytraining.TrainListActivity
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.DateUtil
import com.fx.zfcar.util.SPUtils
import com.fx.zfcar.viewmodel.ApiState
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.getValue

class TrainingFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentTrainingBinding? = null
    private val binding get() = _binding!!

    private var noticeNum = 0
    private var loginStatus = false

    private var name = "" // 公司名称
    private var usualpaytype = "" // 0 个人 1 企业
    private lateinit var examInfo: BeforeExamInfoData // 岗前培训考试信息
    private var areacode = ""

    private var mType = 0
    private var mTitle = ""

    private val trainingViewModel by viewModels<SafetyTrainingViewModel>()
    private var beforeExamStateFlow = MutableStateFlow<ApiState<BeforeExamInfoData>>(ApiState.Idle)
    private var safeUserStateFlow = MutableStateFlow<ApiState<UserInfoData>>(ApiState.Idle)
    private var driverBookStateFlow = MutableStateFlow<ApiState<SignViewData>>(ApiState.Idle)
    private var yiqingStateFlow = MutableStateFlow<ApiState<EpidemicViewData>>(ApiState.Idle)
    private val orderIsPayState = MutableStateFlow<ApiState<QuestionOrderPayData>>(ApiState.Idle)

    // Handler
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrainingBinding.inflate(inflater, container, false)

        // 初始化视图
        initView()

        // 设置月份显示
        binding.tvMonth.text = "${DateUtil.getCurrentMonth()}月"

        // 检查登录状态
        checkLoginStatus()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 释放绑定
        _binding = null
        // 移除所有 handler 回调
        handler.removeCallbacksAndMessages(null)
    }

    // 初始化视图和点击事件
    private fun initView() {
        addListener()
        initStateFlow()
    }

    private fun addListener() {
        PressEffectUtils.setCommonPressEffect(binding.trainingHomeRing)
        PressEffectUtils.setCommonPressEffect(binding.trainingHomeUser)
        PressEffectUtils.setCommonPressEffect(binding.trainingHomeLearn)
        PressEffectUtils.setCommonPressEffect(binding.cvSafeTraining)

        PressEffectUtils.setCommonPressEffect(binding.trainingHomeExamPast)
        PressEffectUtils.setCommonPressEffect(binding.trainingHomeExamMeeting)
        PressEffectUtils.setCommonPressEffect(binding.trainingHomeExamContinueEdu)
        PressEffectUtils.setCommonPressEffect(binding.trainingHomeExamQualifyPast)

        PressEffectUtils.setCommonPressEffect(binding.preTraining)
        PressEffectUtils.setCommonPressEffect(binding.trainingHomePreTrainingLearn)
        PressEffectUtils.setCommonPressEffect(binding.trainingHomePreTrainingTest)

        PressEffectUtils.setCommonPressEffect(binding.trainingHomeDrivingLog)
        PressEffectUtils.setCommonPressEffect(binding.trainingHomeSafetyCheck)
        PressEffectUtils.setCommonPressEffect(binding.trainingHomeInspect)
        PressEffectUtils.setCommonPressEffect(binding.trainingHomeDuty)
        PressEffectUtils.setCommonPressEffect(binding.layoutYiqingSign)

        PressEffectUtils.setCommonPressEffect(binding.trainingHomeFindJob)

        binding.trainingHomeRing.setOnClickListener(this)
        binding.trainingHomeUser.setOnClickListener(this)
        binding.trainingHomeLearn.setOnClickListener(this)
        binding.cvSafeTraining.setOnClickListener(this)

        binding.trainingHomeExamPast.setOnClickListener(this)
        binding.trainingHomeExamMeeting.setOnClickListener(this)
        binding.trainingHomeExamContinueEdu.setOnClickListener(this)
        binding.trainingHomeExamQualifyPast.setOnClickListener(this)

        binding.trainingHomePreTrainingTest.setOnClickListener(this)
        binding.trainingHomePreTrainingLearn.setOnClickListener(this)
        binding.preTraining.setOnClickListener(this)

        binding.trainingHomeDrivingLog.setOnClickListener(this)
        binding.trainingHomeSafetyCheck.setOnClickListener(this)
        binding.trainingHomeInspect.setOnClickListener(this)
        binding.trainingHomeDuty.setOnClickListener(this)
        binding.layoutYiqingSign.setOnClickListener(this)

        binding.trainingHomeFindJob.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (MyApp.isTrainingLogin != true) {
            DialogUtils.showTrainingLoginPromptDialog(requireActivity())
            return
        }
        when(v.id) {
            binding.cvSafeTraining.id -> {
                study(0, "安全培训")
            }
            binding.trainingHomeRing.id -> {
                startActivity(Intent(requireActivity(), NoticeActivity::class.java))
            }
            binding.trainingHomeUser.id -> {
                startActivity(Intent(requireActivity(), UserCenterActivity::class.java))
            }
            binding.trainingHomeLearn.id -> {
                study(0, "安全培训")
//                startActivity(Intent(requireActivity(), SafetyTrainingActivity::class.java))
            }
            binding.trainingHomeExamPast.id -> {
                study(5, "两类人员真题")
            }
            binding.trainingHomeExamMeeting.id -> {
                study(3, "安全会议")
            }
            binding.trainingHomeExamContinueEdu.id -> {
                study(4, "继续教育")
            }
            binding.trainingHomeExamQualifyPast.id -> {
                question("")
            }
            binding.trainingHomePreTrainingTest.id -> {
                getBeforeExamInfo()
            }
            binding.preTraining.id -> {
                study(1, "岗前培训")
            }
            binding.trainingHomePreTrainingLearn.id -> {
                study(1, "岗前培训")
            }
            binding.trainingHomeDrivingLog.id -> {
                driveDaily()
            }
            binding.trainingHomeSafetyCheck.id -> {
                driveCheck()
            }
            binding.trainingHomeInspect.id -> {
                dangerCheck()
            }
            binding.trainingHomeDuty.id -> {
                driverBook()
            }
            binding.layoutYiqingSign.id -> {
                yiqing()
            }
            binding.trainingHomeFindJob.id -> {
                job()
            }
        }
    }

    private fun initStateFlow() {
        lifecycleScope.launch {
            beforeExamStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                    }

                    is ApiState.Success -> {
                        uiState.data?.let {
                            examInfo = uiState.data
//                            val intent = Intent(requireContext(), TrainTestActivity::class.java)
//                            intent.putExtra("id", examInfo.exams_id)
//                            intent.putExtra("name", examInfo.name)
//                            intent.putExtra("type", "before")
//                            intent.putExtra("training_safetyplan_id", examInfo.training_before_id)
//                            startActivity(intent)
                        }
                    }

                    is ApiState.Error -> {
                        context?.showToast(uiState.msg)
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }

        lifecycleScope.launch {
            safeUserStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                    }

                    is ApiState.Success -> {
                        uiState.data?.let {
                            // 更新通知数量
                            noticeNum = uiState.data.notice
                            binding.badgeNotice.text = if (noticeNum > 99) "99+" else noticeNum.toString()
                            binding.badgeNotice.visibility = if (noticeNum > 0) View.VISIBLE else View.GONE

                            // 保存公司信息和用户信息
                            name = uiState.data.category.name
                            usualpaytype = uiState.data.info.usualpaytype

                            SPUtils.save("userInfo", Gson().toJson(uiState.data.info))
                            SPUtils.save("companyInfo", Gson().toJson(uiState.data.category))
                        }
                    }

                    is ApiState.Error -> {
                        context?.showToast("加载失败：${uiState.msg}")
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }

        lifecycleScope.launch {
            driverBookStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                    }

                    is ApiState.Success -> {
                        // 已登录
                        loginStatus = true
                        var signOne = false
                        val nowYear = DateUtil.getCurrentYear().toString()

                        uiState.data?.let {
                            val data = uiState.data
                            areacode = data.areacode.take(2)

                            // 控制承诺书显示/隐藏
                            if (areacode != "34" && areacode != "37") {
                                binding.layoutYiqingSign.visibility = View.VISIBLE
                            } else {
                                binding.layoutYiqingSign.visibility = View.GONE
                            }

                            // 3=继续教育 或 涡阳县(341621) 不需要每年签
                            if (data.group_id == 3 || data.areacode == "341621") {
                                return@let
                            }

                            // 检查签署年份
                            if (data.signtype == "1") {
                                val signTime = DateUtil.timestamp2Date(data.signtime).substring(0, 4)
                                if (signTime.toInt() < DateUtil.getCurrentYear()) {
                                    // 需要重新签署
                                    startActivity(Intent(requireContext(), DriverBookActivity::class.java))
                                    return@let
                                } else {
                                    signOne = true
                                }
                            } else {
                                // 未签署
                                startActivity(Intent(requireContext(), DriverBookActivity::class.java))
                                return@let
                            }
                        }
                        // 检查承诺书签署状态 (34/37 地区且未签责任书时跳过)
                        if (!signOne && (areacode == "34" || areacode == "37")) {
                            return@collect
                        }

                        trainingViewModel.epidemicView(yiqingStateFlow)
                    }

                    is ApiState.Error -> {
                        context?.showToast(uiState.msg)
                        loginStatus = false
                        DialogUtils.showTrainingLoginPromptDialog(requireActivity())
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }

        lifecycleScope.launch {
            yiqingStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                    }

                    is ApiState.Success -> {
                        uiState.data?.let {
                            if (uiState.data.epidemictype == "1") {
                                val signTime = uiState.data.epidemictime.take(4).toInt()
                                if (signTime < DateUtil.getCurrentYear()) {
                                    // 需要重新签署
                                    startActivity(Intent(requireContext(), YiqingSignActivity::class.java))
                                }
                            } else {
                                // 未签署
                                startActivity(Intent(requireContext(), YiqingSignActivity::class.java))
                            }
                        }
                    }

                    is ApiState.Error -> {
                        context?.showToast(uiState.msg)
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }

        lifecycleScope.launch {
            orderIsPayState.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                    }

                    is ApiState.Success -> {
                        // 需要支付
//                        val intent = Intent(requireContext(), TrainPayActivity::class.java)
//                        intent.putExtra("payName", "岗前培训")
//                        intent.putExtra("payNum", uiState.data?.money ?: "10")
//                        intent.putExtra("usualpaytype", usualpaytype)
//                        startActivity(intent)
                    }

                    is ApiState.Error -> {
                        // 不需要支付
                        goToTrainHome(mType, mTitle)
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }
    }

    // 检查登录状态
    private fun checkLoginStatus() {
        val trainLogin = SPUtils.get("trainLogin")
        if ("yes" == trainLogin) {
            // 退出登录状态
            loginStatus = false
            SPUtils.save("trainLogin", "")
        } else {
            // 检查 Token
            val trainToken = SPUtils.get("trainToken")
            if (trainToken.isNotEmpty()) {
                // 已登录，检查签署状态并获取通知信息
                isSign()
                getNoticeInfo()
            } else {
                loginStatus = false
            }
        }
    }

    // 跳转到登录页面
    private fun goLogin() {
        val intent = Intent(context, LoginActivity::class.java)
        intent.putExtra(LoginActivity.LOGIN_TYPE_TRAINING, true)
        startActivity(intent)
    }

    // 获取通知信息和用户信息
    private fun getNoticeInfo() {
        // 获取用户信息
        trainingViewModel.getUserInfoSafe(safeUserStateFlow)
    }

    // 检查责任书和承诺书签署状态
    private fun isSign() {
        SPUtils.save("requestType", "train")

        // 检查责任书签署状态
        trainingViewModel.signView(driverBookStateFlow)
    }

    // 获取岗前培训列表（支付检查）
    private fun getBeforeList(type: Int, title: String) {
        mType = type
        mTitle = title
        trainingViewModel.orderIsPay("", orderIsPayState)
    }

    // 学习入口
    private fun study(type: Int, title: String) {
        if (type == 5) {
            // 两类人员真题
            question("twoList")
        } else {
            if (type == 1) {
                // 岗前培训
                getBeforeList(type, title)
                return
            }
            // 其他培训类型
            goToTrainHome(type, title)
        }
    }

    // 考试入口
    private fun question(data: String) {
//        val intent = Intent(requireContext(), TestBaseActivity::class.java)
//        if (data == "twoList") {
//            intent.putExtra("from", data)
//        }
//        startActivity(intent)
    }

    // 获取岗前考试信息
    private fun getBeforeExamInfo() {
        trainingViewModel.beforeExamInfo(beforeExamStateFlow)
    }

    // 行车日志
    private fun driveDaily() {
//        startActivity(Intent(requireContext(), DriveDailyActivity::class.java))
    }

    // 车辆安全检查
    private fun driveCheck() {
//        startActivity(Intent(requireContext(), DriveCheckActivity::class.java))
    }

    // 隐患排查
    private fun dangerCheck() {
        startActivity(Intent(requireContext(), DangerCheckActivity::class.java))
    }

    // 驾驶员责任书
    private fun driverBook() {
        startActivity(Intent(requireContext(), DriverBookActivity::class.java))
    }

    // 驾驶员承诺书
    private fun yiqing() {
        val intent = Intent(requireContext(), YiqingSignActivity::class.java)
        intent.putExtra("name", name)
        startActivity(intent)
    }

    // 招聘求职
    private fun job() {
        startActivity(Intent(requireContext(), JobsActivity::class.java))
    }

    // 跳转到培训首页
    private fun goToTrainHome(type: Int, title: String) {
        val intent = Intent(requireContext(), TrainListActivity::class.java)
        intent.putExtra("type", type)
        intent.putExtra("title", title)
        startActivity(intent)
    }

}