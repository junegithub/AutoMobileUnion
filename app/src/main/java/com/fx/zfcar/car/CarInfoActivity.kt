package com.fx.zfcar.car

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.fx.zfcar.car.adapter.CarInfoPagerAdapter
import com.fx.zfcar.car.viewmodel.CarInfoViewModel
import com.fx.zfcar.net.CarInfo
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.viewmodel.ApiState
import com.google.android.material.tabs.TabLayoutMediator
import com.fx.zfcar.databinding.ActivityCarInfoBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.getValue

class CarInfoActivity : AppCompatActivity() {

    companion object {
        const val KEY_CAR_ID = "key_car_id"
    }

    val titles = listOf("车辆信息", "终端信息", "其他信息")

    private lateinit var binding: ActivityCarInfoBinding
    private val carInfoViewModel by viewModels<CarInfoViewModel>()
    private var carInfoStateFlow = MutableStateFlow<ApiState<CarInfo>>(ApiState.Idle)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PressEffectUtils.setCommonPressEffect(binding.ivBack)
        binding.ivBack.setOnClickListener { finish() }

        lifecycleScope.launch {
            carInfoStateFlow.collect {state ->
                when (state) {
                    is ApiState.Loading -> {
                        // 加载中：显示进度条，隐藏其他视图
                    }
                    is ApiState.Success -> {
                        // 成功：隐藏进度条，显示数据
                        state.data?.let {
                            refreshAdapter(state.data)
                        }
                    }
                    is ApiState.Error -> {
                        // 失败：显示错误信息，隐藏其他视图
                    }
                    is ApiState.Idle -> {
                        // 初始状态，无需处理
                    }
                }
            }
        }

        carInfoViewModel.getCarInfo(intent.getIntExtra(KEY_CAR_ID, 0), carInfoStateFlow)
    }

    private fun refreshAdapter(carInfo: CarInfo) {
        val pagerAdapter = CarInfoPagerAdapter(this@CarInfoActivity, titles, carInfo)
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }
}