package com.fx.zfcar.training.safetycheck

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.fx.zfcar.databinding.ActivityCarCheckStageBinding
import com.fx.zfcar.databinding.LayoutStage1Binding
import com.fx.zfcar.databinding.LayoutStage2Binding
import com.fx.zfcar.databinding.LayoutStage3Binding
import com.fx.zfcar.databinding.LayoutStage4Binding
import com.fx.zfcar.databinding.LayoutStage5Binding
import com.fx.zfcar.databinding.LayoutStage6Binding
import com.fx.zfcar.databinding.LayoutStage7Binding
import com.fx.zfcar.net.CarCheckForm
import com.fx.zfcar.net.CheckStage
import com.fx.zfcar.training.adapter.ImageAdapter
import com.fx.zfcar.training.drivelog.CarSearchActivity
import com.fx.zfcar.util.BitmapUtils
import kotlinx.coroutines.launch
import java.util.*

class CarCheckStageActivity : AppCompatActivity() {
    // ViewBinding
    private lateinit var binding: ActivityCarCheckStageBinding
    private lateinit var stage1Binding: LayoutStage1Binding
    private lateinit var stage2Binding: LayoutStage2Binding
    private lateinit var stage3Binding: LayoutStage3Binding
    private lateinit var stage4Binding: LayoutStage4Binding
    private lateinit var stage5Binding: LayoutStage5Binding
    private lateinit var stage6Binding: LayoutStage6Binding
    private lateinit var stage7Binding: LayoutStage7Binding

    // ViewModel
    private val viewModel by viewModels<CarCheckViewModel>()

    // 请求码
    private val REQUEST_CODE_CAR_SEARCH = 1001
    private val REQUEST_CODE_IMAGE_SELECT = 1002

    // 图片适配器
    private lateinit var carCertiAdapter: ImageAdapter
    private lateinit var peopleCertiAdapter: ImageAdapter
    private lateinit var insureAdapter: ImageAdapter
    private lateinit var carCheckAdapter: ImageAdapter
    private lateinit var urgentAdapter: ImageAdapter
    private lateinit var signAdapter: ImageAdapter
    private lateinit var canbodyAdapter: ImageAdapter
    private lateinit var cutoffAdapter: ImageAdapter
    private lateinit var staticAdapter: ImageAdapter
    private lateinit var waybillAdapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化ViewBinding
        binding = ActivityCarCheckStageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化步骤Binding
        stage1Binding = LayoutStage1Binding.bind(binding.layoutStage1.root)
        stage2Binding = LayoutStage2Binding.bind(binding.layoutStage2.root)
        stage3Binding = LayoutStage3Binding.bind(binding.layoutStage3.root)
        stage4Binding = LayoutStage4Binding.bind(binding.layoutStage4.root)
        stage5Binding = LayoutStage5Binding.bind(binding.layoutStage5.root)
        stage6Binding = LayoutStage6Binding.bind(binding.layoutStage6.root)
        stage7Binding = LayoutStage7Binding.bind(binding.layoutStage7.root)

        // 初始化图片适配器
        initImageAdapters()

        // 初始化UI事件
        initUiEvents()

        // 监听数据变化
        observeData()

        // 初始化签名View
        initSignatureViews()

        // 初始显示第一步
        updateStageVisibility(CheckStage.STAGE_1)
    }

    /**
     * 初始化所有图片适配器
     */
    private fun initImageAdapters() {
        // 步骤2适配器
        carCertiAdapter = ImageAdapter { position ->
            viewModel.deleteImage("carCerti", position)
        }
        stage2Binding.rvCarCertiImgs.layoutManager = GridLayoutManager(this, 3)
        stage2Binding.rvCarCertiImgs.adapter = carCertiAdapter

        peopleCertiAdapter = ImageAdapter { position ->
            viewModel.deleteImage("peopleCerti", position)
        }
        stage2Binding.rvPeopleCertiImgs.layoutManager = GridLayoutManager(this, 3)
        stage2Binding.rvPeopleCertiImgs.adapter = peopleCertiAdapter

        insureAdapter = ImageAdapter { position ->
            viewModel.deleteImage("insureCerti", position)
        }
        stage2Binding.rvInsureImgs.layoutManager = GridLayoutManager(this, 3)
        stage2Binding.rvInsureImgs.adapter = insureAdapter

        // 步骤3适配器
        carCheckAdapter = ImageAdapter { position ->
            viewModel.deleteImage("carCheck", position)
        }
        stage3Binding.rvCarImgs.layoutManager = GridLayoutManager(this, 3)
        stage3Binding.rvCarImgs.adapter = carCheckAdapter

        urgentAdapter = ImageAdapter { position ->
            viewModel.deleteImage("urgentCheck", position)
        }
        stage3Binding.rvUrgentImgs.layoutManager = GridLayoutManager(this, 3)
        stage3Binding.rvUrgentImgs.adapter = urgentAdapter

        signAdapter = ImageAdapter { position ->
            viewModel.deleteImage("signCheck", position)
        }
        stage3Binding.rvSignImgs.layoutManager = GridLayoutManager(this, 3)
        stage3Binding.rvSignImgs.adapter = signAdapter

        // 步骤4适配器
        canbodyAdapter = ImageAdapter { position ->
            viewModel.deleteImage("canBody", position)
        }
        stage4Binding.rvCanbodyImgs.layoutManager = GridLayoutManager(this, 3)
        stage4Binding.rvCanbodyImgs.adapter = canbodyAdapter

        cutoffAdapter = ImageAdapter { position ->
            viewModel.deleteImage("cutoff", position)
        }
        stage4Binding.rvCutoffImgs.layoutManager = GridLayoutManager(this, 3)
        stage4Binding.rvCutoffImgs.adapter = cutoffAdapter

        staticAdapter = ImageAdapter { position ->
            viewModel.deleteImage("static", position)
        }
        stage4Binding.rvStaticImgs.layoutManager = GridLayoutManager(this, 3)
        stage4Binding.rvStaticImgs.adapter = staticAdapter

        waybillAdapter = ImageAdapter { position ->
            viewModel.deleteImage("waybill", position)
        }
        stage4Binding.rvWaybillImgs.layoutManager = GridLayoutManager(this, 3)
        stage4Binding.rvWaybillImgs.adapter = waybillAdapter
    }

    /**
     * 初始化所有UI事件
     */
    private fun initUiEvents() {
        // 返回按钮（退出确认）
        binding.ivBack.setOnClickListener {
            showExitConfirmDialog()
        }

        // 步骤1：日期选择
        stage1Binding.llChecktime.setOnClickListener {
            showDatePicker()
        }

        // 步骤1：车牌号搜索
        stage1Binding.btnSearchCar.setOnClickListener {
            // 实际项目替换为车牌号搜索页面
            val intent = Intent(this, CarSearchActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_CAR_SEARCH)
        }

        // 步骤1：输入框监听
        stage1Binding.etCarnum.setOnTextChangedListener { text ->
            viewModel.updateFormField { it.carnum = text }
        }

        stage1Binding.etCompany.setOnTextChangedListener { text ->
            viewModel.updateFormField { it.company = text }
        }

        stage1Binding.etName.setOnTextChangedListener { text ->
            viewModel.updateFormField { it.name = text }
        }

        // 步骤2：状态选择
        stage2Binding.rgCarCerti.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                stage2Binding.rbCarCertiOk.id ->
                    viewModel.toggleStatus("car_certificate_status", "0")
                stage2Binding.rbCarCertiNo.id ->
                    viewModel.toggleStatus("car_certificate_status", "1")
            }
        }

        stage2Binding.rgPeopleCerti.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                stage2Binding.rbPeopleCertiOk.id ->
                    viewModel.toggleStatus("people_certificate_status", "0")
                stage2Binding.rbPeopleCertiNo.id ->
                    viewModel.toggleStatus("people_certificate_status", "1")
            }
        }

        stage2Binding.rgInsure.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                stage2Binding.rbInsureOk.id ->
                    viewModel.toggleStatus("insure_status", "0")
                stage2Binding.rbInsureNo.id ->
                    viewModel.toggleStatus("insure_status", "1")
            }
        }

        // 步骤2：添加图片
        stage2Binding.btnAddCarCerti.setOnClickListener {
            selectImage { url ->
                viewModel.addImage("carCerti", url)
            }
        }

        stage2Binding.btnAddPeopleCerti.setOnClickListener {
            selectImage { url ->
                viewModel.addImage("peopleCerti", url)
            }
        }

        stage2Binding.btnAddInsure.setOnClickListener {
            selectImage { url ->
                viewModel.addImage("insureCerti", url)
            }
        }

        // 步骤3：状态选择
        stage3Binding.rgCar.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                stage3Binding.rbCarOk.id ->
                    viewModel.toggleStatus("car_status", "0")
                stage3Binding.rbCarNo.id ->
                    viewModel.toggleStatus("car_status", "1")
            }
        }

        stage3Binding.rgUrgent.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                stage3Binding.rbUrgentOk.id ->
                    viewModel.toggleStatus("urgent_status", "0")
                stage3Binding.rbUrgentNo.id ->
                    viewModel.toggleStatus("urgent_status", "1")
            }
        }

        stage3Binding.rgSign.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                stage3Binding.rbSignOk.id ->
                    viewModel.toggleStatus("sign_status", "0")
                stage3Binding.rbSignNo.id ->
                    viewModel.toggleStatus("sign_status", "1")
            }
        }

        // 步骤3：添加图片
        stage3Binding.btnAddCar.setOnClickListener {
            selectImage { url ->
                viewModel.addImage("carCheck", url)
            }
        }

        stage3Binding.btnAddUrgent.setOnClickListener {
            selectImage { url ->
                viewModel.addImage("urgentCheck", url)
            }
        }

        stage3Binding.btnAddSign.setOnClickListener {
            selectImage { url ->
                viewModel.addImage("signCheck", url)
            }
        }

        // 步骤4：状态选择
        stage4Binding.rgCanbody.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                stage4Binding.rbCanbodyOk.id ->
                    viewModel.toggleStatus("canbody_status", "0")
                stage4Binding.rbCanbodyNo.id ->
                    viewModel.toggleStatus("canbody_status", "1")
            }
        }

        stage4Binding.rgCutoff.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                stage4Binding.rbCutoffOk.id ->
                    viewModel.toggleStatus("cutoff_status", "0")
                stage4Binding.rbCutoffNo.id ->
                    viewModel.toggleStatus("cutoff_status", "1")
            }
        }

        stage4Binding.rgStatic.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                stage4Binding.rbStaticOk.id ->
                    viewModel.toggleStatus("static_status", "0")
                stage4Binding.rbStaticNo.id ->
                    viewModel.toggleStatus("static_status", "1")
            }
        }

        stage4Binding.rgWaybill.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                stage4Binding.rbWaybillOk.id ->
                    viewModel.toggleStatus("waybill_status", "0")
                stage4Binding.rbWaybillNo.id ->
                    viewModel.toggleStatus("waybill_status", "1")
            }
        }

        // 步骤4：添加图片
        stage4Binding.btnAddCanbody.setOnClickListener {
            selectImage { url ->
                viewModel.addImage("canBody", url)
            }
        }

        stage4Binding.btnAddCutoff.setOnClickListener {
            selectImage { url ->
                viewModel.addImage("cutoff", url)
            }
        }

        stage4Binding.btnAddStatic.setOnClickListener {
            selectImage { url ->
                viewModel.addImage("static", url)
            }
        }

        stage4Binding.btnAddWaybill.setOnClickListener {
            selectImage { url ->
                viewModel.addImage("waybill", url)
            }
        }

        // 步骤5：问题和意见输入
        stage5Binding.etQuestion.setOnTextChangedListener { text ->
            viewModel.updateFormField { it.question = text }
        }

        stage5Binding.etIdea.setOnTextChangedListener { text ->
            viewModel.updateFormField { it.idea = text }
        }

        // 步骤6：签名相关
        stage6Binding.btnClearSign.setOnClickListener {
            stage6Binding.signatureView.clearSignature()
            viewModel.setDriverSigned(false)
        }

        // 步骤7：签名相关
        stage7Binding.btnClearSign.setOnClickListener {
            stage7Binding.signatureView.clearSignature()
            viewModel.setCheckerSigned(false)
        }

        // 上一步/下一步按钮
        binding.btnPrev.setOnClickListener {
            viewModel.goPrevious()
        }

        binding.btnNext.setOnClickListener {
            handleNextStep()
        }
    }

    /**
     * 监听ViewModel数据变化
     */
    private fun observeData() {
        // 监听当前步骤
        lifecycleScope.launch {
            viewModel.currentStage.collect { stage ->
                updateStageUI(stage)
            }
        }

        // 监听表单数据
        lifecycleScope.launch {
            viewModel.form.collect { form ->
                updateFormUI(form)
            }
        }

        // 监听退出弹窗
        lifecycleScope.launch {
            viewModel.dialogShow.collect { show ->
                if (show) {
                    showExitConfirmDialog()
                }
            }
        }

        // 监听签名状态
        lifecycleScope.launch {
            viewModel.driverSigned.collect { signed ->
                // 检查人签名状态更新
            }
        }

        lifecycleScope.launch {
            viewModel.checkerSigned.collect { signed ->
                // 负责人签名状态更新
            }
        }
    }

    // 初始化签名View部分修改
    private fun initSignatureViews() {
        // 步骤6：检查人签名
        stage6Binding.signatureView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    val hasSignature = stage6Binding.signatureView.hasSignature()
                    // 获取签名图片（转为Base64或上传后的URL）
                    val signatureBitmap = stage6Binding.signatureView.getSignatureBitmap()
                    val signImage = if (signatureBitmap != null) {
                        // 实际项目中：1.转为Base64 2.上传到服务器获取URL
                        "data:image/png;base64,${BitmapUtils.bitmapToBase64(signatureBitmap)}"
                    } else {
                        ""
                    }
                    viewModel.setDriverSigned(hasSignature, signImage)
                }
            }
            true // 拦截触摸事件，防止ScrollView滚动
        }

        // 步骤7：负责人签名
        stage7Binding.signatureView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    val hasSignature = stage7Binding.signatureView.hasSignature()
                    // 获取签名图片
                    val signatureBitmap = stage7Binding.signatureView.getSignatureBitmap()
                    val signImage = if (signatureBitmap != null) {
                        "data:image/png;base64,${BitmapUtils.bitmapToBase64(signatureBitmap)}"
                    } else {
                        ""
                    }
                    viewModel.setCheckerSigned(hasSignature, signImage)
                }
            }
            true
        }
    }

    /**
     * 更新步骤UI
     */
    private fun updateStageUI(stage: CheckStage) {
        // 更新标题
        binding.tvTitle.text = "车辆安全检查(${stage.step}/7)"

        // 更新进度条
        binding.progressBar.progress = stage.progress

        // 更新步骤可见性
        updateStageVisibility(stage)

        // 更新按钮状态
        binding.btnPrev.isEnabled = stage.step > 1
        binding.btnPrev.visibility = if (stage.step > 1) {
            android.view.View.VISIBLE
        } else {
            android.view.View.GONE
        }

        // 更新下一步按钮文本
        binding.btnNext.text = if (stage.step == 7) "提交检查" else "下一步"
    }

    /**
     * 更新步骤可见性
     */
    private fun updateStageVisibility(stage: CheckStage) {
        // 隐藏所有步骤
        stage1Binding.root.visibility = android.view.View.GONE
        stage2Binding.root.visibility = android.view.View.GONE
        stage3Binding.root.visibility = android.view.View.GONE
        stage4Binding.root.visibility = android.view.View.GONE
        stage5Binding.root.visibility = android.view.View.GONE
        stage6Binding.root.visibility = android.view.View.GONE
        stage7Binding.root.visibility = android.view.View.GONE

        // 显示当前步骤
        when (stage) {
            CheckStage.STAGE_1 -> stage1Binding.root.visibility = android.view.View.VISIBLE
            CheckStage.STAGE_2 -> stage2Binding.root.visibility = android.view.View.VISIBLE
            CheckStage.STAGE_3 -> stage3Binding.root.visibility = android.view.View.VISIBLE
            CheckStage.STAGE_4 -> stage4Binding.root.visibility = android.view.View.VISIBLE
            CheckStage.STAGE_5 -> stage5Binding.root.visibility = android.view.View.VISIBLE
            CheckStage.STAGE_6 -> stage6Binding.root.visibility = android.view.View.VISIBLE
            CheckStage.STAGE_7 -> stage7Binding.root.visibility = android.view.View.VISIBLE
        }
    }

    /**
     * 更新表单UI
     */
    private fun updateFormUI(form: CarCheckForm) {
        // 步骤1数据
        stage1Binding.etCarnum.setText(form.carnum)
        stage1Binding.etCompany.setText(form.company)
        stage1Binding.etName.setText(form.name)
        stage1Binding.tvChecktime.text = form.checktime.ifBlank { "请选择检查日期" }

        // 步骤2状态和图片
        updateRadioStatus(
            form.car_certificate_status,
            stage2Binding.rbCarCertiOk,
            stage2Binding.rbCarCertiNo
        )
        updateRadioStatus(
            form.people_certificate_status,
            stage2Binding.rbPeopleCertiOk,
            stage2Binding.rbPeopleCertiNo
        )
        updateRadioStatus(
            form.insure_status,
            stage2Binding.rbInsureOk,
            stage2Binding.rbInsureNo
        )

        // 更新步骤2图片列表
        carCertiAdapter.submitList(form.car_certificate_fileimg)
        peopleCertiAdapter.submitList(form.people_certificate_fileimg)
        insureAdapter.submitList(form.insure_fileimg)

        // 更新添加按钮可见性
        stage2Binding.btnAddCarCerti.visibility = getAddBtnVisibility(form.car_certificate_fileimg.size)
        stage2Binding.btnAddPeopleCerti.visibility = getAddBtnVisibility(form.people_certificate_fileimg.size)
        stage2Binding.btnAddInsure.visibility = getAddBtnVisibility(form.insure_fileimg.size)

        // 步骤3状态和图片
        updateRadioStatus(
            form.car_status,
            stage3Binding.rbCarOk,
            stage3Binding.rbCarNo
        )
        updateRadioStatus(
            form.urgent_status,
            stage3Binding.rbUrgentOk,
            stage3Binding.rbUrgentNo
        )
        updateRadioStatus(
            form.sign_status,
            stage3Binding.rbSignOk,
            stage3Binding.rbSignNo
        )

        carCheckAdapter.submitList(form.car_fileimg)
        urgentAdapter.submitList(form.urgent_fileimg)
        signAdapter.submitList(form.sign_fileimg)

        stage3Binding.btnAddCar.visibility = getAddBtnVisibility(form.car_fileimg.size)
        stage3Binding.btnAddUrgent.visibility = getAddBtnVisibility(form.urgent_fileimg.size)
        stage3Binding.btnAddSign.visibility = getAddBtnVisibility(form.sign_fileimg.size)

        // 步骤4状态和图片
        updateRadioStatus(
            form.canbody_status,
            stage4Binding.rbCanbodyOk,
            stage4Binding.rbCanbodyNo
        )
        updateRadioStatus(
            form.cutoff_status,
            stage4Binding.rbCutoffOk,
            stage4Binding.rbCutoffNo
        )
        updateRadioStatus(
            form.static_status,
            stage4Binding.rbStaticOk,
            stage4Binding.rbStaticNo
        )
        updateRadioStatus(
            form.waybill_status,
            stage4Binding.rbWaybillOk,
            stage4Binding.rbWaybillNo
        )

        canbodyAdapter.submitList(form.canbody_fileimg)
        cutoffAdapter.submitList(form.cutoff_fileimg)
        staticAdapter.submitList(form.static_fileimg)
        waybillAdapter.submitList(form.waybill_fileimg)

        stage4Binding.btnAddCanbody.visibility = getAddBtnVisibility(form.canbody_fileimg.size)
        stage4Binding.btnAddCutoff.visibility = getAddBtnVisibility(form.cutoff_fileimg.size)
        stage4Binding.btnAddStatic.visibility = getAddBtnVisibility(form.static_fileimg.size)
        stage4Binding.btnAddWaybill.visibility = getAddBtnVisibility(form.waybill_fileimg.size)

        // 步骤5数据
        stage5Binding.etQuestion.setText(form.question)
        stage5Binding.etIdea.setText(form.idea)
    }

    /**
     * 更新单选按钮状态
     */
    private fun updateRadioStatus(
        status: String,
        okBtn: android.widget.RadioButton,
        noBtn: android.widget.RadioButton
    ) {
        when (status) {
            "0" -> okBtn.isChecked = true
            "1" -> noBtn.isChecked = true
        }
    }

    /**
     * 获取添加按钮可见性
     */
    private fun getAddBtnVisibility(size: Int): Int {
        return if (size < 9) android.view.View.VISIBLE else android.view.View.GONE
    }

    /**
     * 显示日期选择器
     */
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val date = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)
                viewModel.updateFormField { it.checktime = date }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    /**
     * 显示退出确认弹窗
     */
    private fun showExitConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("确认退出")
            .setMessage("退出后当前检查进度将丢失，是否确认退出？")
            .setPositiveButton("确认") { dialog, _ ->
                finish()
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ ->
                viewModel.hideDialog()
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    /**
     * 处理下一步/提交操作
     */
    private fun handleNextStep() {
        // 表单校验
        val validateError = viewModel.validateCurrentStage()
        if (validateError != null) {
            Toast.makeText(this, validateError, Toast.LENGTH_SHORT).show()
            return
        }

        val currentStage = viewModel.currentStage.value

        // 最后一步提交
        if (currentStage == CheckStage.STAGE_7) {
            showSubmitLoading()

            viewModel.submitForm(
                onSuccess = {
                    hideSubmitLoading()
                    Toast.makeText(this, "检查表单提交成功", Toast.LENGTH_LONG).show()
                    finish()
                },
                onError = { error ->
                    hideSubmitLoading()
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            viewModel.goNext()
        }
    }

    /**
     * 选择图片（简化版，实际项目替换为图片选择库）
     */
    private fun selectImage(onResult: (String) -> Unit) {
        // 模拟图片选择，实际项目中替换为：
        // 1. 系统相册/相机调用
        // 2. 第三方图片选择库（如PictureSelector）
        // 3. 图片上传后返回URL

        AlertDialog.Builder(this)
            .setTitle("选择图片")
            .setItems(arrayOf("从相册选择", "拍照")) { _, which ->
                // 模拟返回图片URL
                val imageUrl = "https://example.com/image_${System.currentTimeMillis()}.jpg"
                onResult(imageUrl)
            }
            .show()
    }

    /**
     * 显示提交加载中
     */
    private fun showSubmitLoading() {
        binding.btnNext.isEnabled = false
        binding.btnNext.text = "提交中..."
    }

    /**
     * 隐藏提交加载中
     */
    private fun hideSubmitLoading() {
        binding.btnNext.isEnabled = true
        binding.btnNext.text = "提交检查"
    }

    /**
     * 文本变化监听扩展函数
     */
    private fun android.widget.EditText.setOnTextChangedListener(action: (String) -> Unit) {
        this.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: android.text.Editable?) {
                action(s.toString().trim())
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK) return

        when (requestCode) {
            REQUEST_CODE_CAR_SEARCH -> {
                val carNum = data?.getStringExtra("carnum") ?: ""
                viewModel.updateFormField { it.carnum = carNum }
            }
            REQUEST_CODE_IMAGE_SELECT -> {
                val imageUrl = data?.getStringExtra("image_url") ?: ""
                // 根据当前步骤处理图片
                // 实际项目中需要区分是哪个检查项的图片
            }
        }
    }
}