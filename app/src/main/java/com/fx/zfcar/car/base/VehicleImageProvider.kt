package com.fx.zfcar.car.base

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.ScaleDrawable
import android.view.Gravity
import com.fx.zfcar.R
import androidx.core.graphics.scale

/**
 * 车辆图片资源提供者
 *
 * 根据车辆类型和状态返回对应的图片资源ID
 */
object VehicleImageProvider {

    // 状态映射
    private val statusMap = mapOf(
        1 to "xingshi",     // 绿色 - 行驶
        2 to "tingche",     // 蓝色 - 停车
        0 to "lixian",      // 黄色 - 离线
        4 to "weidingwei",  // 灰色 - 未定位
        3 to "baojing"      // 红色 - 报警
    )

    // 地图主页面 marker 状态图标，需与 ytcar-app common/controller.js 的 carIcon() 保持一致
    private val mapMarkerResMap = mapOf(
        1 to R.drawable.treecargreen,
        2 to R.drawable.treecarblue,
        0 to R.drawable.treecaryellow,
        4 to R.drawable.treecargray,
        3 to R.drawable.treecarred
    )

    /**
     * 根据车辆ID和状态获取图片资源ID
     *
     * @param vehicleId 车辆ID
     * @param status 车辆状态
     * @return 对应的图片资源ID
     */
    fun getVehicleImageResId(vehicleId: String, status: Int): Int {
        val vehicleType = getGeneralVehicleType(vehicleId)
        val statusKey = statusMap.getOrDefault(status, "weidingwei")

        return getResIdByName(vehicleType, statusKey)
    }

    /**
     * 详情卡片顶部车辆图标按 ytcar-app pages/map/mapHome.nvue 的规则单独处理。
     */
    fun getDetailVehicleImageResId(vehicleId: String, status: Int): Int {
        val vehicleType = getDetailVehicleType(vehicleId)
        val statusKey = statusMap.getOrDefault(status, "weidingwei")

        return getResIdByName(vehicleType, statusKey)
    }

    /**
     * 地图 marker 图标与 ytcar-app 当前实现对齐，只按状态取统一圆点图标。
     */
    fun getMapMarkerResId(status: Int): Int {
        return mapMarkerResMap[status] ?: R.drawable.treecargray
    }

    /**
     * 根据车辆ID获取车辆类型
     *
     * @param vehicleId 车辆ID
     * @return 车辆类型
     */
    private fun getGeneralVehicleType(vehicleId: String): String {
        val normalizedVehicleId = vehicleId.trim()
        return when {
            normalizedVehicleId.contains("客") -> "keche"
            normalizedVehicleId.contains("轿") ||
                normalizedVehicleId.contains("小型") ||
                normalizedVehicleId.contains("小客") ||
                normalizedVehicleId.contains("小车") ||
                normalizedVehicleId.contains("微型") ||
                normalizedVehicleId.contains("乘用") -> "jiaoche"
            normalizedVehicleId.contains("货") -> "huoche"
            normalizedVehicleId.contains("特") -> "teshuche"
            normalizedVehicleId.contains("拖拉") -> "tuolaji"
            normalizedVehicleId.startsWith("K") -> "keche"              // K系列均为客车
            normalizedVehicleId == "14" -> "jiaoche"                    // 小型轿车
            Regex("^(10|11|12|13|15|16)$").matches(normalizedVehicleId) -> "keche"  // 客车
            Regex("2[0-3]").matches(normalizedVehicleId) -> "huoche"        // 货车 20-23
            Regex("3[0-9]|40").matches(normalizedVehicleId) -> "teshuche"   // 特殊车辆 30-40
            Regex("^(5\\d|60|61|62|63|64)$").matches(normalizedVehicleId) -> "tuolaji" // 拖拉机
            else -> "qitache"                                    // 其他车辆
        }
    }

    private fun getDetailVehicleType(vehicleId: String): String {
        val normalizedVehicleId = vehicleId.trim()
        return when {
            normalizedVehicleId.contains("客") -> "keche"
            normalizedVehicleId.contains("轿") ||
                normalizedVehicleId.contains("小型") ||
                normalizedVehicleId.contains("小客") ||
                normalizedVehicleId.contains("小车") ||
                normalizedVehicleId.contains("微型") ||
                normalizedVehicleId.contains("乘用") -> "jiaoche"
            normalizedVehicleId.contains("货") -> "huoche"
            normalizedVehicleId.contains("特") -> "teshuche"
            normalizedVehicleId.contains("拖拉") -> "tuolaji"
            normalizedVehicleId in setOf("10", "12", "13", "14", "K13", "K23", "K26", "K31") -> "jiaoche"
            else -> getGeneralVehicleType(normalizedVehicleId)
        }
    }

    /**
     * 根据车辆类型和状态获取资源ID
     *
     * @param vehicleType 车辆类型
     * @param status 状态
     * @return 资源ID
     */
    private fun getResIdByName(vehicleType: String, status: String): Int {
        val resourceName = "${vehicleType}_${status}"

        return when (resourceName) {
            // 客车
            "keche_xingshi" -> R.drawable.ic_keche_xingshi
            "keche_tingche" -> R.drawable.ic_keche_tingche
            "keche_lixian" -> R.drawable.ic_keche_lixian
            "keche_weidingwei" -> R.drawable.ic_keche_weidingwei
            "keche_baojing" -> R.drawable.ic_keche_baojing

            // 轿车
            "jiaoche_xingshi" -> R.drawable.ic_jiaoche_xingshi
            "jiaoche_tingche" -> R.drawable.ic_jiaoche_tingche
            "jiaoche_lixian" -> R.drawable.ic_jiaoche_lixian
            "jiaoche_weidingwei" -> R.drawable.ic_jiaoche_weidingwei
            "jiaoche_baojing" -> R.drawable.ic_jiaoche_baojing

            // 货车
            "huoche_xingshi" -> R.drawable.ic_huoche_xingshi
            "huoche_tingche" -> R.drawable.ic_huoche_tingche
            "huoche_lixian" -> R.drawable.ic_huoche_lixian
            "huoche_weidingwei" -> R.drawable.ic_huoche_weidingwei
            "huoche_baojing" -> R.drawable.ic_huoche_baojing

            // 特殊车辆
            "teshuche_xingshi" -> R.drawable.ic_teshuche_xingshi
            "teshuche_tingche" -> R.drawable.ic_teshuche_tingche
            "teshuche_lixian" -> R.drawable.ic_teshuche_lixian
            "teshuche_weidingwei" -> R.drawable.ic_teshuche_weidingwei
            "teshuche_baojing" -> R.drawable.ic_teshuche_baojing

            // 拖拉机
            "tuolaji_xingshi" -> R.drawable.ic_tuolaji_xingshi
            "tuolaji_tingche" -> R.drawable.ic_tuolaji_tingche
            "tuolaji_lixian" -> R.drawable.ic_tuolaji_lixian
            "tuolaji_weidingwei" -> R.drawable.ic_tuolaji_weidingwei
            "tuolaji_baojing" -> R.drawable.ic_tuolaji_baojing

            // 其他车辆
            "qitache_xingshi" -> R.drawable.ic_qitache_xingshi
            "qitache_tingche" -> R.drawable.ic_qitache_tingche
            "qitache_lixian" -> R.drawable.ic_qitache_lixian
            "qitache_weidingwei" -> R.drawable.ic_qitache_weidingwei
            "qitache_baojing" -> R.drawable.ic_qitache_baojing

            // 默认图片
            else -> R.drawable.ic_qitache_weidingwei
        }
    }

    /**
     * 获取所有支持的车辆类型
     */
    fun getAllVehicleTypes(): List<String> {
        return listOf("keche", "jiaoche", "huoche", "teshuche", "tuolaji", "qitache")
    }

    /**
     * 获取所有支持的状态
     */
    fun getAllStatus(): Map<Int, String> {
        return statusMap
    }

    fun scaleBitmapDrawable(context: Context, drawableResId: Int, scale: Float): Drawable {
        // 解码原始位图
        val originalBitmap = BitmapFactory.decodeResource(context.resources, drawableResId)


        // 缩放位图
        val scaledBitmap = originalBitmap.scale(
            (originalBitmap.width * scale).toInt(),
            (originalBitmap.height * scale).toInt()
        )

        // 回收原始位图，避免内存泄漏
        originalBitmap.recycle()

        // 返回新的 BitmapDrawable
        return BitmapDrawable(context.resources, scaledBitmap)
    }
}
