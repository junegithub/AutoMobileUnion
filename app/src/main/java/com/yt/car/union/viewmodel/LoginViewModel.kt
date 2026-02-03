package com.yt.car.union.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import android.util.Log
import com.yt.car.union.MyApp
import com.yt.car.union.net.RetrofitClient
import com.yt.car.union.net.bean.LoginRequest
import com.yt.car.union.util.SPUtils

/**
 * 登录相关ViewModel
 */
class LoginViewModel : ViewModel() {
    // 登录接口调用
    fun login(account: String, password: String, type: Int, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 构建请求参数
                val request = LoginRequest(account, password, type)
                // 调用接口
                val response = RetrofitClient.getApiService().loginApi(request)

                // 业务成功判断（对应原JS的code=200）
                if (response.code == 1 && response.data != null) {
                    // 保存Token
                    SPUtils.saveToken(response.data.userinfo?.token)
                    MyApp.isLogin = true
                    getUserInfo()
                    callback(true, "登录成功")
                } else {
                    callback(false, response.msg ?: "登录失败")
                }
            } catch (e: Exception) {
                // 异常处理（网络错误、解析错误等）
                Log.e("LoginViewModel", "登录失败", e)
                callback(false, "网络异常：${e.message}")
            }
        }
    }

    fun getUserInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.getApiService().getUserInfo()
                if (response.code == 1 && response.data != null) {
                    MyApp.userInfo = response.data.info
                }
            } catch (e: Exception) {
                // 异常处理（网络错误、解析错误等）
                Log.e("LoginViewModel", "获取信息失败", e)
            }
        }
    }

    fun logout(callback: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.getApiService().loginOut()
                callback(response.code == 1)
            } catch (e: Exception) {
                // 异常处理（网络错误、解析错误等）
                Log.e("LoginViewModel", "登出失败", e)
                callback(false)
            }
        }
    }
}
