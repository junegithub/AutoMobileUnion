package com.fx.zfcar.training.drivelog

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.fx.zfcar.R
import com.fx.zfcar.databinding.ActivityDriveLogStageBinding
import com.fx.zfcar.databinding.FormRowDateBinding
import com.fx.zfcar.databinding.FormRowEdittextBinding
import com.fx.zfcar.databinding.FormRowSelectorBinding
import com.fx.zfcar.net.ApiConfig
import com.fx.zfcar.net.TravelPostResponse
import com.fx.zfcar.net.UploadFileData
import com.fx.zfcar.training.adapter.LogStageCheckItemAdapter
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.training.viewmodel.NoticeViewModel
import com.fx.zfcar.training.viewmodel.TravelViewModel
import com.fx.zfcar.util.BitmapUtils
import com.fx.zfcar.util.DateUtil
import com.fx.zfcar.util.DialogUtils
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.util.ProgressDialogUtils
import com.fx.zfcar.util.SPUtils
import com.fx.zfcar.viewmodel.ApiState
import com.kongzue.dialogx.dialogs.BottomMenu
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlin.getValue

class DriveLogStageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDriveLogStageBinding

    // ViewModel
    private val viewModel = DriveLogModel()

    private val driveLogViewModel by viewModels<TravelViewModel>()
    private var postLogStateFlow = MutableStateFlow<ApiState<Int>>(ApiState.Idle)
    private val noticeViewModel by viewModels<NoticeViewModel>()
    private var uploadStateFlow = MutableStateFlow<ApiState<UploadFileData>>(ApiState.Idle)

    // 适配器
    private val beforeItemsAdapter by lazy { LogStageCheckItemAdapter(::onCheckItemChecked) }
    private val drivingItemsAdapter by lazy { LogStageCheckItemAdapter(::onCheckItemChecked) }
    private val endItemsAdapter by lazy { LogStageCheckItemAdapter(::onCheckItemChecked) }

    // 通用表单行绑定
    private val rowDateBinding by lazy { FormRowDateBinding.bind(binding.rowDate.root) }
    private val rowDriverNameBinding by lazy { FormRowEdittextBinding.bind(binding.rowDriverName.root) }
    private val rowCopilotNameBinding by lazy { FormRowEdittextBinding.bind(binding.rowCopilotName.root) }
    private val rowNormalCopilotBinding by lazy { FormRowEdittextBinding.bind(binding.rowNormalCopilot.root) }
    private val rowWeatherBinding by lazy { FormRowEdittextBinding.bind(binding.rowWeather.root) }
    private val rowTemperatureBinding by lazy { FormRowEdittextBinding.bind(binding.rowTemperature.root) }
    private val rowLoadBinding by lazy { FormRowEdittextBinding.bind(binding.rowLoad.root) }
    private val rowRealLoadBinding by lazy { FormRowEdittextBinding.bind(binding.rowRealLoad.root) }
    private val rowGoodsNameBinding by lazy { FormRowEdittextBinding.bind(binding.rowGoodsName.root) }
    private val rowGotimeBinding by lazy { FormRowDateBinding.bind(binding.rowGotime.root) }
    private val rowGettimeBinding by lazy { FormRowDateBinding.bind(binding.rowGettime.root) }
    private val rowStartAddressBinding by lazy { FormRowEdittextBinding.bind(binding.rowStartAddress.root) }
    private val rowEndAddressBinding by lazy { FormRowEdittextBinding.bind(binding.rowEndAddress.root) }
    private val rowMileageBinding by lazy { FormRowEdittextBinding.bind(binding.rowMileage.root) }
    private val rowBeforeResultBinding by lazy { FormRowSelectorBinding.bind(binding.rowBeforeResult.root) }
    private val rowRoadStatusBinding by lazy { FormRowSelectorBinding.bind(binding.rowRoadStatus.root) }
    private val rowDrivingResultBinding by lazy { FormRowSelectorBinding.bind(binding.rowDrivingResult.root) }
    private val rowStopResultBinding by lazy { FormRowEdittextBinding.bind(binding.rowStopResult.root) }
    private val rowStopAddressBinding by lazy { FormRowEdittextBinding.bind(binding.rowStopAddress.root) }
    private val rowStopTimeBinding by lazy { FormRowDateBinding.bind(binding.rowStopTime.root) }
    private val rowEndResultBinding by lazy { FormRowSelectorBinding.bind(binding.rowEndResult.root) }

    private var isCopilotSign = false
    private var currentCheckTime: Long = 0

    // OkHttp客户端
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriveLogStageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化通用表单行标签
        initFormLabels()

        // 初始化检查项列表
        initCheckItemRecyclerViews()

        // 初始化货物类型选择
        initGoodsTypeSelector()

        // 初始化ViewModel
        viewModel.init(intent.getStringExtra("carNum"))

        // 观察数据变化
        observeViewModel()

        // 设置点击事件
        setClickListeners()
    }

    /**
     * 初始化通用表单行标签文本
     */
    private fun initFormLabels() {
        // 日期选择行
        rowDateBinding.tvLabel.text = "日期："
        rowGotimeBinding.tvLabel.text = "出发时间："
        rowGettimeBinding.tvLabel.text = "到达时间："
        rowStopTimeBinding.tvLabel.text = "停车时间："

        // 输入行
        rowDriverNameBinding.tvLabel.text = "驾驶员姓名："
        rowCopilotNameBinding.tvLabel.text = "押运员姓名："
        rowNormalCopilotBinding.tvLabel.text = "副驾驶姓名："
        rowWeatherBinding.tvLabel.text = "天气："
        rowTemperatureBinding.tvLabel.text = "温度："
        rowLoadBinding.tvLabel.text = "核载："
        rowRealLoadBinding.tvLabel.text = "实载："
        rowGoodsNameBinding.tvLabel.text = "货物名称："
        rowStartAddressBinding.tvLabel.text = "出发地址："
        rowEndAddressBinding.tvLabel.text = "到达地址："
        rowMileageBinding.tvLabel.text = "行驶里程："
        rowStopResultBinding.tvLabel.text = "停车原因："
        rowStopAddressBinding.tvLabel.text = "停车地点："

        // 选择器行
        rowBeforeResultBinding.tvLabel.text = "检查结果："
        rowRoadStatusBinding.tvLabel.text = "道路情况："
        rowDrivingResultBinding.tvLabel.text = "检查结果："
        rowEndResultBinding.tvLabel.text = "检查结果："
    }

    /**
     * 初始化检查项列表
     */
    private fun initCheckItemRecyclerViews() {
        // 行车前检查项
        binding.rvBeforeItems.apply {
            layoutManager = GridLayoutManager(this@DriveLogStageActivity, 3)
            adapter = beforeItemsAdapter
        }

        // 行车中检查项
        binding.rvDrivingItems.apply {
            layoutManager = GridLayoutManager(this@DriveLogStageActivity, 3)
            adapter = drivingItemsAdapter
        }

        // 收车后检查项
        binding.rvEndItems.apply {
            layoutManager = GridLayoutManager(this@DriveLogStageActivity, 3)
            adapter = endItemsAdapter
        }
    }

    /**
     * 初始化货物类型选择
     */
    private fun initGoodsTypeSelector() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.goods_types,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGoodsType.adapter = adapter

        // 默认选择危险品
        binding.spinnerGoodsType.setSelection(0)
    }

    /**
     * 观察ViewModel数据变化
     */
    private fun observeViewModel() {
        // 步骤变化
        viewModel.stage.observe(this) {
            updateStageVisibility(it)
        }

        // 进度条
        viewModel.stageStep.observe(this) {
            binding.lineProgress.progress = it
        }

        // 本地表单数据
        viewModel.localForm.observe(this) {
            updateFormUI(it)
        }

        // 检查项数据
        viewModel.beforeItems.observe(this) {
            beforeItemsAdapter.submitList(it)
        }
        viewModel.drivingCheckItemsArr.observe(this) {
            drivingItemsAdapter.submitList(it)
        }
        viewModel.checkEndItems.observe(this) {
            endItemsAdapter.submitList(it)
        }

        // Toast提示
        viewModel.showToast.observe(this) { message ->
            message?.let {
                showToast(it)
                viewModel.showToast.value = null
            }
        }

        // 弹窗显示
        viewModel.diaShow.observe(this) { show ->
            if (show) {
                showDraftDialog()
            }
        }

        // 日期选择器
        viewModel.timeSelect.observe(this) { show ->
            if (show) {
                showDatePicker()
                viewModel.timeSelect.value = false
            }
        }

        // 选择器弹窗
        viewModel.roadStatusShow.observe(this) { show ->
            if (show) {
                showRoadStatusSelector()
            }
        }
        viewModel.twoListShow.observe(this) { show ->
            if (show) {
                showBeforeResultSelector()
            }
        }
        viewModel.threeListShow.observe(this) { show ->
            if (show) {
                showDrivingResultSelector()
            }
        }
        viewModel.endListShow.observe(this) { show ->
            if (show) {
                showEndResultSelector()
            }
        }

        lifecycleScope.launch {
            uploadStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                        ProgressDialogUtils.show(this@DriveLogStageActivity, "上传中...")
                    }

                    is ApiState.Success -> {
                        ProgressDialogUtils.dismiss()

                        val signImgUrl = "${ApiConfig.BASE_URL_TRAINING}${uiState.data?.url ?: ""}"
                        handleUploadResponse(signImgUrl)
                    }

                    is ApiState.Error -> {
                        ProgressDialogUtils.dismiss()
                        showToast("上传失败：${uiState.msg}")
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }

        lifecycleScope.launch {
            postLogStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                        ProgressDialogUtils.show(this@DriveLogStageActivity, "提交中...")
                    }

                    is ApiState.Success -> {
                        ProgressDialogUtils.dismiss()
                        showToast("提交成功")
                        finish()
                    }

                    is ApiState.Error -> {
                        ProgressDialogUtils.dismiss()
                        showToast("提交失败：${uiState.msg}")
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }
    }

    /**
     * 更新步骤显示
     */
    private fun updateStageVisibility(stage: Int) {
        println("June updateStageVisibility ${stage}")
        // 隐藏所有步骤
        binding.layoutStage1.visibility = View.GONE
        binding.layoutStage2.visibility = View.GONE
        binding.layoutStage3.visibility = View.GONE
        binding.layoutStage4.visibility = View.GONE
        binding.layoutStage5.visibility = View.GONE
        binding.layoutStage6.visibility = View.GONE

        // 显示当前步骤
        when (stage) {
            1 -> binding.layoutStage1.visibility = View.VISIBLE
            2 -> binding.layoutStage2.visibility = View.VISIBLE
            3 -> binding.layoutStage3.visibility = View.VISIBLE
            4 -> binding.layoutStage4.visibility = View.VISIBLE
            5 -> binding.layoutStage5.visibility = View.VISIBLE
            6 -> binding.layoutStage6.visibility = View.VISIBLE
        }

        // 更新下一步按钮文本
        when (stage) {
            6 -> binding.btnNext.text = "提交"
            else -> binding.btnNext.text = "下一步"
        }
    }

    /**
     * 更新表单UI
     */
    private fun updateFormUI(form: DriveCheckConstants.LocalFormData) {
        // 日期选择行
        rowDateBinding.tvContent.text = if (form.addtime.isBlank()) "请选择时间" else form.addtime
        rowGotimeBinding.tvContent.text = if (form.gotime.isBlank()) "请选择时间" else form.gotime
        rowGettimeBinding.tvContent.text = if (form.gettime.isBlank()) "请选择时间" else form.gettime
        rowStopTimeBinding.tvContent.text = if (form.stoptime.isBlank()) "请选择时间" else form.stoptime

        // 车牌号
        binding.etCarnum.setText(form.carnum)

        // 输入行
        rowDriverNameBinding.etContent.setText(form.driver_name)
        rowCopilotNameBinding.etContent.setText(form.copilot_name)
        rowNormalCopilotBinding.etContent.setText(form.copilot_name) // 副驾驶和押运员共用一个字段
        rowWeatherBinding.etContent.setText(form.weather)
        rowTemperatureBinding.etContent.setText(form.temperature)
        rowLoadBinding.etContent.setText(form.load)
        rowRealLoadBinding.etContent.setText(form.real_load)
        rowGoodsNameBinding.etContent.setText(form.goods_name)
        rowStartAddressBinding.etContent.setText(form.start_address)
        rowEndAddressBinding.etContent.setText(form.end_address)
        rowMileageBinding.etContent.setText(form.mileage)
        rowStopResultBinding.etContent.setText(form.stopresult)
        rowStopAddressBinding.etContent.setText(form.stopaddress)

        // 选择器行
        rowBeforeResultBinding.tvContent.text = getResultLabel("before", form.sresult)
        rowRoadStatusBinding.tvContent.text = getRoadStatusLabel(form.groad)
        rowDrivingResultBinding.tvContent.text = getResultLabel("driving", form.gresult)
        rowEndResultBinding.tvContent.text = getResultLabel("end", form.eresult)

        // 显示/隐藏押运员/副驾驶字段
        if (form.type == "危险品") {
            binding.rowCopilotName.root.visibility = View.VISIBLE
            binding.rowNormalCopilot.root.visibility = View.GONE
        } else {
            binding.rowCopilotName.root.visibility = View.GONE
            binding.rowNormalCopilot.root.visibility = View.VISIBLE
        }

        // 签名显示
        if (form.dsingimg.isNotBlank()) {
            // 显示驾驶员签名图片
            binding.ivDriverSign.visibility = View.VISIBLE
            binding.layoutDriverSignature.visibility = View.GONE
            // 加载签名图片（实际项目中使用Glide/Picasso）
            // Glide.with(this).load(form.dsingimg).into(binding.ivDriverSign)
        } else {
            binding.ivDriverSign.visibility = View.GONE
            binding.layoutDriverSignature.visibility = View.VISIBLE
        }

        if (form.ysingimg.isNotBlank()) {
            // 显示副驾驶签名图片
            binding.ivCopilotSign.visibility = View.VISIBLE
            binding.layoutCopilotSignature.visibility = View.GONE
            // 加载签名图片
            // Glide.with(this).load(form.ysingimg).into(binding.ivCopilotSign)
        } else {
            binding.ivCopilotSign.visibility = View.GONE
            binding.layoutCopilotSignature.visibility = View.VISIBLE
        }
    }

    /**
     * 获取结果标签
     */
    private fun getResultLabel(type: String, value: Int?): String {
        return when (type) {
            "before" -> DriveCheckConstants.BEFORE_DRIVE_RESULTS.find { it.value == value }?.label ?: "请点击选择"
            "driving" -> DriveCheckConstants.DRIVING_RESULTS.find { it.value == value }?.label ?: "请点击选择"
            "end" -> DriveCheckConstants.AFTER_DRIVE_RESULTS.find { it.value == value }?.label ?: "请点击选择"
            else -> "请点击选择"
        }
    }

    /**
     * 获取道路状态标签
     */
    private fun getRoadStatusLabel(value: Int?): String {
        return DriveCheckConstants.ROAD_STATUS_LIST.find { it.value == value }?.label ?: "请点击选择"
    }

    /**
     * 设置点击事件
     */
    private fun setClickListeners() {
        binding.layoutTitle.tvTitle.text = "行车日志"
        PressEffectUtils.setCommonPressEffect(binding.layoutTitle.tvTitle)
        binding.layoutTitle.tvTitle.setOnClickListener {
            finish()
        }

        // 日期选择点击
        setDateRowClickListener(rowDateBinding, "addtime")
        setDateRowClickListener(rowGotimeBinding, "gotime")
        setDateRowClickListener(rowGettimeBinding, "gettime")
        setDateRowClickListener(rowStopTimeBinding, "stoptime")

        // 搜索车牌号
        PressEffectUtils.setCommonPressEffect(binding.btnSearchCarNum)
        binding.btnSearchCarNum.setOnClickListener {
            val intent = Intent(this, CarSearchActivity::class.java)
            startActivityForResult(intent, 1001)
        }

        // 货物类型选择
        binding.spinnerGoodsType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                viewModel.radioChange(DriveCheckConstants.TYPE_LIST[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 一键检查按钮
        PressEffectUtils.setCommonPressEffect(binding.btnAllCheck2)
        PressEffectUtils.setCommonPressEffect(binding.btnAllCheck3)
        PressEffectUtils.setCommonPressEffect(binding.btnAllCheck4)
        binding.btnAllCheck2.setOnClickListener {
            viewModel.currentCheckType.value = "before"
            beforeItemsAdapter.toggleAllSelection(true)
        }
        binding.btnAllCheck3.setOnClickListener {
            viewModel.currentCheckType.value = "driving"
            drivingItemsAdapter.toggleAllSelection(true)
        }
        binding.btnAllCheck4.setOnClickListener {
            viewModel.currentCheckType.value = "end"
            endItemsAdapter.toggleAllSelection(true)
        }

        // 选择器行点击
        PressEffectUtils.setCommonPressEffect(rowBeforeResultBinding.root)
        PressEffectUtils.setCommonPressEffect(rowRoadStatusBinding.root)
        PressEffectUtils.setCommonPressEffect(rowDrivingResultBinding.root)
        PressEffectUtils.setCommonPressEffect(rowEndResultBinding.root)
        rowBeforeResultBinding.root.setOnClickListener { viewModel.resultTest("before") }
        rowRoadStatusBinding.root.setOnClickListener { viewModel.roadStatusShow.value = true }
        rowDrivingResultBinding.root.setOnClickListener { viewModel.resultTest("driving") }
        rowEndResultBinding.root.setOnClickListener { viewModel.resultTest("end") }

        // 重签按钮
        PressEffectUtils.setCommonPressEffect(binding.btnRetDraw)
        PressEffectUtils.setCommonPressEffect(binding.btnRetDraw2)
        binding.btnRetDraw.setOnClickListener { binding.handwritingView.clearSignature() }
        binding.btnRetDraw2.setOnClickListener {
            // 副驾驶签名需要重新创建HandwritingView实例
            binding.handwritingView2.clearSignature()
        }

        // 下一步按钮
        PressEffectUtils.setCommonPressEffect(binding.btnNext)
        binding.btnNext.setOnClickListener {
            if (viewModel.goNext()) {
                println("June goNext:${viewModel.stage.value}")
                // 签名处理
                if (viewModel.stage.value == 7) {
                    // 副驾签名步骤
                    isCopilotSign = false
                    subCanvas()
                    // 最后一步，提交表单
                    saveFormData()
                } else if (viewModel.stage.value == 6) {
                    // 驾驶员签名步骤
                    isCopilotSign = true
                    subCanvas()
                }
            }
        }
    }

    /**
     * 为日期选择行设置点击事件
     */
    private fun setDateRowClickListener(binding: FormRowDateBinding, timeBelong: String) {
        PressEffectUtils.setCommonPressEffect(binding.root)
        binding.root.setOnClickListener {
            viewModel.timeBelong.value = timeBelong
            viewModel.openDateSelect(timeBelong)
        }
    }

    /**
     * 保存表单数据到ViewModel
     */
    private fun saveFormData() {
        val form = viewModel.localForm.value ?: return

        // 保存输入框数据
        form.driver_name = rowDriverNameBinding.etContent.text.toString().trim()
        form.copilot_name = if (form.type == "危险品") {
            rowCopilotNameBinding.etContent.text.toString().trim()
        } else {
            rowNormalCopilotBinding.etContent.text.toString().trim()
        }
        form.weather = rowWeatherBinding.etContent.text.toString().trim()
        form.temperature = rowTemperatureBinding.etContent.text.toString().trim()
        form.load = rowLoadBinding.etContent.text.toString().trim()
        form.real_load = rowRealLoadBinding.etContent.text.toString().trim()
        form.goods_name = rowGoodsNameBinding.etContent.text.toString().trim()
        form.start_address = rowStartAddressBinding.etContent.text.toString().trim()
        form.end_address = rowEndAddressBinding.etContent.text.toString().trim()
        form.mileage = rowMileageBinding.etContent.text.toString().trim()
        form.stopresult = rowStopResultBinding.etContent.text.toString().trim()
        form.stopaddress = rowStopAddressBinding.etContent.text.toString().trim()
        form.carnum = binding.etCarnum.text.toString().trim()

        viewModel.localForm.value = form

        driveLogViewModel.travelPost(viewModel.convertToTravelPostRequest(form), postLogStateFlow)
    }

    /**
     * 显示日期选择器
     */
    private fun showDatePicker() {
        val dateRange = viewModel.dateRange.value

        if (dateRange?.hour == true && dateRange.minute) {
            DialogUtils.showDateTimePicker(this@DriveLogStageActivity, currentCheckTime) {
                currentCheckTime = it
                updateFormDate(DateUtil.timestamp2String(currentCheckTime))
            }
        } else {
            DialogUtils.showDatePickerDlg(this@DriveLogStageActivity, currentCheckTime) {
                currentCheckTime = it
                updateFormDate(DateUtil.timestamp2Date(currentCheckTime))
            }
        }
    }

    /**
     * 更新表单日期
     */
    private fun updateFormDate(dateStr: String) {
        val form = viewModel.localForm.value ?: return

        when (viewModel.timeBelong.value) {
            "addtime" -> {
                form.addtime = dateStr
                rowDateBinding.tvContent.text = dateStr
            }
            "gotime" -> {
                form.gotime = dateStr
                rowGotimeBinding.tvContent.text = dateStr
            }
            "gettime" -> {
                form.gettime = dateStr
                rowGettimeBinding.tvContent.text = dateStr
            }
            "stoptime" -> {
                form.stoptime = dateStr
                rowStopTimeBinding.tvContent.text = dateStr
            }
        }

        viewModel.localForm.value = form
    }

    /**
     * 显示道路状态选择器
     */
    private fun showRoadStatusSelector() {
        val items = DriveCheckConstants.ROAD_STATUS_LIST
        val labels = items.map { it.label }.toTypedArray()

        BottomMenu.show(labels) { dialog, text, index ->
            viewModel.roadConfirm(index)
            rowRoadStatusBinding.tvContent.text = items[index].label
            false
        }
    }

    /**
     * 显示行车前结果选择器
     */
    private fun showBeforeResultSelector() {
        val items = DriveCheckConstants.BEFORE_DRIVE_RESULTS
        val labels = items.map { it.label }.toTypedArray()

        BottomMenu.show(labels) { dialog, text, which ->
            viewModel.confirm(which)
            rowBeforeResultBinding.tvContent.text = items[which].label
            false
        }
    }

    /**
     * 显示行车中结果选择器
     */
    private fun showDrivingResultSelector() {
        val items = DriveCheckConstants.DRIVING_RESULTS
        val labels = items.map { it.label }.toTypedArray()

        BottomMenu.show(labels) { dialog, text, which ->
            viewModel.roadResultConfirm(which)
            rowDrivingResultBinding.tvContent.text = items[which].label
            false
        }
    }

    /**
     * 显示收车后结果选择器
     */
    private fun showEndResultSelector() {
        val items = DriveCheckConstants.AFTER_DRIVE_RESULTS
        val labels = items.map { it.label }.toTypedArray()

        BottomMenu.show(labels) { dialog, text, which ->
            viewModel.roadResultConfirm(which)
            rowEndResultBinding.tvContent.text = items[which].label
            false
        }
    }

    /**
     * 显示草稿确认弹窗
     */
    private fun showDraftDialog() {
        AlertDialog.Builder(this)
            .setTitle("提示")
            .setMessage("返回会将已编辑的内容存入草稿。")
            .setPositiveButton("确认") { _, _ ->
                saveFormData()
                viewModel.confirmDraft()
                finish()
            }
            .setNegativeButton("取消") { dialog, _ ->
                viewModel.cancelDia()
                dialog.dismiss()
            }
            .show()
    }

    private fun onCheckItemChecked(position: Int, isSelected: Boolean) {
        if (position == -1) {
            // 全选/取消全选操作
            when (viewModel.currentCheckType.value) {
                "before" -> viewModel.handleAllCheck(beforeItemsAdapter.isAllItemsSelected())
                "driving" -> viewModel.handleAllCheck(drivingItemsAdapter.isAllItemsSelected())
                "end" -> viewModel.handleAllCheck(endItemsAdapter.isAllItemsSelected())
            }
        } else {
            // 单个item选中状态变化
            viewModel.toggleCheck(position, isSelected)

            // 显示选中数量提示
            val selectedCount = when (viewModel.currentCheckType.value) {
                "before" -> beforeItemsAdapter.getSelectedCount()
                "driving" -> drivingItemsAdapter.getSelectedCount()
                "end" -> endItemsAdapter.getSelectedCount()
                else -> 0
            }

            if (selectedCount > 0) {
                showToast("已选中 $selectedCount 项")
            }
        }
    }

    /**
     * 检查项点击
     */
    private fun toggleCheck(index: Int) {
        viewModel.toggleCheck(index)
    }

    /**
     * 保存签名
     */
    private fun subCanvas() {
        val handwritingView = if (isCopilotSign) {
            binding.handwritingView2
        } else {
            binding.handwritingView
        }

        val signatureBitmap = handwritingView.getSignatureBitmap()
        val file = BitmapUtils.saveBitmapToFile(this@DriveLogStageActivity, signatureBitmap)
        if (file != null) {
            viewModel.showimg.value = file.absolutePath
            viewModel.sign.value = true

            // 上传签名图片
            uploadFile(file)
        } else {
            viewModel.sign.value = false
            val form = viewModel.localForm.value ?: return

            if (isCopilotSign) {
                handwritingView.clearSignature()
                form.ysingimg = ""
                viewModel.localForm.value = form
            } else {
                form.dsingimg = ""
                viewModel.localForm.value = form
            }
        }
    }

    /**
     * 上传文件
     */
    private fun uploadFile(file: File) {
        noticeViewModel.uploadFile(
            MultipartBody.Part.createFormData("file", file.name,
                file.asRequestBody("image/png".toMediaTypeOrNull())),
            uploadStateFlow)
        }

    /**
     * 处理上传响应
     */
    private fun handleUploadResponse(signImgUrl: String) {

        val form = viewModel.localForm.value ?: return
        if (isCopilotSign) {
            form.ysingimg = signImgUrl
            // 更新副驾驶签名显示
            binding.ivCopilotSign.visibility = View.VISIBLE
            binding.layoutCopilotSignature.visibility = View.GONE
        } else {
            form.dsingimg = signImgUrl
            // 更新驾驶员签名显示
            binding.ivDriverSign.visibility = View.VISIBLE
            binding.layoutDriverSignature.visibility = View.GONE

            // 自动跳转到副驾驶签名步骤
            viewModel.stage.value = 6
            viewModel.stageStep.value = viewModel.stageStep.value?.plus(15) ?: 15
        }

        viewModel.localForm.value = form
    }

    /**
     * 处理返回结果
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            val carNum = data?.getStringExtra("carNum")
            val carId = data?.getIntExtra("carId", 0) ?: 0
            carNum?.let { selectedCarNum ->
                binding.etCarnum.setText(selectedCarNum)
                val form = viewModel.localForm.value ?: return@let
                form.carnum = selectedCarNum
                form.car_id = carId
                viewModel.localForm.value = form
                SPUtils.save(DriveLogModel.KEY_SELECTED_CAR_NUM, selectedCarNum)
                SPUtils.save(DriveLogModel.KEY_SELECTED_CAR_ID, carId)
            }
        }
    }

    override fun onBackPressed() {
        if (viewModel.goBack()) {
            // 显示草稿弹窗
            showDraftDialog()
        } else {
            super.onBackPressed()
        }
    }
}
