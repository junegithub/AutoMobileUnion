package com.yt.car.union.pages.training.user

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.yt.car.union.util.PressEffectUtils

abstract class BaseUserFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PressEffectUtils.setCommonPressEffect(getTitleView())
        // 设置标题
        getTitleView().text = getTitle()
        getTitleView().setOnClickListener { getHostActivity().onBackPressed() }
    }

    fun getHostActivity(): UserCenterActivity {
        return (activity as UserCenterActivity)
    }

    abstract fun getTitle(): String
    abstract fun getTitleView(): TextView
}