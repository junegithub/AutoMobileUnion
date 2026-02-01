package com.yt.car.union.net

import com.yt.car.union.net.bean.BaseResponse
import com.yt.car.union.net.bean.LoginRequest
import com.yt.car.union.net.bean.LoginResponse
import com.yt.car.union.net.bean.LoginSafeRequest
import com.yt.car.union.net.bean.LogoffRequest
import retrofit2.http.Body
import retrofit2.http.POST

import retrofit2.http.*

/**
 * 所有接口的统一定义
 * 说明：
 * 1. GET 请求使用 @QueryMap 接收参数（key-value 形式）
 * 2. POST 请求使用 @Body 接收 JSON 参数（也可根据后端要求改用 @FieldMap + FormUrlEncoded）
 * 3. URL 路径已拼接 BaseUrl，仅保留相对路径
 */
interface ApiService {
    // ===================== 登录/注销 =====================
    // 登录（auth/app/login）
    @POST("auth/app/login")
    suspend fun loginApi(@Body request: LoginRequest): BaseResponse<LoginResponse>

    // 账号密码登录（api/user/login）
    @POST("api/user/login")
    suspend fun loginApiSafe(@Body request: LoginSafeRequest): BaseResponse<LoginResponse>

    // 注销（api/user/logoff）
    @POST("api/user/logoff")
    suspend fun logoff(@Body request: LogoffRequest): BaseResponse<Boolean>

    // 退出登录（api/user/logout）
    @GET("api/user/logout")
    suspend fun loginOut(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // ===================== 用户信息 =====================
    // 获取用户详细信息（system/app/user/getInfo）
    @GET("system/app/user/getInfo")
    suspend fun getInfo(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 获取用户信息（system/app/user）
    @GET("system/app/user")
    suspend fun getUserInfo(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 获取安全培训用户信息（api/user/info）
    @GET("api/user/info")
    suspend fun getUserInfoSafe(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 修改密码（system/app/user/resetpwd）
    @GET("system/app/user/resetpwd")
    suspend fun changePassword(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 安全培训修改密码（api/user/resetpwd）
    @GET("api/user/resetpwd")
    suspend fun changePasswordSafe(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 修改昵称（system/app/user/profile）
    @GET("system/app/user/profile")
    suspend fun editNickname(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 检查密码是否修改（api/user/checkpwd）
    @GET("api/user/checkpwd")
    suspend fun checkpwd(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 用户注册（api/user/register）
    @GET("api/user/register")
    suspend fun register(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // ===================== 首页 =====================
    // 是否是莱阳博慧的用户（car/app/car/isLYBH）
    @GET("car/app/car/isLYBH")
    suspend fun isLYBH(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 首页-到期提醒（car/app/car/getExpiringInfo）
    @GET("car/app/car/getExpiringInfo")
    suspend fun getNotice(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 首页-图表数据（aggregation/app/dashboard/getInfo）
    @GET("aggregation/app/dashboard/getInfo")
    suspend fun getChartData(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // ===================== 一车一档/一人一档 =====================
    // 一车一档（car/app/file/carfile）
    @GET("car/app/file/carfile")
    suspend fun getCarInfo(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 一人一档（car/app/file/peoplefile）
    @GET("car/app/file/peoplefile")
    suspend fun getPeopleInfo(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // ===================== 车辆检查 =====================
    // 日检（api/user/cardaycheck）
    @GET("api/user/cardaycheck")
    suspend fun getCarDayCheck(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 月检（api/user/carmonthcheck）
    @GET("api/user/carmonthcheck")
    suspend fun getCarMonthCheck(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // ===================== 全图监控 =====================
    // 地图-车辆坐标（aggregation/app/dashboard/maorealtime）
    @GET("aggregation/app/dashboard/maorealtime")
    suspend fun getMapPunctuationData(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 地图-车辆坐标（api/user/realtime）
    @GET("api/user/realtime")
    suspend fun getMapPunctuationDataApi(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 车辆详细信息（aggregation/app/position/realtimeaddress）
    @GET("aggregation/app/position/realtimeaddress")
    suspend fun getMapCatInfo(@QueryMap params: Map<String, Any>): BaseResponse<Any>

    // 车辆拍照（jt808/app/jt808/photos）
    @GET("jt808/app/jt808/photos")
    suspend fun takePhoto(@QueryMap params: Map<String, Any>): BaseResponse<Any>

    // 文本信息下发（jt808/app/jt808/sendcontent）
    @GET("jt808/app/jt808/sendcontent")
    suspend fun sendcontent(@QueryMap params: Map<String, Any>): BaseResponse<Any>

    // 获取轨迹点（aggregation/app/position/track）
    @GET("aggregation/app/position/track")
    suspend fun getTrackInfoApi(@QueryMap params: Map<String, Any>): BaseResponse<Any>

    // 实时视频（api/user/video）
    @GET("api/user/video")
    suspend fun getVideoApi(@QueryMap params: Map<String, Any>): BaseResponse<Any>

    // 视频回放列表（api/user/videoplaylist）
    @GET("api/user/videoplaylist")
    suspend fun videoplaylist(@QueryMap params: Map<String, Any>): BaseResponse<Any>

    // 视频回放数据（api/user/historyplay）
    @GET("api/user/historyplay")
    suspend fun historyplay(@QueryMap params: Map<String, Any>): BaseResponse<Any>

    // 视频回放播放（api/user/playcontrol）
    @GET("api/user/playcontrol")
    suspend fun playcontrol(@QueryMap params: Map<String, Any>): BaseResponse<Any>

    // 车辆详细信息（car/app/car/carinfo）
    @GET("car/app/car/carinfo")
    suspend fun carinfo(@QueryMap params: Map<String, Any>): BaseResponse<Any>

    // ===================== 报表 =====================
    // 里程报表（aggregation/app/work/mileage）
    @GET("aggregation/app/work/mileage")
    suspend fun getMileage(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 报警报表（aggregation/app/work/warning）
    @GET("aggregation/app/work/warning")
    suspend fun getWarn(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 报警详情（aggregation/app/work/warningDetail）
    @GET("aggregation/app/work/warningDetail")
    suspend fun getWarnDetail(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 离线统计（aggregation/app/work/offline）
    @GET("aggregation/app/work/offline")
    suspend fun getOffLine(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 离线详情（aggregation/app/work/offlineDetail）
    @GET("aggregation/app/work/offlineDetail")
    suspend fun getOffLineDetail(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 安全查询（aggregation/app/work/activeWarning）
    @GET("aggregation/app/work/activeWarning")
    suspend fun getSafety(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 照片查询（aggregation/app/work/photos）
    @GET("aggregation/app/work/photos")
    suspend fun getPhotos(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 过期查询（aggregation/app/work/caroverlist）
    @GET("aggregation/app/work/caroverlist")
    suspend fun getCaroverlist(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 停车统计（aggregation/app/work/stopDetail）
    @GET("aggregation/app/work/stopDetail")
    suspend fun stopCarList(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 加油报表（aggregation/app/oil/add）
    @GET("aggregation/app/oil/add")
    suspend fun addOil(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 日油耗报表（aggregation/app/oil/day）
    @GET("aggregation/app/oil/day")
    suspend fun dayOil(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 漏油报表（aggregation/app/oil/leak）
    @GET("aggregation/app/oil/leak")
    suspend fun leakOil(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // ===================== 电子运单 =====================
    // 运单列表（api/user/waybilllist）
    @GET("api/user/waybilllist")
    suspend fun getWayBill(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 删除运单（api/user/waybilldel）
    @GET("api/user/waybilldel")
    suspend fun delWayBill(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 添加人员信息（api/user/addpeoplewaybill）
    @POST("api/user/addpeoplewaybill")
    suspend fun addpeoplewaybill(@Body data: Map<String, Any>): BaseResponse<Any>

    // 修改人员信息（api/user/peopleeditpost）
    @POST("api/user/peopleeditpost")
    suspend fun peopleeditpost(@Body data: Map<String, Any>): BaseResponse<Any>

    // 添加电子运单（api/user/addwaybill）
    @POST("api/user/addwaybill")
    suspend fun addwaybill(@Body data: Map<String, Any>): BaseResponse<Any>

    // 编辑电子运单（api/user/waybilledit）
    @GET("api/user/waybilledit")
    suspend fun waybilledit(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // ===================== 车辆管理 =====================
    // 新增车辆（car/app/car）
    @POST("car/app/car")
    suspend fun addCar(@Body data: Map<String, Any>): BaseResponse<Any>

    // 搜索机构（system/app/dept/allDept）
    @GET("system/app/dept/allDept")
    suspend fun seerachCompany(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 搜索机构（安全培训）（api/user/allcategory）
    @GET("api/user/allcategory")
    suspend fun seerachCompanysafe(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 搜索车辆（car/app/car/allcarlist）
    @GET("car/app/car/allcarlist")
    suspend fun searchCar(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 机构树（car/app/tree/ybcompanytree）
    @GET("car/app/tree/ybcompanytree")
    suspend fun getCarTreeData(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 搜索车辆列表（aggregation/app/select/searchcarlist）
    @GET("aggregation/app/select/searchcarlist")
    suspend fun searchcarlist(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 车辆列表（api/user/carlist）
    @GET("api/user/carlist")
    suspend fun carlist(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 获取车辆参数（car/app/factory/list）
    @GET("car/app/factory/list")
    suspend fun canshucar(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 获取车辆参数（安全培训）（api/user/canshucar）
    @GET("api/user/canshucar")
    suspend fun canshucarsafe(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 获取车辆类型（system/dict/data/type/dlcartype）
    @GET("system/dict/data/type/dlcartype")
    suspend fun carTypeList(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 获取查车数据（aggregation/app/dashboard/carStatusList）
    @GET("aggregation/app/dashboard/carStatusList")
    suspend fun carstatuslist(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 获取用户车牌信息（api/user/userotherinfo）
    @GET("api/user/userotherinfo")
    suspend fun getUserCarInfo(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // ===================== 设备管理 =====================
    // 设备管理列表（api/user/equipmentlist）
    @GET("api/user/equipmentlist")
    suspend fun equipmentlist(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 设备管理删除（api/user/equipmentdel）
    @GET("api/user/equipmentdel")
    suspend fun equipmentdel(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 设备管理添加（api/user/equipmentadd）
    @POST("api/user/equipmentadd")
    suspend fun equipmentadd(@Body data: Map<String, Any>): BaseResponse<Any>

    // 设备管理修改（api/user/equipmenteditpost）
    @POST("api/user/equipmenteditpost")
    suspend fun equipmenteditpost(@Body data: Map<String, Any>): BaseResponse<Any>

    // 设备列表（api/user/shebeialllist）
    @GET("api/user/shebeialllist")
    suspend fun shebeialllist(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // ===================== 进货/出货单 =====================
    // 进货单列表（api/user/joingoodlist）
    @GET("api/user/joingoodlist")
    suspend fun joingoodlist(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 进货单添加（api/user/joingoodsadd）
    @POST("api/user/joingoodsadd")
    suspend fun joingoodsadd(@Body data: Map<String, Any>): BaseResponse<Any>

    // 进货单修改（api/user/joingoodseditpost）
    @POST("api/user/joingoodseditpost")
    suspend fun joingoodseditpost(@Body data: Map<String, Any>): BaseResponse<Any>

    // 进货单删除（api/user/jiondel）
    @GET("api/user/jiondel")
    suspend fun jiondel(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 出货单列表（api/user/shipmentlist）
    @GET("api/user/shipmentlist")
    suspend fun shipmentlist(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 出货单添加（api/user/shipmentadd）
    @POST("api/user/shipmentadd")
    suspend fun shipmentadd(@Body data: Map<String, Any>): BaseResponse<Any>

    // 出货单修改（api/user/shipmenteditpost）
    @POST("api/user/shipmenteditpost")
    suspend fun shipmenteditpost(@Body data: Map<String, Any>): BaseResponse<Any>

    // 出货单删除（api/user/shipmentdel）
    @GET("api/user/shipmentdel")
    suspend fun shipmentdel(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // ===================== 维修/费用 =====================
    // 维修列表（api/user/installlog）
    @GET("api/user/installlog")
    suspend fun installlog(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 维修添加（api/user/installadd）
    @POST("api/user/installadd")
    suspend fun installadd(@Body data: Map<String, Any>): BaseResponse<Any>

    // 维修修改（api/user/installeditpost）
    @POST("api/user/installeditpost")
    suspend fun installeditpost(@Body data: Map<String, Any>): BaseResponse<Any>

    // 费用录入添加（car/app/equipment）
    @POST("car/app/equipment")
    suspend fun chargelogadd(@Body data: Map<String, Any>): BaseResponse<Any>

    // ===================== 安全培训/考试 =====================
    // 获取考试题目（api/user/safekspaperview）
    @GET("api/user/safekspaperview")
    suspend fun getquestions(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 提交答案（api/user/safekspost）
    @POST("api/user/safekspost")
    suspend fun answerSbumit(@Body data: Map<String, Any>): BaseResponse<Any>

    // 答案详情（api/user/safecankao）
    @GET("api/user/safecankao")
    suspend fun safecankao(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 查看正确答案（api/user/questionview）
    @GET("api/user/questionview")
    suspend fun questionview(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 安全培训列表（api/user/safetylist）
    @GET("api/user/safetylist")
    suspend fun safetylist(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 安全培训历史列表（api/training/oldsafetylist）
    @GET("api/training/oldsafetylist")
    suspend fun oldsafetylist(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 课件列表（api/user/coursewarelist）
    @GET("api/user/coursewarelist")
    suspend fun coursewarelist(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 课件详情（api/user/coursewareview）
    @GET("api/user/coursewareview")
    suspend fun coursewareview(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 审核验证（api/user/userotherinfo）
    @GET("api/user/userotherinfo")
    suspend fun userotherinfo(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 人脸验证（api/user/safetyadd）
    @GET("api/user/safetyadd")
    suspend fun safetyadd(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 人脸验证（api/user/examslist）
    @GET("api/user/examslist")
    suspend fun examslist(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 证书列表（api/user/userstudyprovelist）
    @GET("api/user/userstudyprovelist")
    suspend fun userstudyprovelist(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 学习详情列表（api/user/studysafetylist）
    @GET("api/user/studysafetylist")
    suspend fun studysafetylist(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 学习记录（api/user/studylist）
    @GET("api/user/studylist")
    suspend fun studylist(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 驾驶员责任书签字上传（api/user/singpost）
    @POST("api/user/singpost")
    suspend fun driverBook(@Body data: Map<String, Any>): BaseResponse<Any>

    // 责任书是否签过字（api/user/signview）
    @GET("api/user/signview")
    suspend fun isDriverBook(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 疫情承诺书上传（api/user/epidemicpost）
    @POST("api/user/epidemicpost")
    suspend fun yiqing(@Body data: Map<String, Any>): BaseResponse<Any>

    // 疫情承诺书是否签过字（api/user/epidemicview）
    @GET("api/user/epidemicview")
    suspend fun isYiqingSign(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 安全会议列表（api/training/meetinglist）
    @GET("api/training/meetinglist")
    suspend fun meetinglist(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 安全会议详情（api/training/meetingview）
    @GET("api/training/meetingview")
    suspend fun meetingview(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 安全会议签名拍照提交（api/training/singpost）
    @POST("api/training/singpost")
    suspend fun singpost(@Body data: Map<String, Any>): BaseResponse<Any>

    // 检查判断是否是泰安公司的教育培训（api/training/education）
    @GET("api/training/education")
    suspend fun eduList(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 评价课件（api/training/evaluate）
    @POST("api/training/evaluate")
    suspend fun evaluateClass(@Body data: Map<String, Any>): BaseResponse<Any>

    // App评价（api/training/evaluateapp）
    @POST("api/training/evaluateapp")
    suspend fun evaluateApp(@Body data: Map<String, Any>): BaseResponse<Any>

    // 签到接口（api/training/sceneplansing）
    @GET("api/training/sceneplansing")
    suspend fun studySign(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 检查计划需不需要支付（api/user/payorder）
    @GET("api/user/payorder")
    suspend fun checkSafe(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 订单支付（api/training/order）
    @GET("api/training/order")
    suspend fun payTrainOrder(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 订单列表（api/training/orderlist）
    @GET("api/training/orderlist")
    suspend fun orderlist(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 判断交互时长（api/training/configtime）
    @GET("api/training/configtime")
    suspend fun getTime(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // ===================== 继续教育 =====================
    // 继续教育计划列表（api/training/subjectlist）
    @GET("api/training/subjectlist")
    suspend fun subjectList(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 继续教育课件列表（api/training/subcoursewarelist）
    @GET("api/training/subcoursewarelist")
    suspend fun subcoursewareList(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 继续教育详细（api/training/coursewareview）
    @GET("api/training/coursewareview")
    suspend fun subjectDetail(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 继续教育人脸识别（api/training/subjectface）
    @GET("api/training/subjectface")
    suspend fun subjectface(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 判断继续教育是否要付款（api/training/subjectorder）
    @GET("api/training/subjectorder")
    suspend fun subjectpay(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 继续教育验证时长（api/training/subjectstudy）
    @GET("api/training/subjectstudy")
    suspend fun subjecttime(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 继续教育认证（api/training/subinfo）
    @POST("api/training/subinfo")
    suspend fun authentication(@Body data: Map<String, Any>): BaseResponse<Any>

    // ===================== 岗前培训 =====================
    // 岗前培训计划列表（api/before/subjectlist）
    @GET("api/before/subjectlist")
    suspend fun beforeList(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 岗前培训计划课件列表（api/before/subcoursewarelist）
    @GET("api/before/subcoursewarelist")
    suspend fun beforecoursewarelist(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 岗前培训计划课件详情（api/before/coursewareview）
    @GET("api/before/coursewareview")
    suspend fun beforecoursewareview(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 岗前培训学习记录添加（api/before/subjectstudy）
    @GET("api/before/subjectstudy")
    suspend fun beforestudy(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 岗前培训人脸识别（api/before/subjectface）
    @GET("api/before/subjectface")
    suspend fun beforeface(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 培训是否需要支付（api/before/orderisPay）
    @GET("api/before/orderisPay")
    suspend fun trainPayIf(): BaseResponse<Any>

    // 培训个人支付（api/before/creatOrder）
    @GET("api/before/creatOrder")
    suspend fun trainPersonPay(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 培训企业支付（api/before/companyPay）
    @GET("api/before/companyPay")
    suspend fun trainCompanyPay(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // ===================== 人脸识别新逻辑 =====================
    // 人脸识别新逻辑接口（api/user/startplan）
    @GET("api/user/startplan")
    suspend fun newCheckFace(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // ===================== 日常安全培训 =====================
    // 日常安全培训是否需要支付（api/dailysafety/orderisPay）
    @GET("api/dailysafety/orderisPay")
    suspend fun orderisPay(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 日常安全培训个人支付订单（api/dailysafety/creatOrder）
    @GET("api/dailysafety/creatOrder")
    suspend fun creatOrder(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 日常安全培训企业支付订单（api/dailysafety/companyPay）
    @GET("api/dailysafety/companyPay")
    suspend fun companyPay(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 日常安全培训人脸识别（api/dailysafety/safeFace）
    @GET("api/dailysafety/safeFace")
    suspend fun safeFace(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 日常安全培训课件列表（api/dailysafety/coursewareList）
    @GET("api/dailysafety/coursewareList")
    suspend fun coursewareList(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 日常安全培训课件详情（api/dailysafety/coursewareView）
    @GET("api/dailysafety/coursewareView")
    suspend fun coursewareView(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 日常安全培训学习记录添加（api/dailysafety/safeStudy）
    @GET("api/dailysafety/safeStudy")
    suspend fun safeStudy(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // ===================== 考试申请 =====================
    // 考试申请分类列表（api/before/examapplycate）
    @GET("api/before/examapplycate")
    suspend fun examapplycate(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 考试申请对应分类时间列表（api/before/examapplylist）
    @GET("api/before/examapplylist")
    suspend fun examapplylist(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 考试申请对应分类计划详情（api/before/examapplyview）
    @GET("api/before/examapplyview")
    suspend fun examapplyview(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 申请提交（api/before/examapply）
    @POST("api/before/examapply")
    suspend fun examapply(@Body data: Map<String, Any>): BaseResponse<Any>

    // 申请列表（api/before/applyList）
    @GET("api/before/applyList")
    suspend fun applyList(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 考试申请准考证列表（api/before/examticket）
    @GET("api/before/examticket")
    suspend fun examticket(@QueryMap data: Map<String, Any>): BaseResponse<Any>

    // 学校列表（api/before/school）
    @GET("api/before/school")
    suspend fun school(@QueryMap data: Map<String, Any>): BaseResponse<Any>
}