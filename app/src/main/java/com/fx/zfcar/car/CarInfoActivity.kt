package com.fx.zfcar.car

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.fx.zfcar.car.adapter.CarInfoPagerAdapter
import com.fx.zfcar.car.viewmodel.CarInfoViewModel
import com.fx.zfcar.net.CarInfo
import com.fx.zfcar.training.user.showToast
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
        private const val KEY_CAR_INFO_STATE = "key_car_info_state"
    }

    val titles = listOf("车辆信息", "终端信息", "其他信息")

    private lateinit var binding: ActivityCarInfoBinding
    private val carInfoViewModel by viewModels<CarInfoViewModel>()
    private var carInfoStateFlow = MutableStateFlow<ApiState<CarInfo>>(ApiState.Idle)
    private var currentCarInfo: CarInfo? = null

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
                            currentCarInfo = it
                            refreshAdapter(it)
                        }
                    }
                    is ApiState.Error -> {
                        showToast(state.msg)
                    }
                    is ApiState.Idle -> {
                        // 初始状态，无需处理
                    }
                }
            }
        }

        savedInstanceState?.getParcelable<CarInfo>(KEY_CAR_INFO_STATE)?.let {
            currentCarInfo = it
            refreshAdapter(it)
            return
        }

        val carId = intent.getStringExtra(KEY_CAR_ID)
            ?: intent.getIntExtra(KEY_CAR_ID, 0).takeIf { it > 0 }?.toString()
            .orEmpty()
        if (carId.isBlank()) {
            showToast("车辆信息异常")
            finish()
            return
        }
        carInfoViewModel.getCarInfo(carId, carInfoStateFlow)
    }

    private fun refreshAdapter(carInfo: CarInfo) {
        val pagerAdapter = CarInfoPagerAdapter(this@CarInfoActivity, titles, carInfo)
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        currentCarInfo?.let { outState.putParcelable(KEY_CAR_INFO_STATE, it) }
    }
}
