package com.yt.car.union.pages.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yt.car.union.databinding.ItemMileageBinding
import com.yt.car.union.net.bean.MileageBean

class MileageAdapter(private val mData: List<MileageBean>) :
    RecyclerView.Adapter<MileageAdapter.MileageViewHolder>() {

    inner class MileageViewHolder(val binding: ItemMileageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MileageViewHolder {
        val binding = ItemMileageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MileageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MileageViewHolder, position: Int) {
        val bean = mData[position]
        holder.binding.tvCarNum.text = bean.carNum
        holder.binding.tvDate.text = bean.date
        holder.binding.tvMileage.text = bean.mileage.toString()
    }

    override fun getItemCount() = mData.size
}