package com.yt.car.union.net

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface CarApiService {
    // 查车相关API
    @GET("/car/app/car/carinfo")
    suspend fun getCarInfo(@Query("car_id") carId: Int): Response<CarInfoResponse>

    @GET("/car/app/car/isLYBH")
    suspend fun isLYBH(): Response<IsLYBHResponse>

    @GET("/aggregation/app/oil/day")
    suspend fun getOilDayReport(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("search") search: String?,
        @Query("timetype") timetype: Int
    ): Response<OilDayReportResponse>

    @GET("/aggregation/app/oil/leak")
    suspend fun getLeakReport(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("search") search: String?,
        @Query("timetype") timetype: Int
    ): Response<LeakReportResponse>

    @GET("/aggregation/app/position/getTop")
    suspend fun getMapPositions(@Query("size") size: Int?): Response<MapPositionResponse>

    @GET("/aggregation/app/position/realtimeaddress")
    suspend fun getRealTimeAddress(
        @Query("car_id") carId: Int?,
        @Query("carnum") carnum: String?
    ): Response<RealTimeAddressResponse>

    @POST("/car/app/history")
    suspend fun addSearchHistory(@Body request: SearchHistoryRequest): Response<SearchHistoryResponse>

    @GET("/car/app/tree/getTree")
    suspend fun getTree(
        @Query("ancestors") ancestors: String?,
        @Query("pos") pos: Boolean?,
        @Query("tree") tree: Boolean?
    ): Response<TreeNodeResponse>

    @GET("/car/app/tree/getTreeBlurry")
    suspend fun getTreeBlurry(
        @Query("blurry") blurry: String,
        @Query("pos") pos: Boolean?,
        @Query("tree") tree: Boolean?
    ): Response<TreeNodeResponse>

    @GET("/aggregation/app/carStatus/listByType")
    suspend fun getCarStatusByType(
        @Query("carType") carType: String,
        @Query("pageNum") pageNum: Int,
        @Query("pageSize") pageSize: Int
    ): Response<CarStatusDetailResponse>

    @GET("/aggregation/app/carStatus/statusList")
    suspend fun getCarStatusList(): Response<CarStatusListResponse>

    @GET("/aggregation/app/dashboard/appSearchCarType")
    suspend fun searchCarByType(
        @Query("search") search: String,
        @Query("tree") tree: Boolean?,
        @Query("type") type: String,
        @Query("pageSize") pageSize: String,
        @Query("pageNum") pageNum: String
    ): Response<SearchCarTypeResponse>

    @GET("/aggregation/app/dashboard/getInfo")
    suspend fun getDashboardInfo(): Response<DashboardInfoResponse>

    @GET("/aggregation/app/oil/add")
    suspend fun getOilAddReport(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("search") search: String?,
        @Query("timetype") timetype: Int
    ): Response<OilAddReportResponse>

    @POST("/aggregation/app/position/shareLastPosition")
    suspend fun shareLastPosition(@Body carId: Long): Response<ShareLastPositionResponse>

    @GET("/aggregation/app/position/track")
    suspend fun getTrackInfo(
        @Query("car_id") carId: Int,
        @Query("endtime") endtime: String,
        @Query("is704") is704: Boolean?,
        @Query("isFilter") isFilter: Boolean?,
        @Query("starttime") starttime: String
    ): Response<TrackResponse>

    @GET("/aggregation/app/video/videonew")
    suspend fun getVideoInfo(@Query("car_id") carId: Int): Response<VideoInfoResponse>

    @GET("/aggregation/app/work/activeWarning")
    suspend fun getActiveWarning(
        @Query("search") search: String?,
        @Query("timetype") timetype: Int,
        @Query("page") page: String
    ): Response<ActiveWarningResponse>

    @GET("/aggregation/app/work/caroverlist")
    suspend fun getExpiredCars(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("search") search: String?
    ): Response<ExpiredCarResponse>

    @GET("/aggregation/app/work/mileage")
    suspend fun getMileageReport(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("search") search: String?,
        @Query("timetype") timetype: Int
    ): Response<MileageResponse>

    @GET("/aggregation/app/work/offline")
    suspend fun getOfflineReport(
        @Query("end") end: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("search") search: String?,
        @Query("start") start: String
    ): Response<OfflineReportResponse>

    @GET("/aggregation/app/work/photos")
    suspend fun getPhotoReport(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("search") search: String?,
        @Query("timetype") timetype: Int
    ): Response<PhotoReportResponse>

    @GET("/aggregation/app/work/stopDetail")
    suspend fun getStopDetailReport(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("search") search: String,
        @Query("timetype") timetype: Int
    ): Response<StopDetailResponse>

    @GET("/aggregation/app/work/warning")
    suspend fun getWarningReport(
        @Query("search") search: String?,
        @Query("timetype") timetype: Int,
        @Query("page") page: String
    ): Response<WarningReportResponse>

    @GET("/aggregation/app/work/warningDetail")
    suspend fun getWarningDetail(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("timetype") timetype: Int,
        @Query("type") type: Int
    ): Response<WarningDetailResponse>

    @POST("/jt808/app/jt808/sendcontent")
    suspend fun sendContent(@Body request: SendContentRequest): Response<SendContentResponse>

    @POST("/jt808/app/jt808/photos")
    suspend fun takePhoto(@Query("car_id") carId: String): Response<TakePhotoResponse>

    @POST("/auth/app/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @DELETE("/auth/app/logout")
    suspend fun logout(): Response<LogoutResponse>

    // 安全培训相关API
    @POST("/api/user/login")
    suspend fun userLogin(@Body request: UserLoginRequest): Response<UserLoginResponse>

    @GET("/api/user/signview")
    suspend fun signView(): Response<SignViewResponse>

    @GET("/api/user/info")
    suspend fun getUserInfo(): Response<UserInfoResponse>

    @GET("/api/user/userotherinfo")
    suspend fun getUserOtherInfo(): Response<UserOtherInfoResponse>

    @GET("/api/user/epidemicview")
    suspend fun epidemicView(): Response<EpidemicViewResponse>

    @GET("/api/other/travellog")
    suspend fun getTravelLog(): Response<TravelLogResponse>

    @GET("/api/job/companylist")
    suspend fun getCompanyList(
        @Query("page") page: String?,
        @Query("type") type: String?
    ): Response<CompanyListResponse>

    @GET("/api/user/safetylist")
    suspend fun getSafetyList(
        @Query("page") page: String?,
        @Query("type") type: String?
    ): Response<SafetyListResponse>

    @GET("/api/dailysafety/orderisPay")
    suspend fun dailySafetyOrderPay(
        @Query("training_publicplan_id") trainingPublicPlanId: String?
    ): Response<DailySafetyOrderResponse>

    @GET("/api/training/oldsafetylist")
    suspend fun getOldSafetyList(@Query("page") page: String): Response<OldSafetyListResponse>

    @GET("/api/dailysafety/coursewareList")
    suspend fun getCoursewareList(
        @Query("page") page: String,
        @Query("training_publicplan_id") trainingPublicPlanId: String
    ): Response<CoursewareListResponse>

    @GET("/api/dailysafety/coursewareView")
    suspend fun getCoursewareView(
        @Query("subject_id") subjectId: String,
        @Query("training_publicplan_id") trainingPublicPlanId: String
    ): Response<CoursewareViewResponse>

    @GET("/api/training/configtime")
    suspend fun getConfigTime(): Response<ConfigTimeResponse>

    @GET("/api/dailysafety/safeStudy")
    suspend fun safeStudy(
        @Query("subject_id") subjectId: String,
        @Query("training_publicplan_id") trainingPublicPlanId: String,
        @Query("longtime") longtime: String
    ): Response<SafeStudyResponse>

    @GET("/api/user/safekspaperview")
    suspend fun getExamView(
        @Query("exam_id") examId: String,
        @Query("training_publicplan_id") trainingPublicPlanId: String
    ): Response<ExamViewResponse>

    @POST("/api/user/newuplode")
    suspend fun uploadFile(@Part filePath: MultipartBody.Part): Response<UploadFileResponse>

    @POST("/api/user/safekspost")
    suspend fun submitExam(@Body request: SubmitExamRequest): Response<SubmitExamResponse>

    @GET("/safecankao")
    suspend fun getExamResult(
        @Query("exam_id") examId: String,
        @Query("training_publicplan_id") trainingPublicPlanId: String
    ): Response<ExamResultResponse>

    @GET("/api/user/questionview")
    suspend fun getQuestionView(
        @Query("exam_id") examId: String,
        @Query("question_id") questionId: String,
        @Query("uanswer") uanswer: String,
        @Query("training_publicplan_id") trainingPublicPlanId: String
    ): Response<QuestionViewResponse>

    @GET("/api/before/creatOrder")
    suspend fun createOrder(
        @Query("money") money: String,
        @Query("training_publicplan_id") trainingPublicPlanId: Int,
        @Query("type") type: String,
        @Query("method") method: String
    ): Response<CreateOrderResponse>

    @GET("/api/dailysafety/yearPay")
    suspend fun yearPay(
        @Query("code") code: Int,
        @Query("type") type: String,
        @Query("method") method: String,
        @Query("year_id") yearId: Int
    ): Response<CreateOrderResponse>

    @GET("/api/training/orderlist")
    suspend fun getOrderList(@Query("page") page: String): Response<OrderListResponse>

    @GET("/api/dailysafety/safeFace")
    suspend fun safeFace(
        @Query("imgurl") imgurl: String,
        @Query("training_publicplan_id") trainingPublicPlanId: Int,
        @Query("type") type: String
    ): Response<FaceResponse>

    @GET("/api/user/safetyadd")
    suspend fun safetyAdd(
        @Header("subject_id") subjectId: String,
        @Header("training_safetyplan_id") trainingSafetyPlanId: Int,
        @Header("longtime") longtime: Int,
        @Header("imgurl") imgurl: String
    ): Response<CreateOrderResponse>

    @GET("/api/before/subjectface")
    suspend fun beforeSubjectFace(
        @Query("imgurl") imgurl: String,
        @Query("training_publicplan_id") trainingPublicPlanId: Int,
        @Query("type") type: String
    ): Response<FaceResponse>

    @GET("/api/training/subjectface")
    suspend fun subjectFace(
        @Query("signfile") signfile: String,
        @Query("id") id: String
    ): Response<FaceResponse>

    @POST("/api/user/singpost")
    suspend fun singPost(@Body request: RequestBody): Response<CreateOrderResponse>

    @GET("/api/question/twoList")
    suspend fun getTwoList(@Query("user_category_id") userCategoryId: String): Response<TwoListResponse>

    @GET("/api/question/twoOrderisPay")
    suspend fun twoOrderPay(@Query("question_category_id") questionCategoryId: String): Response<TwoOrderPayResponse>

    @GET("/api/question/selectTwoQuestionList")
    suspend fun selectTwoQuestionList(@Query("user_exam_id") userExamId: String): Response<SelectTwoQuestionListResponse>

    @POST("/api/question/startTwoAnswer")
    suspend fun startTwoAnswer(@Body request: StartTwoAnswerRequest): Response<StartTwoAnswerResponse>

    @POST("/api/question/updateTwoQuestion")
    suspend fun updateTwoQuestion(@Body request: UpdateTwoQuestionRequest): Response<CreateOrderResponse>

    @GET("/api/question/creatTwoOrder")
    suspend fun createTwoOrder(
        @Query("type") type: String,
        @Query("method") method: String,
        @Query("question_category_id") questionCategoryId: Int
    ): Response<CreateTwoOrderResponse>

    @GET("/api/training/meetinglist")
    suspend fun getMeetingList(
        @Query("page") page: String,
        @Query("status") status: String
    ): Response<MeetingListResponse>

    @GET("/api/training/meetingview")
    suspend fun getMeetingView(@Query("id") id: String): Response<MeetingViewResponse>

    @GET("/api/training/subjectlist")
    suspend fun getSubjectList(@Query("page") page: String): Response<SubjectListResponse>

    @GET("/api/training/subjectorder")
    suspend fun subjectOrder(@Query("training_safetyplan_id") trainingSafetyPlanId: String): Response<SubjectOrderResponse>

    @GET("/api/training/subcoursewarelist")
    suspend fun getSubCoursewareList(
        @Query("page") page: String,
        @Query("training_safetyplan_id") trainingSafetyPlanId: String,
        @Query("number") number: String
    ): Response<SubCoursewareListResponse>

    @GET("/api/training/subjectstudy")
    suspend fun subjectStudy(
        @Query("subject_id") subjectId: String,
        @Query("training_safetyplan_id") trainingSafetyPlanId: String,
        @Query("longtime") longtime: String
    ): Response<SubjectStudyResponse>

    @GET("/api/before/orderisPay")
    suspend fun beforeOrderPay(): Response<BeforeOrderPayResponse>

    @GET("/api/before/subjectlist")
    suspend fun getBeforeSubjectList(): Response<BeforeSubjectListResponse>

    @GET("/api/before/subcoursewarelist")
    suspend fun getBeforeSubCoursewareList(
        @Query("page") page: String,
        @Query("training_safetyplan_id") trainingSafetyPlanId: String,
        @Query("number") number: String
    ): Response<BeforeSubCoursewareListResponse>

    @GET("/api/before/coursewareview")
    suspend fun getBeforeCoursewareView(
        @Query("training_safetyplan_id") trainingSafetyPlanId: String,
        @Query("subject_id") subjectId: String
    ): Response<BeforeCoursewareViewResponse>

    @GET("/api/before/subjectstudy")
    suspend fun beforeSubjectStudy(
        @Query("subject_id") subjectId: String,
        @Query("training_safetyplan_id") trainingSafetyPlanId: String,
        @Query("longtime") longtime: String,
        @Query("number") number: String,
        @Query("pageScoll") pageScoll: String?
    ): Response<BeforeSubjectStudyResponse>

    @GET("/api/before/examinfo")
    suspend fun beforeExamInfo(): Response<BeforeExamInfoResponse>

    @GET("/api/question")
    suspend fun getQuestionList(): Response<QuestionListResponse>

    @GET("/api/question/orderisPay")
    suspend fun questionOrderPay(@Query("question_category_id") questionCategoryId: String): Response<QuestionOrderPayResponse>

    @POST("/api/other/travelpost")
    suspend fun travelPost(@Body request: TravelPostRequest): Response<TravelPostResponse>

    @GET("/api/other/travedel")
    suspend fun travelDel(@Query("id") id: String): Response<TravelDelResponse>

    @POST("/api/question/startAnswer")
    suspend fun startAnswer(@Body request: StartAnswerRequest): Response<StartAnswerResponse>

    @GET("/api/question/selectQuestionList")
    suspend fun selectQuestionList(@Query("user_exam_id") userExamId: String): Response<SelectQuestionListResponse>

    @POST("/api/question/answer")
    suspend fun answer(@Body request: AnswerRequest): Response<AnswerResponse>

    @GET("/api/other/carnumSearch")
    suspend fun carnumSearch(
        @Query("carnum") carnum: String,
        @Query("page") page: String
    ): Response<CarNumSearchResponse>

    @GET("/api/user/resetpwd")
    suspend fun resetPwd(
        @Query("newpassword") newpassword: String,
        @Query("oldpassword") oldpassword: String
    ): Response<ResetPwdResponse>

    @GET("/api/user/userstudyprovelist")
    suspend fun getUserStudyProveList(@Query("month") month: String): Response<UserStudyProveListResponse>

    @GET("/api/training/educationCertificate")
    suspend fun getEducationCertificate(): Response<EducationCertificateResponse>

    @GET("/api/before/educationCertificate")
    suspend fun getBeforeEducationCertificate(): Response<BeforeEducationCertificateResponse>

    @GET("/api/user/studysafetylist")
    suspend fun getStudySafetyList(@Query("month") month: String): Response<StudySafetyListResponse>

    @GET("/api/other/carcheck")
    suspend fun getCarCheck(): Response<CarCheckResponse>

    @POST("/api/other/carcheckpost")
    suspend fun carCheckPost(@Body request: CarCheckPostRequest): Response<CreateOrderResponse>

    @GET("/api/other/danger")
    suspend fun getDanger(): Response<DangerResponse>

    @POST("/api/other/dangerpost")
    suspend fun dangerPost(@Body request: DangerPostRequest): Response<CreateOrderResponse>

    @GET("/api/job/myjoblist")
    suspend fun getMyJobList(
        @Query("page") page: String,
        @Query("type") type: String
    ): Response<MyJobListResponse>

    @POST("/api/user/logoff")
    suspend fun logoff(): Response<LogoffResponse>
}