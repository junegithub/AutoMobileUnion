package com.yt.car.union.viewmodel

import com.yt.car.union.net.TravelPostRequest

/**
 * 行程相关ViewModel（行程上报、删除、日志等）
 */
class TravelViewModel : TrainingBaseViewModel() {

    // 行程上报
    fun travelPost(request: TravelPostRequest) {
        launchRequest(
            block = { vehicleRepository.travelPost(request) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.car_id != null) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 行程删除
    fun travelDel(id: String) {
        launchRequest(
            block = { vehicleRepository.travelDel(id) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 获取行程日志
    fun getTravelLog() {
        launchRequest(
            block = { vehicleRepository.getTravelLog() },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }
}