package com.yt.car.union.viewmodel

import com.yt.car.union.net.UserLoginRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import kotlin.code

/**
 * 安全培训相关ViewModel（课程、签到、人脸验证、培训计划等）
 */
class SafetyTrainingViewModel : TrainingBaseViewModel() {

    // 签到
    fun signView() {
        launchRequest(
            block = { vehicleRepository.signView() },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code != null) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 疫情相关
    fun epidemicView() {
        launchRequest(
            block = { vehicleRepository.epidemicView() },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 获取公司列表
    fun getCompanyList(page: String?, type: String?) {
        launchRequest(
            block = { vehicleRepository.getCompanyList(page, type) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 获取安全培训列表
    fun getSafetyList(page: String?, type: String?) {
        launchRequest(
            block = { vehicleRepository.getSafetyList(page, type) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 日常安全订单支付
    fun dailySafetyOrderPay(trainingPublicPlanId: String?) {
        launchRequest(
            block = { vehicleRepository.dailySafetyOrderPay(trainingPublicPlanId) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code != null) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 课程学习
    fun safeStudy(subjectId: String, trainingPublicPlanId: String, longtime: String) {
        launchRequest(
            block = { vehicleRepository.safeStudy(subjectId, trainingPublicPlanId, longtime) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 人脸验证（安全培训）
    fun safeFace(imgurl: String, trainingPublicPlanId: Int, type: String) {
        launchRequest(
            block = { vehicleRepository.safeFace(imgurl, trainingPublicPlanId, type) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code != null) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 签到提交
    fun singPost(request: RequestBody) {
        launchRequest(
            block = { vehicleRepository.singPost(request) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code != null) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 其他安全培训相关方法...（按相同逻辑迁移）
    fun getCoursewareList(page: String, trainingPublicPlanId: String) {
        launchRequest(
            block = { vehicleRepository.getCoursewareList(page, trainingPublicPlanId) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 按车牌号搜索
    fun carnumSearch(carnum: String, page: String) {
        launchRequest(
            block = { vehicleRepository.carnumSearch(carnum, page) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    fun uploadFile(filePath: MultipartBody.Part) {
        launchRequest(
            block = { vehicleRepository.uploadFile(filePath) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 安全培训专属登录
    fun userLogin(request: UserLoginRequest) {
        launchRequest(
            block = { vehicleRepository.userLogin(request) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 获取用户其他信息
    fun getUserOtherInfo() {
        launchRequest(
            block = { vehicleRepository.getUserOtherInfo() },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 重置密码
    fun resetPwd(newpassword: String, oldpassword: String) {
        launchRequest(
            block = { vehicleRepository.resetPwd(newpassword, oldpassword) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code != null) {
                    // 业务成功逻辑
                }
            }
        )
    }
}