package com.fx.zfcar.pages

import android.os.Bundle
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.fx.zfcar.car.CarFragment
import com.fx.zfcar.training.TrainingFragment
import com.fx.zfcar.R
import com.fx.zfcar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var lastBackPressedAt = 0L
    private var exitDialog: AlertDialog? = null

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
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> binding.bottomNav.selectedItemId = R.id.nav_car
                    1 -> binding.bottomNav.selectedItemId = R.id.nav_training
                }
            }
        })

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleExitBackPress()
                }
            }
        )
    }

    private fun handleExitBackPress() {
        val now = SystemClock.elapsedRealtime()
        if (now - lastBackPressedAt > 2000L) {
            lastBackPressedAt = now
            Toast.makeText(this, getString(R.string.main_exit_hint), Toast.LENGTH_SHORT).show()
            return
        }
        lastBackPressedAt = 0L
        showExitDialog()
    }

    private fun showExitDialog() {
        if (exitDialog?.isShowing == true) {
            return
        }
        exitDialog = AlertDialog.Builder(this)
            .setTitle(R.string.main_exit_dialog_title)
            .setMessage(R.string.main_exit_dialog_message)
            .setNegativeButton(R.string.main_exit_dialog_cancel, null)
            .setPositiveButton(R.string.main_exit_dialog_confirm) { _, _ ->
                finishAffinity()
            }
            .create()
        exitDialog?.show()
    }
}
