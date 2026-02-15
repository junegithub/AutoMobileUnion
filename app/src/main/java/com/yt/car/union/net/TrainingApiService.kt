package com.yt.car.union.net

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface TrainingApiService {
    // 安全培训相关API（/api/ 前缀及其他非车相关前缀）
    @POST("api/user/login")
    suspend fun userLogin(@Body request: UserLoginRequest): Response<TrainingBaseResponse<UserLoginData>>

    @GET("api/user/signview")
    suspend fun signView(): Response<TrainingBaseResponse<SignViewData>>

    @GET("api/user/info")
    suspend fun getUserInfo(): Response<TrainingBaseResponse<UserInfoData>>

    @GET("api/user/userotherinfo")
    suspend fun getUserOtherInfo(): Response<TrainingBaseResponse<OtherUserInfo>>

    @GET("api/user/epidemicview")
    suspend fun epidemicView(): Response<TrainingBaseResponse<EpidemicViewData>>

    @GET("api/other/travellog")
    suspend fun getTravelLog(): Response<TrainingBaseResponse<TravelLogData>>

    @GET("api/job/companylist")
    suspend fun getCompanyList(
        @Query("page") page: String?,
        @Query("type") type: String?
    ): Response<TrainingBaseResponse<CompanyListData>>

    @GET("api/user/safetylist")
    suspend fun getSafetyList(
        @Query("page") page: String?,
        @Query("type") type: String?
    ): Response<TrainingBaseResponse<SafetyListData>>

    @GET("api/dailysafety/orderisPay")
    suspend fun dailySafetyOrderPay(
        @Query("training_publicplan_id") trainingPublicPlanId: String?
    ): Response<TrainingBaseResponse<DailySafetyOrderData>>

    @GET("api/training/oldsafetylist")
    suspend fun getOldSafetyList(@Query("page") page: String): Response<TrainingBaseResponse<OldSafetyListData>>

    @GET("api/dailysafety/coursewareList")
    suspend fun getCoursewareList(
        @Query("page") page: String,
        @Query("training_publicplan_id") trainingPublicPlanId: String
    ): Response<TrainingBaseResponse<CoursewareListData>>

    @GET("api/dailysafety/coursewareView")
    suspend fun getCoursewareView(
        @Query("subject_id") subjectId: String,
        @Query("training_publicplan_id") trainingPublicPlanId: String
    ): Response<TrainingBaseResponse<CoursewareViewData>>

    @GET("api/training/configtime")
    suspend fun getConfigTime(): Response<TrainingBaseResponse<Int>>

    @GET("api/dailysafety/safeStudy")
    suspend fun safeStudy(
        @Query("subject_id") subjectId: String,
        @Query("training_publicplan_id") trainingPublicPlanId: String,
        @Query("longtime") longtime: String
    ): Response<TrainingBaseResponse<SafeStudyData>>

    @GET("api/user/safekspaperview")
    suspend fun getExamView(
        @Query("exam_id") examId: String,
        @Query("training_publicplan_id") trainingPublicPlanId: String
    ): Response<TrainingBaseResponse<ExamViewData>>

    @POST("api/user/newuplode")
    suspend fun uploadFile(@Part filePath: MultipartBody.Part): Response<TrainingBaseResponse<UploadFileData>>

    @POST("api/user/safekspost")
    suspend fun submitExam(@Body request: SubmitExamRequest): Response<TrainingBaseResponse<String>>

    @GET("safecankao")
    suspend fun getExamResult(
        @Query("exam_id") examId: String,
        @Query("training_publicplan_id") trainingPublicPlanId: String
    ): Response<TrainingBaseResponse<ExamResultData>>

    @GET("api/user/questionview")
    suspend fun getQuestionView(
        @Query("exam_id") examId: String,
        @Query("question_id") questionId: String,
        @Query("uanswer") uanswer: String,
        @Query("training_publicplan_id") trainingPublicPlanId: String
    ): Response<TrainingBaseResponse<List<QuestionViewItem>>>

    @GET("api/before/creatOrder")
    suspend fun createOrder(
        @Query("money") money: String,
        @Query("training_publicplan_id") trainingPublicPlanId: Int,
        @Query("type") type: String,
        @Query("method") method: String
    ): Response<TrainingBaseResponse<String>>

    @GET("api/dailysafety/yearPay")
    suspend fun yearPay(
        @Query("code") code: Int,
        @Query("type") type: String,
        @Query("method") method: String,
        @Query("year_id") yearId: Int
    ): Response<TrainingBaseResponse<String>>

    @GET("api/training/orderlist")
    suspend fun getOrderList(@Query("page") page: String): Response<TrainingBaseResponse<OrderListData>>

    @GET("api/dailysafety/safeFace")
    suspend fun safeFace(
        @Query("imgurl") imgurl: String,
        @Query("training_publicplan_id") trainingPublicPlanId: Int,
        @Query("type") type: String
    ): Response<TrainingBaseResponse<FaceData>>

    @GET("api/user/safetyadd")
    suspend fun safetyAdd(
        @Header("subject_id") subjectId: String,
        @Header("training_safetyplan_id") trainingSafetyPlanId: Int,
        @Header("longtime") longtime: Int,
        @Header("imgurl") imgurl: String
    ): Response<TrainingBaseResponse<String>>

    @GET("api/before/subjectface")
    suspend fun beforeSubjectFace(
        @Query("imgurl") imgurl: String,
        @Query("training_publicplan_id") trainingPublicPlanId: Int,
        @Query("type") type: String
    ): Response<TrainingBaseResponse<FaceData>>

    @GET("api/training/subjectface")
    suspend fun subjectFace(
        @Query("signfile") signfile: String,
        @Query("id") id: String
    ): Response<TrainingBaseResponse<FaceData>>

    @POST("api/user/singpost")
    suspend fun singPost(@Body request: RequestBody): Response<TrainingBaseResponse<String>>

    @GET("api/question/twoList")
    suspend fun getTwoList(@Query("user_category_id") userCategoryId: String): Response<TrainingBaseResponse<TwoListData>>

    @GET("api/question/twoOrderisPay")
    suspend fun twoOrderPay(@Query("question_category_id") questionCategoryId: String): Response<TrainingBaseResponse<TwoOrderPayData>>

    @GET("api/question/selectTwoQuestionList")
    suspend fun selectTwoQuestionList(@Query("user_exam_id") userExamId: String): Response<TrainingBaseResponse<SelectTwoQuestionListData>>

    @POST("api/question/startTwoAnswer")
    suspend fun startTwoAnswer(@Body request: StartTwoAnswerRequest): Response<TrainingBaseResponse<StartTwoAnswerData>>

    @POST("api/question/updateTwoQuestion")
    suspend fun updateTwoQuestion(@Body request: UpdateTwoQuestionRequest): Response<TrainingBaseResponse<String>>

    @GET("api/question/creatTwoOrder")
    suspend fun createTwoOrder(
        @Query("type") type: String,
        @Query("method") method: String,
        @Query("question_category_id") questionCategoryId: Int
    ): Response<TrainingBaseResponse<String>>

    @GET("api/training/meetinglist")
    suspend fun getMeetingList(
        @Query("page") page: String,
        @Query("status") status: String
    ): Response<TrainingBaseResponse<MeetingListData>>

    @GET("api/training/meetingview")
    suspend fun getMeetingView(@Query("id") id: String): Response<TrainingBaseResponse<MeetingViewData>>

    @GET("api/training/subjectlist")
    suspend fun getSubjectList(@Query("page") page: String): Response<TrainingBaseResponse<SubjectListData>>

    @GET("api/training/subjectorder")
    suspend fun subjectOrder(@Query("training_safetyplan_id") trainingSafetyPlanId: String): Response<TrainingBaseResponse<SubjectOrderData>>

    @GET("api/training/subcoursewarelist")
    suspend fun getSubCoursewareList(
        @Query("page") page: String,
        @Query("training_safetyplan_id") trainingSafetyPlanId: String,
        @Query("number") number: String
    ): Response<TrainingBaseResponse<SubCoursewareListData>>

    @GET("api/training/subjectstudy")
    suspend fun subjectStudy(
        @Query("subject_id") subjectId: String,
        @Query("training_safetyplan_id") trainingSafetyPlanId: String,
        @Query("longtime") longtime: String
    ): Response<TrainingBaseResponse<SubjectStudyData>>

    @GET("api/before/orderisPay")
    suspend fun beforeOrderPay(): Response<TrainingBaseResponse<BeforeOrderPayData>>

    @GET("api/before/subjectlist")
    suspend fun getBeforeSubjectList(): Response<TrainingBaseResponse<BeforeSubjectListData>>

    @GET("api/before/subcoursewarelist")
    suspend fun getBeforeSubCoursewareList(
        @Query("page") page: String,
        @Query("training_safetyplan_id") trainingSafetyPlanId: String,
        @Query("number") number: String
    ): Response<TrainingBaseResponse<BeforeSubCoursewareListData>>

    @GET("api/before/coursewareview")
    suspend fun getBeforeCoursewareView(
        @Query("training_safetyplan_id") trainingSafetyPlanId: String,
        @Query("subject_id") subjectId: String
    ): Response<TrainingBaseResponse<BeforeCoursewareViewData>>

    @GET("api/before/subjectstudy")
    suspend fun beforeSubjectStudy(
        @Query("subject_id") subjectId: String,
        @Query("training_safetyplan_id") trainingSafetyPlanId: String,
        @Query("longtime") longtime: String,
        @Query("number") number: String,
        @Query("pageScoll") pageScoll: String?
    ): Response<TrainingBaseResponse<BeforeSubjectStudyData>>

    @GET("api/before/examinfo")
    suspend fun beforeExamInfo(): Response<TrainingBaseResponse<BeforeExamInfoData>>

    @GET("api/question")
    suspend fun getQuestionList(): Response<TrainingBaseResponse<QuestionListData>>

    @GET("api/question/orderisPay")
    suspend fun questionOrderPay(@Query("question_category_id") questionCategoryId: String): Response<TrainingBaseResponse<QuestionOrderPayData>>

    @POST("api/other/travelpost")
    suspend fun travelPost(@Body request: TravelPostRequest): Response<TrainingBaseResponse<TravelPostResponse>>

    @GET("api/other/travedel")
    suspend fun travelDel(@Query("id") id: String): Response<TrainingBaseResponse<Int>>

    @POST("api/question/startAnswer")
    suspend fun startAnswer(@Body request: StartAnswerRequest): Response<TrainingBaseResponse<StartAnswerData>>

    @GET("api/question/selectQuestionList")
    suspend fun selectQuestionList(@Query("user_exam_id") userExamId: String): Response<TrainingBaseResponse<SelectQuestionListData>>

    @POST("api/question/answer")
    suspend fun answer(@Body request: AnswerRequest): Response<TrainingBaseResponse<AnswerData>>

    @GET("api/other/carnumSearch")
    suspend fun carnumSearch(
        @Query("carnum") carnum: String,
        @Query("page") page: String
    ): Response<TrainingBaseResponse<CarNumSearchData>>

    @GET("api/user/resetpwd")
    suspend fun resetPwd(
        @Query("newpassword") newpassword: String,
        @Query("oldpassword") oldpassword: String
    ): Response<TrainingBaseResponse<Any>>

    @GET("api/user/userstudyprovelist")
    suspend fun getUserStudyProveList(@Query("month") month: String): Response<TrainingBaseResponse<UserStudyProveListData>>

    @GET("api/training/educationCertificate")
    suspend fun getEducationCertificate(): Response<TrainingBaseResponse<List<EducationCertificate>>>

    @GET("api/before/educationCertificate")
    suspend fun getBeforeEducationCertificate(): Response<TrainingBaseResponse<BeforeEducationCertificateData>>

    @GET("api/user/studysafetylist")
    suspend fun getStudySafetyList(@Query("month") month: String): Response<TrainingBaseResponse<List<SafetyPlan>>>

    @GET("api/other/carcheck")
    suspend fun getCarCheck(): Response<TrainingBaseResponse<CarCheckData>>

    @POST("api/other/carcheckpost")
    suspend fun carCheckPost(@Body request: CarCheckPostRequest): Response<TrainingBaseResponse<String>>

    @GET("api/other/danger")
    suspend fun getDanger(): Response<TrainingBaseResponse<DangerData>>

    @POST("api/other/dangerpost")
    suspend fun dangerPost(@Body request: DangerPostRequest): Response<TrainingBaseResponse<String>>

    @GET("api/job/myjoblist")
    suspend fun getMyJobList(
        @Query("page") page: String,
        @Query("type") type: String
    ): Response<TrainingBaseResponse<MyJobListData>>

    @POST("api/user/logoff")
    suspend fun logoff(): Response<TrainingBaseResponse<Any>>
}