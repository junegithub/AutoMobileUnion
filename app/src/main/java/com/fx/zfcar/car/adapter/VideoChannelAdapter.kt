package com.fx.zfcar.car.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.RecyclerView
import com.fx.zfcar.databinding.ItemVideoChannelBinding
import com.fx.zfcar.net.VideoChannel

class VideoChannelAdapter(
    private var data: List<VideoChannel>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<VideoChannelAdapter.VideoViewHolder>() {

    var activeNum = 0
    var tabShow = true

    // 存储播放器和View的映射
    private val playerMap = mutableMapOf<Int, ExoPlayer>()

    inner class VideoViewHolder(val binding: ItemVideoChannelBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onItemClick(adapterPosition)
            }

            // 配置 PlayerView（隐藏控制栏，对应原代码的 controls 属性）
            binding.pvVideo.apply {
                controllerAutoShow = false
                controllerShowTimeoutMs = 0
                useController = true  // 如需显示控制栏设为true，否则false
            }
        }

        fun bind(wayNum: VideoChannel, position: Int) {
            // 设置通道标签
            binding.tvChannelLabel.text = "通道${wayNum.wayNumCode}"
            binding.tvChannelLabel.visibility = if (tabShow && activeNum == position) View.VISIBLE else View.GONE

            // 绑定 Media3 ExoPlayer 到 PlayerView
            playerMap[position]?.let { player ->
                binding.pvVideo.player = player
            }
        }
    }

    fun updateData(newData: List<VideoChannel>) {
        data = newData
        notifyDataSetChanged()
    }

    fun setPlayer(position: Int, player: ExoPlayer) {
        playerMap[position] = player
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoChannelBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        if (position < data.size) {
            holder.bind(data[position], position)
        }
    }

    override fun getItemCount(): Int = data.size

    // Adapter销毁时释放播放器
    override fun onViewDetachedFromWindow(holder: VideoViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.binding.pvVideo.player = null
    }
}