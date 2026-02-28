package com.fx.zfcar.training.viewmodel

import com.fx.zfcar.net.BeforeEducationCertificateData
import com.fx.zfcar.net.CarNumSearchData
import com.fx.zfcar.net.CompanyListData
import com.fx.zfcar.net.CoursewareListData
import com.fx.zfcar.net.DailySafetyOrderData
import com.fx.zfcar.net.EducationCertificate
import com.fx.zfcar.net.EpidemicViewData
import com.fx.zfcar.net.FaceData
import com.fx.zfcar.net.NoticeData
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
class NoticeViewModel : TrainingBaseViewModel() {

    fun getNoticeInfo(page: Int, index: Int, type: Int, stateFlow: MutableStateFlow<ApiState<NoticeData>>) {
        launchRequest(
            block = { vehicleRepository.getNoticeInfo(page, index, type) },
            stateFlow
        )
    }

    fun readNotice(noticeId: String, signimg: String, stateFlow: MutableStateFlow<ApiState<Any>>) {
        launchRequest(
            block = { vehicleRepository.readNotice(noticeId, signimg) },
            stateFlow
        )
    }


    fun warningNotice(page: Int, index: Int, type: Int, stateFlow: MutableStateFlow<ApiState<NoticeData>>) {
        launchRequest(
            block = { vehicleRepository.warningNotice(page, index, type) },
            stateFlow
        )
    }

    fun readWarningNotice(noticeId: String, signimg: String, stateFlow: MutableStateFlow<ApiState<Any>>) {
        launchRequest(
            block = { vehicleRepository.readWarningNotice(noticeId, signimg) },
            stateFlow
        )
    }

    fun uploadFile(filePath: MultipartBody.Part, stateFlow: MutableStateFlow<ApiState<UploadFileData>>) {
        launchRequest(
            block = { vehicleRepository.uploadFile(filePath) },
            stateFlow
        )
    }
}