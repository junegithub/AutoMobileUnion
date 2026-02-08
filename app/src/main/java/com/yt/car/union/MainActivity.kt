package com.yt.car.union

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.yt.car.union.databinding.ActivityMainBinding
import com.yt.car.union.pages.car.CarFragment
import com.yt.car.union.pages.training.TrainingFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 配置ViewPager2适配器
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            // Tab数量：2个
            override fun getItemCount(): Int = 2

            // 根据位置创建对应Fragment
            override fun createFragment(position: Int) = when (position) {
                0 -> CarFragment() // 第一个Tab：查车（地图）
                1 -> TrainingFragment() // 第二个Tab：培训
                else -> CarFragment()
            }
        }

        // 禁止ViewPager2左右滑动（符合底部Tab常规使用习惯，可选开启）
        binding.viewPager.isUserInputEnabled = false

        // 2. 底部Tab选中监听，联动ViewPager2
        binding.bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_car -> binding.viewPager.currentItem = 0 // 查车Tab
                R.id.nav_training -> binding.viewPager.currentItem = 1 // 培训Tab
            }
            true // 表示选中成功
        }

        // 3. ViewPager2页面切换，联动底部Tab（可选，防止手动滑动后Tab不匹配）
        binding.viewPager.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> binding.bottomNav.selectedItemId = R.id.nav_car
                    1 -> binding.bottomNav.selectedItemId = R.id.nav_training
                }
            }
        })
    }

}