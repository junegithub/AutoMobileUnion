package com.fx.zfcar.training.safetytraining

import com.fx.zfcar.net.BeforeSubjectStudyData

sealed class BeforeStudyAction {
    data class NextCourse(val subjectId: Int) : BeforeStudyAction()
    data class EndFace(val examId: Int) : BeforeStudyAction()
    data object Stay : BeforeStudyAction()
}

sealed class BeforeFaceAction {
    data object Sign : BeforeFaceAction()
    data class Exam(val examId: Int, val planId: Int) : BeforeFaceAction()
    data object CourseList : BeforeFaceAction()
}

object BeforeTrainingFlowPolicy {
    fun afterStudy(data: BeforeSubjectStudyData): BeforeStudyAction {
        if (data.nextsubject_id > 0) {
            return BeforeStudyAction.NextCourse(data.nextsubject_id)
        }

        if (data.isend == 0) {
            return BeforeStudyAction.EndFace(data.training_exams_id)
        }

        return BeforeStudyAction.Stay
    }

    fun afterFace(faceType: String, needSign: String, examId: String, planId: String): BeforeFaceAction {
        if (faceType != "end") {
            return BeforeFaceAction.CourseList
        }

        if (needSign == "1") {
            return BeforeFaceAction.Sign
        }

        return examAction(examId, planId) ?: BeforeFaceAction.CourseList
    }

    fun afterSign(examId: String, planId: String): BeforeFaceAction {
        return examAction(examId, planId) ?: BeforeFaceAction.CourseList
    }

    private fun examAction(examId: String, planId: String): BeforeFaceAction.Exam? {
        val parsedExamId = examId.toIntOrNull() ?: return null
        val parsedPlanId = planId.toIntOrNull() ?: return null

        if (parsedExamId <= 0 || parsedPlanId <= 0) {
            return null
        }

        return BeforeFaceAction.Exam(parsedExamId, parsedPlanId)
    }
}
