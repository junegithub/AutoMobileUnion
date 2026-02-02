package com.yt.car.union.net.bean

data class UserInfoResponse (
    val info: UserInfo = UserInfo() // 给嵌套对象加默认值
)

data class UserInfo(
    val createtime: Long = 0,
    val group_id: Int = 0,
    val nickname: String = "",
    val username: String = ""
)