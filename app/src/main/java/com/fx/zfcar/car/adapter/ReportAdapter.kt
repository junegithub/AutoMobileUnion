// ReportAdapter.kt
package com.fx.zfcar.car.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter4.BaseQuickAdapter
import com.fx.zfcar.net.ActiveWarningDataItem
import com.fx.zfcar.net.ExpiredCarItem
import com.fx.zfcar.net.LeakReportItem
import com.fx.zfcar.net.MileageDataItem
import com.fx.zfcar.net.OfflineReportItem
import com.fx.zfcar.net.OilAddReportItem
import com.fx.zfcar.net.OilDayReportItem
import com.fx.zfcar.net.PhotoReportItem
import com.fx.zfcar.net.WarningReportItem
import com.fx.zfcar.databinding.ItemMileageBinding
import com.fx.zfcar.databinding.ItemWarningBinding
import com.fx.zfcar.databinding.ItemActiveWarningBinding
import com.fx.zfcar.databinding.ItemPhotoBinding
import com.fx.zfcar.databinding.ItemExpiredBinding
import com.fx.zfcar.databinding.ItemOilAddBinding
import com.fx.zfcar.databinding.ItemOilDayBinding
import com.fx.zfcar.databinding.ItemLeakBinding
import com.fx.zfcar.databinding.ItemOfflineBinding

// 列表项类型
sealed class ReportItem {
    data class MileageItem(val data: MileageDataItem) : ReportItem()
    data class WarningItem(val data: WarningReportItem) : ReportItem()
    data class ActiveWarningItem(val data: ActiveWarningDataItem) : ReportItem()
    data class PhotoItem(val data: PhotoReportItem) : ReportItem()
    data class ExpiredItem(val data: ExpiredCarItem) : ReportItem()
    data class OilAddItem(val data: OilAddReportItem) : ReportItem()
    data class OilDayItem(val data: OilDayReportItem) : ReportItem()
    data class LeakItem(val data: LeakReportItem) : ReportItem()
    data class OfflineItem(val data: OfflineReportItem) : ReportItem()
}

class ReportAdapter(val type: ReportType) : BaseQuickAdapter<ReportItem, RecyclerView.ViewHolder>(DiffCallback()) {

    enum class ReportType {
        MILEAGE, WARNING, ACTIVE_WARNING, PHOTO, EXPIRED, OIL_ADD, OIL_DAY, LEAK, OFFLINE
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (type) {
            ReportType.MILEAGE -> MileageViewHolder(ItemMileageBinding.inflate(inflater, parent, false))
            ReportType.WARNING -> WarningViewHolder(ItemWarningBinding.inflate(inflater, parent, false))
            ReportType.ACTIVE_WARNING -> ActiveWarningViewHolder(ItemActiveWarningBinding.inflate(inflater, parent, false))
            ReportType.PHOTO -> PhotoViewHolder(ItemPhotoBinding.inflate(inflater, parent, false))
            ReportType.EXPIRED -> ExpiredViewHolder(ItemExpiredBinding.inflate(inflater, parent, false))
            ReportType.OIL_ADD -> OilAddViewHolder(ItemOilAddBinding.inflate(inflater, parent, false))
            ReportType.OIL_DAY -> OilDayViewHolder(ItemOilDayBinding.inflate(inflater, parent, false))
            ReportType.LEAK -> LeakViewHolder(ItemLeakBinding.inflate(inflater, parent, false))
            ReportType.OFFLINE -> OfflineViewHolder(ItemOfflineBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, item: ReportItem?) {
        when (holder) {
            is MileageViewHolder -> holder.bind((item as ReportItem.MileageItem).data)
            is WarningViewHolder -> holder.bind((item as ReportItem.WarningItem).data)
            is ActiveWarningViewHolder -> holder.bind((item as ReportItem.ActiveWarningItem).data)
            is PhotoViewHolder -> holder.bind((item as ReportItem.PhotoItem).data)
            is ExpiredViewHolder -> holder.bind((item as ReportItem.ExpiredItem).data)
            is OilAddViewHolder -> holder.bind((item as ReportItem.OilAddItem).data)
            is OilDayViewHolder -> holder.bind((item as ReportItem.OilDayItem).data)
            is LeakViewHolder -> holder.bind((item as ReportItem.LeakItem).data)
            is OfflineViewHolder -> holder.bind((item as ReportItem.OfflineItem).data)
        }
    }

    // 里程项ViewHolder
    class MileageViewHolder(private val binding: ItemMileageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: MileageDataItem) {
            binding.tvCarNum.text = data.carnum
            binding.tvDate.text = data.time
            binding.tvMileage.text = data.mileage.toString()
        }
    }

    // 报警项ViewHolder
    class WarningViewHolder(private val binding: ItemWarningBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: WarningReportItem) {
            binding.tvWarningType.text = data.name
            binding.tvCount.text = data.num.toString()
        }
    }

    // 安全报警项ViewHolder
    class ActiveWarningViewHolder(private val binding: ItemActiveWarningBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ActiveWarningDataItem) {
            binding.tvSafeType.text = data.name
            binding.tvCount.text = data.num.toString()
        }
    }

    // 照片项ViewHolder
    class PhotoViewHolder(private val binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: PhotoReportItem) {
            binding.tvTime.text = data.time
            binding.tvCarNum.text = data.carNum
            binding.tvAddress.text = data.address
            // 加载图片（替换为真实URL）
            Glide.with(binding.ivPhoto.context)
                .load(data.url.ifEmpty { "https://placeholder.pics/svg/200x100/CCCCCC/999999/暂无图片" })
                .into(binding.ivPhoto)
        }
    }

    // 过期项ViewHolder
    class ExpiredViewHolder(private val binding: ItemExpiredBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ExpiredCarItem) {
            binding.tvCarNum.text = data.carNum
            binding.tvExpiredDate.text = data.expiredDate
        }
    }

    // 加油项ViewHolder
    class OilAddViewHolder(private val binding: ItemOilAddBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: OilAddReportItem) {
            binding.tvCarNum.text = "车牌号:${data.carNum}"
            binding.tvDept.text = "所属机构:${data.deptName}"
            binding.tvAddCount.text = "加油次数:${data.num}"
            binding.tvAddOil.text = "加油液量:${data.oil}L"
        }
    }

    // 油耗项ViewHolder
    class OilDayViewHolder(private val binding: ItemOilDayBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: OilDayReportItem) {
            binding.tvCarNum.text = "车牌号:${data.carNum}"
            binding.tvDept.text = "所属机构:${data.deptName}"
            binding.tvDeviceNum.text = "设备卡号:${data.sim}"
            binding.tvMileage.text = "行驶里程:${data.mileage}"
            binding.tvOil.text = "行驶油耗:${data.oil}"
            binding.tvPercent.text = "百公里综合油耗:${data.percent}"
        }
    }

    // 漏油项ViewHolder
    class LeakViewHolder(private val binding: ItemLeakBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: LeakReportItem) {
            binding.tvCarNum.text = "车牌号:${data.carNum}"
            binding.tvDept.text = "所属机构:${data.deptName}"
            binding.tvLeakCount.text = "漏油次数:${data.num}"
            binding.tvLeakOil.text = "漏油液量:${data.oil}L"
        }
    }

    // 离线项ViewHolder
    class OfflineViewHolder(private val binding: ItemOfflineBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: OfflineReportItem) {
            binding.tvCarNum.text = data.carNum
            binding.tvDept.text = data.deptName
            binding.tvOfflineDays.text = data.offline.toString()
        }
    }

    // 数据刷新回调
    class DiffCallback : DiffUtil.ItemCallback<ReportItem>() {
        override fun areItemsTheSame(oldItem: ReportItem, newItem: ReportItem): Boolean {
            return when {
                oldItem is ReportItem.MileageItem && newItem is ReportItem.MileageItem -> oldItem.data.carId == newItem.data.carId
                oldItem is ReportItem.WarningItem && newItem is ReportItem.WarningItem -> oldItem.data.warningType == newItem.data.warningType
                oldItem is ReportItem.ActiveWarningItem && newItem is ReportItem.ActiveWarningItem -> oldItem.data.warningType == newItem.data.warningType
                oldItem is ReportItem.PhotoItem && newItem is ReportItem.PhotoItem -> oldItem.data.carId == newItem.data.carId && oldItem.data.ts == newItem.data.ts
                oldItem is ReportItem.ExpiredItem && newItem is ReportItem.ExpiredItem -> oldItem.data.carId == newItem.data.carId
                oldItem is ReportItem.OilAddItem && newItem is ReportItem.OilAddItem -> oldItem.data.carId == newItem.data.carId
                oldItem is ReportItem.OilDayItem && newItem is ReportItem.OilDayItem -> oldItem.data.carId == newItem.data.carId
                oldItem is ReportItem.LeakItem && newItem is ReportItem.LeakItem -> oldItem.data.carId == newItem.data.carId
                oldItem is ReportItem.OfflineItem && newItem is ReportItem.OfflineItem -> oldItem.data.carId == newItem.data.carId
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: ReportItem, newItem: ReportItem): Boolean {
            return oldItem == newItem
        }
    }
}