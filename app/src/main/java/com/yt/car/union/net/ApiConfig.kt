package com.yt.car.union.net

object ApiConfig {
    // 基础域名（与你提供的接口一致）
    const val BASE_URL = "http://www.ezbeidou.com/prod-api/"
    const val BASE_URL_TRAINING = "http://safe.ezbeidou.com"

    // 超时时间（单位：秒）
    const val CONNECT_TIMEOUT = 15L
    const val READ_TIMEOUT = 15L
    const val WRITE_TIMEOUT = 15L
    // Token存储键（项目全局，与前序接口一致）
    const val SP_KEY_TOKEN = "user_token"

    // 车牌颜色静态配置（与前端label/value完全一致）
    enum class CarColor(val value: Int, val label: String) {
        BLUE(1, "蓝色"),
        WHITE(4, "白色"),
        YELLOW(2, "黄色"),
        BLACK(3, "黑色"),
        OTHER(9, "其他");

        // 根据value获取对应的label
        companion object {
            fun getLabelByValue(value: Int): String {
                return values().find { it.value == value }?.label ?: "其他"
            }
        }
    }

    // 接口成功状态码（与前端一致：终端厂商=1，车辆类型=200）
    object SuccessCode {
        const val MANUFACTURER = 1    // 终端厂商列表成功
        const val CAR_TYPE = 200      // 车辆类型列表成功
        const val DEFAULT = 200       // 机构搜索默认成功状态码（前端无显式标注）
    }
}