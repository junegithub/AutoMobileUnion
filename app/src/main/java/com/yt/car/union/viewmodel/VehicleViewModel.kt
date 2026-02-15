package com.yt.car.union.viewmodel

// ViewModel Layer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yt.car.union.MyApp
import com.yt.car.union.net.AnswerRequest
import com.yt.car.union.net.CarCheckPostRequest
import com.yt.car.union.net.DangerPostRequest
import com.yt.car.union.net.LoginRequest
import com.yt.car.union.net.RetrofitClient
import com.yt.car.union.net.SearchHistoryRequest
import com.yt.car.union.net.SendContentRequest
import com.yt.car.union.net.StartAnswerRequest
import com.yt.car.union.net.StartTwoAnswerRequest
import com.yt.car.union.net.SubmitExamRequest
import com.yt.car.union.net.TravelPostRequest
import com.yt.car.union.net.UpdateTwoQuestionRequest
import com.yt.car.union.net.UserLoginRequest
import com.yt.car.union.net.CarRepository
import com.yt.car.union.net.TrainingRepository
import com.yt.car.union.util.SPUtils
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class VehicleViewModel() : ViewModel() {
    private val repository: CarRepository by lazy {
        CarRepository(RetrofitClient.getCarApiService())
    }
    private val trainingRepository: TrainingRepository by lazy {
        TrainingRepository(RetrofitClient.getTrainingApiService())
    }

    fun getCarInfo(carId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getCarInfo(carId)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun isLYBH() {
        viewModelScope.launch {
            try {
                val response = repository.isLYBH()
                if (response.isSuccessful && response.body()?.code != null) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getOilDayReport(page: Int, pageSize: Int?, search: String?, timetype: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getOilDayReport(page, pageSize, search, timetype)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getLeakReport(page: Int, pageSize: Int?, search: String?, timetype: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getLeakReport(page, pageSize, search, timetype)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getMapPositions(size: Int?) {
        viewModelScope.launch {
            try {
                val response = repository.getMapPositions(size)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getRealTimeAddress(carId: Int?, carnum: String?) {
        viewModelScope.launch {
            try {
                val response = repository.getRealTimeAddress(carId, carnum)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun addSearchHistory(request: SearchHistoryRequest) {
        viewModelScope.launch {
            try {
                val response = repository.addSearchHistory(request)
                if (response.isSuccessful && response.body()?.code != null) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getTree(ancestors: String?, pos: Boolean?, tree: Boolean?) {
        viewModelScope.launch {
            try {
                val response = repository.getTree(ancestors, pos, tree)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getTreeBlurry(blurry: String, pos: Boolean?, tree: Boolean?) {
        viewModelScope.launch {
            try {
                val response = repository.getTreeBlurry(blurry, pos, tree)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getCarStatusByType(carType: String, pageNum: Int, pageSize: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getCarStatusByType(carType, pageNum, pageSize)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getCarStatusList() {
        viewModelScope.launch {
            try {
                val response = repository.getCarStatusList()
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun searchCarByType(search: String, tree: Boolean?, type: String, pageSize: String, pageNum: String) {
        viewModelScope.launch {
            try {
                val response = repository.searchCarByType(search, tree, type, pageSize, pageNum)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getDashboardInfo() {
        viewModelScope.launch {
            try {
                val response = repository.getDashboardInfo()
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getOilAddReport(page: Int, pageSize: Int?, search: String?, timetype: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getOilAddReport(page, pageSize, search, timetype)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun shareLastPosition(carId: Long) {
        viewModelScope.launch {
            try {
                val response = repository.shareLastPosition(carId)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getTrackInfo(carId: Int, endtime: String, is704: Boolean?, isFilter: Boolean?, starttime: String) {
        viewModelScope.launch {
            try {
                val response = repository.getTrackInfo(carId, endtime, is704, isFilter, starttime)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getVideoInfo(carId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getVideoInfo(carId)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getActiveWarning(search: String?, timetype: Int, page: String) {
        viewModelScope.launch {
            try {
                val response = repository.getActiveWarning(search, timetype, page)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getExpiredCars(page: Int, pageSize: Int?, search: String?) {
        viewModelScope.launch {
            try {
                val response = repository.getExpiredCars(page, pageSize, search)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getMileageReport(page: Int, pageSize: Int?, search: String?, timetype: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getMileageReport(page, pageSize, search, timetype)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getOfflineReport(end: String, page: Int, pageSize: Int?, search: String?, start: String) {
        viewModelScope.launch {
            try {
                val response = repository.getOfflineReport(end, page, pageSize, search, start)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getPhotoReport(page: Int, pageSize: Int?, search: String?, timetype: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getPhotoReport(page, pageSize, search, timetype)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getStopDetailReport(page: Int, pageSize: Int?, search: String, timetype: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getStopDetailReport(page, pageSize, search, timetype)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getWarningReport(search: String?, timetype: Int, page: String) {
        viewModelScope.launch {
            try {
                val response = repository.getWarningReport(search, timetype, page)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getWarningDetail(page: Int, pageSize: Int?, timetype: Int, type: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getWarningDetail(page, pageSize, timetype, type)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun sendContent(request: SendContentRequest) {
        viewModelScope.launch {
            try {
                val response = repository.sendContent(request)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun takePhoto(carId: String) {
        viewModelScope.launch {
            try {
                val response = repository.takePhoto(carId)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            try {
                val response = repository.login(request)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                    // 保存Token
                    SPUtils.saveToken(response.body()?.data?.userinfo?.token)
                    MyApp.isLogin = true
                    getUserInfo()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                val response = repository.logout()
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    // 安全培训相关API
    fun userLogin(request: UserLoginRequest) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.userLogin(request)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun signView() {
        viewModelScope.launch {
            try {
                val response = trainingRepository.signView()
                if (response.isSuccessful && response.body()?.code != null) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getUserInfo() {
        viewModelScope.launch {
            try {
                val response = repository.getCarUserInfo()
                if (response.isSuccessful && response.body()?.code == 1) {
                    MyApp.userInfo = response.body()?.data?.info
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getUserOtherInfo() {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getUserOtherInfo()
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun epidemicView() {
        viewModelScope.launch {
            try {
                val response = trainingRepository.epidemicView()
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getTravelLog() {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getTravelLog()
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getCompanyList(page: String?, type: String?) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getCompanyList(page, type)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getSafetyList(page: String?, type: String?) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getSafetyList(page, type)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun dailySafetyOrderPay(trainingPublicPlanId: String?) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.dailySafetyOrderPay(trainingPublicPlanId)
                if (response.isSuccessful && response.body()?.code != null) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getOldSafetyList(page: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getOldSafetyList(page)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getCoursewareList(page: String, trainingPublicPlanId: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getCoursewareList(page, trainingPublicPlanId)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getCoursewareView(subjectId: String, trainingPublicPlanId: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getCoursewareView(subjectId, trainingPublicPlanId)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getConfigTime() {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getConfigTime()
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun safeStudy(subjectId: String, trainingPublicPlanId: String, longtime: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.safeStudy(subjectId, trainingPublicPlanId, longtime)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getExamView(examId: String, trainingPublicPlanId: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getExamView(examId, trainingPublicPlanId)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun uploadFile(filePath: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.uploadFile(filePath)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun submitExam(request: SubmitExamRequest) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.submitExam(request)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getExamResult(examId: String, trainingPublicPlanId: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getExamResult(examId, trainingPublicPlanId)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getQuestionView(examId: String, questionId: String, uanswer: String, trainingPublicPlanId: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getQuestionView(examId, questionId, uanswer, trainingPublicPlanId)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun createOrder(money: String, trainingPublicPlanId: Int, type: String, method: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.createOrder(money, trainingPublicPlanId, type, method)
                if (response.isSuccessful && response.body()?.code != null) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun yearPay(code: Int, type: String, method: String, yearId: Int) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.yearPay(code, type, method, yearId)
                if (response.isSuccessful && response.body()?.code != null) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getOrderList(page: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getOrderList(page)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun safeFace(imgurl: String, trainingPublicPlanId: Int, type: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.safeFace(imgurl, trainingPublicPlanId, type)
                if (response.isSuccessful && response.body()?.code != null) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun safetyAdd(subjectId: String, trainingSafetyPlanId: Int, longtime: Int, imgurl: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.safetyAdd(subjectId, trainingSafetyPlanId, longtime, imgurl)
                if (response.isSuccessful && response.body()?.code != null) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun beforeSubjectFace(imgurl: String, trainingPublicPlanId: Int, type: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.beforeSubjectFace(imgurl, trainingPublicPlanId, type)
                if (response.isSuccessful && response.body()?.code != null) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun subjectFace(signfile: String, id: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.subjectFace(signfile, id)
                if (response.isSuccessful && response.body()?.code != null) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun singPost(request: RequestBody) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.singPost(request)
                if (response.isSuccessful && response.body()?.code != null) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getTwoList(userCategoryId: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getTwoList(userCategoryId)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun twoOrderPay(questionCategoryId: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.twoOrderPay(questionCategoryId)
                if (response.isSuccessful && response.body()?.code != null) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun selectTwoQuestionList(userExamId: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.selectTwoQuestionList(userExamId)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun startTwoAnswer(request: StartTwoAnswerRequest) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.startTwoAnswer(request)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateTwoQuestion(request: UpdateTwoQuestionRequest) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.updateTwoQuestion(request)
                if (response.isSuccessful && response.body()?.code != null) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun createTwoOrder(type: String, method: String, questionCategoryId: Int) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.createTwoOrder(type, method, questionCategoryId)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getMeetingList(page: String, status: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getMeetingList(page, status)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getMeetingView(id: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getMeetingView(id)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getSubjectList(page: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getSubjectList(page)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun subjectOrder(trainingSafetyPlanId: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.subjectOrder(trainingSafetyPlanId)
                if (response.isSuccessful && response.body()?.code != null) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getSubCoursewareList(page: String, trainingSafetyPlanId: String, number: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getSubCoursewareList(page, trainingSafetyPlanId, number)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun subjectStudy(subjectId: String, trainingSafetyPlanId: String, longtime: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.subjectStudy(subjectId, trainingSafetyPlanId, longtime)
                if (response.isSuccessful && response.body()?.code != null) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun beforeOrderPay() {
        viewModelScope.launch {
            try {
                val response = trainingRepository.beforeOrderPay()
                if (response.isSuccessful && response.body()?.code != null) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getBeforeSubjectList() {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getBeforeSubjectList()
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getBeforeSubCoursewareList(page: String, trainingSafetyPlanId: String, number: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getBeforeSubCoursewareList(page, trainingSafetyPlanId, number)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getBeforeCoursewareView(trainingSafetyPlanId: String, subjectId: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getBeforeCoursewareView(trainingSafetyPlanId, subjectId)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun beforeSubjectStudy(subjectId: String, trainingSafetyPlanId: String, longtime: String, number: String, pageScoll: String?) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.beforeSubjectStudy(subjectId, trainingSafetyPlanId, longtime, number, pageScoll)
                if (response.isSuccessful && response.body()?.code != null) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun beforeExamInfo() {
        viewModelScope.launch {
            try {
                val response = trainingRepository.beforeExamInfo()
                if (response.isSuccessful && response.body()?.code != null) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getQuestionList() {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getQuestionList()
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun questionOrderPay(questionCategoryId: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.questionOrderPay(questionCategoryId)
                if (response.isSuccessful && response.body()?.code != null) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun travelPost(request: TravelPostRequest) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.travelPost(request)
                if (response.isSuccessful && response.body() != null) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun travelDel(id: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.travelDel(id)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun startAnswer(request: StartAnswerRequest) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.startAnswer(request)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun selectQuestionList(userExamId: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.selectQuestionList(userExamId)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun answer(request: AnswerRequest) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.answer(request)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun carnumSearch(carnum: String, page: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.carnumSearch(carnum, page)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun resetPwd(newpassword: String, oldpassword: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.resetPwd(newpassword, oldpassword)
                if (response.isSuccessful && response.body()?.code != null) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getUserStudyProveList(month: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getUserStudyProveList(month)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getEducationCertificate() {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getEducationCertificate()
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getBeforeEducationCertificate() {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getBeforeEducationCertificate()
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getStudySafetyList(month: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getStudySafetyList(month)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getCarCheck() {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getCarCheck()
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun carCheckPost(request: CarCheckPostRequest) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.carCheckPost(request)
                if (response.isSuccessful && response.body()?.code != null) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getDanger() {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getDanger()
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun dangerPost(request: DangerPostRequest) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.dangerPost(request)
                if (response.isSuccessful && response.body()?.code != null) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getMyJobList(page: String, type: String) {
        viewModelScope.launch {
            try {
                val response = trainingRepository.getMyJobList(page, type)
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun logoff() {
        viewModelScope.launch {
            try {
                val response = trainingRepository.logoff()
                if (response.isSuccessful && response.body()?.code == 1) {
                    // Handle successful response
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}