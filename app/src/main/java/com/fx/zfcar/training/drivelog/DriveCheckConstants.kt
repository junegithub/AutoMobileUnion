package com.fx.zfcar.training.drivelog

// 检查项常量
object DriveCheckConstants {

    val ROAD_STATUS_LIST = listOf(
        SelectItem(0, "正常"),
        SelectItem(1, "不正常")
    )

    // 行车前检查项
    val BEFORE_DRIVE_ITEMS = listOf(
        CheckItem(0, "灯光"),
        CheckItem(1,"刹车"),
        CheckItem(2,"轮胎"),
        CheckItem(3,"引擎"),
        CheckItem(4,"仪表"),
        CheckItem(5,"灯、牌"),
        CheckItem(6,"阀门"),
        CheckItem(7,"装载情况"),
        CheckItem(8,"GPS状况"),
        CheckItem(9,"安全卡"),
        CheckItem(10,"防护用品"),
        CheckItem(11,"机油"),
        CheckItem(12,"燃油"),
        CheckItem(13,"玻璃水"),
        CheckItem(14,"液压油"),
        CheckItem(15,"刹车油"),
        CheckItem(16,"机油"),
        CheckItem(17,"防冻液")
    )

    // 行车中检查项
    val DRIVING_ITEMS = listOf(
        CheckItem(0,"方向"),
        CheckItem(1,"灯光"),
        CheckItem(2,"刹车"),
        CheckItem(3,"轮胎"),
        CheckItem(4,"引擎"),
        CheckItem(5,"仪表"),
        CheckItem(6,"灯、牌"),
        CheckItem(7,"阀门"),
        CheckItem(8,"装载情况"),
        CheckItem(9,"GPS状况"),
        CheckItem(10,"防护用品")
    )

    // 收车后检查项
    val AFTER_DRIVE_ITEMS = listOf(
        CheckItem(0,"外观"),
        CheckItem(1,"门锁"),
        CheckItem(2,"轮胎"),
        CheckItem(3,"电源"),
        CheckItem(4,"其他")
    )

    // 行车前检查结果
    val BEFORE_DRIVE_RESULTS = listOf(
        CheckResult(0, "良好，可以行驶"),
        CheckResult(1, "需修复，检验合格后行驶"),
        CheckResult(2, "不符合，禁止出车")
    )

    // 行车中检查结果
    val DRIVING_RESULTS = listOf(
        CheckResult(0, "车辆和道路良好，可以继续行驶"),
        CheckResult(1, "车须修复，检验合格后行驶"),
        CheckResult(2, "车或路不合格，禁止行车维修或改变路线")
    )

    // 收车后检查结果
    val AFTER_DRIVE_RESULTS = listOf(
        CheckResult(0, "良好，可以停放"),
        CheckResult(1, "须修复，维修合格后停放")
    )

    // 检查项数据类
    data class CheckItem(var id: Int = 0, var name: String, var active: Boolean = false)

    // 检查结果数据类
    data class CheckResult(val value: Int, val label: String)

    // 货物类型列表
    val TYPE_LIST = listOf(
        SelectItem(0, "危险品"),
        SelectItem(1, "普通货物")
    )

    // 选择项数据类
    data class SelectItem(val value: Int, val label: String)

    // 本地表单数据模型（用于UI交互，最后转换为TravelPostRequest）
    data class LocalFormData(
        var id: Int = 0,
        var car_id: Int = 0,
        var driver_name: String = "",
        var addtime: String = "",
        var carnum: String = "",
        var user_id: Int = 0,
        var type: String = "危险品", // UI显示用："危险品"/"普通货物"
        var copilot_name: String = "",
        var weather: String = "",
        var temperature: String = "",
        var load: String = "",
        var real_load: String = "",
        var goods_name: String = "",
        var gotime: String = "",
        var gettime: String = "", // UI显示用字符串，提交时转Int
        var start_address: String = "",
        var end_address: String = "",
        var mileage: String = "",
        var sresult: Int? = null,
        var groad: Int? = null, // UI用Int，提交时转String
        var gresult: Int? = null, // UI用Int，提交时转String
        var stopresult: String = "",
        var stopaddress: String = "",
        var stoptime: String = "",
        var eresult: Int? = null,
        var dsingimg: String = "",
        var ysingimg: String = "", // 副驾驶签名（本地使用）
        var staus: String = "0",
        var updatetime: String = ""
    )
}