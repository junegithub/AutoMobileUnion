package com.yt.car.union.pages.car

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.yt.car.union.databinding.FragmentPhotoBinding
import com.yt.car.union.net.bean.PhotoBean
import com.yt.car.union.pages.adapter.PhotoAdapter

class PhotoFragment : Fragment() {
    private var _binding: FragmentPhotoBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PhotoAdapter
    private val mData = mutableListOf<PhotoBean>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoBinding.inflate(inflater, container, false)
        initView()
        initData()
        return binding.root
    }

    private fun initView() {
        binding.rvPhoto.layoutManager = LinearLayoutManager(context)
    }

    private fun initData() {
        // 模拟截图数据（图片URL为占位符）
        mData.apply {
            add(
                PhotoBean(
                    "https://placeholder.pics/svg/100x80/CCCCCC/666666/IMG",
                    "2026-02-02 12:17:56",
                    "鲁H98W61",
                    "山西省,晋中市,左权县"
                )
            )
            add(
                PhotoBean(
                    "https://placeholder.pics/svg/100x80/CCCCCC/666666/IMG",
                    "2026-02-02 12:35:01",
                    "鲁H009S7",
                    "四川省,德阳市,旌阳区,德阿公路(1米),余家庵(西北224米)"
                )
            )
            add(
                PhotoBean(
                    "https://placeholder.pics/svg/100x80/CCCCCC/666666/IMG",
                    "2026-02-02 16:40:39",
                    "鲁H783M0",
                    "内蒙古自治区,阿拉善盟,阿拉善右旗,S312(1米)"
                )
            )
            add(
                PhotoBean(
                    "https://placeholder.pics/svg/100x80/CCCCCC/666666/IMG",
                    "2026-02-02 07:40:24",
                    "鲁BA85318",
                    "山东省,青岛市,胶南市,永安路(西84米),精华幼儿园(东南90米)"
                )
            )
            add(
                PhotoBean(
                    "https://placeholder.pics/svg/100x80/CCCCCC/666666/IMG",
                    "2026-02-02 07:45:25",
                    "鲁BA85318",
                    "山东省,青岛市,胶南市,人民路(南15米),精华幼儿园(西北26米)"
                )
            )
        }
        adapter = PhotoAdapter(mData)
        binding.rvPhoto.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}