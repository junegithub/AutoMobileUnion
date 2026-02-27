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

    /**
     * 根据车辆ID和状态获取图片资源ID
     *
     * @param vehicleId 车辆ID
     * @param status 车辆状态
     * @return 对应的图片资源ID
     */
    fun getVehicleImageResId(vehicleId: String, status: Int): Int {
        val vehicleType = getVehicleType(vehicleId)
        val statusKey = statusMap.getOrDefault(status, "weidingwei")

        return getResIdByName(vehicleType, statusKey)
    }

    /**
     * 根据车辆ID获取车辆类型
     *
     * @param vehicleId 车辆ID
     * @return 车辆类型
     */
    private fun getVehicleType(vehicleId: String): String {
        return when {
            Regex("1[0-35-6]").matches(vehicleId) -> "keche"       // 客车 10-16
            vehicleId == "14" -> "jiaoche"                        // 轿车
            Regex("2[0-3]").matches(vehicleId) -> "huoche"        // 货车 20-23
            Regex("3[0-9]|40").matches(vehicleId) -> "teshuche"   // 特殊车辆 30-40
            Regex("5[0-9]|6[0-4]").matches(vehicleId) -> "tuolaji" // 拖拉机 50-64
            else -> "qitache"                                    // 其他车辆
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