package com.yt.car.union.pages.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yt.car.union.databinding.ItemAlarmSafetyBinding
import com.yt.car.union.net.bean.AlarmSafetyBean

class AlarmSafetyAdapter(private val mData: List<AlarmSafetyBean>) :
    RecyclerView.Adapter<AlarmSafetyAdapter.AlarmSafetyViewHolder>() {

    inner class AlarmSafetyViewHolder(val binding: ItemAlarmSafetyBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmSafetyViewHolder {
        val binding = ItemAlarmSafetyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlarmSafetyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmSafetyViewHolder, position: Int) {
        val bean = mData[position]
        holder.binding.tvType.text = bean.type
        holder.binding.tvCount.text = bean.count.toString()
    }

    override fun getItemCount() = mData.size
}