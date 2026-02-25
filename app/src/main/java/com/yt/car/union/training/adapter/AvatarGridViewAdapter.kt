package com.yt.car.union.training.adapter

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide


class AvatarGridViewAdapter (
    private val mContext: Context, private val mAvatarUrls: List<String>, // 图片尺寸（px）
    private val mAvatarSize: Int = 100
) : BaseAdapter() {

    override fun getCount(): Int {
        return mAvatarUrls.size
    }

    override fun getItem(position: Int): String {
        return mAvatarUrls[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val imageView: ImageView
        // 复用View，提升性能
        if (convertView == null) {
            imageView = ImageView(mContext)
            // 设置图片尺寸
            val params = ViewGroup.LayoutParams(mAvatarSize, mAvatarSize)
            imageView.setLayoutParams(params)
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP)
            imageView.setBackgroundColor(Color.parseColor("#F0F0F0"))
        } else {
            imageView = convertView as ImageView
        }

        // 加载网络图片（圆形裁剪）
        val url = mAvatarUrls[position]
        Glide.with(mContext)
            .load(url)
            .into(imageView)

        return imageView
    }
}