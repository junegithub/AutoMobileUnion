package com.yt.car.union.net

import okhttp3.MultipartBody
import okhttp3.RequestBody


// Repository Layer
class VehicleRepository(private val apiService: CarApiService) {
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

    suspend fun getPhotoReport(page: Int, pageSize: Int?, search: String?, timetype: Int) =
        apiService.getPhotoReport(page, pageSize, search, timetype)

    suspend fun getStopDetailReport(page: Int, pageSize: Int?, search: String, timetype: Int) =
        apiService.getStopDetailReport(page, pageSize, search, timetype)

    suspend fun getWarningReport(search: String?, timetype: Int, page: String) =
        apiService.getWarningReport(search, timetype, page)

    suspend fun getWarningDetail(page: Int, pageSize: Int?, timetype: Int, type: Int) =
        apiService.getWarningDetail(page, pageSize, timetype, type)

    suspend fun sendContent(request: SendContentRequest) = apiService.sendContent(request)

    suspend fun takePhoto(carId: String) = apiService.takePhoto(carId)

    suspend fun login(request: LoginRequest) = apiService.login(request)

    suspend fun logout() = apiService.logout()

    // 安全培训相关API
    suspend fun userLogin(request: UserLoginRequest) = apiService.userLogin(request)

    suspend fun signView() = apiService.signView()

    suspend fun getUserInfo() = apiService.getUserInfo()

    suspend fun getUserOtherInfo() = apiService.getUserOtherInfo()

    suspend fun epidemicView() = apiService.epidemicView()

    suspend fun getTravelLog() = apiService.getTravelLog()

    suspend fun getCompanyList(page: String?, type: String?) = apiService.getCompanyList(page, type)

    suspend fun getSafetyList(page: String?, type: String?) = apiService.getSafetyList(page, type)

    suspend fun dailySafetyOrderPay(trainingPublicPlanId: String?) =
        apiService.dailySafetyOrderPay(trainingPublicPlanId)

    suspend fun getOldSafetyList(page: String) = apiService.getOldSafetyList(page)

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