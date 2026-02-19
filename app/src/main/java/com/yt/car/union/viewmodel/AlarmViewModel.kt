package com.yt.car.union.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow

class AlarmViewModel : CarBaseViewModel() {
    fun getAlarmDetailsList(start: String, end: String, warningType: String, stateFlow: MutableStateFlow<ApiState<Any>>) {
        launchRequest(
            block = { vehicleRepository.getAlarmDetailsList(start, end, warningType) },
            stateFlow
        )
    }
}