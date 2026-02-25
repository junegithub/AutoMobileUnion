package com.yt.car.union.net

import okhttp3.MultipartBody
import okhttp3.RequestBody

// Repository Layer
class TrainingRepository(private val apiService: TrainingApiService) {

    // 安全培训相关API
    suspend fun userLogin(request: UserLoginRequest) = apiService.userLogin(request)

    suspend fun signView() = apiService.signView()

    suspend fun getUserInfoSafe() = apiService.getUserInfoSafe()

    suspend fun getUserOtherInfo() = apiService.getUserOtherInfo()

    suspend fun epidemicView() = apiService.epidemicView()

    suspend fun getTravelLog() = apiService.getTravelLog()

    suspend fun getCompanyList(page: String?, type: String?) = apiService.getCompanyList(page, type)

    suspend fun getSafetyList(page: Int, type: Int) = apiService.getSafetyList(page, type)

    suspend fun dailySafetyOrderPay(trainingPublicPlanId: String?) =
        apiService.dailySafetyOrderPay(trainingPublicPlanId)

    suspend fun getOldSafetyList(page: Int) = apiService.getOldSafetyList(page)

    suspend fun getCoursewareList(page: String, trainingPublicPlanId: String) =
        apiService.getCoursewareList(page, trainingPublicPlanId)

    suspend fun getCoursewareView(subjectId: String, trainingPublicPlanId: String) =
        apiService.getCoursewareView(subjectId, trainingPublicPlanId)

    suspend fun getConfigTime() = apiService.getConfigTime()

    suspend fun safeStudy(subjectId: String, trainingPublicPlanId: String, longtime: String) =
        apiService.safeStudy(subjectId, trainingPublicPlanId, longtime)

    suspend fun getExamView(examId: String, trainingPublicPlanId: String) =
        apiService.getExamView(examId, trainingPublicPlanId)

    suspend fun uploadFile(filePath: MultipartBody.Part) = apiService.uploadFile(filePath)

    suspend fun submitExam(request: SubmitExamRequest) = apiService.submitExam(request)

    suspend fun getExamResult(examId: String, trainingPublicPlanId: String) =
        apiService.getExamResult(examId, trainingPublicPlanId)

    suspend fun getQuestionView(examId: String, questionId: String, uanswer: String, trainingPublicPlanId: String) =
        apiService.getQuestionView(examId, questionId, uanswer, trainingPublicPlanId)

    suspend fun createOrder(money: String, trainingPublicPlanId: Int, type: String, method: String) =
        apiService.createOrder(money, trainingPublicPlanId, type, method)

    suspend fun yearPay(code: Int, type: String, method: String, yearId: Int) =
        apiService.yearPay(code, type, method, yearId)

    suspend fun getOrderList(page: String) = apiService.getOrderList(page)

    suspend fun safeFace(imgurl: String, trainingPublicPlanId: Int, type: String) =
        apiService.safeFace(imgurl, trainingPublicPlanId, type)

    suspend fun safetyAdd(subjectId: String, trainingSafetyPlanId: Int, longtime: Int, imgurl: String) =
        apiService.safetyAdd(subjectId, trainingSafetyPlanId, longtime, imgurl)

    suspend fun beforeSubjectFace(imgurl: String, trainingPublicPlanId: Int, type: String) =
        apiService.beforeSubjectFace(imgurl, trainingPublicPlanId, type)

    suspend fun subjectFace(signfile: String, id: String) =
        apiService.subjectFace(signfile, id)

    suspend fun singPost(request: RequestBody) = apiService.singPost(request)

    suspend fun getTwoList(userCategoryId: String) = apiService.getTwoList(userCategoryId)

    suspend fun twoOrderPay(questionCategoryId: String) = apiService.twoOrderPay(questionCategoryId)

    suspend fun selectTwoQuestionList(userExamId: String) =
        apiService.selectTwoQuestionList(userExamId)

    suspend fun startTwoAnswer(request: StartTwoAnswerRequest) =
        apiService.startTwoAnswer(request)

    suspend fun updateTwoQuestion(request: UpdateTwoQuestionRequest) =
        apiService.updateTwoQuestion(request)

    suspend fun createTwoOrder(type: String, method: String, questionCategoryId: Int) =
        apiService.createTwoOrder(type, method, questionCategoryId)

    suspend fun getMeetingList(page: String, status: String) =
        apiService.getMeetingList(page, status)

    suspend fun getMeetingView(id: String) = apiService.getMeetingView(id)

    suspend fun getSubjectList(page: String) = apiService.getSubjectList(page)

    suspend fun subjectOrder(trainingSafetyPlanId: String) =
        apiService.subjectOrder(trainingSafetyPlanId)

    suspend fun getSubCoursewareList(page: String, trainingSafetyPlanId: String, number: String) =
        apiService.getSubCoursewareList(page, trainingSafetyPlanId, number)

    suspend fun subjectStudy(subjectId: String, trainingSafetyPlanId: String, longtime: String) =
        apiService.subjectStudy(subjectId, trainingSafetyPlanId, longtime)

    suspend fun beforeOrderPay() = apiService.beforeOrderPay()

    suspend fun getBeforeSubjectList() = apiService.getBeforeSubjectList()

    suspend fun getBeforeSubCoursewareList(page: String, trainingSafetyPlanId: String, number: String) =
        apiService.getBeforeSubCoursewareList(page, trainingSafetyPlanId, number)

    suspend fun getBeforeCoursewareView(trainingSafetyPlanId: String, subjectId: String) =
        apiService.getBeforeCoursewareView(trainingSafetyPlanId, subjectId)

    suspend fun beforeSubjectStudy(subjectId: String, trainingSafetyPlanId: String, longtime: String, number: String, pageScoll: String?) =
        apiService.beforeSubjectStudy(subjectId, trainingSafetyPlanId, longtime, number, pageScoll)

    suspend fun beforeExamInfo() = apiService.beforeExamInfo()

    suspend fun getQuestionList() = apiService.getQuestionList()

    suspend fun questionOrderPay(questionCategoryId: String) =
        apiService.questionOrderPay(questionCategoryId)

    suspend fun travelPost(request: TravelPostRequest) = apiService.travelPost(request)

    suspend fun travelDel(id: String) = apiService.travelDel(id)

    suspend fun startAnswer(request: StartAnswerRequest) = apiService.startAnswer(request)

    suspend fun selectQuestionList(userExamId: String) =
        apiService.selectQuestionList(userExamId)

    suspend fun answer(request: AnswerRequest) = apiService.answer(request)

    suspend fun carnumSearch(carnum: String, page: String) =
        apiService.carnumSearch(carnum, page)

    suspend fun resetPwd(newpassword: String, oldpassword: String) =
        apiService.resetPwd(newpassword, oldpassword)

    suspend fun getUserStudyProveList(month: String) =
        apiService.getUserStudyProveList(month)

    suspend fun getEducationCertificate() = apiService.getEducationCertificate()

    suspend fun getBeforeEducationCertificate() = apiService.getBeforeEducationCertificate()

    suspend fun getStudySafetyList(month: String) = apiService.getStudySafetyList(month)

    suspend fun getCarCheck() = apiService.getCarCheck()

    suspend fun carCheckPost(request: CarCheckPostRequest) = apiService.carCheckPost(request)

    suspend fun getDanger() = apiService.getDanger()

    suspend fun dangerPost(request: DangerPostRequest) = apiService.dangerPost(request)

    suspend fun getMyJobList(page: String, type: String) = apiService.getMyJobList(page, type)

    suspend fun logoff() = apiService.logoff()
}