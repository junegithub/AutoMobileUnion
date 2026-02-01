package com.yt.car.union.pages.car

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.amap.api.maps.AMap
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.PolylineOptions
import com.yt.car.union.R
import com.yt.car.union.databinding.FragmentCarTrackBinding

class TrackFragment : Fragment() {
    private var _binding: FragmentCarTrackBinding? = null
    private val binding get() = _binding!!
    private lateinit var mapView: MapView
    private lateinit var aMap: AMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarTrackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化地图（若不需要真实地图，可注释此部分，保留静态展示）
        mapView = binding.root.findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        aMap = mapView.map

        // 模拟轨迹数据
        val trackPoints = listOf(
            LatLng(39.908823, 116.397470),
            LatLng(39.910823, 116.400470),
            LatLng(39.912823, 116.403470),
            LatLng(39.914823, 116.406470)
        )

        // 绘制轨迹
        aMap.addPolyline(
            PolylineOptions()
                .addAll(trackPoints)
                .width(10f)
                .color(android.graphics.Color.RED)
        )

        // 定位到轨迹起点
        aMap.moveCamera(
            com.amap.api.maps.CameraUpdateFactory.newLatLngZoom(
                trackPoints[0],
                15f
            )
        )

        // 轨迹回放按钮
        binding.btnPlayback.setOnClickListener {
            android.widget.Toast.makeText(context, "轨迹回放中...", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // 地图生命周期管理
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}