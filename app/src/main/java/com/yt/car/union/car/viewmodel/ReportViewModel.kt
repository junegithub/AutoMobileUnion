package com.yt.car.union.car.viewmodel

import com.yt.car.union.net.ActiveWarningData
import com.yt.car.union.net.ExpiredCarData
import com.yt.car.union.net.LeakReportData
import com.yt.car.union.net.MileageData
import com.yt.car.union.net.OfflineReportData
import com.yt.car.union.net.OilAddReportData
import com.yt.car.union.net.OilDayReportData
import com.yt.car.union.net.PhotoReportData
import com.yt.car.union.net.StopDetailData
import com.yt.car.union.net.WarningDetailData
import com.yt.car.union.net.WarningReportData
import com.yt.car.union.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 车辆信息相关ViewModel（车辆信息、位置、报表、状态等）
 */
class ReportViewModel : CarBaseViewModel() {

    // 日油耗报表
    fun getOilDayReport(page: Int, pageSize: Int?, search: String?, timetype: Int,
                        stateFlow: MutableStateFlow<ApiState<OilDayReportData>>) {
        launchRequest(
            block = { vehicleRepository.getOilDayReport(page, pageSize, search, timetype) },
            stateFlow
        )
    }

    // 漏油报表
    fun getLeakReport(page: Int, pageSize: Int?, search: String?, timetype: Int,
                      stateFlow: MutableStateFlow<ApiState<LeakReportData>>) {
        launchRequest(
            block = { vehicleRepository.getLeakReport(page, pageSize, search, timetype) },
            stateFlow
        )
    }

    //加油报表
    fun getOilAddReport(page: Int, pageSize: Int?, search: String?, timetype: Int,
                        stateFlow: MutableStateFlow<ApiState<OilAddReportData>>) {
        launchRequest(
            block = { vehicleRepository.getOilAddReport(page, pageSize, search, timetype) },
            stateFlow
        )
    }

    //安全查询
    fun getActiveWarning(search: String?, timetype: Int, page: String,
                        stateFlow: MutableStateFlow<ApiState<ActiveWarningData>>) {
        launchRequest(
            block = { vehicleRepository.getActiveWarning(search, timetype, page) },
            stateFlow
        )
    }

    //过期查询
    fun getExpiredCars(page: Int, pageSize: Int?, search: String?,
                       stateFlow: MutableStateFlow<ApiState<ExpiredCarData>>) {
        launchRequest(
            block = { vehicleRepository.getExpiredCars(page, pageSize, search) },
            stateFlow
        )
    }

    //里程查询
    fun getMileageReport(page: Int, pageSize: Int?, search: String?, timetype: Int,
                       stateFlow: MutableStateFlow<ApiState<MileageData>>) {
        launchRequest(
            block = { vehicleRepository.getMileageReport(page, pageSize, search, timetype) },
            stateFlow
        )
    }
    //离线天数统计
    fun getOfflineReport(end: String, page: Int, pageSize: Int?, search: String?, start: String,
                         stateFlow: MutableStateFlow<ApiState<OfflineReportData>>) {
        launchRequest(
            block = { vehicleRepository.getOfflineReport(end, page, pageSize, search, start) },
            stateFlow
        )
    }
    //离线天数详情
    fun getOfflineDetailReport(carId: Int, end: String, start: String,
                         stateFlow: MutableStateFlow<ApiState<List<String>>>) {
        launchRequest(
            block = { vehicleRepository.getOfflineDetailReport(carId, end, start) },
            stateFlow
        )
    }

    //照片查询
    fun getPhotoReport(page: Int, pageSize: Int?, search: String?, timetype: Int,
                         stateFlow: MutableStateFlow<ApiState<PhotoReportData>>) {
        launchRequest(
            block = { vehicleRepository.getPhotoReport(page, pageSize, search, timetype) },
            stateFlow
        )
    }
    //停车明细
    fun getStopDetailReport(page: Int, pageSize: Int?, search: String, timetype: Int,
                         stateFlow: MutableStateFlow<ApiState<StopDetailData>>) {
        launchRequest(
            block = { vehicleRepository.getStopDetailReport(page, pageSize, search, timetype) },
            stateFlow
        )
    }
    //报警查询
    fun getWarningReport(search: String?, timetype: Int, page: String,
                            stateFlow: MutableStateFlow<ApiState<WarningReportData>>) {
        launchRequest(
            block = { vehicleRepository.getWarningReport(search, timetype, page) },
            stateFlow
        )
    }
    //报警查询详情
    fun getWarningDetail(page: Int, pageSize: Int?, timetype: Int, type: Int,
                            stateFlow: MutableStateFlow<ApiState<WarningDetailData>>) {
        launchRequest(
            block = { vehicleRepository.getWarningDetail(page, pageSize, timetype, type) },
            stateFlow
        )
    }
}