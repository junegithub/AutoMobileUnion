package com.fx.zfcar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fx.zfcar.MyApp
import com.fx.zfcar.net.DictItem
import com.fx.zfcar.net.DictMapManager
import com.fx.zfcar.net.IBaseResponse
import com.fx.zfcar.util.DialogUtils
import com.fx.zfcar.util.NetworkErrorMapper
import com.fx.zfcar.util.SPUtils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

/**
 * 基础ViewModel，封装通用逻辑
 */
open class BaseViewModel : ViewModel() {

    // 通用异常处理器
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onRequestError(throwable)
    }

    /**
     * 通用网络请求协程封装
     * @param block 具体的请求逻辑
     * @param onSuccess 请求成功后的回调（可选）
     */
    protected fun <T, R: IBaseResponse<T>> launchRequest(
        block: suspend () -> Response<R>,
        stateFlow: MutableStateFlow<ApiState<T>>?
    ) {
        viewModelScope.launch(exceptionHandler) {
            stateFlow?.value = ApiState.Loading
            runCatching {
                block()
            }.onSuccess { response ->
                handleResponse(response, stateFlow)
            }.onFailure { exception ->
                stateFlow?.value = ApiState.Error(
                    msg = NetworkErrorMapper.fromThrowable(exception),
                    throwable = exception
                )
            }
        }
    }

    private fun <T, R : IBaseResponse<T>> handleResponse(
        response: Response<R>,
        stateFlow: MutableStateFlow<ApiState<T>>?
    ) {
        when {
            !response.isSuccessful -> {
                if (response.code() == 401) {
                    handleAuthExpired(response)
                }
                stateFlow?.value = ApiState.Error(
                    NetworkErrorMapper.fromHttp(response.code(), response.message())
                )
            }
            response.body() == null -> {
                stateFlow?.value = ApiState.Error("响应体为空")
            }
            response.body()?.isSuccess() == true -> {
                stateFlow?.value = ApiState.Success(response.body()!!.data)
                if (response.body()!!.rows != null) {
                    DictMapManager.initDictMap(response.body()!!.rows as List<DictItem>)
                }
            }
            response.body()?.code == 401 -> {
                handleAuthExpired(response)
                stateFlow?.value = ApiState.Error("登录已失效，请重新登录")
            }
            else -> {
                val errorBody = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                val errorMsg = response.body()?.msg ?: errorBody ?: "请求失败"
                stateFlow?.value = ApiState.Error(errorMsg)
            }
        }
    }

    private fun <T, R : IBaseResponse<T>> handleAuthExpired(response: Response<R>) {
        val isTrainingRequest = response.raw().request.url.host == "safe.ezbeidou.com"
        if (isTrainingRequest) {
            SPUtils.saveTrainingToken("")
            SPUtils.save("trainLogin", "yes")
            MyApp.isTrainingLogin = false
            MyApp.trainingUserInfo = null
        } else {
            SPUtils.saveToken("")
            MyApp.isLogin = false
            MyApp.userInfo = null
        }
        MyApp.getCurrentActivity()?.let { activity ->
            activity.runOnUiThread {
                if (!activity.isFinishing && !activity.isDestroyed) {
                    if (isTrainingRequest) {
                        DialogUtils.showTrainingLoginPromptDialog(activity)
                    } else {
                        DialogUtils.showLoginPromptDialog(activity)
                    }
                }
            }
        }
    }

    /**
     * 请求失败的通用回调（子类可重写）
     */
    protected open fun onRequestError(throwable: Throwable) {}
}
