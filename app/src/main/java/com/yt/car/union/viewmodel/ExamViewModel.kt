package com.yt.car.union.viewmodel

import com.yt.car.union.net.AnswerData
import com.yt.car.union.net.AnswerRequest
import com.yt.car.union.net.ExamResultData
import com.yt.car.union.net.ExamViewData
import com.yt.car.union.net.QuestionListData
import com.yt.car.union.net.StartAnswerData
import com.yt.car.union.net.StartAnswerRequest
import com.yt.car.union.net.StartTwoAnswerData
import com.yt.car.union.net.StartTwoAnswerRequest
import com.yt.car.union.net.SubmitExamRequest
import com.yt.car.union.net.UpdateTwoQuestionRequest
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 考试答题相关ViewModel（题库、答题、提交、成绩等）
 */
class ExamViewModel : TrainingBaseViewModel() {

    // 获取考试视图
    fun getExamView(examId: String, trainingPublicPlanId: String,
                    stateFlow: MutableStateFlow<ApiState<ExamViewData>>) {
        launchRequest(
            block = { vehicleRepository.getExamView(examId, trainingPublicPlanId) },
            stateFlow
        )
    }

    // 提交考试
    fun submitExam(request: SubmitExamRequest, stateFlow: MutableStateFlow<ApiState<String>>) {
        launchRequest(
            block = { vehicleRepository.submitExam(request) },
            stateFlow
        )
    }

    // 获取考试结果
    fun getExamResult(examId: String, trainingPublicPlanId: String,
                      stateFlow: MutableStateFlow<ApiState<ExamResultData>>) {
        launchRequest(
            block = { vehicleRepository.getExamResult(examId, trainingPublicPlanId) },
            stateFlow
        )
    }

    // 开始答题
    fun startAnswer(request: StartAnswerRequest,
                    stateFlow: MutableStateFlow<ApiState<StartAnswerData>>) {
        launchRequest(
            block = { vehicleRepository.startAnswer(request) },
            stateFlow
        )
    }

    // 答题提交
    fun answer(request: AnswerRequest, stateFlow: MutableStateFlow<ApiState<AnswerData>>) {
        launchRequest(
            block = { vehicleRepository.answer(request) },
            stateFlow
        )
    }

    // 二类答题-开始
    fun startTwoAnswer(request: StartTwoAnswerRequest,
                       stateFlow: MutableStateFlow<ApiState<StartTwoAnswerData>>) {
        launchRequest(
            block = { vehicleRepository.startTwoAnswer(request) },
            stateFlow
        )
    }

    // 二类答题-更新
    fun updateTwoQuestion(request: UpdateTwoQuestionRequest,
                          stateFlow: MutableStateFlow<ApiState<String>>) {
        launchRequest(
            block = { vehicleRepository.updateTwoQuestion(request) },
            stateFlow
        )
    }

    // 其他考试相关方法...
    fun getQuestionList(stateFlow: MutableStateFlow<ApiState<QuestionListData>>) {
        launchRequest(
            block = { vehicleRepository.getQuestionList() },
            stateFlow
        )
    }
}