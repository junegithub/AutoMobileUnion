package com.yt.car.union.viewmodel

import com.yt.car.union.net.SearchHistoryRequest
import okhttp3.MultipartBody

/**
 * 车辆信息相关ViewModel（车辆信息、位置、报表、状态等）
 */
class CarInfoViewModel : CarBaseViewModel() {

    // 获取车辆基础信息
    fun getCarInfo(carId: Int) {
        launchRequest(
            block = { vehicleRepository.getCarInfo(carId) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 检查是否是LYBH（业务逻辑保留）
    fun isLYBH() {
        launchRequest(
            block = { vehicleRepository.isLYBH() },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code != null) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 油费日报
    fun getOilDayReport(page: Int, pageSize: Int?, search: String?, timetype: Int) {
        launchRequest(
            block = { vehicleRepository.getOilDayReport(page, pageSize, search, timetype) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 泄漏报表
    fun getLeakReport(page: Int, pageSize: Int?, search: String?, timetype: Int) {
        launchRequest(
            block = { vehicleRepository.getLeakReport(page, pageSize, search, timetype) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 地图位置
    fun getMapPositions(size: Int?) {
        launchRequest(
            block = { vehicleRepository.getMapPositions(size) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 实时地址
    fun getRealTimeAddress(carId: Int?, carnum: String?) {
        launchRequest(
            block = { vehicleRepository.getRealTimeAddress(carId, carnum) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 车辆状态（按类型）
    fun getCarStatusByType(carType: String, pageNum: Int, pageSize: Int) {
        launchRequest(
            block = { vehicleRepository.getCarStatusByType(carType, pageNum, pageSize) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 仪表盘信息
    fun getDashboardInfo() {
        launchRequest(
            block = { vehicleRepository.getDashboardInfo() },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 轨迹信息
    fun getTrackInfo(carId: Int, endtime: String, is704: Boolean?, isFilter: Boolean?, starttime: String) {
        launchRequest(
            block = { vehicleRepository.getTrackInfo(carId, endtime, is704, isFilter, starttime) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 拍照指令
    fun takePhoto(carId: String) {
        launchRequest(
            block = { vehicleRepository.takePhoto(carId) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 其他车辆报表/状态相关方法...（按相同逻辑迁移）
    fun getOilAddReport(page: Int, pageSize: Int?, search: String?, timetype: Int) {
        launchRequest(
            block = { vehicleRepository.getOilAddReport(page, pageSize, search, timetype) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    fun getExpiredCars(page: Int, pageSize: Int?, search: String?) {
        launchRequest(
            block = { vehicleRepository.getExpiredCars(page, pageSize, search) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }


}