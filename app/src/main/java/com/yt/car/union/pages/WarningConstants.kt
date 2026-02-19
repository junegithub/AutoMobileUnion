package com.yt.car.union.pages


/**
 * 报警相关全局常量单例
 * 所有报警相关的全局数据都放在这里，便于维护
 */
object WarningConstants {
    // 报警类型Map，全局可通过 WarningConstants.WARNING_TYPE_MAP 访问
    val WARNING_TYPE_MAP: Map<Int, String> = mapOf(
        0 to "紧急报警",
        1 to "超速报警",
        2 to "疲劳驾驶",
        3 to "危险预警",
        5 to "GNSS天线未接或被剪断",
        9 to "终端LCD或显示器故障",
        11 to "摄像头故障",
        12 to "道路运输证IC卡模块故障",
        13 to "超速预警",
        14 to "疲劳驾驶预警",
        20 to "进出区域",
        21 to "进出路线",
        22 to "路段行驶时间不足/过长",
        23 to "路线偏离报警",
        24 to "车辆VSS故障",
        25 to "车辆油量异常",
        26 to "车辆被盗(通过车辆防盗器)",
        31 to "非法开门报警",
        35 to "夜间禁行",
        7 to "终端电压欠压",
        18 to "当天累计疲劳超时数量",
        8 to "终端电压掉电",
        19 to "临时停车报警数量",
        4 to "GNSS模块发生故障数量",
        6 to "GNSS天线短路",
        10 to "TTS 模块故障"
    )

    // 可扩展其他报警相关常量
    const val DEFAULT_WARNING_NAME = "未知报警类型"
}