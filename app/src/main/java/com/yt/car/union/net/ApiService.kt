package com.yt.car.union.net

import com.yt.car.union.net.bean.BaseResponse
import com.yt.car.union.net.bean.CarStatisticsResponse
import com.yt.car.union.net.bean.LoginRequest
import com.yt.car.union.net.bean.LoginResponse
import com.yt.car.union.net.bean.LogoffRequest
import com.yt.car.union.net.bean.MapCarInfoResponse
import com.yt.car.union.net.bean.RegionData
import com.yt.car.union.net.bean.UserInfoResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap

/**
 * 对应原有 apis.js 的接口定义
 * 所有参数说明：
 * - @QueryMap: GET 请求的参数（对应 JS 里的 data/params）
 * - @Body: POST 请求的 JSON 体（对应 JS 里的 data）
 * - 方法名尽量与 JS 导出名保持一致，便于对照
 */
interface ApiService {

    // ===================== 登录/注销相关 =====================
    /** 登录 */
    @POST("auth/app/login")
    suspend fun loginApi(@Body request: LoginRequest): BaseResponse<LoginResponse>

    /** 账号密码登录 */
    @POST("api/user/login")
    suspend fun loginApiSafe(@Body data: Map<String, Any>): BaseResponse<Any>

    /** 注销 */
    @POST("api/user/logoff")
    suspend fun logoff(@Body request: LogoffRequest): BaseResponse<Any>

    /** 退出登录 */
    @DELETE("auth/app/logout")
    suspend fun loginOut(): BaseResponse<Any>

    // ===================== 用户信息相关 =====================
    /** 获取用户详细信息 */
    @GET("system/app/user/getInfo")
    suspend fun getInfo(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 是否是莱阳博慧的用户 */
    @GET("car/app/car/isLYBH")
    suspend fun isLYBH(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 获取用户信息 */
    @GET("system/app/user")
    suspend fun getUserInfo(): BaseResponse<UserInfoResponse>

    /** 获取安全培训用户信息 */
    @GET("api/user/info")
    suspend fun getUserInfoSafe(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 修改密码 */
    @GET("system/app/user/resetpwd")
    suspend fun changePassword(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 安全培训修改密码 */
    @GET("api/user/resetpwd")
    suspend fun changePasswordSafe(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 修改昵称 */
    @GET("system/app/user/profile")
    suspend fun editNickname(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 检查密码是否修改 */
    @GET("api/user/checkpwd")
    suspend fun checkpwd(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 获取用户车牌信息 */
    @GET("api/user/userotherinfo")
    suspend fun getUserCarInfo(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    // ===================== 首页相关 =====================
    /** 首页-获取过期提醒信息 */
    @GET("car/app/car/getExpiringInfo")
    suspend fun getNotice(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 首页-获取图表数据 */
    @GET("aggregation/app/dashboard/getInfo")
    suspend fun getChartData(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    // ===================== 一车一档/一人一档 =====================
    /** 一车一档-获取车辆信息 */
    @GET("car/app/file/carfile")
    suspend fun getCarInfo(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 一人一档-获取人员信息 */
    @GET("car/app/file/peoplefile")
    suspend fun getPeopleInfo(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    // ===================== 车辆检查 =====================
    /** 车辆日检 */
    @GET("api/user/cardaycheck")
    suspend fun getCarDayCheck(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 车辆月检 */
    @GET("api/user/carmonthcheck")
    suspend fun getCarMonthCheck(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    // ===================== 全图监控 =====================
    /** 地图-获取车辆坐标信息 */
    @GET("aggregation/app/dashboard/maorealtime")
    suspend fun getMapPunctuationData(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 地图-获取车辆数量（最大150辆） */
    @GET("aggregation/app/position/getTop")
    suspend fun getAllcars(): BaseResponse<CarStatisticsResponse>

    /** 地图-车辆坐标信息(备用接口) */
    @GET("api/user/realtime")
    suspend fun getMapPunctuationDataApi(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 地图-获取车辆详细信息 */
    @GET("aggregation/app/position/realtimeaddress")
    suspend fun getMapCatInfo(@QueryMap params: Map<String, String>? = null): BaseResponse<MapCarInfoResponse>

    /** 车辆拍照 */
    @GET("jt808/app/jt808/photos")
    suspend fun takePhoto(@QueryMap params: Map<String, Any>? = null): BaseResponse<Any>

    /** 文本信息下发 */
    @GET("jt808/app/jt808/sendcontent")
    suspend fun sendcontent(@QueryMap params: Map<String, Any>? = null): BaseResponse<Any>

    /** 获取轨迹点 */
    @GET("aggregation/app/position/track")
    suspend fun getTrackInfoApi(@QueryMap params: Map<String, Any>? = null): BaseResponse<Any>

    /** 实时视频 */
    @GET("api/user/video")
    suspend fun getVideoApi(@QueryMap params: Map<String, Any>? = null): BaseResponse<Any>

    /** 视频回放列表 */
    @GET("api/user/videoplaylist")
    suspend fun videoplaylist(@QueryMap params: Map<String, Any>? = null): BaseResponse<Any>

    /** 视频回放数据 */
    @GET("api/user/historyplay")
    suspend fun historyplay(@QueryMap params: Map<String, Any>? = null): BaseResponse<Any>

    /** 视频回放播放控制 */
    @GET("api/user/playcontrol")
    suspend fun playcontrol(@QueryMap params: Map<String, Any>? = null): BaseResponse<Any>

    // ===================== 报表相关 =====================
    /** 里程报表 */
    @GET("aggregation/app/work/mileage")
    suspend fun getMileage(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 报警报表 */
    @GET("aggregation/app/work/warning")
    suspend fun getWarn(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 报警详情 */
    @GET("aggregation/app/work/warningDetail")
    suspend fun getWarnDetail(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 离线统计 */
    @GET("aggregation/app/work/offline")
    suspend fun getOffLine(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 离线详情 */
    @GET("aggregation/app/work/offlineDetail")
    suspend fun getOffLineDetail(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 安全查询 */
    @GET("aggregation/app/work/activeWarning")
    suspend fun getSafety(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 照片查询 */
    @GET("aggregation/app/work/photos")
    suspend fun getPhotos(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 过期查询 */
    @GET("aggregation/app/work/caroverlist")
    suspend fun getCaroverlist(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 停车统计 */
    @GET("aggregation/app/work/stopDetail")
    suspend fun stopCarList(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 加油报表 */
    @GET("aggregation/app/oil/add")
    suspend fun addOil(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 日油耗报表 */
    @GET("aggregation/app/oil/day")
    suspend fun dayOil(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 漏油报表 */
    @GET("aggregation/app/oil/leak")
    suspend fun leakOil(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    // ===================== 电子运单 =====================
    /** 运单列表 */
    @GET("api/user/waybilllist")
    suspend fun getWayBill(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 删除运单信息 */
    @GET("api/user/waybilldel")
    suspend fun delWayBill(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 添加人员信息 */
    @POST("api/user/addpeoplewaybill")
    suspend fun addpeoplewaybill(@Body data: Map<String, Any>): BaseResponse<Any>

    /** 修改人员信息 */
    @POST("api/user/peopleeditpost")
    suspend fun peopleeditpost(@Body data: Map<String, Any>): BaseResponse<Any>

    /** 添加电子运单 */
    @POST("api/user/addwaybill")
    suspend fun addwaybill(@Body data: Map<String, Any>): BaseResponse<Any>

    /** 编辑电子运单 */
    @GET("api/user/waybilledit")
    suspend fun waybilledit(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    // ===================== 车辆管理 =====================
    /** 新增车辆 */
    @POST("car/app/car")
    suspend fun addCar(@Body data: Map<String, Any>): BaseResponse<Any>

    /** 搜索机构 */
    @GET("system/app/dept/allDept")
    suspend fun seerachCompany(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 安全培训-搜索机构 */
    @GET("api/user/allcategory")
    suspend fun seerachCompanysafe(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 搜索车辆 */
    @GET("car/app/car/allcarlist")
    suspend fun searchCar(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 机构树 */
    @GET("car/app/tree/ybcompanytree")
    suspend fun getCarTreeData(@QueryMap data: Map<String, Any>? = null): BaseResponse<List<RegionData>>

    /** 机构树V2版本 */
    @GET("car/app/tree/getTree")
    suspend fun getCarTreeInfo(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 模糊搜索机构名称 */
    @GET("car/app/tree/getTreeBlurry")
    suspend fun searchByDept(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 模糊搜索车牌(含报警数量) */
    @GET("aggregation/app/dashboard/appSearchCarType")
    suspend fun getSearchCarType(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 模糊搜索车牌号 */
    @GET("car/app/car/allCarListPage")
    suspend fun searchByCarName(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 获取车辆参数 */
    @GET("car/app/factory/list")
    suspend fun canshucar(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 安全培训-获取车辆参数 */
    @GET("api/user/canshucar")
    suspend fun canshucarsafe(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 获取车辆类型 */
    @GET("system/dict/data/type/dlcartype")
    suspend fun carTypeList(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 获取查车数据 */
    @GET("aggregation/app/dashboard/carStatusList")
    suspend fun carstatuslist(@QueryMap data: Map<String, Any>? = null): BaseResponse<CarStatisticsResponse>

    /** 查询分类车辆数量 */
    @GET("aggregation/app/carStatus/statusList")
    suspend fun getStatusList(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 查询分类车辆详细信息 */
    @GET("aggregation/app/carStatus/listByType")
    suspend fun getStatusListByType(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 查询过期车辆 */
    @GET("aggregation/app/carStatus/listByExpired")
    suspend fun getOutdate(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    // ===================== 设备管理 =====================
    /** 设备管理列表 */
    @GET("api/user/equipmentlist")
    suspend fun equipmentlist(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 设备管理删除 */
    @GET("api/user/equipmentdel")
    suspend fun equipmentdel(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 设备管理添加 */
    @POST("api/user/equipmentadd")
    suspend fun equipmentadd(@Body data: Map<String, Any>): BaseResponse<Any>

    /** 设备管理修改 */
    @POST("api/user/equipmenteditpost")
    suspend fun equipmenteditpost(@Body data: Map<String, Any>): BaseResponse<Any>

    /** 设备列表 */
    @GET("api/user/shebeialllist")
    suspend fun shebeialllist(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 费用录入添加 */
    @POST("car/app/equipment")
    suspend fun chargelogadd(@Body data: Map<String, Any>): BaseResponse<Any>

    // ===================== 进销存 =====================
    /** 进货单列表 */
    @GET("api/user/joingoodlist")
    suspend fun joingoodlist(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 进货单添加 */
    @POST("api/user/joingoodsadd")
    suspend fun joingoodsadd(@Body data: Map<String, Any>): BaseResponse<Any>

    /** 进货单修改 */
    @POST("api/user/joingoodseditpost")
    suspend fun joingoodseditpost(@Body data: Map<String, Any>): BaseResponse<Any>

    /** 进货单删除 */
    @GET("api/user/jiondel")
    suspend fun jiondel(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 出货单列表 */
    @GET("api/user/shipmentlist")
    suspend fun shipmentlist(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 出货单添加 */
    @POST("api/user/shipmentadd")
    suspend fun shipmentadd(@Body data: Map<String, Any>): BaseResponse<Any>

    /** 出货单修改 */
    @POST("api/user/shipmenteditpost")
    suspend fun shipmenteditpost(@Body data: Map<String, Any>): BaseResponse<Any>

    /** 出货单删除 */
    @GET("api/user/shipmentdel")
    suspend fun shipmentdel(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    // ===================== 维修管理 =====================
    /** 维修列表 */
    @GET("api/user/installlog")
    suspend fun installlog(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 维修添加 */
    @POST("api/user/installadd")
    suspend fun installadd(@Body data: Map<String, Any>): BaseResponse<Any>

    /** 维修修改 */
    @POST("api/user/installeditpost")
    suspend fun installeditpost(@Body data: Map<String, Any>): BaseResponse<Any>

    // ===================== 安全培训/考试 =====================
    /** 获取考试题目 */
    @GET("api/user/safekspaperview")
    suspend fun getquestions(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 提交答案 */
    @POST("api/user/safekspost")
    suspend fun answerSbumit(@Body data: Map<String, Any>): BaseResponse<Any>

    /** 答案详情 */
    @GET("api/user/safecankao")
    suspend fun safecankao(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 查看正确答案 */
    @GET("api/user/questionview")
    suspend fun questionview(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 安全培训列表 */
    @GET("api/user/safetylist")
    suspend fun safetylist(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 安全培训历史列表 */
    @GET("api/training/oldsafetylist")
    suspend fun oldsafetylist(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 课件列表 */
    @GET("api/user/coursewarelist")
    suspend fun coursewarelist(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 课件详情 */
    @GET("api/user/coursewareview")
    suspend fun coursewareview(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 安全会议列表 */
    @GET("api/training/meetinglist")
    suspend fun meetinglist(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 安全会议详情 */
    @GET("api/training/meetingview")
    suspend fun meetingview(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 安全会议签名拍照提交 */
    @POST("api/training/singpost")
    suspend fun singpost(@Body data: Map<String, Any>): BaseResponse<Any>

    /** 评价课件 */
    @POST("api/training/evaluate")
    suspend fun evaluateClass(@Body data: Map<String, Any>): BaseResponse<Any>

    /** App评价 */
    @POST("api/training/evaluateapp")
    suspend fun evaluateApp(@Body data: Map<String, Any>): BaseResponse<Any>

    /** 签到接口 */
    @GET("api/training/sceneplansing")
    suspend fun studySign(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 用户注册 */
    @GET("api/user/register")
    suspend fun register(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 检查计划需不需要支付 */
    @GET("api/user/payorder")
    suspend fun checkSafe(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 订单支付 */
    @GET("api/training/order")
    suspend fun payTrainOrder(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 订单列表 */
    @GET("api/training/orderlist")
    suspend fun orderlist(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 判断交互时长 */
    @GET("api/training/configtime")
    suspend fun getTime(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    // ===================== 继续教育 =====================
    /** 继续教育计划列表 */
    @GET("api/training/subjectlist")
    suspend fun subjectList(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 继续教育课件列表 */
    @GET("api/training/subcoursewarelist")
    suspend fun subcoursewareList(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 继续教育课件详情 */
    @GET("api/training/coursewareview")
    suspend fun subjectDetail(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 继续教育人脸识别 */
    @GET("api/training/subjectface")
    suspend fun subjectface(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 判断继续教育是否要付款 */
    @GET("api/training/subjectorder")
    suspend fun subjectpay(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 继续教育验证时长 */
    @GET("api/training/subjectstudy")
    suspend fun subjecttime(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 继续教育认证 */
    @POST("api/training/subinfo")
    suspend fun authentication(@Body data: Map<String, Any>): BaseResponse<Any>

    // ===================== 岗前培训 =====================
    /** 岗前培训计划列表 */
    @GET("api/before/subjectlist")
    suspend fun beforeList(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 岗前培训计划课件列表 */
    @GET("api/before/subcoursewarelist")
    suspend fun beforecoursewarelist(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 岗前培训计划课件详情 */
    @GET("api/before/coursewareview")
    suspend fun beforecoursewareview(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 岗前培训学习记录添加 */
    @GET("api/before/subjectstudy")
    suspend fun beforestudy(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 岗前培训人脸识别 */
    @GET("api/before/subjectface")
    suspend fun beforeface(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 培训是否需要支付 */
    @GET("api/before/orderisPay")
    suspend fun trainPayIf(): BaseResponse<Any>

    /** 培训个人支付 */
    @GET("api/before/creatOrder")
    suspend fun trainPersonPay(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 培训企业支付 */
    @GET("api/before/companyPay")
    suspend fun trainCompanyPay(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    // ===================== 日常安全培训 =====================
    /** 日常安全培训是否需要支付 */
    @GET("api/dailysafety/orderisPay")
    suspend fun orderisPay(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 日常安全培训个人支付订单 */
    @GET("api/dailysafety/creatOrder")
    suspend fun creatOrder(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 日常安全培训企业支付订单 */
    @GET("api/dailysafety/companyPay")
    suspend fun companyPay(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 日常安全培训人脸识别 */
    @GET("api/dailysafety/safeFace")
    suspend fun safeFace(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 日常安全培训课件列表 */
    @GET("api/dailysafety/coursewareList")
    suspend fun coursewareList(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 日常安全培训课件详情 */
    @GET("api/dailysafety/coursewareView")
    suspend fun coursewareView(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 日常安全培训学习记录添加 */
    @GET("api/dailysafety/safeStudy")
    suspend fun safeStudy(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    // ===================== 考试申请 =====================
    /** 考试申请分类列表 */
    @GET("api/before/examapplycate")
    suspend fun examapplycate(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 考试申请对应分类时间列表 */
    @GET("api/before/examapplylist")
    suspend fun examapplylist(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 考试申请对应分类计划详情 */
    @GET("api/before/examapplyview")
    suspend fun examapplyview(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 申请提交 */
    @POST("api/before/examapply")
    suspend fun examapply(@Body data: Map<String, Any>): BaseResponse<Any>

    /** 申请列表 */
    @GET("api/before/applyList")
    suspend fun applyList(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 考试申请准考证列表 */
    @GET("api/before/examticket")
    suspend fun examticket(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 学校列表 */
    @GET("api/before/school")
    suspend fun school(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    // ===================== 其他 =====================
    /** 人脸识别新逻辑接口 */
    @GET("api/user/startplan")
    suspend fun newCheckFace(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 检查判断是否是泰安公司的教育培训 */
    @GET("api/training/education")
    suspend fun eduList(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 驾驶员责任书签字上传 */
    @POST("api/user/singpost")
    suspend fun driverBook(@Body data: Map<String, Any>): BaseResponse<Any>

    /** 责任书是否签过字 */
    @GET("api/user/signview")
    suspend fun isDriverBook(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 疫情承诺书上传 */
    @POST("api/user/epidemicpost")
    suspend fun yiqing(@Body data: Map<String, Any>): BaseResponse<Any>

    /** 疫情承诺书是否签过字 */
    @GET("api/user/epidemicview")
    suspend fun isYiqingSign(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 上传头像(暂未使用) */
    @GET("api/user/avatar")
    suspend fun uploadImg(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 审核验证 */
    @GET("api/user/userotherinfo")
    suspend fun userotherinfo(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 人脸验证 */
    @GET("api/user/safetyadd")
    suspend fun safetyadd(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 人脸验证-考试列表 */
    @GET("api/user/examslist")
    suspend fun examslist(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 证书列表 */
    @GET("api/user/userstudyprovelist")
    suspend fun userstudyprovelist(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 学习详情列表 */
    @GET("api/user/studysafetylist")
    suspend fun studysafetylist(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 学习记录 */
    @GET("api/user/studylist")
    suspend fun studylist(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 车辆列表 */
    @GET("api/user/carlist")
    suspend fun carlist(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 搜索 */
    @GET("aggregation/app/select/searchcarlist")
    suspend fun searchcarlist(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 车辆详细信息 */
    @GET("car/app/car/carinfo")
    suspend fun carinfo(@QueryMap params: Map<String, Any>? = null): BaseResponse<Any>

    /** 获取报警列表 */
    @GET("aggregation/app/work/warningList")
    suspend fun getWarningList(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 获取报警类型 */
    @GET("system/app/dict/data/list")
    suspend fun getWarningType(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** V2-获取报警列表(按日期/类型/分页) */
    @GET("aggregation/app/work/warningList")
    suspend fun getWarningListByParms(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 查询历史记录 */
    @GET("car/app/history")
    suspend fun getHistory(@QueryMap data: Map<String, Any>? = null): BaseResponse<Any>

    /** 新增历史记录 */
    @POST("car/app/history")
    suspend fun addHistory(@Body data: Map<String, Any>): BaseResponse<Any>

    /** 分享车辆信息(首页) */
    @POST("aggregation/app/position/shareLastPosition")
    suspend fun sharePosition(@Body data: Map<String, Any>): BaseResponse<Any>
}