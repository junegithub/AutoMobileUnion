package com.yt.car.union.pages.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.yt.car.union.R

/**
 * 自定义RecyclerView分割线（仅显示底部分割线，匹配截图样式）
 */
class DeviceStatusDivider(context: Context) : RecyclerView.ItemDecoration() {
    private val divider: Drawable? = ContextCompat.getDrawable(context, R.drawable.divider_horizontal)

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        val left = parent.paddingLeft + parent.resources.getDimensionPixelOffset(R.dimen.item_padding_horizontal)
        val right = parent.width - parent.paddingRight - parent.resources.getDimensionPixelOffset(R.dimen.item_padding_horizontal)

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + (divider?.intrinsicHeight ?: 0)
            divider?.setBounds(left, top, right, bottom)
            divider?.draw(c)
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        // 最后一项不显示分割线
        if (parent.getChildAdapterPosition(view) != parent.adapter?.itemCount?.minus(1)) {
            outRect.bottom = divider?.intrinsicHeight ?: 1
        }
    }
}