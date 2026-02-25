package com.yt.car.union.car.base

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.yt.car.union.R
import com.yt.car.union.car.base.VehicleImageProvider

object MarkerViewUtil {

    /**
     * 将自定义布局转成高德可用的 BitmapDescriptor
     */
    fun createCarMarker(
        context: Context,
        dlcartype: String, status: Int, rotation: Float, carnum: String
    ): BitmapDescriptor {
        // 加载布局
        val view = LayoutInflater.from(context).inflate(R.layout.view_car_marker, null)
        val ivCar = view.findViewById<ImageView>(R.id.iv_car_icon)
        val tvNum = view.findViewById<TextView>(R.id.tv_car_num)

        ivCar.setImageResource(VehicleImageProvider.getVehicleImageResId(dlcartype, status))
        ivCar.rotation = 90f - rotation
        tvNum.text = carnum

        // 测量 + 绘制View到Bitmap
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        // 转为高德Descriptor（自动管理内存）
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}