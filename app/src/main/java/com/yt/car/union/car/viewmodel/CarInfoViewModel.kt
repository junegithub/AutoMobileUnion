package com.yt.car.union.car.viewmodel

import com.yt.car.union.net.CarExpireResponse
import com.yt.car.union.net.CarInfo
import com.yt.car.union.net.CarStatusDetailItem
import com.yt.car.union.net.CarStatusListData
import com.yt.car.union.net.DashboardInfoData
import com.yt.car.union.net.MapPositionData
import com.yt.car.union.net.RealTimeAddressData
import com.yt.car.union.net.SearchHistoryRequest
import com.yt.car.union.net.SearchResult
import com.yt.car.union.net.SendContentRequest
import com.yt.car.union.net.TrackData
import com.yt.car.union.net.VideoInfoData
import com.yt.car.union.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 车辆信息相关ViewModel（车辆信息、位置、报表、状态等）
 */
class CarInfoViewModel : CarBaseViewModel() {

    // 获取车辆基础信息
    fun getCarInfo(carId: Int, stateFlow: MutableStateFlow<ApiState<CarInfo>>) {
        launchRequest(
            block = { vehicleRepository.getCarInfo(carId) },
            stateFlow
        )
    }

    // 地图位置
    fun getMapPositions(size: Int?, stateFlow: MutableStateFlow<ApiState<MapPositionData>>) {
        launchRequest(
            block = { vehicleRepository.getMapPositions(size) },
            stateFlow
        )
    }

    // 实时地址
    fun getRealTimeAddress(carId: Int?, carnum: String?,
                           stateFlow: MutableStateFlow<ApiState<RealTimeAddressData>>) {
        launchRequest(
            block = { vehicleRepository.getRealTimeAddress(carId, carnum) },
            stateFlow
        )
    }

    // 车辆状态（按类型）
    fun getCarStatusByType(carType: String, pageNum: Int, pageSize: Int,
                           stateFlow: MutableStateFlow<ApiState<List<CarStatusDetailItem>>>) {
        launchRequest(
            block = { vehicleRepository.getCarStatusByType(carType, pageNum, pageSize) },
            stateFlow
        )
    }

    fun getOutdate(expired: Boolean, pageNum: Int, pageSize: Int,
                           stateFlow: MutableStateFlow<ApiState<CarExpireResponse>>) {
        launchRequest(
            block = { vehicleRepository.getOutdate(expired, pageNum, pageSize) },
            stateFlow
        )
    }

    // 仪表盘信息
    fun getDashboardInfo(stateFlow: MutableStateFlow<ApiState<DashboardInfoData>>) {
        launchRequest(
            block = { vehicleRepository.getDashboardInfo() },
            stateFlow
        )
    }

    // 轨迹信息
    fun getTrackInfo(carId: Int, endtime: String, is704: Boolean?, isFilter: Boolean?,
                     starttime: String, stateFlow: MutableStateFlow<ApiState<TrackData>>) {
        launchRequest(
            block = { vehicleRepository.getTrackInfo(carId, endtime, is704, isFilter, starttime) },
            stateFlow
        )
    }

    fun sendContent(carId: String, content: String, stateFlow: MutableStateFlow<ApiState<Any>>) {
        launchRequest(
            block = { vehicleRepository.sendContent(SendContentRequest(carId, content)) },
            stateFlow
        )
    }

    // 拍照指令
    fun takePhoto(carId: String, stateFlow: MutableStateFlow<ApiState<Any>>) {
        launchRequest(
            block = { vehicleRepository.takePhoto(carId) },
            stateFlow
        )
    }

    fun getCarStatusList(stateFlow: MutableStateFlow<ApiState<CarStatusListData>>) {
        launchRequest(
            block = { vehicleRepository.getCarStatusList() },
            stateFlow
        )
    }

    // 添加搜索历史
    fun addSearchHistory(request: SearchHistoryRequest,
                         stateFlow: MutableStateFlow<ApiState<Int>>) {
        launchRequest(
            block = { vehicleRepository.addSearchHistory(request) },
            stateFlow
        )
    }

    fun getSearchHistory(stateFlow: MutableStateFlow<ApiState<List<SearchResult>>>) {
        launchRequest(
            block = { vehicleRepository.getSearchHistory() },
            stateFlow
        )
    }

    fun getVideoInfo(carId: Int, stateFlow: MutableStateFlow<ApiState<VideoInfoData>>) {
        launchRequest(
            block = { vehicleRepository.getVideoInfo(carId) },
            stateFlow
        )
    }

    fun shareLastPosition(carId: Long, stateFlow: MutableStateFlow<ApiState<String>>) {
        launchRequest(
            block = { vehicleRepository.shareLastPosition(carId) },
            stateFlow
        )
    }
}