package com.fx.zfcar.training.safetytraining

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.fx.zfcar.util.PressEffectUtils

abstract class BaseSafetyFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PressEffectUtils.setCommonPressEffect(getTitleView())
        // 设置标题
        getTitleView().text = getTitle()
        getTitleView().setOnClickListener { getHostActivity().onBackPressed() }
    }

    fun getHostActivity(): SafetyTrainingActivity {
        return (activity as SafetyTrainingActivity)
    }

    abstract fun getTitle(): String
    abstract fun getTitleView(): TextView
}