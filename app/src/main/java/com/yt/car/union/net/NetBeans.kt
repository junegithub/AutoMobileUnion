package com.yt.car.union.net

// Data Classes
data class LoginRequest(
    val account: String,
    val password: String
)

data class LoginResponse(
    val msg: String?,
    val code: Int,
    val data: LoginData?
)

data class LoginData(
    val otherinfo: OtherInfo?,
    val userState: Int?,
    val userid: Long?,
    val userinfo: UserInfo?
)

data class OtherInfo(
    val type: Int
)

data class UserInfo(
    val token: String?
)

data class CarInfoResponse(
    val msg: String?,
    val code: Int,
    val data: CarInfo?
)

data class CarInfo(
    val deptName: String,
    val firstonlinetime: String?,
    val creditcode: String,
    val intime: String,
    val productdate: String,
    val cityId: String,
    val realsim: String,
    val carnumcolor: String,
    val drivercardeId: String,
    val provice: String,
    val id: String,
    val frameno: String,
    val phonecheck: String,
    val boxw: Double,
    val oiltype: String,
    val tcnum: String,
    val deptId: String,
    val dlsumweight: Double,
    val validtime: String,
    val boxl: Double,
    val startmileage: Int,
    val deleteTime: String?,
    val phone: String,
    val cardwarning: String,
    val bussinessArea: String,
    val radioway: String,
    val onlineMonthLog3: String,
    val holdertype_text: String,
    val onlineMonthLog1: String,
    val onlineMonthLog2: String,
    val dlcartype: String,
    val dlboxh: Double,
    val simtype: String,
    val city: String,
    val bcategoryId: String,
    val fatiguetime: String?,
    val dlboxl: Double,
    val carnum: String,
    val insurancedate: String,
    val dlboxw: Double,
    val intercomway: String,
    val paylongtime: String,
    val bcategory: BrandInfo?,
    val carlong: String?,
    val kindId: String,
    val tiresnum: String,
    val proviceId: String,
    val dlenginenum: String,
    val buytype: String,
    val paytime: String,
    val userId: String,
    val dlimages: String,
    val bodyimages: String,
    val areaId: String,
    val waynums: String,
    val contacts: String,
    val buytype_text: String,
    val dlcheckweight: Double,
    val fueltype: String,
    val bcategoryName: String,
    val num: String,
    val fueltype_text: String,
    val licensenum: String?,
    val isoil: String,
    val tcrange: String,
    val makerName: String,
    val enginetype: String,
    val holdertype: String,
    val area: String,
    val affiliationcontact: String,
    val platecolor: String,
    val weight: Double,
    val dlcartype_text: String,
    val ttotalmass: Double,
    val tiresize: String,
    val volume: String,
    val tcertificateno: String,
    val axisnum: String,
    val dlusedate: String,
    val skus: List<CarSku>,
    val warningspeed: String,
    val carmodeltype: String,
    val remark: String,
    val drivercardId: String,
    val playerMode: Int,
    val lkm: Double,
    val qtregimages: String,
    val boxh: Double,
    val icbusivaliddate: String,
    val sim: String,
    val kindName: String,
    val checkvalidtime: String,
    val Industrytype_text: String,
    val carstatus: String,
    val affiliationphone: String,
    val makerId: String,
    val industrytype: String,
    val trailernum: String?,
    val jointype: String,
    val userName: String?,
    val oilAddVal: Int,
    val tccheckdate: String,
    val isVideoCar: Boolean,
    val category: CategoryInfo?,
    val audiocode: String
)

data class BrandInfo(
    val name: String,
    val nickname: String,
    val type: String
)

data class CarSku(
    val type: String,
    val carId: String,
    val money: Double,
    val carIds: String?
)

data class CategoryInfo(
    val name: String,
    val nickname: String,
    val type: String
)

data class IsLYBHResponse(
    val msg: String?,
    val code: Int,
    val data: Boolean?
)

data class OilDayReportResponse(
    val msg: String?,
    val code: Int,
    val data: OilDayReportData?
)

data class OilDayReportData(
    val total: Int,
    val list: List<OilDayReportItem>
)

data class OilDayReportItem(
    val carId: String,
    val deptId: String,
    val carNum: String,
    val deptName: String,
    val sim: String,
    val mileage: Double,
    val oil: Double,
    val percent: Double
)

data class LeakReportResponse(
    val msg: String?,
    val code: Int,
    val data: LeakReportData?
)

data class LeakReportData(
    val total: Int,
    val list: List<LeakReportItem>
)

data class LeakReportItem(
    val ts: String?,
    val carId: String,
    val carNum: String,
    val deptId: String,
    val deptName: String,
    val sim: String,
    val num: Int,
    val oil: Double
)

data class MapPositionResponse(
    val msg: String?,
    val code: Int,
    val data: MapPositionData?
)

data class MapPositionData(
    val mapnum: Int,
    val total: Int,
    val latitude: String,
    val longitude: String,
    val list: List<MapPositionItem>
)

data class MapPositionItem(
    val dlcartype: String,
    val deptName: String?,
    val altitude: String?,
    val latitude: Double,
    val rotation: String,
    val id: String,
    val carnum: String,
    val longitude: Double,
    val status: Int,
    val direction: String
)

data class RealTimeAddressResponse(
    val msg: String?,
    val code: Int,
    val data: RealTimeAddressData?
)

data class RealTimeAddressData(
    val address: String,
    val carinfo: RealTimeCarInfo
)

data class RealTimeCarInfo(
    val dlcartype: String,
    val gpscomutime_text: String,
    val bcategoryName: String,
    val latitude: Double,
    val milege: Double,
    val todayMileage: Double,
    val carnum: String,
    val speed: Double,
    val expired: Boolean,
    val gpsloctime_text: String,
    val drivercard_name: String?,
    val gpsstatus: String,
    val stopTime: String,
    val id: String,
    val longitude: Double,
    val direction: Int,
    val alarmmsg: String,
    val temperaturestr: String,
    val oil1: String,
    val baidulatitude: Double,
    val baidulongitude: Double,
    val online: Boolean,
    val categoryname: String,
    val contacts: String,
    val status: String
)

data class SearchHistoryRequest(
    val content: String
)

data class SearchHistoryResponse(
    val msg: String?,
    val code: Int,
    val data: Int
)

data class TreeNodeResponse(
    val msg: String?,
    val code: Int,
    val data: List<TreeNode>
)

data class TreeNode(
    val id: String,
    val realId: String,
    val name: String,
    val leaf: Boolean?,
    val pid: String,
    val totalNum: Int,
    val onlineNum: Int,
    val ancestors: String,
    val children: List<TreeNode>?
)

data class CarStatusListResponse(
    val msg: String?,
    val code: Int,
    val data: CarStatusListData?
)

data class CarStatusListData(
    val all: Int,
    val drive: Int,
    val stop: Int,
    val offline: Int,
    val overSpeed: Int,
    val tired: Int,
    val other: Int,
    val expired: Int
)

data class CarStatusDetailResponse(
    val msg: String?,
    val code: Int,
    val data: List<CarStatusDetailItem>
)

data class CarStatusDetailItem(
    val carId: String,
    val carNum: String,
    val ts: String,
    val gpsTime: String,
    val statusFlag: String,
    val alarmFlag: String,
    val videoRelateAlarm: String,
    val speed: Double,
    val statusString: String,
    val lon: Double,
    val lat: Double,
    val position: String,
    val driveCardId: String,
    val driveCardName: String,
    val deptId: String,
    val deptName: String,
    val phone: String,
    val offNum: String,
    val altitude: Int,
    val direction: Int
)

data class SearchCarTypeResponse(
    val msg: String?,
    val code: Int,
    val data: SearchCarTypeData?
)

data class SearchCarTypeData(
    val count: SearchCarCount,
    val list: List<SearchCarItem>
)

data class SearchCarCount(
    val all: Int,
    val driving: Int,
    val stop: Int,
    val offline: Int,
    val expired: Int
)

data class SearchCarItem(
    val carId: String,
    val carNum: String
)

data class DashboardInfoResponse(
    val msg: String?,
    val code: Int,
    val data: DashboardInfoData?
)

data class DashboardInfoData(
    val caroperate: CarOperateInfo,
    val carwarning: List<WarningTypeInfo>
)

data class CarOperateInfo(
    val arrearsnum: Int,
    val caroperatenum: Int,
    val carrepairnum: Int,
    val carstopnum: Int
)

data class WarningTypeInfo(
    val data: Int,
    val name: String
)

data class OilAddReportResponse(
    val msg: String?,
    val code: Int,
    val data: OilAddReportData?
)

data class OilAddReportData(
    val total: Int,
    val list: List<OilAddReportItem>
)

data class OilAddReportItem(
    val ts: String?,
    val carId: String,
    val carNum: String,
    val deptId: String,
    val deptName: String,
    val sim: String,
    val num: Int,
    val oil: Double
)

data class ActiveWarningResponse(
    val msg: String?,
    val code: Int,
    val data: ActiveWarningData?
)

data class ActiveWarningData(
    val total: Int,
    val list: List<ActiveWarningItem>
)

data class ActiveWarningItem(
    val num: Int,
    val name: String,
    val warningType: Int
)

data class ExpiredCarResponse(
    val msg: String?,
    val code: Int,
    val data: ExpiredCarData?
)

data class ExpiredCarData(
    val total: Int,
    val list: List<ExpiredCarItem>
)

data class ExpiredCarItem(
    val carId: String,
    val carNum: String,
    val expiredDate: String
)

data class MileageResponse(
    val msg: String?,
    val code: Int,
    val data: MileageData?
)

data class MileageData(
    val total: Int,
    val list: List<MileageItem>
)

data class MileageItem(
    val carId: String,
    val ts: String,
    val time: String,
    val carnum: String,
    val mileage: Double
)

data class OfflineReportResponse(
    val msg: String?,
    val code: Int,
    val data: OfflineReportData?
)

data class OfflineReportData(
    val total: Int,
    val list: List<OfflineReportItem>
)

data class OfflineReportItem(
    val carId: String,
    val carNum: String,
    val deptId: String,
    val deptName: String,
    val offline: Int,
    val online: Int
)

data class PhotoReportResponse(
    val msg: String?,
    val code: Int,
    val data: PhotoReportData?
)

data class PhotoReportData(
    val total: Int,
    val list: List<PhotoReportItem>
)

data class PhotoReportItem(
    val ts: String,
    val time: String?,
    val carNum: String,
    val url: String,
    val carId: String,
    val lon: Double,
    val lat: Double,
    val address: String,
    val bucketName: String,
    val objectName: String
)

data class StopDetailResponse(
    val msg: String?,
    val code: Int,
    val data: StopDetailData?
)

data class StopDetailData(
    val total: Int,
    val list: List<StopDetailItem>
)

data class StopDetailItem(
    val carId: String,
    val ts: String,
    val time: String,
    val carnum: String,
    val startTime: String,
    val endTime: String,
    val duration: String,
    val lon: Double,
    val lat: Double,
    val position: String
)

data class WarningReportResponse(
    val msg: String?,
    val code: Int,
    val data: WarningReportData?
)

data class WarningReportData(
    val total: Int,
    val list: List<WarningReportItem>
)

data class WarningReportItem(
    val num: Int,
    val name: String,
    val warningType: Int
)

data class WarningDetailResponse(
    val msg: String?,
    val code: Int,
    val data: WarningDetailData?
)

data class WarningDetailData(
    val total: Int,
    val list: List<WarningDetailItem>
)

data class WarningDetailItem(
    val id: String,
    val carId: String,
    val carNum: String,
    val deptId: String,
    val deptName: String,
    val ts: String,
    val speed: Double,
    val lon: Double,
    val lat: Double,
    val position: String
)

data class ShareLastPositionRequest(
    val carId: Long
)

data class ShareLastPositionResponse(
    val msg: String?,
    val code: Int,
    val data: String?
)

data class TrackResponse(
    val msg: String?,
    val code: Int,
    val data: TrackData?
)

data class TrackData(
    val gotime: String,
    val carinfo: TrackCarInfo,
    val stop: List<TrackStopPoint>,
    val driveSecond: String,
    val postlist: List<TrackPosition>,
    val avgspeed: String,
    val maxspeed: String,
    val stopSecond: String,
    val stoptime: String,
    val mileage: String
)

data class TrackCarInfo(
    val categoryname: String,
    val carnum: String,
    val contacts: String
)

data class TrackStopPoint(
    val address: String,
    val lng: Double,
    val endtime: String,
    val stoptime: String,
    val gpstime: String,
    val lat: Double
)

data class TrackPosition(
    val address: String,
    val lng: Double,
    val statusFlagString: String,
    val time: String,
    val gpstime: String,
    val speed: String,
    val lat: Double,
    val direction: Int
)

data class VideoInfoResponse(
    val msg: String?,
    val code: Int,
    val data: VideoInfoData?
)

data class VideoInfoData(
    val waynums: List<VideoChannel>,
    val category_id: String,
    val sim: String,
    val android: Int,
    val versioninfo: Int,
    val ios: Int,
    val version: Int
)

data class VideoChannel(
    val realId: String?,
    val name: String?,
    val wayNumCode: String,
    val wayNumLabel: String
)

data class SendContentRequest(
    val car_id: String,
    val content: String
)

data class SendContentResponse(
    val msg: String?,
    val code: Int
)

data class TakePhotoResponse(
    val msg: String?,
    val code: Int
)

data class LogoutResponse(
    val msg: String?,
    val code: Int
)

data class UserLoginRequest(
    val account: String,
    val password: String
)

data class UserLoginResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: UserLoginData?
)

data class UserLoginData(
    val userinfo: UserInfo?,
    val userid: String,
    val timestamp: Long,
    val webtoken: String,
    val token: String,
    val userState: Int,
    val otherinfo: OtherUserInfo?,
    val eachtime: Long
)

data class OtherUserInfo(
    val id: Int,
    val group_id: Int,
    val username: String,
    val nickname: String,
    val password: String,
    val salt: String,
    val email: String,
    val cardmun: String,
    val mobile: String,
    val type: String,
    val category_id: Int,
    val avatar: String,
    val avtvartime: Long?,
    val level: Int,
    val gender: Int,
    val birthday: String?,
    val bio: String,
    val money: String,
    val score: Int,
    val successions: Int,
    val maxsuccessions: Int,
    val prevtime: Long,
    val logintime: Long,
    val loginip: String,
    val loginfailure: Int,
    val joinip: String,
    val jointime: Long,
    val createtime: Long,
    val updatetime: Long,
    val deletetime: String?,
    val token: String,
    val status: String,
    val verification: String,
    val yzstatus: String,
    val stype: String,
    val car_id: Int,
    val carnum: String,
    val signtype: String,
    val signtime: Long,
    val signfile: String,
    val epidemictype: String,
    val epidemictime: Long,
    val epidemicfile: String,
    val jobtype: String?,
    val provice_id: Int,
    val city_id: Int,
    val area_id: Int,
    val endtime: Long,
    val openid: String?,
    val pid: Int,
    val cardimg: String?,
    val backcardimg: String?,
    val practicetime: Long?,
    val year: Int?,
    val jxstatus: String,
    val validtime: Long,
    val renzhen: String,
    val otherinfo: String?,
    val renzhengtime: String?,
    val school_id: Int,
    val usualtime: Long,
    val usualpaytype: String,
    val usualpaytime: Long,
    val check_adminid: Int,
    val check_name: String?
)

data class SignViewResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: SignViewData?
)

data class SignViewData(
    val id: Int,
    val group_id: Int,
    val username: String,
    val nickname: String,
    val password: String,
    val salt: String,
    val email: String,
    val cardmun: String,
    val mobile: String,
    val type: String,
    val category_id: Int,
    val avatar: String,
    val avtvartime: String?,
    val level: Int,
    val gender: Int,
    val birthday: String?,
    val bio: String,
    val money: String,
    val score: Int,
    val successions: Int,
    val maxsuccessions: Int,
    val prevtime: Long,
    val logintime: Long,
    val loginip: String,
    val loginfailure: Int,
    val joinip: String,
    val jointime: Long,
    val createtime: Long,
    val updatetime: Long,
    val deletetime: String?,
    val token: String,
    val status: String,
    val verification: String,
    val yzstatus: String,
    val stype: String,
    val car_id: Int,
    val carnum: String,
    val signtype: String,
    val signtime: Long,
    val signfile: String,
    val epidemictype: String,
    val epidemictime: Long,
    val epidemicfile: String,
    val jobtype: String?,
    val provice_id: Int,
    val city_id: Int,
    val area_id: Int,
    val endtime: Long,
    val openid: String?,
    val pid: Int,
    val cardimg: String?,
    val backcardimg: String?,
    val practicetime: Long?,
    val year: Int?,
    val jxstatus: String,
    val validtime: Long,
    val qualificationId: String?,
    val renzhen: String,
    val otherinfo: String?,
    val renzhengtime: String?,
    val school_id: Int,
    val usualtime: Long,
    val usualpaytype: String,
    val usualpaytime: Long,
    val check_adminid: Int,
    val check_name: String?,
    val ischange: Int,
    val areacode: String
)

data class UserInfoResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: UserInfoData?
)

data class UserInfoData(
    val info: UserInfoDetail,
    val category: CategoryDetail,
    val year_money: String,
    val year: YearMoney,
    val notice: Int
)

data class UserInfoDetail(
    val id: Int,
    val group_id: Int,
    val username: String,
    val nickname: String,
    val email: String,
    val cardmun: String,
    val mobile: String,
    val type: String,
    val category_id: Int,
    val avatar: String,
    val avtvartime: String?,
    val level: Int,
    val gender: Int,
    val birthday: String?,
    val bio: String,
    val money: String,
    val score: Int,
    val successions: Int,
    val maxsuccessions: Int,
    val prevtime: Long,
    val logintime: Long,
    val loginip: String,
    val loginfailure: Int,
    val joinip: String,
    val jointime: Long,
    val createtime: Long,
    val updatetime: Long,
    val deletetime: String?,
    val token: String,
    val status: String,
    val verification: String,
    val yzstatus: String,
    val stype: String,
    val car_id: Int,
    val carnum: String,
    val signtype: String,
    val signtime: Long,
    val signfile: String,
    val epidemictype: String,
    val epidemictime: Long,
    val epidemicfile: String,
    val jobtype: String?,
    val provice_id: Int,
    val city_id: Int,
    val area_id: Int,
    val endtime: Long,
    val openid: String?,
    val pid: Int,
    val cardimg: String?,
    val backcardimg: String?,
    val practicetime: Long?,
    val fristpracticetime: String?,
    val year: String?,
    val jxstatus: String,
    val validtime: Long,
    val qualificationId: String?,
    val renzhen: String,
    val otherinfo: String?,
    val renzhengtime: String?,
    val school_id: Int,
    val usualtime: Long,
    val usualpaytype: String,
    val usualpaytime: Long,
    val check_adminid: Int,
    val check_name: String?,
    val password: String,
    val salt: String
)

data class CategoryDetail(
    val id: Int,
    val pid: Int,
    val type: String,
    val name: String,
    val nickname: String,
    val flag: String,
    val image: String,
    val keywords: String,
    val description: String,
    val diyname: String,
    val createtime: String?,
    val updatetime: Long,
    val weigh: Int,
    val status: String,
    val contacts: String,
    val phone: String,
    val email: String,
    val images: String,
    val managementtype: String,
    val areacode: String,
    val level: Int,
    val warningwb: String,
    val connection809_id: Int,
    val longtime: String,
    val numbers: Int,
    val robotkey: String?,
    val isuserpay: String,
    val carnums: Int,
    val safetype: String
)

data class YearMoney(
    val id: Int,
    val money: String,
    val provice_id: Int,
    val city_id: Int,
    val area_id: Int,
    val createtime: Long
)

data class UserOtherInfoResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: OtherUserInfo?
)

data class EpidemicViewResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: EpidemicViewData?
)

data class EpidemicViewData(
    val epidemictype: String,
    val epidemictime: String,
    val epidemicfile: String,
    val category: String,
    val category_id: Int,
    val areacode: String
)

data class TravelLogResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: TravelLogData?
)

data class TravelLogData(
    val rows: String?,
    val list: List<TravelLogItem>
)

data class TravelLogItem(
    val id: Int,
    val car_id: Int,
    val driver_name: String,
    val addtime: String,
    val carnum: String,
    val user_id: Int,
    val type: Int,
    val copilot_name: String,
    val weather: String,
    val temperature: String,
    val load: String,
    val real_load: String,
    val goods_name: String,
    val gotime: String,
    val gettime: Int,
    val start_address: String,
    val end_address: String,
    val mileage: String,
    val sresult: Int,
    val groad: String,
    val gresult: String,
    val stopresult: String,
    val stopaddress: String,
    val stoptime: String,
    val eresult: Int,
    val dsingimg: String,
    val staus: String,
    val updatetime: String,
    val ysingimg: String
)

data class CompanyListResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: CompanyListData?
)

data class CompanyListData(
    val total: Int,
    val rows: CompanyListRow,
    val allnum: Int
)

data class CompanyListRow(
    val id: Int,
    val title: String,
    val content: String,
    val createtime: String,
    val admin_id: String,
    val category_id: String,
    val company: String,
    val status: String,
    val starttime: String,
    val endtime: String,
    val weigh: String,
    val istuijian: String
)

data class SafetyListResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: SafetyListData?
)

data class SafetyListData(
    val total: Int,
    val rows: List<Any>,
    val allnum: Int,
    val publicList: List<SafetyPlan>
)

data class SafetyPlan(
    val id: Int,
    val name: String,
    val money: String,
    val createtime: Long,
    val updatetime: Long,
    val longtime: Int,
    val subject_id: String,
    val starttime: Long,
    val endtime: Long,
    val overnums: Int,
    val sublongtime: Int,
    val isface: String,
    val industrytype: String,
    val provice_id: Int,
    val city_id: Int,
    val area_id: Int,
    val training_exams_id: Int,
    val buynums: Int,
    val checktime: Int,
    val issign: String,
    val type: String,
    val package_id: Int,
    val courtype: String,
    val studytype: Int,
    val slongtime: Int,
    val progress: Int,
    val paystatus: Int,
    val imgurl: String,
    val joinexams: Int
)

data class DailySafetyOrderResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: DailySafetyOrderData?
)

data class DailySafetyOrderData(
    val training_publicplan: TrainingPublicPlan,
    val money: String,
    val usualpaytype: String
)

data class TrainingPublicPlan(
    val id: Int,
    val name: String,
    val money: String,
    val createtime: Long,
    val updatetime: Long,
    val longtime: Int,
    val subject_id: String,
    val starttime: Long,
    val endtime: Long,
    val overnums: Int,
    val sublongtime: Int,
    val isface: String,
    val industrytype: String,
    val provice_id: Int,
    val city_id: Int,
    val area_id: Int,
    val training_exams_id: Int,
    val buynums: Int,
    val checktime: Int,
    val issign: String,
    val type: String,
    val package_id: Int,
    val courtype: String
)

data class OldSafetyListResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: OldSafetyListData?
)

data class OldSafetyListData(
    val total: Int,
    val rows: List<OldSafetyPlan>,
    val allnum: Int
)

data class OldSafetyPlan(
    val id: Int,
    val name: String,
    val longtime: Int,
    val subject_id: String,
    val suser_id: Int,
    val user_id: String,
    val training_exams_id: Int,
    val category_id: Int,
    val starttime: Long,
    val endtime: Long,
    val type: String,
    val jionnum: Int,
    val sublongtime: Int,
    val paytype: String,
    val issing: String,
    val isface: String,
    val checktime: Int,
    val isopen: String,
    val year_id: Int,
    val otherid: String,
    val package_id: Int,
    val courtype: String,
    val eva_info: String?,
    val studytype: Int,
    val progress: Int,
    val joinexams: Int
)

data class CoursewareListResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: CoursewareListData?
)

data class CoursewareListData(
    val total: Int,
    val list: List<CoursewareItem>,
    val row: TrainingPublicPlan,
    val allnum: Int,
    val isstart: Int,
    val isend: Int
)

data class CoursewareItem(
    val id: Int,
    val name: String,
    val type: Int,
    val imgurl: String,
    val videourl: String,
    val longtime: Int,
    val createtime: Int,
    val content: String,
    val category_id: Int,
    val highvideourl: String,
    val standardvideourl: String,
    val company_id: Int,
    val company_money: Int,
    val user_money: Int,
    val cardnum: Int,
    val endtime: Int,
    val studytype: Int,
    val progress: Int,
    val studytime: Int,
    val type_text: String
)

data class CoursewareViewResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: CoursewareViewData?
)

data class CoursewareViewData(
    val row: TrainingPublicPlan,
    val longtime: Int,
    val courrow: CoursewareItem
)

data class ConfigTimeResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: Int
)

data class SafeStudyResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: SafeStudyData?
)

data class SafeStudyData(
    val nextsubject_id: Int,
    val isend: Int
)

data class ExamViewResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: ExamViewData?
)

data class ExamViewData(
    val questions: List<ExamQuestion>,
    val exams_id: Int,
    val typeList: TypeList
)

data class ExamQuestion(
    val questionID: Int,
    val fldName: String,
    val fldAnswer: String?,
    val questionType: Int,
    val QuestionOptionList: List<QuestionOption>
)

data class QuestionOption(
    val fldOptionIndex: String,
    val fldOptionText: String
)

data class TypeList(
    val `1`: String,
    val `2`: String,
    val `3`: String
)

data class UploadFileResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: UploadFileData?
)

data class UploadFileData(
    val url: String
)

data class SubmitExamRequest(
    val answer: List<String>,
    val exams_id: String,
    val training_publicplan_id: String,
    val imgurl: String
)

data class SubmitExamResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: String
)

data class ExamResultResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: ExamResultData?
)

data class ExamResultData(
    val questions: List<ExamResultQuestion>,
    val info: ExamInfo,
    val row: ExamRow,
    val passtype: Boolean,
    val typeList: TypeList
)

data class ExamResultQuestion(
    val id: Int,
    val admin_id: Int,
    val subject_id: Int,
    val type: String,
    val question: String,
    val selectdata: List<SelectData>,
    val selectnumber: Int,
    val answer: String,
    val describe: String,
    val level: String,
    val status: String,
    val createtime: Int,
    val updatetime: Int,
    val deletetime: String?,
    val annex: String,
    val company_id: Int,
    val uanswercolor: String,
    val uanswer: String
)

data class SelectData(
    val key: String,
    val value: String,
    val checked: String?
)

data class ExamInfo(
    val id: Int,
    val user_id: Int,
    val questionsdata: String,
    val answersdata: String,
    val real_answersdata: String,
    val scorelistdata: String?,
    val score: Int,
    val status: String,
    val usetime: Int,
    val starttime: Int,
    val lasttime: Int,
    val exams_id: Int,
    val training_safetyplan_id: Int,
    val imgurl: String,
    val subject_id: String?,
    val training_publicplan_id: Int,
    val training_before_id: Int,
    val status_text: String,
    val starttime_text: String,
    val lasttime_text: String
)

data class ExamRow(
    val id: Int,
    val category_id: String,
    val admin_id: Int,
    val subject_id: String,
    val exam_name: String,
    val settingdata: String,
    val questionsdata: String,
    val pass: Int,
    val score: Int,
    val type: String,
    val keyword: String,
    val status: Int,
    val createtime: Int,
    val updatetime: Int,
    val deletetime: String?,
    val starttime: Int,
    val endtime: Int,
    val longtime: Int,
    val training_safetyplan_id: Int,
    val training_subjectplan_id: String?,
    val training_beforeplan_id: String?,
    val training_publicplan_id: String?,
    val company_id: Int,
    val type_text: String
)

data class QuestionViewResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: List<QuestionViewItem>
)

data class QuestionViewItem(
    val questionID: Int,
    val fldName: String,
    val fldAnswer: String,
    val questionType: Int,
    val QuestionOptionList: List<QuestionOption>
)

data class CreateOrderResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: String?
)

data class OrderListResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: OrderListData?
)

data class OrderListData(
    val total: Int,
    val list: List<OrderItem>
)

data class OrderItem(
    val id: Int,
    val out_trade_no: String,
    val amount: Int,
    val type: String,
    val subject: String,
    val createtime: String,
    val paytime: String,
    val courseware_id: Int,
    val user_id: Int,
    val training_sceneplan_id: Int,
    val status: String,
    val category_id: Int,
    val subject_id: Int,
    val refundtime: String?,
    val training_publicplan_id: Int,
    val training_beforeplan_id: Int,
    val question_category_id: Int,
    val tow_category_id: Int,
    val year_id: String,
    val courseware: String?,
    val paystatus: String
)

data class FaceResponse(
    val data: FaceData?,
    val code: String,
    val msg: String,
    val time: String
)

data class FaceData(
    val isjump: Int,
    val issing: String,
    val training_exams_id: String,
    val score: String,
    val face_list: FaceList?
)

data class FaceList(
    val face_token: String
)

data class TwoListResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: TwoListData?
)

data class TwoListData(
    val user_id: Int,
    val nickname: String,
    val stype: String,
    val question_count: Int,
    val user_exam_id: Int,
    val answer_count: Int,
    val category_list: CategoryList
)

data class CategoryList(
    val `107`: CategoryListItem
)

data class CategoryListItem(
    val category_name: String,
    val question_count: Int,
    val answer_count: Int,
    val user_exam_id: Int,
    val money: Int
)

data class TwoOrderPayResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: TwoOrderPayData?
)

data class TwoOrderPayData(
    val question_category: QuestionCategory,
    val money: Int
)

data class QuestionCategory(
    val id: Int,
    val pid: Int,
    val name: String,
    val createtime: Long,
    val money: Int,
    val provice_id: Int
)

data class SelectTwoQuestionListResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: SelectTwoQuestionListData?
)

data class SelectTwoQuestionListData(
    val question_count: Int,
    val answer_count: Int,
    val right_count: Int,
    val wrong_count: Int,
    val question_list: QuestionList
)

data class QuestionList(
    val `1`: QuestionItem
)

data class QuestionItem(
    val question_id: Int,
    val is_right: String
)

data class StartTwoAnswerRequest(
    val from: String,
    val user_category_id: Int,
    val question_category_id: Int,
    val question_id: Int
)

data class StartTwoAnswerResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: StartTwoAnswerData?
)

data class StartTwoAnswerData(
    val user_exam_id: Int,
    val question_count: Int,
    val right_count: Int,
    val wrong_count: Int,
    val answer_count: Int,
    val question: StartTwoAnswerQuestion
)

data class StartTwoAnswerQuestion(
    val id: Int,
    val admin_id: Int,
    val type: String,
    val category_id: Int,
    val question: String,
    val selectdata: List<QuestionOption>,
    val selectnumber: Int,
    val answer: String,
    val describe: String,
    val level: String,
    val status: String,
    val createtime: Int,
    val updatetime: Int,
    val deletetime: String?,
    val annex: String,
    val stype: Int
)

data class UpdateTwoQuestionRequest(
    val from: String,
    val user_category_id: Int,
    val question_category_id: Int,
    val question_id: Int
)

data class CreateTwoOrderResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: String
)

data class MeetingListResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: MeetingListData?
)

data class MeetingListData(
    val total: Int,
    val rows: List<MeetingItem>,
    val allnum: Int
)

data class MeetingItem(
    val id: Int,
    val name: String,
    val suser_id: Int,
    val type: String,
    val imgurl: String,
    val content: String,
    val user_id: String,
    val category_id: Int,
    val starttime: String,
    val endtime: String,
    val jionnum: Int,
    val address: String,
    val status: String,
    val longitude: String?,
    val latitude: String?,
    val singtype: String,
    val summarytype: String?,
    val singfile: String?,
    val summary: String?,
    val summaryfile: String?,
    val uppersummary: String?,
    val upperfile: String?,
    val downsummary: String?,
    val downfile: String?,
    val othername: String?,
    val othercongtent: String?,
    val meettype: String,
    val studytype: Int
)

data class MeetingViewResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: MeetingViewData?
)

data class MeetingViewData(
    val id: Int,
    val name: String,
    val suser_id: Int,
    val type: String,
    val imgurl: List<String>,
    val content: String,
    val user_id: String,
    val category_id: Int,
    val starttime: String,
    val endtime: String,
    val jionnum: Int,
    val address: String,
    val status: String,
    val longitude: String?,
    val latitude: String?,
    val singtype: String,
    val summarytype: String?,
    val singfile: String?,
    val summary: String?,
    val summaryfile: String?,
    val uppersummary: String?,
    val upperfile: String?,
    val downsummary: String?,
    val downfile: String?,
    val othername: String?,
    val othercongtent: String?,
    val meettype: String,
    val studytype: Int,
    val signfile: List<Any>
)

data class SubjectListResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: SubjectListData?
)

data class SubjectListData(
    val total: Int,
    val rows: List<SubjectItem>,
    val allnum: Int
)

data class SubjectItem(
    val id: Int,
    val name: String,
    val category_id: Int,
    val money: Int,
    val createtime: Long,
    val updatetime: Long,
    val deletetime: String?,
    val training_exams_id: Int,
    val type: String,
    val number: Int,
    val studytype: Int,
    val slongtime: String,
    val progress: Int,
    val examtype: Int,
    val joinexams: Int,
    val jxstatus: String,
    val paystatus: Int
)

data class SubjectOrderResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: SubjectOrderData?
)

data class SubjectOrderData(
    val training_subjectplan: SubjectPlan,
    val money: Int
)

data class SubjectPlan(
    val id: Int,
    val name: String,
    val category_id: Int,
    val money: Int,
    val createtime: Long,
    val updatetime: Long,
    val deletetime: String?,
    val training_exams_id: Int
)

data class SubCoursewareListResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: SubCoursewareListData?
)

data class SubCoursewareListData(
    val total: Int,
    val list: List<CoursewareItem>,
    val row: SubjectPlanDetail,
    val allnum: Int,
    val isstart: Int,
    val isend: Int
)

data class SubjectPlanDetail(
    val id: Int,
    val name: String,
    val money: String,
    val createtime: Long,
    val updatetime: Long,
    val longtime: Int,
    val subject_id: String,
    val starttime: Long,
    val endtime: Long,
    val overnums: Int,
    val sublongtime: Int,
    val isface: String,
    val industrytype: String,
    val provice_id: Int,
    val city_id: Int,
    val area_id: Int,
    val training_exams_id: Int,
    val buynums: Int,
    val checktime: Int,
    val issign: String,
    val type: String,
    val package_id: Int,
    val courtype: String,
    val studytype: Int,
    val progress: Int,
    val ksnum: Double
)

data class SubjectStudyResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: SubjectStudyData?
)

data class SubjectStudyData(
    val nextsubject_id: Int,
    val isend: Int,
    val training_exams_id: Int
)

data class BeforeOrderPayResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: BeforeOrderPayData?
)

data class BeforeOrderPayData(
    val money: String
)

data class BeforeSubjectListResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: BeforeSubjectListData?
)

data class BeforeSubjectListData(
    val total: Int,
    val rows: List<BeforeSubjectItem>,
    val allnum: Int
)

data class BeforeSubjectItem(
    val id: Int,
    val name: String,
    val cateid: Int,
    val money: String,
    val createtime: Long,
    val updatetime: Long,
    val deletetime: String?,
    val training_exams_id: Int,
    val category_id: String,
    val company_id: Int,
    val issign: String,
    val type: String,
    val studytype: Int,
    val slongtime: Int,
    val progress: Double,
    val joinexams: Int
)

data class BeforeSubCoursewareListResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: BeforeSubCoursewareListData?
)

data class BeforeSubCoursewareListData(
    val total: Int,
    val list: List<BeforeCoursewareItem>,
    val row: BeforePlanDetail,
    val allnum: Int,
    val isstart: Int,
    val isend: Int
)

data class BeforeCoursewareItem(
    val id: Int,
    val name: String,
    val type: Int,
    val imgurl: String,
    val videourl: String,
    val longtime: Int,
    val createtime: Int,
    val content: String,
    val category_id: Int,
    val highvideourl: String,
    val standardvideourl: String,
    val company_id: Int,
    val company_money: Int,
    val user_money: Int,
    val cardnum: Int,
    val endtime: Int,
    val studytype: Int,
    val progress: Int,
    val studytime: Int,
    val type_text: String
)

data class BeforePlanDetail(
    val id: Int,
    val name: String,
    val cateid: Int,
    val money: String,
    val createtime: Long,
    val updatetime: Long,
    val deletetime: String?,
    val training_exams_id: Int,
    val category_id: String,
    val company_id: Int,
    val issign: String,
    val studytype: Int,
    val progress: Double,
    val ksnum: Int
)

data class BeforeCoursewareViewResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: BeforeCoursewareViewData?
)

data class BeforeCoursewareViewData(
    val row: BeforeSimplePlan,
    val longtime: Int,
    val courrow: BeforeCoursewareItem,
    val other: List<Any>
)

data class BeforeSimplePlan(
    val id: Int,
    val name: String,
    val cateid: Int,
    val money: String,
    val createtime: Long,
    val updatetime: Long,
    val deletetime: String?,
    val training_exams_id: Int,
    val category_id: String,
    val company_id: Int,
    val issign: String
)

data class BeforeSubjectStudyResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: BeforeSubjectStudyData?
)

data class BeforeSubjectStudyData(
    val nextsubject_id: Int,
    val isend: Int,
    val training_exams_id: Int
)

data class BeforeExamInfoResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: BeforeExamInfoData?
)

data class BeforeExamInfoData(
    val exams_id: Int,
    val training_before_id: Int
)

data class QuestionListResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: QuestionListData?
)

data class QuestionListData(
    val user_id: Int,
    val nickname: String,
    val stype: String,
    val question_count: Int,
    val answer_count: Int,
    val user_exam_id: Int,
    val category_list: QuestionCategoryList
)

data class QuestionCategoryList(
    val `97`: QuestionCategoryDetail
)

data class QuestionCategoryDetail(
    val question_count: Int,
    val category_name: String,
    val answer_count: Int,
    val user_exam_id: Int,
    val money: Int
)

data class QuestionOrderPayResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: QuestionOrderPayData?
)

data class QuestionOrderPayData(
    val question_category: QuestionCategory,
    val money: Int
)

data class TravelPostRequest(
    val id: Int,
    val car_id: Int,
    val driver_name: String,
    val addtime: String,
    val carnum: String,
    val user_id: Int,
    val type: Int,
    val copilot_name: String,
    val weather: String,
    val temperature: String,
    val load: String,
    val real_load: String,
    val goods_name: String,
    val gotime: String,
    val gettime: Int,
    val start_address: String,
    val end_address: String,
    val mileage: String,
    val sresult: Int,
    val groad: String,
    val gresult: String,
    val stopresult: String,
    val stopaddress: String,
    val stoptime: String,
    val eresult: Int,
    val dsingimg: String,
    val staus: String,
    val updatetime: String
)

data class TravelPostResponse(
    val car_id: Int,
    val driver_name: String,
    val addtime: String,
    val carnum: String,
    val user_id: Int,
    val type: Int,
    val copilot_name: String,
    val weather: String,
    val temperature: String,
    val load: String,
    val real_load: String,
    val goods_name: String,
    val gotime: String,
    val gettime: Int,
    val start_address: String,
    val end_address: String,
    val mileage: String,
    val sresult: Int,
    val groad: String,
    val gresult: String,
    val stopresult: String,
    val stopaddress: String,
    val stoptime: String,
    val eresult: Int,
    val dsingimg: String,
    val staus: String,
    val updatetime: String
)

data class TravelDelResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: Int
)

data class StartAnswerRequest(
    val question_category_id: String,
    val user_exam_id: String,
    val user_category_id: String
)

data class StartAnswerResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: StartAnswerData?
)

data class StartAnswerData(
    val user_exam_id: Int,
    val question_count: Int,
    val right_count: Int,
    val wrong_count: Int,
    val answer_count: Int,
    val question: StartAnswerQuestion
)

data class StartAnswerQuestion(
    val id: Int,
    val admin_id: Int,
    val type: String,
    val category_id: Int,
    val question: String,
    val selectdata: List<QuestionOption>,
    val selectnumber: Int,
    val answer: String,
    val describe: String,
    val level: String,
    val status: String,
    val createtime: Int,
    val updatetime: Int,
    val deletetime: String?,
    val annex: String,
    val stype: Int
)

data class SelectQuestionListResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: SelectQuestionListData?
)

data class SelectQuestionListData(
    val question_count: Int,
    val answer_count: Int,
    val right_count: Int,
    val wrong_count: Int,
    val question_list: SelectQuestionList
)

data class SelectQuestionList(
    val `1`: SelectQuestionItem
)

data class SelectQuestionItem(
    val question_id: Int,
    val is_right: Int
)

data class AnswerRequest(
    val user_exam_id: Int,
    val question_id: Int,
    val answer: String
)

data class AnswerResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: AnswerData?
)

data class AnswerData(
    val is_right: Boolean,
    val has_next: Boolean
)

data class CarNumSearchResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: CarNumSearchData?
)

data class CarNumSearchData(
    val total: Int,
    val rows: List<CarNumSearchItem>,
    val allnum: Int
)

data class CarNumSearchItem(
    val id: Int,
    val carnum: String
)

data class ResetPwdResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: Any?
)

data class UserStudyProveListResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: UserStudyProveListData?
)

data class UserStudyProveListData(
    val rows: List<Any>,
    val num: Int,
    val plan: List<StudyPlan>,
    val ischange: Int
)

data class StudyPlan(
    val id: Int,
    val month: String,
    val list: List<Certificate>
)

data class Certificate(
    val getTime: String,
    val title: String,
    val id: Int,
    val lasttime: String,
    val date: String,
    val cardId: String,
    val carnum: String,
    val gender: String,
    val avatar: String,
    val company: String,
    val name: String,
    val codenum: String,
    val url: String,
    val pic: String
)

data class EducationCertificateResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: List<EducationCertificate>
)

data class EducationCertificate(
    val name: String,
    val cardnum: String,
    val avatar: String,
    val category: String,
    val codenum: String,
    val start: String,
    val starttime: String,
    val end: String,
    val endtime: String,
    val imgurl: List<String>,
    val city_id: Int,
    val category_id: Int
)

data class BeforeEducationCertificateResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: BeforeEducationCertificateData?
)

data class BeforeEducationCertificateData(
    val ksnum: Int,
    val ischange: Int,
    val gender: String,
    val stype: String,
    val avatar: String,
    val nickname: String,
    val cardmun: String,
    val carnum: String,
    val date: String,
    val codenum: String,
    val addtime: String,
    val typename: String,
    val category: CategoryDetail
)

data class StudySafetyListResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: List<SafetyPlan>
)

data class CarCheckResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: CarCheckData?
)

data class CarCheckData(
    val rows: CarCheckDetail
)

data class CarCheckDetail(
    val id: Int,
    val user_id: Int,
    val carnum: String,
    val name: String,
    val company: String,
    val checktime: String,
    val car_certificate_status: String,
    val people_certificate_status: String,
    val car_certificate_fileimg: String,
    val people_certificate_fileimg: String,
    val insure_status: String,
    val insure_fileimg: String,
    val car_status: String,
    val car_fileimg: String,
    val urgent_status: String,
    val urgent_fileimg: String,
    val sign_status: String,
    val sign_fileimg: String,
    val canbody_status: String,
    val canbody_fileimg: String,
    val cutoff_status: String,
    val cutoff_fileimg: String,
    val waybill_status: String,
    val waybill_fileimg: String,
    val question: String,
    val idea: String,
    val checksign_img: String,
    val dirversign_img: String,
    val static_status: String,
    val static_fileimg: String,
    val updatetime: Int
)

data class CarCheckPostRequest(
    val carnum: String,
    val name: String,
    val checktime: String,
    val company: String,
    val car_certificate_status: String,
    val car_certificate_fileimg: String,
    val people_certificate_status: String,
    val people_certificate_fileimg: String,
    val insure_status: String,
    val insure_fileimg: String,
    val car_status: String,
    val car_fileimg: String,
    val urgent_status: String,
    val urgent_fileimg: String,
    val sign_status: String,
    val sign_fileimg: String,
    val canbody_status: String,
    val canbody_fileimg: String,
    val cutoff_status: String,
    val cutoff_fileimg: String,
    val static_status: String,
    val static_fileimg: String,
    val waybill_status: String,
    val waybill_fileimg: String,
    val question: String,
    val idea: String,
    val checksign_img: String,
    val dirversign_img: String
)

data class DangerResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: DangerData?
)

data class DangerData(
    val total: Int,
    val rows: List<Any>,
    val allnum: Int,
    val dslist: DangerDetail
)

data class DangerDetail(
    val id: Int,
    val user_id: Int,
    val name: String,
    val telphone: String,
    val checktime: Int,
    val check_address: String?,
    val carnum: String?,
    val roadnum: String?,
    val driver_name: String,
    val driver_tel: String,
    val driver_number: String,
    val lamp_status: String?,
    val retardation_status: String?,
    val warning_status: String?,
    val tyre_status: String?,
    val safety_status: String?,
    val check_status: String?,
    val procedures_status: String?,
    val other_status: String?,
    val content: String?,
    val fileimg: String?,
    val dirversign_img: String?,
    val checksign_img: String?,
    val admin_id: String?,
    val admin: String?,
    val status: String,
    val fbstaus: String,
    val before_left: String?,
    val before_right: String?,
    val after_left: String?,
    val after_right: String?,
    val dlimages: String,
    val tcimages: String,
    val driverimg: String?,
    val qualification: String?,
    val beidou: String?,
    val beidou_ticket: String?,
    val tripod: String?,
    val fire: String?,
    val meno: String?,
    val shtime: String?,
    val singtime: String?,
    val check_admin: String?
)

data class DangerPostRequest(
    val id: Int,
    val user_id: Int,
    val name: String,
    val telphone: String,
    val checktime: String,
    val check_address: String?,
    val carnum: String?,
    val roadnum: String?,
    val driver_name: String,
    val driver_tel: String,
    val driver_number: String,
    val lamp_status: String?,
    val retardation_status: String?,
    val warning_status: String?,
    val tyre_status: String?,
    val safety_status: String?,
    val check_status: String?,
    val procedures_status: String?,
    val other_status: String?,
    val content: String?,
    val fileimg: String?,
    val dirversign_img: String?,
    val checksign_img: String?,
    val admin_id: String?,
    val admin: String?,
    val status: String,
    val fbstaus: String,
    val before_left: String?,
    val before_right: String?,
    val after_left: String?,
    val after_right: String?,
    val dlimages: String,
    val tcimages: String,
    val driverimg: String?,
    val qualification: String?,
    val beidou: String?,
    val beidou_ticket: String?,
    val tripod: String?,
    val fire: String?,
    val meno: String?,
    val shtime: String?,
    val singtime: String?,
    val check_admin: String?
)

data class MyJobListResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: MyJobListData?
)

data class MyJobListData(
    val total: Int,
    val rows: MyJobItem,
    val allnum: Int
)

data class MyJobItem(
    val id: Int,
    val title: String,
    val content: String,
    val createtime: String,
    val category_id: String,
    val user_id: Int,
    val nickname: String,
    val mobile: String,
    val driverimages: String,
    val qualification: String,
    val frontcard: String,
    val backcard: String
)

data class LogoffResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: Any?
)

data class CarUserInfoResponse(
    val code: Int,
    val msg: String,
    val time: String,
    val data: CarUserInfo?
)

data class CarUserInfo(
    val info: CarUser?
)

data class CarUser(
    val createtime: Long = 0,
    val group_id: Int = 0,
    val nickname: String = "",
    val username: String = ""
)