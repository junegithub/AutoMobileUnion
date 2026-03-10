package com.fx.zfcar.training.drivelog

import androidx.lifecycle.MutableLiveData
import com.fx.zfcar.net.TravelPostRequest
import com.fx.zfcar.util.DateUtil
import com.fx.zfcar.util.SPUtils
import com.google.gson.Gson
import java.util.regex.Pattern

class DriveLogModel {
    // 步骤控制
    val stage = MutableLiveData<Int>(1)
    val stageStep = MutableLiveData<Int>(15)

    // 弹窗控制
    val diaShow = MutableLiveData<Boolean>(false)

    // 选择器显示控制
    val roadStatusShow = MutableLiveData<Boolean>(false)
    val twoListShow = MutableLiveData<Boolean>(false)
    val threeListShow = MutableLiveData<Boolean>(false)
    val endListShow = MutableLiveData<Boolean>(false)

    // 日期选择相关
    val timeSelect = MutableLiveData<Boolean>(false)
    val timeBelong = MutableLiveData<String>("")
    val dateRange = MutableLiveData<DateRange>(DateRange(true, true, true, false, false))

    // 签名相关
    val secondSign = MutableLiveData<Boolean>(false)
    val sign = MutableLiveData<Boolean>(false)
    val showimg = MutableLiveData<String>("")

    // 本地表单数据（UI交互用）
    val localForm = MutableLiveData(DriveCheckConstants.LocalFormData())

    // 检查项数据
    val beforeItems = MutableLiveData<MutableList<DriveCheckConstants.CheckItem>>(
        DriveCheckConstants.BEFORE_DRIVE_ITEMS.toMutableList()
    )
    val drivingCheckItemsArr = MutableLiveData<MutableList<DriveCheckConstants.CheckItem>>(
        DriveCheckConstants.DRIVING_ITEMS.toMutableList()
    )
    val checkEndItems = MutableLiveData<MutableList<DriveCheckConstants.CheckItem>>(
        DriveCheckConstants.AFTER_DRIVE_ITEMS.toMutableList()
    )

    // Toast提示
    val showToast = MutableLiveData<String?>()

    /**
     * 初始化
     */
    fun init(carNum: String? = null) {

        // 获取用户信息
        val userInfo = SPUtils.get("userInfo")
        val (driverName, userId) = if (!userInfo.isNullOrEmpty()) {
            // 解析用户信息获取昵称和ID - 实际项目中解析JSON
            Pair("默认驾驶员", 0)
        } else {
            Pair("默认驾驶员", 0)
        }

        // 获取车辆信息
        val (carId, carNumber) = if (!carNum.isNullOrEmpty()) {
            Pair(0, carNum) // 实际项目中根据车牌号获取car_id
        } else {
            val carInfo = SPUtils.get("carInfo")
            if (!carInfo.isNullOrEmpty()) {
                Pair(0, "默认车牌号") // 解析车辆信息
            } else {
                Pair(0, "鲁Y88888")
            }
        }

        // 更新本地表单默认值
        val currentForm = localForm.value ?: DriveCheckConstants.LocalFormData()
        currentForm.apply {
            addtime = DateUtil.timestamp2Date(System.currentTimeMillis())
            driver_name = driverName
            user_id = userId
            car_id = carId
            carnum = carNumber
            updatetime = DateUtil.timestamp2String(System.currentTimeMillis())
        }

        // 加载草稿
        val draft = SPUtils.get("draft")
        if (!draft.isNullOrEmpty()) {
            // 解析草稿数据 - 实际项目中使用Gson解析
            handleDraft(currentForm)
        }

        localForm.postValue(currentForm)
    }

    /**
     * 处理草稿数据
     */
    private fun handleDraft(form: DriveCheckConstants.LocalFormData) {
        // 如果有检查结果，一键勾选所有检查项
        if (form.sresult != null || form.groad != null || form.eresult != null) {
            allCheck()
        }
    }

    /**
     * 打开日期选择器
     */
    fun openDateSelect(data: String) {
        timeBelong.value = data

        // 设置日期选择范围
        if (stage.value == 3 || data == "gotime" || data == "gettime") {
            // 选择时分秒
            dateRange.value = DateRange(true, true, true, true, true)
        } else {
            dateRange.value = DateRange(true, true, true, false, false)
        }

        timeSelect.value = true
    }

    /**
     * 确认日期选择
     */
    fun comfirmDate(date: DateData) {
        val currentForm = localForm.value ?: return

        val dateStr = when (timeBelong.value) {
            "addtime" -> "${date.year}-${date.month}-${date.day}"
            "stoptime" -> "${date.year}-${date.month}-${date.day}-${date.hour}:${date.minute}"
            "gotime" -> "${date.year}-${date.month}-${date.day}-${date.hour}:${date.minute}"
            "gettime" -> "${date.year}${date.month}${date.day}${date.hour}${date.minute}".toIntOrNull()?.toString() ?: ""
            else -> ""
        }

        when (timeBelong.value) {
            "addtime" -> currentForm.addtime = dateStr
            "stoptime" -> currentForm.stoptime = dateStr
            "gotime" -> currentForm.gotime = dateStr
            "gettime" -> currentForm.gettime = dateStr
        }

        localForm.postValue(currentForm)
        timeSelect.value = false
    }

    val currentCheckType = MutableLiveData<String>("before")

    // 5. 新增全选处理方法
    fun handleAllCheck(isAllSelected: Boolean) {
        // 根据全选状态更新数据
        when (currentCheckType.value) {
            "before" -> {
                val newItems = beforeItems.value?.map {
                    it.copy(active = isAllSelected)
                } ?: emptyList()
                beforeItems.postValue(newItems as MutableList<DriveCheckConstants.CheckItem>?)
            }
            "driving" -> {
                val newItems = drivingCheckItemsArr.value?.map {
                    it.copy(active = isAllSelected)
                } ?: emptyList()
                drivingCheckItemsArr.postValue(newItems as MutableList<DriveCheckConstants.CheckItem>?)
            }
            "end" -> {
                val newItems = checkEndItems.value?.map {
                    it.copy(active = isAllSelected)
                } ?: emptyList()
                checkEndItems.postValue(newItems as MutableList<DriveCheckConstants.CheckItem>?)
            }
        }

        showToast.postValue(if (isAllSelected) "已全选所有检查项" else "已取消全选")
    }

    // 6. 更新toggleCheck方法支持选中状态参数
    fun toggleCheck(position: Int, isSelected: Boolean? = null) {
        val currentType = currentCheckType.value ?: return

        when (currentType) {
            "before" -> {
                val items = beforeItems.value?.toMutableList() ?: return
                val item = items[position]
                items[position] = item.copy(active = isSelected ?: !item.active)
                beforeItems.postValue(items)
            }
            "driving" -> {
                val items = drivingCheckItemsArr.value?.toMutableList() ?: return
                val item = items[position]
                items[position] = item.copy(active = isSelected ?: !item.active)
                drivingCheckItemsArr.postValue(items)
            }
            "end" -> {
                val items = checkEndItems.value?.toMutableList() ?: return
                val item = items[position]
                items[position] = item.copy(active = isSelected ?: !item.active)
                checkEndItems.postValue(items)
            }
        }
    }

    /**
     * 一键检查所有项
     */
    fun allCheck() {
        when (stage.value) {
            2 -> {
                val items = beforeItems.value ?: return
                items.forEach { it.active = true }
                beforeItems.postValue(items)
            }
            3 -> {
                val items = drivingCheckItemsArr.value ?: return
                items.forEach { it.active = true }
                drivingCheckItemsArr.postValue(items)
            }
            4 -> {
                val items = checkEndItems.value ?: return
                items.forEach { it.active = true }
                checkEndItems.postValue(items)
            }
        }
    }

    /**
     * 货物类型选择
     */
    fun radioChange(item: DriveCheckConstants.SelectItem) {
        val currentForm = localForm.value ?: return
        currentForm.type = item.label // "危险品"/"普通货物"
        localForm.postValue(currentForm)
    }

    /**
     * 结果选择处理
     */
    fun resultTest(data: String) {
        when (data) {
            "before" -> {
                val items = beforeItems.value ?: return
                if (items.all { it.active }) {
                    twoListShow.value = true
                } else {
                    showToast.value = "请确认检查所有项目"
                }
            }
            "driving" -> {
                val items = drivingCheckItemsArr.value ?: return
                if (items.all { it.active }) {
                    threeListShow.value = true
                } else {
                    showToast.value = "请确认检查所有项目"
                }
            }
            "end" -> {
                val items = checkEndItems.value ?: return
                if (items.all { it.active }) {
                    endListShow.value = true
                } else {
                    showToast.value = "请确认检查所有项目"
                }
            }
        }
    }

    /**
     * 确认行车前结果选择
     */
    fun confirm(selection: Int) {
        val currentForm = localForm.value ?: return
        currentForm.sresult = selection
        localForm.postValue(currentForm)
        twoListShow.value = false
    }

    /**
     * 确认道路状态选择
     */
    fun roadConfirm(selection: Int) {
        val currentForm = localForm.value ?: return
        currentForm.groad = selection // 暂存为Int，提交时转String
        localForm.postValue(currentForm)
        roadStatusShow.value = false
    }

    /**
     * 确认道路结果选择
     */
    fun roadResultConfirm(selection: Int) {
        val currentForm = localForm.value ?: return
        when (stage.value) {
            3 -> currentForm.gresult = selection // 行车中结果暂存为Int
            4 -> currentForm.eresult = selection // 收车后结果
        }
        localForm.postValue(currentForm)
        threeListShow.value = false
        endListShow.value = false
    }

    /**
     * 下一步
     */
    fun goNext(): Boolean {
        // 验证当前步骤
        val currentForm = localForm.value ?: return false

        when (stage.value) {
            1 -> {
                // 步骤1验证
                when {
                    currentForm.driver_name.isBlank() -> {
                        showToast.value = "请输入驾驶员姓名"
                        return false
                    }
                    currentForm.carnum.isBlank() -> {
                        showToast.value = "请输入车牌号"
                        return false
                    }
                    !isLicenseNo(currentForm.carnum) -> {
                        showToast.value = "请输入正确的车牌号"
                        return false
                    }
                    currentForm.type.isBlank() -> {
                        showToast.value = "请选择货物类型"
                        return false
                    }
                }
            }
            2 -> {
                // 步骤2验证
                if (currentForm.sresult == null) {
                    showToast.value = "选择行车前状态"
                    return false
                }
            }
            3 -> {
                // 步骤3验证
                when {
                    currentForm.gresult == null -> {
                        showToast.value = "请选择行车中检查结果"
                        return false
                    }
                    currentForm.groad == null -> {
                        showToast.value = "请选择行车中道路情况"
                        return false
                    }
                }
            }
            4 -> {
                // 步骤4验证
                if (currentForm.eresult == null) {
                    showToast.value = "请选择行车结束的状态"
                    return false
                }
            }
        }

        // 更新步骤
        val currentStage = stage.value ?: 1
        stage.value = currentStage + 1
        stageStep.value = stageStep.value?.plus(15) ?: 15

        // 重置检查项状态
        resetCheckItems()

        // 重置签名状态
        if (currentStage == 5 || currentStage == 6) {
            sign.value = false
            showimg.value = ""
        }

        return true
    }

    /**
     * 重置检查项
     */
    private fun resetCheckItems() {
        beforeItems.postValue(DriveCheckConstants.BEFORE_DRIVE_ITEMS.toMutableList())
        drivingCheckItemsArr.postValue(DriveCheckConstants.DRIVING_ITEMS.toMutableList())
        checkEndItems.postValue(DriveCheckConstants.AFTER_DRIVE_ITEMS.toMutableList())
    }

    /**
     * 验证车牌号
     */
    private fun isLicenseNo(license: String): Boolean {
        val pattern = Pattern.compile("^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-Z0-9]{4}[A-Z0-9挂学警港澳]{1}$")
        return pattern.matcher(license).matches()
    }

    /**
     * 取消弹窗
     */
    fun cancelDia() {
        diaShow.value = false
    }

    /**
     * 确认保存草稿
     */
    fun confirmDraft() {
        submitDriveInfo("draft")
        diaShow.value = false
    }

    /**
     * 返回处理
     */
    fun goBack(): Boolean {
        val currentForm = localForm.value ?: return false
        return if (currentForm.carnum.isNotBlank() && currentForm.type.isNotBlank()) {
            diaShow.value = true
            true
        } else {
            false
        }
    }

    /**
     * 转换本地表单为提交模型
     */
    fun convertToTravelPostRequest(localForm: DriveCheckConstants.LocalFormData): TravelPostRequest {
        return TravelPostRequest(
            id = localForm.id,
            car_id = localForm.car_id,
            driver_name = localForm.driver_name,
            addtime = localForm.addtime,
            carnum = localForm.carnum,
            user_id = localForm.user_id,
            type = if (localForm.type == "危险品") 0 else 1, // 转换为数字类型
            copilot_name = localForm.copilot_name,
            weather = localForm.weather,
            temperature = localForm.temperature,
            load = localForm.load,
            real_load = localForm.real_load,
            goods_name = localForm.goods_name,
            gotime = localForm.gotime,
            gettime = localForm.gettime.toIntOrNull() ?: 0, // 字符串转Int
            start_address = localForm.start_address,
            end_address = localForm.end_address,
            mileage = localForm.mileage,
            sresult = localForm.sresult ?: 0,
            groad = localForm.groad?.toString() ?: "", // Int转String
            gresult = localForm.gresult?.toString() ?: "", // Int转String
            stopresult = localForm.stopresult,
            stopaddress = localForm.stopaddress,
            stoptime = localForm.stoptime,
            eresult = localForm.eresult ?: 0,
            dsingimg = localForm.dsingimg,
            staus = localForm.staus,
            updatetime = localForm.updatetime
        )
    }

    /**
     * 提交行车日志
     */
    fun submitDriveInfo(type: String = "") {
        val currentLocalForm = localForm.value ?: return

        // 设置状态
        if (type == "draft") {
            currentLocalForm.staus = "1"
            localForm.postValue(currentLocalForm)
        }

        // 转换为提交模型
        val submitModel = convertToTravelPostRequest(currentLocalForm)

        // 保存草稿或提交
        if (type == "draft") {
            // 保存到本地
            // 实际项目中使用Gson将submitModel转为JSON字符串
            SPUtils.save("draft", Gson().toJson(submitModel))
            showToast.value = "草稿保存成功"
        }
    }

    /**
     * 日期范围数据类
     */
    data class DateRange(
        val year: Boolean,
        val month: Boolean,
        val day: Boolean,
        val hour: Boolean,
        val minute: Boolean
    )

    /**
     * 日期选择数据类
     */
    data class DateData(
        val year: String,
        val month: String,
        val day: String,
        val hour: String = "",
        val minute: String = ""
    )
}