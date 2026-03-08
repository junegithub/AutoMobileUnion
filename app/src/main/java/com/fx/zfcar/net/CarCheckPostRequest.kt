package com.fx.zfcar.net

data class CarCheckPostRequest(
    val carnum: String,
    val name: String,
    val checktime: String,
    val company: String,
    val car_certificate_status: String,
    val car_certificate_fileimg: String,
    val people_certificate_status: String,
    val people_certificate_fileimg: String,
    val insure_status: String,
    val insure_fileimg: String,
    val car_status: String,
    val car_fileimg: String,
    val urgent_status: String,
    val urgent_fileimg: String,
    val sign_status: String,
    val sign_fileimg: String,
    val canbody_status: String,
    val canbody_fileimg: String,
    val cutoff_status: String,
    val cutoff_fileimg: String,
    val static_status: String,
    val static_fileimg: String,
    val waybill_status: String,
    val waybill_fileimg: String,
    val question: String,
    val idea: String,
    val checksign_img: String,
    val dirversign_img: String
)

// 本地数据存储模型（用于UI层临时存储）
data class CarCheckForm(
    // 基础信息
    var carnum: String = "",
    var name: String = "",
    var checktime: String = "",
    var company: String = "",

    // 检查状态（0=合格，1=不合格）
    var car_certificate_status: String = "0",
    var people_certificate_status: String = "0",
    var insure_status: String = "0",
    var car_status: String = "0",
    var urgent_status: String = "0",
    var sign_status: String = "0",
    var canbody_status: String = "0",
    var cutoff_status: String = "0",
    var static_status: String = "0",
    var waybill_status: String = "0",

    // 检查图片（URL列表，提交时转为逗号分隔的字符串）
    var car_certificate_fileimg: MutableList<String> = mutableListOf(),
    var people_certificate_fileimg: MutableList<String> = mutableListOf(),
    var insure_fileimg: MutableList<String> = mutableListOf(),
    var car_fileimg: MutableList<String> = mutableListOf(),
    var urgent_fileimg: MutableList<String> = mutableListOf(),
    var sign_fileimg: MutableList<String> = mutableListOf(),
    var canbody_fileimg: MutableList<String> = mutableListOf(),
    var cutoff_fileimg: MutableList<String> = mutableListOf(),
    var static_fileimg: MutableList<String> = mutableListOf(),
    var waybill_fileimg: MutableList<String> = mutableListOf(),

    // 问题和意见
    var question: String = "",
    var idea: String = "",

    // 签名图片（Base64或URL）
    var checksign_img: String = "",
    var dirversign_img: String = ""
) {
    // 转换为提交请求模型
    fun toPostRequest(): CarCheckPostRequest {
        return CarCheckPostRequest(
            carnum = carnum,
            name = name,
            checktime = checktime,
            company = company,
            car_certificate_status = car_certificate_status,
            car_certificate_fileimg = car_certificate_fileimg.joinToString(","),
            people_certificate_status = people_certificate_status,
            people_certificate_fileimg = people_certificate_fileimg.joinToString(","),
            insure_status = insure_status,
            insure_fileimg = insure_fileimg.joinToString(","),
            car_status = car_status,
            car_fileimg = car_fileimg.joinToString(","),
            urgent_status = urgent_status,
            urgent_fileimg = urgent_fileimg.joinToString(","),
            sign_status = sign_status,
            sign_fileimg = sign_fileimg.joinToString(","),
            canbody_status = canbody_status,
            canbody_fileimg = canbody_fileimg.joinToString(","),
            cutoff_status = cutoff_status,
            cutoff_fileimg = cutoff_fileimg.joinToString(","),
            static_status = static_status,
            static_fileimg = static_fileimg.joinToString(","),
            waybill_status = waybill_status,
            waybill_fileimg = waybill_fileimg.joinToString(","),
            question = question,
            idea = idea,
            checksign_img = checksign_img,
            dirversign_img = dirversign_img
        )
    }
}

enum class CheckStage(val step: Int, val progress: Int) {
    STAGE_1(1, 15),
    STAGE_2(2, 30),
    STAGE_3(3, 45),
    STAGE_4(4, 60),
    STAGE_5(5, 75),
    STAGE_6(6, 90),
    STAGE_7(7, 100);

    companion object {
        fun fromStep(step: Int): CheckStage {
            return values().find { it.step == step } ?: STAGE_1
        }
    }
}
