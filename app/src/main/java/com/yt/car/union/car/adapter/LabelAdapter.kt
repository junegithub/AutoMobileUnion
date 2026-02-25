package com.yt.car.union.car.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yt.car.union.databinding.ItemLabelBinding
import com.yt.car.union.net.SearchResult
import com.yt.car.union.pages.EventData
import com.yt.car.union.util.PressEffectUtils
import org.greenrobot.eventbus.EventBus

class LabelAdapter() :
    RecyclerView.Adapter<LabelAdapter.VehicleViewHolder>() {

    private val vehicleList: MutableList<SearchResult> = emptyList<SearchResult>().toMutableList()

    class VehicleViewHolder(private val binding: ItemLabelBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bean: SearchResult?) {
            binding.vehicleNumber.text = bean?.content
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val binding = ItemLabelBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        val holder = VehicleViewHolder(binding)
        binding.vehicleNumber.setOnClickListener {v ->
            EventBus.getDefault().post(EventData(EventData.EVENT_LABEL_DETAIL, (v as TextView).text))
        }
        PressEffectUtils.setCommonPressEffect(binding.vehicleNumber)
        return holder
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        val vehicle = vehicleList[position]
        holder.bind(vehicle)
    }

    fun updateData(data: List<SearchResult>?) {
        data?.let {
            if (!data.isEmpty()) {
                vehicleList.clear()
                vehicleList.addAll(data)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount() = vehicleList.size
}