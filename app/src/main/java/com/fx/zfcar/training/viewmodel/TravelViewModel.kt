package com.fx.zfcar.training.viewmodel

import com.fx.zfcar.net.TravelLogData
import com.fx.zfcar.net.TravelPostRequest
import com.fx.zfcar.net.TravelPostResponse
import com.fx.zfcar.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 行程相关ViewModel（行程上报、删除、日志等）
 */
class TravelViewModel : TrainingBaseViewModel() {

    // 行程上报
    fun travelPost(request: TravelPostRequest,
                   stateFlow: MutableStateFlow<ApiState<TravelPostResponse>>) {
        launchRequest(
            block = { vehicleRepository.travelPost(request) },
            stateFlow
        )
    }

    // 行程删除
    fun travelDel(id: String, stateFlow: MutableStateFlow<ApiState<Int>>) {
        launchRequest(
            block = { vehicleRepository.travelDel(id) },
            stateFlow
        )
    }

    // 获取行程日志
    fun getTravelLog(stateFlow: MutableStateFlow<ApiState<TravelLogData>>) {
        launchRequest(
            block = { vehicleRepository.getTravelLog() },
            stateFlow
        )
    }
}