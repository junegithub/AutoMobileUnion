package com.fx.zfcar.training.viewmodel

import com.fx.zfcar.net.NoticeData
import com.fx.zfcar.net.UploadFileData
import com.fx.zfcar.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.MultipartBody

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