package com.fx.zfcar.training.exam

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExamAnswerParamsPolicyTest {
    @Test
    fun parsesValidAnswerParams() {
        val params = ExamAnswerParamsPolicy.parse(
            from = "twoList",
            userCategoryId = "1",
            questionCategoryId = "97",
            questionId = 123
        )

        assertEquals(
            ExamAnswerParams(
                from = "twoList",
                userCategoryId = 1,
                questionCategoryId = 97,
                questionId = 123
            ),
            params
        )
    }

    @Test
    fun rejectsBlankAndNonNumericAnswerParams() {
        assertNull(
            ExamAnswerParamsPolicy.parse(
                from = "twoList",
                userCategoryId = "",
                questionCategoryId = "97",
                questionId = 0
            )
        )
        assertNull(
            ExamAnswerParamsPolicy.parse(
                from = "",
                userCategoryId = "abc",
                questionCategoryId = "97",
                questionId = 0
            )
        )
    }

    @Test
    fun rejectsZeroAndNegativeAnswerParams() {
        assertNull(
            ExamAnswerParamsPolicy.parse(
                from = "",
                userCategoryId = "0",
                questionCategoryId = "97",
                questionId = 0
            )
        )
        assertNull(
            ExamAnswerParamsPolicy.parse(
                from = "",
                userCategoryId = "1",
                questionCategoryId = "-1",
                questionId = 0
            )
        )
    }

    @Test
    fun filtersInvalidNormalRoleIds() {
        val roles = ExamRolePolicy.parseNormalRoles("0, ,abc,-1,3,9")

        assertEquals(
            listOf(
                RoleModel("驾驶员", "0"),
                RoleModel("安全负责人", "3")
            ),
            roles
        )
    }
}
