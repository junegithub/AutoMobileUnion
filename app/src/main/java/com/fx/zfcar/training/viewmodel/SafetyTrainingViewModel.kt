package com.fx.zfcar.training.viewmodel

import com.fx.zfcar.net.BeforeEducationCertificateData
import com.fx.zfcar.net.CarNumSearchData
import com.fx.zfcar.net.CompanyListData
import com.fx.zfcar.net.CoursewareListData
import com.fx.zfcar.net.DailySafetyOrderData
import com.fx.zfcar.net.EducationCertificate
import com.fx.zfcar.net.EpidemicViewData
import com.fx.zfcar.net.ExamsListData // 新增：在线测验列表
import com.fx.zfcar.net.FaceData
import com.fx.zfcar.net.MeetingListData // 新增：安全会议列表
import com.fx.zfcar.net.OldSafetyListData
import com.fx.zfcar.net.BeforeSubjectListData
import com.fx.zfcar.net.SafeStudyData
import com.fx.zfcar.net.SafetyListData
import com.fx.zfcar.net.SignViewData
import com.fx.zfcar.net.StudyDetailData
import com.fx.zfcar.net.SubjectListData
import com.fx.zfcar.net.TrainingOtherInfo
import com.fx.zfcar.net.UserInfoData
import com.fx.zfcar.net.UserLoginData
import com.fx.zfcar.net.UserLoginRequest
import com.fx.zfcar.net.UserStudyProveListData
import com.fx.zfcar.net.CheckSafeData // 新增：支付检查
import com.fx.zfcar.net.CompanyPayData // 新增：企业支付
import com.fx.zfcar.net.PostSignImgData // 新增：提交签名
import com.fx.zfcar.net.QuestionOrderPayData
import com.fx.zfcar.net.SafetySignRequest
import com.fx.zfcar.net.SubjectOrderData
import com.fx.zfcar.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.RequestBody

/**
 * 安全培训相关ViewModel（课程、签到、人脸验证、培训计划等）
 */
class SafetyTrainingViewModel : TrainingBaseViewModel() {

    // 签到
    fun signView(stateFlow: MutableStateFlow<ApiState<SignViewData>>) {
        launchRequest(
            block = { vehicleRepository.signView() },
            stateFlow
        )
    }

    // 疫情相关
    fun epidemicView(stateFlow: MutableStateFlow<ApiState<EpidemicViewData>>) {
        launchRequest(
            block = { vehicleRepository.epidemicView() },
            stateFlow
        )
    }

    // 获取公司列表
    fun getCompanyList(page: String?, type: String?,
                       stateFlow: MutableStateFlow<ApiState<CompanyListData>>) {
        launchRequest(
            block = { vehicleRepository.getCompanyList(page, type) },
            stateFlow
        )
    }

    // 获取安全培训列表
    fun getSafetyList(page: Int, type: Int,
                      stateFlow: MutableStateFlow<ApiState<SafetyListData>>) {
        launchRequest(
            block = { vehicleRepository.getSafetyList(page, type) },
            stateFlow
        )
    }

    // 历史安全培训列表
    fun getOldSafetyList(page: Int,
                         stateFlow: MutableStateFlow<ApiState<OldSafetyListData>>) {
        launchRequest(
            block = { vehicleRepository.getOldSafetyList(page) },
            stateFlow
        )
    }

    // 岗前培训列表
    fun getBeforeList(stateFlow: MutableStateFlow<ApiState<BeforeSubjectListData>>) {
        launchRequest(
            block = { vehicleRepository.getBeforeSubjectList() },
            stateFlow
        )
    }

    // 在线测验列表
    fun getExamsList(page: Int, examTab: Int, starttime: String, endtime: String,
                     stateFlow: MutableStateFlow<ApiState<ExamsListData>>) {
        launchRequest(
            block = { vehicleRepository.getExamsList(page, examTab, starttime, endtime) },
            stateFlow
        )
    }

    // 安全会议列表
    fun getMeetingList(page: Int, meetingType: Int,
                       stateFlow: MutableStateFlow<ApiState<MeetingListData>>) {
        launchRequest(
            block = { vehicleRepository.getMeetingList(page, meetingType) },
            stateFlow
        )
    }

    // 继续教育列表
    fun getSubjectList(page: Int,
                       stateFlow: MutableStateFlow<ApiState<SubjectListData>>) {
        launchRequest(
            block = { vehicleRepository.getSubjectList(page) },
            stateFlow
        )
    }

    // 日常安全订单支付
    fun dailySafetyOrderPay(trainingPublicPlanId: String?,
                            stateFlow: MutableStateFlow<ApiState<DailySafetyOrderData>>) {
        launchRequest(
            block = { vehicleRepository.dailySafetyOrderPay(trainingPublicPlanId) },
            stateFlow
        )
    }

    // 检查安全培训支付状态
    fun checkSafe(training_safetyplan_id: String,
                  stateFlow: MutableStateFlow<ApiState<CheckSafeData>>) {
        launchRequest(
            block = { vehicleRepository.checkSafe(training_safetyplan_id) },
            stateFlow
        )
    }

    // 检查订单支付状态
    fun orderIsPay(id: String,
                   stateFlow: MutableStateFlow<ApiState<QuestionOrderPayData>>) {
        launchRequest(
            block = { vehicleRepository.questionOrderPay(id) },
            stateFlow
        )
    }

    // 继续教育支付检查
    fun subjectPay(id: String,
                   stateFlow: MutableStateFlow<ApiState<SubjectOrderData>>) {
        launchRequest(
            block = { vehicleRepository.subjectOrder(id) },
            stateFlow
        )
    }

    // 企业支付
    fun companyPay(id: String,
                   stateFlow: MutableStateFlow<ApiState<CompanyPayData>>) {
        launchRequest(
            block = { vehicleRepository.companyPay(id) },
            stateFlow
        )
    }

    // 提交签名图片
    fun postSignImg(id: String, signfile: String,
                    stateFlow: MutableStateFlow<ApiState<PostSignImgData>>) {
        launchRequest(
            block = { vehicleRepository.postSignImg(SafetySignRequest(id, signfile)) },
            stateFlow
        )
    }

    // 课程学习
    fun safeStudy(subjectId: String, trainingPublicPlanId: String, longtime: String,
                  stateFlow: MutableStateFlow<ApiState<SafeStudyData>>) {
        launchRequest(
            block = { vehicleRepository.safeStudy(subjectId, trainingPublicPlanId, longtime) },
            stateFlow
        )
    }

    // 人脸验证（安全培训）
    fun safeFace(imgurl: String, trainingPublicPlanId: Int, type: String,
                 stateFlow: MutableStateFlow<ApiState<FaceData>>) {
        launchRequest(
            block = { vehicleRepository.safeFace(imgurl, trainingPublicPlanId, type) },
            stateFlow
        )
    }

    // 签到提交
    fun singPost(request: RequestBody, stateFlow: MutableStateFlow<ApiState<String>>) {
        launchRequest(
            block = { vehicleRepository.singPost(request) },
            stateFlow
        )
    }

    // 其他安全培训相关方法...（按相同逻辑迁移）
    fun getCoursewareList(page: String, trainingPublicPlanId: String,
                          stateFlow: MutableStateFlow<ApiState<CoursewareListData>>) {
        launchRequest(
            block = { vehicleRepository.getCoursewareList(page, trainingPublicPlanId) },
            stateFlow
        )
    }

    // 按车牌号搜索
    fun carnumSearch(carnum: String, page: String,
                     stateFlow: MutableStateFlow<ApiState<CarNumSearchData>>) {
        launchRequest(
            block = { vehicleRepository.carnumSearch(carnum, page) },
            stateFlow
        )
    }

    // 安全培训专属登录
    fun userLogin(request: UserLoginRequest, stateFlow: MutableStateFlow<ApiState<UserLoginData>>) {
        launchRequest(
            block = { vehicleRepository.userLogin(request) },
            stateFlow
        )
    }

    // 获取用户其他信息
    fun getUserOtherInfo(stateFlow: MutableStateFlow<ApiState<TrainingOtherInfo>>) {
        launchRequest(
            block = { vehicleRepository.getUserOtherInfo() },
            stateFlow
        )
    }

    // 获取用户其他信息
    fun getUserInfoSafe(stateFlow: MutableStateFlow<ApiState<UserInfoData>>) {
        launchRequest(
            block = { vehicleRepository.getUserInfoSafe() },
            stateFlow
        )
    }

    // 重置密码
    fun resetPwd(newpassword: String, oldpassword: String,
                 stateFlow: MutableStateFlow<ApiState<Any>>) {
        launchRequest(
            block = { vehicleRepository.resetPwd(newpassword, oldpassword) },
            stateFlow
        )
    }

    fun getUserStudyProveList(month: String,
                              stateFlow: MutableStateFlow<ApiState<UserStudyProveListData>>) {
        launchRequest(
            block = { vehicleRepository.getUserStudyProveList(month) },
            stateFlow
        )
    }

    fun getEducationCertificate(stateFlow: MutableStateFlow<ApiState<List<EducationCertificate>>>) {
        launchRequest(
            block = { vehicleRepository.getEducationCertificate() },
            stateFlow
        )
    }

    fun getBeforeEducationCertificate(stateFlow: MutableStateFlow<ApiState<BeforeEducationCertificateData>>) {
        launchRequest(
            block = { vehicleRepository.getBeforeEducationCertificate() },
            stateFlow
        )
    }

    fun getStudySafetyList(month: String, stateFlow: MutableStateFlow<ApiState<StudyDetailData>>) {
        launchRequest(
            block = { vehicleRepository.getStudySafetyList(month) },
            stateFlow
        )
    }
}