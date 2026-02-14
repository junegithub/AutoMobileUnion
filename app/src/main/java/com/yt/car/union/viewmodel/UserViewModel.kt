package com.yt.car.union.viewmodel

import com.yt.car.union.MyApp
import com.yt.car.union.net.LoginRequest
import com.yt.car.union.util.SPUtils

/**
 * 用户相关ViewModel（登录、登出、信息、密码重置等）
 */
class UserViewModel : CarBaseViewModel() {

    // 登录
    fun login(request: LoginRequest) {
        launchRequest(
            block = { vehicleRepository.login(request) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 保存Token
                    SPUtils.saveToken(response.body()?.data?.userinfo?.token)
                    MyApp.isLogin = true
                    getUserInfo()
                }
            }
        )
    }

    // 登出
    fun logout() {
        launchRequest(
            block = { vehicleRepository.logout() },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 获取用户信息
    fun getUserInfo() {
        launchRequest(
            block = { vehicleRepository.getCarUserInfo() },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    MyApp.userInfo = response.body()?.data?.info
                }
            }
        )
    }
}