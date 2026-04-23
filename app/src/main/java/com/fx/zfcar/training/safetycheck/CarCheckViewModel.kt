package com.fx.zfcar.training.safetycheck

import com.fx.zfcar.training.viewmodel.TrainingBaseViewModel

import androidx.lifecycle.viewModelScope
import com.fx.zfcar.net.CarCheckForm
import com.fx.zfcar.net.CarCheckPostRequest
import com.fx.zfcar.net.CategoryDetail
import com.fx.zfcar.net.CheckStage
import com.fx.zfcar.net.UserInfoDetail
import com.fx.zfcar.util.DateUtil
import com.fx.zfcar.util.SPUtils
import com.fx.zfcar.viewmodel.ApiState
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CarCheckViewModel: TrainingBaseViewModel() {
    // 表单数据（本地存储）
    private val _form = MutableStateFlow(CarCheckForm())
    val form: StateFlow<CarCheckForm> = _form.asStateFlow()

    // 当前检查步骤
    private val _currentStage = MutableStateFlow(CheckStage.STAGE_1)
    val currentStage: StateFlow<CheckStage> = _currentStage.asStateFlow()

    // 退出确认弹窗状态
    private val _dialogShow = MutableStateFlow(false)
    val dialogShow: StateFlow<Boolean> = _dialogShow.asStateFlow()

    // 签名状态
    private val _driverSigned = MutableStateFlow(false)  // 检查人签名
    val driverSigned: StateFlow<Boolean> = _driverSigned.asStateFlow()

    private val _checkerSigned = MutableStateFlow(false) // 负责人签名
    val checkerSigned: StateFlow<Boolean> = _checkerSigned.asStateFlow()

    private val postStateFlow = MutableStateFlow<ApiState<String>>(ApiState.Idle)

    init {
        val user = parseUserInfo()
        val company = parseCompanyInfo()
        // 初始化默认值
        _form.update {
            it.copy(
                checktime = DateUtil.timestamp2Date(System.currentTimeMillis()),
                name = user?.nickname?.takeIf { name -> name.isNotBlank() }
                    ?: user?.username.orEmpty(),
                company = company?.name?.takeIf { name -> name.isNotBlank() }
                    ?: company?.nickname.orEmpty()
            )
        }
    }

    private fun parseUserInfo(): UserInfoDetail? {
        return SPUtils.get("userInfo").takeIf { it.isNotBlank() }?.let {
            runCatching { Gson().fromJson(it, UserInfoDetail::class.java) }.getOrNull()
        }
    }

    private fun parseCompanyInfo(): CategoryDetail? {
        return SPUtils.get("companyInfo").takeIf { it.isNotBlank() }?.let {
            runCatching { Gson().fromJson(it, CategoryDetail::class.java) }.getOrNull()
        }
    }

    // 步骤导航
    fun goNext() {
        val current = _currentStage.value
        if (current.step < 7) {
            _currentStage.value = CheckStage.fromStep(current.step + 1)
        }
    }

    fun goPrevious() {
        val current = _currentStage.value
        if (current.step > 1) {
            _currentStage.value = CheckStage.fromStep(current.step - 1)
        }
    }

    // 更新表单字段
    fun updateFormField(block: (CarCheckForm) -> Unit) {
        _form.update {
            block(it)
            it
        }
    }

    // 切换检查状态（合格/不合格）
    fun toggleStatus(field: String, value: String) {
        _form.update { form ->
            when (field) {
                "car_certificate_status" -> form.copy(car_certificate_status = value)
                "people_certificate_status" -> form.copy(people_certificate_status = value)
                "insure_status" -> form.copy(insure_status = value)
                "car_status" -> form.copy(car_status = value)
                "urgent_status" -> form.copy(urgent_status = value)
                "sign_status" -> form.copy(sign_status = value)
                "canbody_status" -> form.copy(canbody_status = value)
                "cutoff_status" -> form.copy(cutoff_status = value)
                "static_status" -> form.copy(static_status = value)
                "waybill_status" -> form.copy(waybill_status = value)
                else -> form
            }
        }
    }

    // 添加检查图片
    fun addImage(field: String, url: String) {
        if (url.isBlank()) return

        _form.update { form ->
            when (field) {
                "carCerti" -> form.car_certificate_fileimg.add(url)
                "peopleCerti" -> form.people_certificate_fileimg.add(url)
                "insureCerti" -> form.insure_fileimg.add(url)
                "carCheck" -> form.car_fileimg.add(url)
                "urgentCheck" -> form.urgent_fileimg.add(url)
                "signCheck" -> form.sign_fileimg.add(url)
                "canBody" -> form.canbody_fileimg.add(url)
                "cutoff" -> form.cutoff_fileimg.add(url)
                "static" -> form.static_fileimg.add(url)
                "waybill" -> form.waybill_fileimg.add(url)
            }
            form
        }
    }

    // 删除检查图片
    fun deleteImage(field: String, index: Int) {
        if (index < 0) return

        _form.update { form ->
            when (field) {
                "carCerti" -> if (index < form.car_certificate_fileimg.size) form.car_certificate_fileimg.removeAt(index)
                "peopleCerti" -> if (index < form.people_certificate_fileimg.size) form.people_certificate_fileimg.removeAt(index)
                "insureCerti" -> if (index < form.insure_fileimg.size) form.insure_fileimg.removeAt(index)
                "carCheck" -> if (index < form.car_fileimg.size) form.car_fileimg.removeAt(index)
                "urgentCheck" -> if (index < form.urgent_fileimg.size) form.urgent_fileimg.removeAt(index)
                "signCheck" -> if (index < form.sign_fileimg.size) form.sign_fileimg.removeAt(index)
                "canBody" -> if (index < form.canbody_fileimg.size) form.canbody_fileimg.removeAt(index)
                "cutoff" -> if (index < form.cutoff_fileimg.size) form.cutoff_fileimg.removeAt(index)
                "static" -> if (index < form.static_fileimg.size) form.static_fileimg.removeAt(index)
                "waybill" -> if (index < form.waybill_fileimg.size) form.waybill_fileimg.removeAt(index)
            }
            form
        }
    }

    // 设置签名状态和图片
    fun setDriverSigned(signed: Boolean, signImage: String = "") {
        _driverSigned.value = signed
        _form.update { form ->
            when {
                !signed -> form.copy(checksign_img = "")
                signImage.isNotBlank() -> form.copy(checksign_img = signImage)
                else -> form
            }
        }
    }

    fun setCheckerSigned(signed: Boolean, signImage: String = "") {
        _checkerSigned.value = signed
        _form.update { form ->
            when {
                !signed -> form.copy(dirversign_img = "")
                signImage.isNotBlank() -> form.copy(dirversign_img = signImage)
                else -> form
            }
        }
    }

    // 弹窗控制
    fun showDialog() {
        _dialogShow.value = true
    }

    fun hideDialog() {
        _dialogShow.value = false
    }

    // 表单校验
    fun validateCurrentStage(): String? {
        val form = _form.value
        return when (_currentStage.value) {
            CheckStage.STAGE_1 -> {
                when {
                    form.checktime.isBlank() -> "请选择检查日期"
                    form.carnum.isBlank() -> "请输入车牌号"
                    form.company.isBlank() -> "请输入检查单位"
                    form.name.isBlank() -> "请输入检查人姓名"
                    else -> null
                }
            }
            CheckStage.STAGE_2 -> {
                when {
                    form.car_certificate_fileimg.isEmpty() -> "请上传车辆证件检查图片"
                    form.people_certificate_fileimg.isEmpty() -> "请上传人员证件检查图片"
                    form.insure_fileimg.isEmpty() -> "请上传车辆保险检查图片"
                    else -> null
                }
            }
            CheckStage.STAGE_3 -> {
                when {
                    form.car_fileimg.isEmpty() -> "请上传车辆检查图片"
                    form.urgent_fileimg.isEmpty() -> "请上传应急器材检查图片"
                    form.sign_fileimg.isEmpty() -> "请上传标识标志检查图片"
                    else -> null
                }
            }
            CheckStage.STAGE_4 -> {
                when {
                    form.canbody_fileimg.isEmpty() -> "请上传罐体检查图片"
                    form.cutoff_fileimg.isEmpty() -> "请上传紧急切断阀照片"
                    form.static_fileimg.isEmpty() -> "请上传导静电检查图片"
                    form.waybill_fileimg.isEmpty() -> "请上传运单检查图片"
                    else -> null
                }
            }
            CheckStage.STAGE_5 -> {
                when {
                    form.question.isBlank() -> "请输入存在问题"
                    form.idea.isBlank() -> "请输入处理意见"
                    else -> null
                }
            }
            CheckStage.STAGE_6 -> {
                if (!_driverSigned.value) "请完成检查人签名" else null
            }
            CheckStage.STAGE_7 -> {
                if (!_checkerSigned.value) "请完成车辆负责人签名" else null
            }
        }
    }

    // 提交检查表单（使用CarCheckPostRequest）
    fun submitForm(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentForm = _form.value

        // 数据校验
        val validateError = validateCurrentStage()
        if (validateError != null) {
            onError(validateError)
            return
        }

        viewModelScope.launch {
            postStateFlow.drop(1)
                .collect { state ->
                    when (state) {
                        is ApiState.Success -> {
                            state.data?.let {
                                onSuccess()
                            }
                        }
                        is ApiState.Error -> {
                            onError(state.msg)
                        }
                        else -> {}
                    }
                }
        }

        val postRequest = currentForm.toPostRequest()
        carCheckPost(postRequest, postStateFlow)
    }

    fun carCheckPost(request: CarCheckPostRequest, stateFlow: MutableStateFlow<ApiState<String>>) {
        launchRequest(
            block = { vehicleRepository.carCheckPost(request) },
            stateFlow
        )
    }
}
