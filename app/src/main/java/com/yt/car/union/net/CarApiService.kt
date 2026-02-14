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
    suspend fun getCarInfo(@Query("car_id") carId: Int): Response<CarInfoResponse>

    @GET("car/app/car/isLYBH")
    suspend fun isLYBH(): Response<IsLYBHResponse>

    @POST("car/app/history")
    suspend fun addSearchHistory(@Body request: SearchHistoryRequest): Response<SearchHistoryResponse>

    @GET("car/app/tree/getTree")
    suspend fun getTree(
        @Query("ancestors") ancestors: String?,
        @Query("pos") pos: Boolean?,
        @Query("tree") tree: Boolean?
    ): Response<TreeNodeResponse>

    @GET("car/app/tree/getTreeBlurry")
    suspend fun getTreeBlurry(
        @Query("blurry") blurry: String,
        @Query("pos") pos: Boolean?,
        @Query("tree") tree: Boolean?
    ): Response<TreeNodeResponse>

    // /aggregation/app 前缀
    @GET("aggregation/app/oil/day")
    suspend fun getOilDayReport(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("search") search: String?,
        @Query("timetype") timetype: Int
    ): Response<OilDayReportResponse>

    @GET("aggregation/app/oil/leak")
    suspend fun getLeakReport(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("search") search: String?,
        @Query("timetype") timetype: Int
    ): Response<LeakReportResponse>

    @GET("aggregation/app/position/getTop")
    suspend fun getMapPositions(@Query("size") size: Int?): Response<MapPositionResponse>

    @GET("aggregation/app/position/realtimeaddress")
    suspend fun getRealTimeAddress(
        @Query("car_id") carId: Int?,
        @Query("carnum") carnum: String?
    ): Response<RealTimeAddressResponse>

    @GET("aggregation/app/carStatus/listByType")
    suspend fun getCarStatusByType(
        @Query("carType") carType: String,
        @Query("pageNum") pageNum: Int,
        @Query("pageSize") pageSize: Int
    ): Response<CarStatusDetailResponse>

    @GET("aggregation/app/carStatus/statusList")
    suspend fun getCarStatusList(): Response<CarStatusListResponse>

    @GET("aggregation/app/dashboard/appSearchCarType")
    suspend fun searchCarByType(
        @Query("search") search: String,
        @Query("tree") tree: Boolean?,
        @Query("type") type: String,
        @Query("pageSize") pageSize: String,
        @Query("pageNum") pageNum: String
    ): Response<SearchCarTypeResponse>

    @GET("aggregation/app/dashboard/getInfo")
    suspend fun getDashboardInfo(): Response<DashboardInfoResponse>

    @GET("aggregation/app/oil/add")
    suspend fun getOilAddReport(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("search") search: String?,
        @Query("timetype") timetype: Int
    ): Response<OilAddReportResponse>

    @POST("aggregation/app/position/shareLastPosition")
    suspend fun shareLastPosition(@Body carId: Long): Response<ShareLastPositionResponse>

    @GET("aggregation/app/position/track")
    suspend fun getTrackInfo(
        @Query("car_id") carId: Int,
        @Query("endtime") endtime: String,
        @Query("is704") is704: Boolean?,
        @Query("isFilter") isFilter: Boolean?,
        @Query("starttime") starttime: String
    ): Response<TrackResponse>

    @GET("aggregation/app/video/videonew")
    suspend fun getVideoInfo(@Query("car_id") carId: Int): Response<VideoInfoResponse>

    @GET("aggregation/app/work/activeWarning")
    suspend fun getActiveWarning(
        @Query("search") search: String?,
        @Query("timetype") timetype: Int,
        @Query("page") page: String
    ): Response<ActiveWarningResponse>

    @GET("aggregation/app/work/caroverlist")
    suspend fun getExpiredCars(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("search") search: String?
    ): Response<ExpiredCarResponse>

    @GET("aggregation/app/work/mileage")
    suspend fun getMileageReport(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("search") search: String?,
        @Query("timetype") timetype: Int
    ): Response<MileageResponse>

    @GET("aggregation/app/work/offline")
    suspend fun getOfflineReport(
        @Query("end") end: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("search") search: String?,
        @Query("start") start: String
    ): Response<OfflineReportResponse>

    @GET("aggregation/app/work/photos")
    suspend fun getPhotoReport(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("search") search: String?,
        @Query("timetype") timetype: Int
    ): Response<PhotoReportResponse>

    @GET("aggregation/app/work/stopDetail")
    suspend fun getStopDetailReport(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("search") search: String,
        @Query("timetype") timetype: Int
    ): Response<StopDetailResponse>

    @GET("aggregation/app/work/warning")
    suspend fun getWarningReport(
        @Query("search") search: String?,
        @Query("timetype") timetype: Int,
        @Query("page") page: String
    ): Response<WarningReportResponse>

    @GET("aggregation/app/work/warningDetail")
    suspend fun getWarningDetail(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("timetype") timetype: Int,
        @Query("type") type: Int
    ): Response<WarningDetailResponse>

    // /jt808/app 前缀
    @POST("jt808/app/jt808/sendcontent")
    suspend fun sendContent(@Body request: SendContentRequest): Response<SendContentResponse>

    @POST("jt808/app/jt808/photos")
    suspend fun takePhoto(@Query("car_id") carId: String): Response<TakePhotoResponse>

    // /auth/app 前缀
    @POST("auth/app/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @DELETE("auth/app/logout")
    suspend fun logout(): Response<LogoutResponse>

    /** 获取用户信息 */
    @GET("system/app/user")
    suspend fun getCarUserInfo(): Response<CarUserInfoResponse>
}