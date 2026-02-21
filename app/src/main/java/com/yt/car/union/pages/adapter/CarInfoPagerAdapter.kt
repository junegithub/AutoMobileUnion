package com.yt.car.union.pages.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.yt.car.union.net.CarInfo
import com.yt.car.union.pages.car.CarInfoFragment
import com.yt.car.union.pages.car.OtherInfoFragment
import com.yt.car.union.pages.car.TerminalInfoFragment

class CarInfoPagerAdapter(
    activity: FragmentActivity,
    val titles: List<String>,
    private val carInfo: CarInfo
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = titles.size

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CarInfoFragment.newInstance(carInfo)
            1 -> TerminalInfoFragment.newInstance(carInfo)
            2 -> OtherInfoFragment.newInstance(carInfo)
            else -> CarInfoFragment.newInstance(carInfo)
        }
    }
}