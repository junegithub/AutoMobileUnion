package com.fx.zfcar.training.exam

data class ExamAnswerParams(
    val from: String,
    val userCategoryId: Int,
    val questionCategoryId: Int,
    val questionId: Int
)

object ExamAnswerParamsPolicy {
    fun parse(
        from: String,
        userCategoryId: String,
        questionCategoryId: String,
        questionId: Int
    ): ExamAnswerParams? {
        val parsedUserCategoryId = userCategoryId.trim().toIntOrNull() ?: return null
        val parsedQuestionCategoryId = questionCategoryId.trim().toIntOrNull() ?: return null

        if (parsedUserCategoryId <= 0 || parsedQuestionCategoryId <= 0) {
            return null
        }

        return ExamAnswerParams(
            from = from,
            userCategoryId = parsedUserCategoryId,
            questionCategoryId = parsedQuestionCategoryId,
            questionId = questionId
        )
    }
}

object ExamRolePolicy {
    private val normalRoles = listOf("驾驶员", "安全员", "押运员", "安全负责人")

    fun parseNormalRoles(stype: String): List<RoleModel> {
        return stype.split(",")
            .mapNotNull { item ->
                val roleId = item.trim()
                val index = roleId.toIntOrNull() ?: return@mapNotNull null
                val label = normalRoles.getOrNull(index) ?: return@mapNotNull null

                RoleModel(label, roleId)
            }
    }
}
