package com.yt.car.union.util

import android.content.Context
import android.view.View
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.yt.car.union.R
import com.yt.car.union.net.MapPositionItem
import com.yt.car.union.net.bean.CarStatusItem

object MarkerViewUtil {

    /**
     * 将自定义布局转成高德可用的 BitmapDescriptor
     */
    fun createCarMarker(
        context: Context,
        carStatus: MapPositionItem
    ): BitmapDescriptor {
        // 加载布局
        val view = LayoutInflater.from(context).inflate(R.layout.view_car_marker, null)
        val ivCar = view.findViewById<ImageView>(R.id.iv_car_icon)
        val tvNum = view.findViewById<TextView>(R.id.tv_car_num)

        ivCar.setImageResource(VehicleImageProvider.getVehicleImageResId(carStatus.dlcartype.toString(), carStatus.status))
        ivCar.rotation = 90f - carStatus.rotation.toFloat()
        tvNum.text = carStatus.carnum

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