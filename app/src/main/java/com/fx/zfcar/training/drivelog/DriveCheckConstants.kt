package com.fx.zfcar.training.drivelog

// 检查项常量
object DriveCheckConstants {
    // 行车前检查项
    val BEFORE_DRIVE_ITEMS = listOf(
        CheckItem("灯光"),
        CheckItem("刹车"),
        CheckItem("轮胎"),
        CheckItem("引擎"),
        CheckItem("仪表"),
        CheckItem("灯、牌"),
        CheckItem("阀门"),
        CheckItem("装载情况"),
        CheckItem("GPS状况"),
        CheckItem("安全卡"),
        CheckItem("防护用品"),
        CheckItem("机油"),
        CheckItem("燃油"),
        CheckItem("玻璃水"),
        CheckItem("液压油"),
        CheckItem("刹车油"),
        CheckItem("机油"),
        CheckItem("防冻液")
    )

    // 行车中检查项
    val DRIVING_ITEMS = listOf(
        CheckItem("方向"),
        CheckItem("灯光"),
        CheckItem("刹车"),
        CheckItem("轮胎"),
        CheckItem("引擎"),
        CheckItem("仪表"),
        CheckItem("灯、牌"),
        CheckItem("阀门"),
        CheckItem("装载情况"),
        CheckItem("GPS状况"),
        CheckItem("防护用品")
    )

    // 收车后检查项
    val AFTER_DRIVE_ITEMS = listOf(
        CheckItem("外观"),
        CheckItem("门锁"),
        CheckItem("轮胎"),
        CheckItem("电源"),
        CheckItem("其他")
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
    data class CheckItem(val name: String, val active: Boolean = false)

    // 检查结果数据类
    data class CheckResult(val value: Int, val label: String)
}