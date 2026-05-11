package com.fx.zfcar.training.safetytraining

import com.fx.zfcar.net.BeforeSubjectStudyData
import org.junit.Assert.assertEquals
import org.junit.Test

class BeforeTrainingFlowPolicyTest {
    @Test
    fun jumpsToNextCourseWhenBeforeStudyReturnsNextSubject() {
        val action = BeforeTrainingFlowPolicy.afterStudy(
            BeforeSubjectStudyData(
                nextsubject_id = 24,
                isend = 1,
                training_exams_id = 0
            )
        )

        assertEquals(BeforeStudyAction.NextCourse(24), action)
    }

    @Test
    fun asksForEndFaceWhenBeforeStudyFinishesCourseChain() {
        val action = BeforeTrainingFlowPolicy.afterStudy(
            BeforeSubjectStudyData(
                nextsubject_id = 0,
                isend = 0,
                training_exams_id = 88
            )
        )

        assertEquals(BeforeStudyAction.EndFace(88), action)
    }

    @Test
    fun startFaceReturnsToCourseListInsteadOfUsingStaleExamId() {
        val action = BeforeTrainingFlowPolicy.afterFace(
            faceType = "start",
            needSign = "0",
            examId = "88",
            planId = "12"
        )

        assertEquals(BeforeFaceAction.CourseList, action)
    }

    @Test
    fun endFaceRequiresSignatureBeforeExamWhenConfigured() {
        val action = BeforeTrainingFlowPolicy.afterFace(
            faceType = "end",
            needSign = "1",
            examId = "88",
            planId = "12"
        )

        assertEquals(BeforeFaceAction.Sign, action)
    }

    @Test
    fun signatureCompletionOpensExamWhenIdsAreValid() {
        val action = BeforeTrainingFlowPolicy.afterSign(
            examId = "88",
            planId = "12"
        )

        assertEquals(BeforeFaceAction.Exam(examId = 88, planId = 12), action)
    }
}
