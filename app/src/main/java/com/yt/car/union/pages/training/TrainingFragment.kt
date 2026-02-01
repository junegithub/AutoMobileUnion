package com.yt.car.union.pages.training

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yt.car.union.R
import com.yt.car.union.util.StatusBarHeightUtil

class TrainingFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_training, container, false)
        view.findViewById<View>(R.id.content_root).setPaddingRelative(0, StatusBarHeightUtil.getStatusBarHeight(requireContext()), 0,0)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 此处可扩展：
        // 1. 调用接口获取用户信息、培训列表、待办事项
        // 2. 给按钮添加点击事件（开始学习、立即处理等）
        // 3. 初始化RecyclerView展示更多待办/培训内容
    }
}