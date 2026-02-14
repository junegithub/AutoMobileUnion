package com.yt.car.union.viewmodel

import com.yt.car.union.net.AnswerRequest
import com.yt.car.union.net.StartAnswerRequest
import com.yt.car.union.net.StartTwoAnswerRequest
import com.yt.car.union.net.SubmitExamRequest
import com.yt.car.union.net.UpdateTwoQuestionRequest

/**
 * 考试答题相关ViewModel（题库、答题、提交、成绩等）
 */
class ExamViewModel : TrainingBaseViewModel() {

    // 获取考试视图
    fun getExamView(examId: String, trainingPublicPlanId: String) {
        launchRequest(
            block = { vehicleRepository.getExamView(examId, trainingPublicPlanId) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 提交考试
    fun submitExam(request: SubmitExamRequest) {
        launchRequest(
            block = { vehicleRepository.submitExam(request) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 获取考试结果
    fun getExamResult(examId: String, trainingPublicPlanId: String) {
        launchRequest(
            block = { vehicleRepository.getExamResult(examId, trainingPublicPlanId) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 开始答题
    fun startAnswer(request: StartAnswerRequest) {
        launchRequest(
            block = { vehicleRepository.startAnswer(request) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 答题提交
    fun answer(request: AnswerRequest) {
        launchRequest(
            block = { vehicleRepository.answer(request) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 二类答题-开始
    fun startTwoAnswer(request: StartTwoAnswerRequest) {
        launchRequest(
            block = { vehicleRepository.startTwoAnswer(request) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 二类答题-更新
    fun updateTwoQuestion(request: UpdateTwoQuestionRequest) {
        launchRequest(
            block = { vehicleRepository.updateTwoQuestion(request) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code != null) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 其他考试相关方法...
    fun getQuestionList() {
        launchRequest(
            block = { vehicleRepository.getQuestionList() },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }
}