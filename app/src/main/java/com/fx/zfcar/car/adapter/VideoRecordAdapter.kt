package com.fx.zfcar.car.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fx.zfcar.car.VideoPlaybackActivity.VideoRecordItem
import com.fx.zfcar.databinding.ItemVideoRecordBinding
import com.fx.zfcar.util.PressEffectUtils

class VideoRecordAdapter(
    private val onItemClick: (VideoRecordItem) -> Unit
) : ListAdapter<VideoRecordItem, VideoRecordAdapter.VideoRecordViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoRecordViewHolder {
        val binding = ItemVideoRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VideoRecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoRecordViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class VideoRecordViewHolder(
        private val binding: ItemVideoRecordBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            PressEffectUtils.setCommonPressEffect(binding.root)
        }

        fun bind(item: VideoRecordItem, position: Int) {
            binding.tvTitle.text = "视频${position + 1}"
            binding.tvChannel.text = "通道${item.Channel}"
            binding.tvStartTime.text = formatVideoTime(item.StartTime)
            binding.tvEndTime.text = formatVideoTime(item.EndTime)
            binding.tvFileSize.text = "${(item.FileSize / 1048576.0).toInt()}MB"
            binding.tvMediaType.text = mediaTypeText(item.MediaType)
            binding.tvStreamType.text = streamTypeText(item.StreamType)
            binding.tvStorageType.text = storageTypeText(item.StorageType)
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    private fun formatVideoTime(time: String): String {
        if (time.length < 12) return time
        return "20${time.substring(0, 2)}-${time.substring(2, 4)}-${time.substring(4, 6)} " +
            "${time.substring(6, 8)}:${time.substring(8, 10)}:${time.substring(10, 12)}"
    }

    private fun mediaTypeText(type: Int): String = when (type) {
        0 -> "音视频"
        1 -> "音频"
        2 -> "视频"
        3 -> "视频或音视频"
        else -> "-"
    }

    private fun streamTypeText(type: Int): String = when (type) {
        0 -> "所有码流"
        1 -> "主码流"
        2 -> "子码流"
        else -> "-"
    }

    private fun storageTypeText(type: Int): String = when (type) {
        0 -> "所有存储器"
        1 -> "主存储器"
        2 -> "灾备存储器"
        else -> "-"
    }

    class DiffCallback : DiffUtil.ItemCallback<VideoRecordItem>() {
        override fun areItemsTheSame(oldItem: VideoRecordItem, newItem: VideoRecordItem): Boolean {
            return oldItem.Channel == newItem.Channel &&
                oldItem.StartTime == newItem.StartTime &&
                oldItem.EndTime == newItem.EndTime
        }

        override fun areContentsTheSame(oldItem: VideoRecordItem, newItem: VideoRecordItem): Boolean {
            return oldItem == newItem
        }
    }
}
