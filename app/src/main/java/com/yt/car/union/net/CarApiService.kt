package com.yt.car.union.net

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CarApiService {
    // 查车相关API（/car/app 前缀）
    @GET("car/app/car/carinfo")
    suspend fun getCarInfo(@Query("car_id") carId: Int): Response<BaseResponse<CarInfo>>

    @GET("car/app/car/isLYBH")
    suspend fun isLYBH(): Response<BaseResponse<Boolean>>

    @POST("car/app/history")
    suspend fun addSearchHistory(@Body request: SearchHistoryRequest): Response<BaseResponse<Int>>

    @GET("car/app/tree/getTree")
    suspend fun getTree(
        @Query("ancestors") ancestors: String?,
        @Query("pos") pos: Boolean?,
        @Query("tree") tree: Boolean?
    ): Response<BaseResponse<List<TreeNode>>>

    @GET("car/app/tree/getTreeBlurry")
    suspend fun getTreeBlurry(
        @Query("blurry") blurry: String,
        @Query("pos") pos: Boolean?,
        @Query("tree") tree: Boolean?
    ): Response<BaseResponse<List<TreeNode>>>

    // /aggregation/app 前缀
    @GET("aggregation/app/oil/day")
    suspend fun getOilDayReport(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("search") search: String?,
        @Query("timetype") timetype: Int
    ): Response<BaseResponse<OilDayReportData>>

    @GET("aggregation/app/oil/leak")
    suspend fun getLeakReport(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("search") search: String?,
        @Query("timetype") timetype: Int
    ): Response<BaseResponse<LeakReportData>>

    @GET("aggregation/app/position/getTop")
    suspend fun getMapPositions(@Query("size") size: Int?): Response<BaseResponse<MapPositionData>>

    @GET("aggregation/app/position/realtimeaddress")
    suspend fun getRealTimeAddress(
        @Query("car_id") carId: Int?,
        @Query("carnum") carnum: String?
    ): Response<BaseResponse<RealTimeAddressData>>

    @GET("aggregation/app/carStatus/listByType")
    suspend fun getCarStatusByType(
        @Query("carType") carType: String,
        @Query("pageNum") pageNum: Int,
        @Query("pageSize") pageSize: Int
    ): Response<BaseResponse<List<CarStatusDetailItem>>>

    @GET("aggregation/app/carStatus/statusList")
    suspend fun getCarStatusList(): Response<BaseResponse<CarStatusListData?>>

    @GET("aggregation/app/dashboard/appSearchCarType")
    suspend fun searchCarByType(
        @Query("search") search: String,
        @Query("tree") tree: Boolean?,
        @Query("type") type: String,
        @Query("pageSize") pageSize: String,
        @Query("pageNum") pageNum: String
    ): Response<BaseResponse<SearchCarTypeData>>

    @GET("aggregation/app/dashboard/getInfo")
    suspend fun getDashboardInfo(): Response<BaseResponse<DashboardInfoData>>

    @GET("aggregation/app/oil/add")
    suspend fun getOilAddReport(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("search") search: String?,
        @Query("timetype") timetype: Int
    ): Response<BaseResponse<OilAddReportData>>

    @POST("aggregation/app/position/shareLastPosition")
    suspend fun shareLastPosition(@Body carId: Long): Response<BaseResponse<String>>

    @GET("aggregation/app/position/track")
    suspend fun getTrackInfo(
        @Query("car_id") carId: Int,
        @Query("endtime") endtime: String,
        @Query("is704") is704: Boolean?,
        @Query("isFilter") isFilter: Boolean?,
        @Query("starttime") starttime: String
    ): Response<BaseResponse<TrackData>>

    @GET("aggregation/app/video/videonew")
    suspend fun getVideoInfo(@Query("car_id") carId: Int): Response<BaseResponse<VideoInfoData>>

    @GET("aggregation/app/work/activeWarning")
    suspend fun getActiveWarning(
        @Query("search") search: String?,
        @Query("timetype") timetype: Int,
        @Query("page") page: String
    ): Response<BaseResponse<ActiveWarningData>>

    @GET("aggregation/app/work/caroverlist")
    suspend fun getExpiredCars(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("search") search: String?
    ): Response<BaseResponse<ExpiredCarData>>

    @GET("aggregation/app/work/mileage")
    suspend fun getMileageReport(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("search") search: String?,
        @Query("timetype") timetype: Int
    ): Response<BaseResponse<MileageData>>

    @GET("aggregation/app/work/offline")
    suspend fun getOfflineReport(
        @Query("end") end: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("search") search: String?,
        @Query("start") start: String
    ): Response<BaseResponse<OfflineReportData>>

    @GET("aggregation/app/work/photos")
    suspend fun getPhotoReport(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("search") search: String?,
        @Query("timetype") timetype: Int
    ): Response<BaseResponse<PhotoReportData>>

    @GET("aggregation/app/work/stopDetail")
    suspend fun getStopDetailReport(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("search") search: String,
        @Query("timetype") timetype: Int
    ): Response<BaseResponse<StopDetailData>>

    @GET("aggregation/app/work/warning")
    suspend fun getWarningReport(
        @Query("search") search: String?,
        @Query("timetype") timetype: Int,
        @Query("page") page: String
    ): Response<BaseResponse<WarningReportData>>

    @GET("aggregation/app/work/warningDetail")
    suspend fun getWarningDetail(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("timetype") timetype: Int,
        @Query("type") type: Int
    ): Response<BaseResponse<WarningDetailData>>

    @GET("aggregation/app/work/warningList")
    suspend fun alarmDetailsList(
        @Query("start") start: String,
        @Query("end") end: String,
        @Query("pageNum") pageNum: Int,
        @Query("pageSize") pageSize: Int,
        @Query("warningType") warningType: String
    ): Response<BaseResponse<AlarmListData>>

    // /jt808/app 前缀
    @GET("jt808/app/jt808/sendcontent")
    suspend fun sendContent(@Query("car_id") search: String?,@Query("content") carId: String): Response<BaseResponse<Any>>

    @GET("jt808/app/jt808/photos")
    suspend fun takePhoto(@Query("car_id") carId: String): Response<BaseResponse<Any>>

    // /auth/app 前缀
    @POST("auth/app/login")
    suspend fun login(@Body request: LoginRequest): Response<BaseResponse<LoginData>>

    @DELETE("auth/app/logout")
    suspend fun logout(): Response<BaseResponse<Any>>

    /** 获取用户信息 */
    @GET("system/app/user")
    suspend fun getCarUserInfo(): Response<BaseResponse<CarUserInfo>>
}