package com.yt.car.union.training.viewmodel

import com.yt.car.union.net.BeforeEducationCertificateData
import com.yt.car.union.net.CarNumSearchData
import com.yt.car.union.net.CompanyListData
import com.yt.car.union.net.CoursewareListData
import com.yt.car.union.net.DailySafetyOrderData
import com.yt.car.union.net.EducationCertificate
import com.yt.car.union.net.EpidemicViewData
import com.yt.car.union.net.FaceData
import com.yt.car.union.net.SafeStudyData
import com.yt.car.union.net.SafetyListData
import com.yt.car.union.net.SafetyPlan
import com.yt.car.union.net.SignViewData
import com.yt.car.union.net.StudyDetailData
import com.yt.car.union.net.TrainingOtherInfo
import com.yt.car.union.net.UploadFileData
import com.yt.car.union.net.UserLoginData
import com.yt.car.union.net.UserLoginRequest
import com.yt.car.union.net.UserStudyProveListData
import com.yt.car.union.viewmodel.ApiState
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
    fun getSafetyList(page: String?, type: String?,
                      stateFlow: MutableStateFlow<ApiState<SafetyListData>>) {
        launchRequest(
            block = { vehicleRepository.getSafetyList(page, type) },
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

    fun uploadFile(filePath: MultipartBody.Part, stateFlow: MutableStateFlow<ApiState<UploadFileData>>) {
        launchRequest(
            block = { vehicleRepository.uploadFile(filePath) },
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