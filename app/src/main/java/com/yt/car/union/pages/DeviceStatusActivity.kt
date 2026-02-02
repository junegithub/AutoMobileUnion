package com.yt.car.union.pages

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yt.car.union.R
import com.yt.car.union.databinding.ActivityDeviceStatusBinding
import com.yt.car.union.pages.adapter.DeviceStatusAdapter
import com.yt.car.union.pages.adapter.DeviceStatusBean
import com.yt.car.union.pages.adapter.DeviceStatusDivider

class DeviceStatusActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeviceStatusBinding
    private lateinit var statusAdapter: DeviceStatusAdapter
    private val statusList = mutableListOf<DeviceStatusBean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initData()
        initListener()
    }

    private fun initView() {
        // 初始化RecyclerView
        statusAdapter = DeviceStatusAdapter(this)
        binding.rvDeviceStatus.apply {
            adapter = statusAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@DeviceStatusActivity)
            addItemDecoration(DeviceStatusDivider(this@DeviceStatusActivity)) // 添加分割线
        }
    }

    /**
     * 初始化截图中的设备状态数据
     */
    private fun initData() {
        statusList.apply {
            add(DeviceStatusBean(getString(R.string.driving), "7000"))
            add(DeviceStatusBean(getString(R.string.str_static), "16492"))
            add(DeviceStatusBean(getString(R.string.offline), "72947"))
            add(DeviceStatusBean(getString(R.string.overspeed), "22"))
            add(DeviceStatusBean(getString(R.string.fatigue), "184"))
            add(DeviceStatusBean(getString(R.string.expired), "24854"))
        }
        statusAdapter.submitList(statusList)
    }

    private fun initListener() {
        // 返回按钮点击
        binding.ivBack.setOnClickListener { finish() }

        // 列表项点击事件（可选扩展：跳转详情页）
        statusAdapter.setOnItemClickListener { bean ->
            // 示例：Toast提示点击的状态
            android.widget.Toast.makeText(this, "点击了${bean.statusName}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}