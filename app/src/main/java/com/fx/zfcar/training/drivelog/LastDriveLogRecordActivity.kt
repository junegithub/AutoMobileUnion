package com.fx.zfcar.training.drivelog

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fx.zfcar.R
import com.fx.zfcar.databinding.ActivityLastDriveLogRecordBinding
import com.fx.zfcar.net.TravelLogItem
import com.fx.zfcar.training.adapter.CheckItemAdapter
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.util.SPUtils
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

/**
 * 上一次提交记录详情页面
 */
class LastDriveLogRecordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLastDriveLogRecordBinding
    private var lastRecord: TravelLogItem? = null

    // 检查项适配器
    private val beforeDriveAdapter by lazy { CheckItemAdapter() }
    private val drivingAdapter by lazy { CheckItemAdapter() }
    private val afterDriveAdapter by lazy { CheckItemAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLastDriveLogRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 从SP获取上一次记录数据（增加异常处理）
        if (!getLastRecordFromSP()) {
            // 获取数据失败，返回上一页
            goBack()
            return
        }

        // 初始化视图
        initView()

        // 填充数据
        fillData()
    }

    /**
     * 从SharedPreferences获取上一次记录（增加异常处理）
     * @return 是否获取成功
     */
    private fun getLastRecordFromSP(): Boolean {
        return try {
            val lastRecordStr = SPUtils.get("lastRecord")

            if (lastRecordStr.isNullOrEmpty()) {
                return false
            }

            // 使用Gson解析，增加异常捕获
            lastRecord = Gson().fromJson(lastRecordStr, TravelLogItem::class.java)
            lastRecord != null
        } catch (e: JsonSyntaxException) {
            // JSON解析失败
            e.printStackTrace()
            false
        } catch (e: Exception) {
            // 其他异常
            e.printStackTrace()
            false
        }
    }

    /**
     * 初始化视图
     */
    private fun initView() {
        // 标题栏返回按钮
        binding.layoutTitle.tvTitle.text="上一次提交记录"
        PressEffectUtils.setCommonPressEffect(binding.layoutTitle.tvTitle)
        binding.layoutTitle.tvTitle.setOnClickListener {
            goBack()
        }

        // 初始化检查项列表（增加间距和缓存优化）
        initCheckItemRecyclerViews()
    }

    /**
     * 初始化检查项RecyclerView（优化网格布局）
     */
    private fun initCheckItemRecyclerViews() {
        // 通用的GridLayoutManager配置
        val gridLayoutConfig = { spanCount: Int ->
            GridLayoutManager(this, spanCount).apply {
                // 设置间距
                val spacing = resources.getDimensionPixelSize(R.dimen.dp_8)
                binding.rvBeforeDrive.addItemDecoration(GridSpacingItemDecoration(spanCount, spacing, true))
                binding.rvDriving.addItemDecoration(GridSpacingItemDecoration(spanCount, spacing, true))
                binding.rvAfterDrive.addItemDecoration(GridSpacingItemDecoration(spanCount, spacing, true))
            }
        }

        // 行车前检查项（3列网格）
        binding.rvBeforeDrive.apply {
            layoutManager = gridLayoutConfig(3)
            adapter = beforeDriveAdapter
            setHasFixedSize(true)
            itemAnimator = null // 关闭动画，提升性能
        }

        // 行车中检查项（3列网格）
        binding.rvDriving.apply {
            layoutManager = gridLayoutConfig(3)
            adapter = drivingAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }

        // 收车后检查项（3列网格）
        binding.rvAfterDrive.apply {
            layoutManager = gridLayoutConfig(3)
            adapter = afterDriveAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }

        // 设置检查项数据
        beforeDriveAdapter.submitList(DriveCheckConstants.BEFORE_DRIVE_ITEMS)
        drivingAdapter.submitList(DriveCheckConstants.DRIVING_ITEMS)
        afterDriveAdapter.submitList(DriveCheckConstants.AFTER_DRIVE_ITEMS)
    }

    /**
     * 填充页面数据（增加空值保护）
     */
    private fun fillData() {
        lastRecord?.let { record ->
            // 基本信息（增加空值保护）
            binding.tvAddtime.text = handleEmptyValue(record.addtime)
            binding.tvCarnum.text = handleEmptyValue(record.carnum)
            binding.tvGoodsType.text = getGoodsTypeText(record.type)
            binding.tvDriverName.text = handleEmptyValue(record.driver_name)
            binding.tvCopilotName.text = handleEmptyValue(record.copilot_name)
            binding.tvWeather.text = handleEmptyValue(record.weather)
            binding.tvTemperature.text = handleEmptyValue(record.temperature)

            // 行车前检查结果
            binding.tvBeforeDriveResult.text = getBeforeDriveResultText(record.sresult)

            // 行车中检查结果
            binding.tvDrivingResult.text = getDrivingResultText(record.gresult ?: "")

            // 收车后检查结果
            binding.tvAfterDriveResult.text = getAfterDriveResultText(record.eresult)

            // 驾驶员签名（优化Glide加载）
            if (isSignatureValid(record.dsingimg)) {
                binding.tvDriverSign.visibility = View.VISIBLE
                binding.ivDriverSign.visibility = View.VISIBLE
                Glide.with(this)
                    .load(record.dsingimg)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // 缓存优化
                    .into(binding.ivDriverSign)
            } else {
                binding.tvDriverSign.visibility = View.GONE
                binding.ivDriverSign.visibility = View.GONE
            }

            // 押运员签名（优化Glide加载）
            if (isSignatureValid(record.ysingimg)) {
                binding.tvCopilotSign.visibility = View.VISIBLE
                binding.ivCopilotSign.visibility = View.VISIBLE
                Glide.with(this)
                    .load(record.ysingimg)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.ivCopilotSign)
            } else {
                binding.tvCopilotSign.visibility = View.GONE
                binding.ivCopilotSign.visibility = View.GONE
            }
        }
    }

    /**
     * 返回上一页
     */
    private fun goBack() {
        finish()
    }

    /**
     * 网格布局间距装饰器（新增）
     */
    inner class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int,
        private val includeEdge: Boolean
    ) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: android.graphics.Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view) // item position
            val column = position % spanCount // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing
                }
                outRect.bottom = spacing // item bottom
            } else {
                outRect.left = column * spacing / spanCount // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing // item top
                }
            }
        }
    }

    /**
     * 获取货物类型文本
     */
    fun getGoodsTypeText(type: Int): String {
        return if (type == 0) "危险品" else "普通货物"
    }

    /**
     * 获取行车前检查结果文本
     */
    fun getBeforeDriveResultText(sresult: Int): String {
        return DriveCheckConstants.BEFORE_DRIVE_RESULTS
            .find { it.value == sresult }?.label ?: "未知结果"
    }

    /**
     * 获取行车中检查结果文本（增强空值处理）
     */
    fun getDrivingResultText(gresult: String): String {
        return try {
            val value = gresult.toInt()
            DriveCheckConstants.DRIVING_RESULTS
                .find { it.value == value }?.label ?: "未知结果"
        } catch (e: NumberFormatException) {
            "未知结果"
        } catch (e: Exception) {
            "未知结果"
        }
    }

    /**
     * 获取收车后检查结果文本
     */
    fun getAfterDriveResultText(eresult: Int): String {
        return DriveCheckConstants.AFTER_DRIVE_RESULTS
            .find { it.value == eresult }?.label ?: "未知结果"
    }

    /**
     * 处理空值显示（增强逻辑）
     */
    fun handleEmptyValue(value: String?): String {
        return when {
            value.isNullOrEmpty() -> "-"
            value == "0" -> "-"
            else -> value
        }
    }

    /**
     * 检查签名图片是否有效（增强逻辑）
     */
    fun isSignatureValid(signImg: String?): Boolean {
        return when {
            signImg.isNullOrEmpty() -> false
            signImg == "0" -> false
            signImg.lowercase() == "null" -> false // 处理null字符串
            else -> true
        }
    }
}