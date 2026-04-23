package com.fx.zfcar.training.viewmodel

import androidx.lifecycle.viewModelScope
import com.fx.zfcar.MyApp
import com.fx.zfcar.net.NoticeData
import com.fx.zfcar.net.TrainingBaseResponse
import com.fx.zfcar.net.UploadFileData
import com.fx.zfcar.util.DialogUtils
import com.fx.zfcar.util.SPUtils
import com.fx.zfcar.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.Response

/**
 * 安全培训相关ViewModel（课程、签到、人脸验证、培训计划等）
 */
class NoticeViewModel : TrainingBaseViewModel() {

    fun getNoticeInfo(page: Int, index: Int, type: Int, stateFlow: MutableStateFlow<ApiState<NoticeData>>) {
        launchNoticeListRequest(stateFlow) {
            vehicleRepository.getNoticeInfo(page, index, type)
        }
    }

    fun readNotice(noticeId: String, signimg: String, stateFlow: MutableStateFlow<ApiState<Any>>) {
        launchRequest(
            block = { vehicleRepository.readNotice(noticeId, signimg) },
            stateFlow
        )
    }


    fun warningNotice(page: Int, index: Int, type: Int, stateFlow: MutableStateFlow<ApiState<NoticeData>>) {
        launchNoticeListRequest(stateFlow) {
            vehicleRepository.warningNotice(page, index, type)
        }
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

    private fun launchNoticeListRequest(
        stateFlow: MutableStateFlow<ApiState<NoticeData>>,
        block: suspend () -> Response<TrainingBaseResponse<NoticeData>>
    ) {
        viewModelScope.launch {
            stateFlow.value = ApiState.Loading
            runCatching {
                block()
            }.onSuccess { response ->
                val body = response.body()
                when {
                    !response.isSuccessful -> {
                        stateFlow.value = ApiState.Error("请求失败(${response.code()})")
                    }
                    body?.code == 401 -> {
                        handleTrainingAuthExpired()
                        stateFlow.value = ApiState.Error("登录已失效，请重新登录")
                    }
                    body?.code == 500 -> {
                        stateFlow.value = ApiState.Error(body.msg.ifBlank { "请求错误，请联系管理员" })
                    }
                    body?.data != null -> {
                        stateFlow.value = ApiState.Success(body.data)
                    }
                    else -> {
                        stateFlow.value = ApiState.Error(body?.msg?.takeIf { it.isNotBlank() } ?: "加载失败")
                    }
                }
            }.onFailure { exception ->
                stateFlow.value = ApiState.Error(exception.message ?: "加载失败", exception)
            }
        }
    }

    private fun handleTrainingAuthExpired() {
        SPUtils.saveTrainingToken("")
        SPUtils.save("trainLogin", "yes")
        MyApp.isTrainingLogin = false
        MyApp.trainingUserInfo = null
        MyApp.getCurrentActivity()?.let { activity ->
            activity.runOnUiThread {
                if (!activity.isFinishing && !activity.isDestroyed) {
                    DialogUtils.showTrainingLoginPromptDialog(activity)
                }
            }
        }
    }
}
