package com.yt.car.union.viewmodel

import com.yt.car.union.net.CarUserInfo
import com.yt.car.union.net.LoginData
import com.yt.car.union.net.LoginRequest
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 用户相关ViewModel（登录、登出、信息、密码重置等）
 */
class UserViewModel : CarBaseViewModel() {

    // 登录
    fun login(request: LoginRequest, stateFlow: MutableStateFlow<ApiState<LoginData>>) {
        launchRequest(
            block = { vehicleRepository.login(request) },
            stateFlow
        )
    }

    // 登出
    fun logout(stateFlow: MutableStateFlow<ApiState<Any>>) {
        launchRequest(
            block = { vehicleRepository.logout() },
            stateFlow
        )
    }

    // 获取用户信息
    fun getUserInfo(stateFlow: MutableStateFlow<ApiState<CarUserInfo>>) {
        launchRequest(
            block = { vehicleRepository.getCarUserInfo() },
            stateFlow
        )
    }

    // 检查是否是LYBH（业务逻辑保留）
    fun isLYBH(stateFlow: MutableStateFlow<ApiState<Boolean>>) {
        launchRequest(
            block = { vehicleRepository.isLYBH() },
            stateFlow
        )
    }
}