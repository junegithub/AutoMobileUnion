package com.yt.car.union.net


// Repository Layer
class CarRepository(private val apiService: CarApiService) {
    // 查车相关API
    suspend fun getCarInfo(carId: Int) = apiService.getCarInfo(carId)

    suspend fun isLYBH() = apiService.isLYBH()

    suspend fun getOilDayReport(page: Int, pageSize: Int?, search: String?, timetype: Int) =
        apiService.getOilDayReport(page, pageSize, search, timetype)

    suspend fun getLeakReport(page: Int, pageSize: Int?, search: String?, timetype: Int) =
        apiService.getLeakReport(page, pageSize, search, timetype)

    suspend fun getMapPositions(size: Int?) = apiService.getMapPositions(size)

    suspend fun getRealTimeAddress(carId: Int?, carnum: String?) =
        apiService.getRealTimeAddress(carId, carnum)

    suspend fun addSearchHistory(request: SearchHistoryRequest) =
        apiService.addSearchHistory(request)

    suspend fun getTree(ancestors: String?, pos: Boolean?, tree: Boolean?) =
        apiService.getTree(ancestors, pos, tree)

    suspend fun getTreeBlurry(blurry: String, pos: Boolean?, tree: Boolean?) =
        apiService.getTreeBlurry(blurry, pos, tree)

    suspend fun getCarStatusByType(carType: String, pageNum: Int, pageSize: Int) =
        apiService.getCarStatusByType(carType, pageNum, pageSize)

    suspend fun getOutdate(expired: Boolean, pageNum: Int, pageSize: Int,) = apiService.getOutdate(expired, pageNum, pageSize)

    suspend fun getCarStatusList() = apiService.getCarStatusList()

    suspend fun searchCarByType(search: String, tree: Boolean?, type: String, pageSize: String, pageNum: String) =
        apiService.searchCarByType(search, tree, type, pageSize, pageNum)

    suspend fun getDashboardInfo() = apiService.getDashboardInfo()

    suspend fun getOilAddReport(page: Int, pageSize: Int?, search: String?, timetype: Int) =
        apiService.getOilAddReport(page, pageSize, search, timetype)

    suspend fun shareLastPosition(carId: Long) = apiService.shareLastPosition(carId)

    suspend fun getTrackInfo(carId: Int, endtime: String, is704: Boolean?, isFilter: Boolean?, starttime: String) =
        apiService.getTrackInfo(carId, endtime, is704, isFilter, starttime)

    suspend fun getVideoInfo(carId: Int) = apiService.getVideoInfo(carId)

    suspend fun getActiveWarning(search: String?, timetype: Int, page: String) =
        apiService.getActiveWarning(search, timetype, page)

    suspend fun getExpiredCars(page: Int, pageSize: Int?, search: String?) =
        apiService.getExpiredCars(page, pageSize, search)

    suspend fun getMileageReport(page: Int, pageSize: Int?, search: String?, timetype: Int) =
        apiService.getMileageReport(page, pageSize, search, timetype)

    suspend fun getOfflineReport(end: String, page: Int, pageSize: Int?, search: String?, start: String) =
        apiService.getOfflineReport(end, page, pageSize, search, start)

    suspend fun getOfflineDetailReport(carId: Int, end: String, start: String) =
        apiService.getOfflineDetailReport(carId, end, start)

    suspend fun getPhotoReport(page: Int, pageSize: Int?, search: String?, timetype: Int) =
        apiService.getPhotoReport(page, pageSize, search, timetype)

    suspend fun getStopDetailReport(page: Int, pageSize: Int?, search: String, timetype: Int) =
        apiService.getStopDetailReport(page, pageSize, search, timetype)

    suspend fun getWarningReport(search: String?, timetype: Int, page: String) =
        apiService.getWarningReport(search, timetype, page)

    suspend fun getWarningDetail(page: Int, pageSize: Int?, timetype: Int, type: Int) =
        apiService.getWarningDetail(page, pageSize, timetype, type)

    suspend fun sendContent(request: SendContentRequest) = apiService.sendContent(request.car_id, request.content)

    suspend fun takePhoto(carId: String) = apiService.takePhoto(carId)

    suspend fun login(request: LoginRequest) = apiService.login(request)

    suspend fun logout() = apiService.logout()
    suspend fun getCarUserInfo() = apiService.getCarUserInfo()

    suspend fun getAlarmDetailsList(start: String, end: String, pageNum: Int, pageSize: Int, warningType: String) = apiService.alarmDetailsList(start, end, pageNum, pageSize,warningType)
    suspend fun getWarningType(warningType: String) = apiService.getWarningType(warningType)
}