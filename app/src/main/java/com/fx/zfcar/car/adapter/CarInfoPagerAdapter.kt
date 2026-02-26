package com.fx.zfcar.car.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.fx.zfcar.car.CarInfoFragment
import com.fx.zfcar.car.OtherInfoFragment
import com.fx.zfcar.car.TerminalInfoFragment
import com.fx.zfcar.net.CarInfo

class CarInfoPagerAdapter(
    activity: FragmentActivity,
    val titles: List<String>,
    private val carInfo: CarInfo
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = titles.size

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CarInfoFragment.Companion.newInstance(carInfo)
            1 -> TerminalInfoFragment.Companion.newInstance(carInfo)
            2 -> OtherInfoFragment.Companion.newInstance(carInfo)
            else -> CarInfoFragment.Companion.newInstance(carInfo)
        }
    }
}