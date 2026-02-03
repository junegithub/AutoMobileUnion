package com.yt.car.union.pages

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.yt.car.union.R
import com.yt.car.union.databinding.ActivityReportBinding
import com.yt.car.union.pages.adapter.ViewPagerAdapter
import com.yt.car.union.pages.car.AlarmFragment
import com.yt.car.union.pages.car.MileageFragment
import com.yt.car.union.pages.car.PhotoFragment
import com.yt.car.union.pages.car.SafetyFragment

class ReportActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityReportBinding
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private val fragments = mutableListOf<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initFragment()
        initListener()
    }

    private fun initView() {
        // 默认选中里程Tab和昨天时间
        setTabSelected(binding.tvMileage)
        setTimeSelected(binding.tvYesterday)
    }

    private fun initFragment() {
        fragments.apply {
            add(MileageFragment())
            add(AlarmFragment())
            add(SafetyFragment())
            add(PhotoFragment())
        }
        viewPagerAdapter = ViewPagerAdapter(this, fragments)
        binding.viewPager.adapter = viewPagerAdapter

        // ViewPager2联动Tab
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                resetTab()
                when (position) {
                    0 -> setTabSelected(binding.tvMileage)
                    1 -> setTabSelected(binding.tvAlarm)
                    2 -> setTabSelected(binding.tvSafety)
                    3 -> setTabSelected(binding.tvPhoto)
                }
            }
        })
    }

    private fun initListener() {
        // Tab点击事件
        binding.tvMileage.setOnClickListener(this)
        binding.tvAlarm.setOnClickListener(this)
        binding.tvSafety.setOnClickListener(this)
        binding.tvPhoto.setOnClickListener(this)

        // 时间筛选点击事件
        binding.tvYesterday.setOnClickListener(this)
        binding.tvToday.setOnClickListener(this)
        binding.tv3days.setOnClickListener(this)
        binding.tv7days.setOnClickListener(this)

        // 返回按钮点击
        binding.ivBack.setOnClickListener { finish() }
    }

    override fun onClick(v: View) {
        when (v.id) {
            // Tab切换
            binding.tvMileage.id -> binding.viewPager.setCurrentItem(0, false)
            binding.tvAlarm.id -> binding.viewPager.setCurrentItem(1, false)
            binding.tvSafety.id -> binding.viewPager.setCurrentItem(2, false)
            binding.tvPhoto.id -> binding.viewPager.setCurrentItem(3, false)

            // 时间筛选切换
            binding.tvYesterday.id -> setTimeSelected(binding.tvYesterday)
            binding.tvToday.id -> setTimeSelected(binding.tvToday)
            binding.tv3days.id -> setTimeSelected(binding.tv3days)
            binding.tv7days.id -> setTimeSelected(binding.tv7days)
        }
    }

    // 设置Tab选中态
    private fun setTabSelected(tv: View) {
        resetTab()
        when (tv) {
            binding.tvMileage -> {
                binding.tvMileage.setBackgroundResource(R.drawable.bg_tab_selected)
                binding.tvMileage.setTextColor(resources.getColor(R.color.white, theme))
            }
            binding.tvAlarm -> {
                binding.tvAlarm.setBackgroundResource(R.drawable.bg_tab_selected)
                binding.tvAlarm.setTextColor(resources.getColor(R.color.white, theme))
            }
            binding.tvSafety -> {
                binding.tvSafety.setBackgroundResource(R.drawable.bg_tab_selected)
                binding.tvSafety.setTextColor(resources.getColor(R.color.white, theme))
            }
            binding.tvPhoto -> {
                binding.tvPhoto.setBackgroundResource(R.drawable.bg_tab_selected)
                binding.tvPhoto.setTextColor(resources.getColor(R.color.white, theme))
            }
        }
    }

    // 重置Tab未选中态
    private fun resetTab() {
        listOf(binding.tvMileage, binding.tvAlarm, binding.tvSafety, binding.tvPhoto).forEach {
            it.setBackgroundResource(R.drawable.bg_tab_unselected)
            it.setTextColor(resources.getColor(R.color.tab_unselected, theme))
        }
    }

    // 设置时间筛选选中态
    private fun setTimeSelected(tv: View) {
        resetTime()
        when (tv) {
            binding.tvYesterday -> {
                binding.tvYesterday.setTextColor(resources.getColor(R.color.time_selected, theme))
                binding.tvYesterday.typeface = binding.tvYesterday.typeface?.let {
                    android.graphics.Typeface.create(it, android.graphics.Typeface.BOLD)
                }
            }
            binding.tvToday -> {
                binding.tvToday.setTextColor(resources.getColor(R.color.time_selected, theme))
                binding.tvToday.typeface = binding.tvToday.typeface?.let {
                    android.graphics.Typeface.create(it, android.graphics.Typeface.BOLD)
                }
            }
            binding.tv3days -> {
                binding.tv3days.setTextColor(resources.getColor(R.color.time_selected, theme))
                binding.tv3days.typeface = binding.tv3days.typeface?.let {
                    android.graphics.Typeface.create(it, android.graphics.Typeface.BOLD)
                }
            }
            binding.tv7days -> {
                binding.tv7days.setTextColor(resources.getColor(R.color.time_selected, theme))
                binding.tv7days.typeface = binding.tv7days.typeface?.let {
                    android.graphics.Typeface.create(it, android.graphics.Typeface.BOLD)
                }
            }
        }
    }

    // 重置时间筛选未选中态
    private fun resetTime() {
        listOf(binding.tvYesterday, binding.tvToday, binding.tv3days, binding.tv7days).forEach {
            it.setTextColor(resources.getColor(R.color.time_unselected, theme))
            it.typeface = android.graphics.Typeface.DEFAULT
        }
    }
}