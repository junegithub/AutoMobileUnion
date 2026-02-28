package com.fx.zfcar.training.viewmodel

import com.fx.zfcar.net.BeforeEducationCertificateData
import com.fx.zfcar.net.CarNumSearchData
import com.fx.zfcar.net.CompanyListData
import com.fx.zfcar.net.CoursewareListData
import com.fx.zfcar.net.DailySafetyOrderData
import com.fx.zfcar.net.EducationCertificate
import com.fx.zfcar.net.EpidemicViewData
import com.fx.zfcar.net.FaceData
import com.fx.zfcar.net.OldSafetyListData
import com.fx.zfcar.net.SafeStudyData
import com.fx.zfcar.net.SafetyListData
import com.fx.zfcar.net.SignViewData
import com.fx.zfcar.net.StudyDetailData
import com.fx.zfcar.net.TrainingOtherInfo
import com.fx.zfcar.net.UploadFileData
import com.fx.zfcar.net.UserInfoData
import com.fx.zfcar.net.UserLoginData
import com.fx.zfcar.net.UserLoginRequest
import com.fx.zfcar.net.UserStudyProveListData
import com.fx.zfcar.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.MultipartBody
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

    fun getOldSafetyList(page: Int,
                      stateFlow: MutableStateFlow<ApiState<OldSafetyListData>>) {
        launchRequest(
            block = { vehicleRepository.getOldSafetyList(page) },
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