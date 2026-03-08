package com.fx.zfcar.training.drivelog

import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import com.fx.zfcar.training.adapter.DriveLogDraftAdapter
import com.fx.zfcar.training.viewmodel.TravelViewModel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fx.zfcar.databinding.ActivityDriveLogBinding
import com.fx.zfcar.net.DriveLogUiState
import com.fx.zfcar.net.TravelLogData
import com.fx.zfcar.net.TravelLogItem
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.util.SPUtils
import com.fx.zfcar.viewmodel.ApiState
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 货车行车日志页面（集中管理所有StateFlow）
 */
class DriveLogActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDriveLogBinding
    private val viewModel by viewModels<TravelViewModel>()
    private lateinit var draftAdapter: DriveLogDraftAdapter

    // SharedPreferences存储KEY
    private val KEY_DRAFT = "draft"
    private val KEY_LAST_RECORD = "lastRecord"

    // UI状态
    private val _uiState = MutableStateFlow(DriveLogUiState(lastRecord = TravelLogItem()))
    val uiState: StateFlow<DriveLogUiState> = _uiState.asStateFlow()

    // 弹窗状态
    private val _draftFullDialog = MutableStateFlow(false)
    val draftFullDialog: StateFlow<Boolean> = _draftFullDialog.asStateFlow()

    private val _deleteConfirmDialog = MutableStateFlow(false)
    val deleteConfirmDialog: StateFlow<Boolean> = _deleteConfirmDialog.asStateFlow()

    // 网络请求状态
    private val travelLogState = MutableStateFlow<ApiState<TravelLogData>>(ApiState.Idle)

    private val travelDelState = MutableStateFlow<ApiState<Int>>(ApiState.Idle)

    // 当前要删除的草稿
    private val _currentDeleteDraft = MutableStateFlow<TravelLogItem?>(null)
    val currentDeleteDraft: StateFlow<TravelLogItem?> = _currentDeleteDraft.asStateFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriveLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initData()
        observeAllStates()
    }

    /**
     * 初始化视图
     */
    private fun initView() {
        PressEffectUtils.setCommonPressEffect(binding.layoutTitle.tvTitle)
        binding.layoutTitle.tvTitle.text = "货车行车日志"
        binding.layoutTitle.tvTitle.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 初始化草稿箱列表适配器
        draftAdapter = DriveLogDraftAdapter(
            onItemClick = ::onDraftItemClick,
            onDeleteClick = ::onDraftDeleteClick
        )

        // 设置RecyclerView
        binding.rvDrafts.apply {
            layoutManager = LinearLayoutManager(this@DriveLogActivity)
            adapter = draftAdapter
            setHasFixedSize(true)
        }

        PressEffectUtils.setCommonPressEffect(binding.layoutLastRecord)
        // 上一次记录点击事件
        binding.layoutLastRecord.setOnClickListener {
            val lastRecord = _uiState.value.lastRecord
            if (lastRecord.updatetime.isNotEmpty()) {
                goToLastRecordDetail(lastRecord)
            }
        }

        PressEffectUtils.setCommonPressEffect(binding.layoutAddNew)
        // 新增日志点击事件
        binding.layoutAddNew.setOnClickListener {
            if (canAddNewLog(_uiState.value.drafts)) {
                goToDriveStage()
            } else {
                _draftFullDialog.value = true
            }
        }

        PressEffectUtils.setCommonPressEffect(binding.dialogDraftFull.btnConfirm)
        PressEffectUtils.setCommonPressEffect(binding.dialogDraftFull.btnCancel)
        // 弹窗按钮事件
        binding.dialogDraftFull.btnConfirm.setOnClickListener {
            _draftFullDialog.value = false
        }
        binding.dialogDraftFull.btnCancel.setOnClickListener {
            _draftFullDialog.value = false
        }

        PressEffectUtils.setCommonPressEffect(binding.dialogDeleteConfirm.btnConfirm)
        PressEffectUtils.setCommonPressEffect(binding.dialogDeleteConfirm.btnCancel)
        binding.dialogDeleteConfirm.btnConfirm.setOnClickListener {
            _currentDeleteDraft.value?.let { draft ->
                deleteDraft(draft)
            }
        }
        binding.dialogDeleteConfirm.btnCancel.setOnClickListener {
            dismissAllDialogs()
        }
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        SPUtils.remove(KEY_DRAFT)
        // 加载行车日志数据
        loadTravelLogData()
    }

    /**
     * 加载行车日志数据
     */
    private fun loadTravelLogData() {
        // 更新加载状态
        _uiState.update { it.copy(isLoading = true) }

        viewModel.getTravelLog(travelLogState)
    }

    /**
     * 处理行车日志加载成功
     */
    private fun handleTravelLogSuccess(data: TravelLogData) {
        // 格式化时间
        val formattedDrafts = mutableListOf<TravelLogItem>()
        data.list?.let {
            data.list.map { item ->
//            item.copy(updatetime = item.updatetime)
            }
        }

        // 解析最后一条记录
        val lastRecord = data.rows

        // 更新UI状态
        _uiState.update {
            it.copy(
                isLoading = false,
                lastRecord = lastRecord,
                drafts = formattedDrafts,
                errorMsg = ""
            )
        }

        // 更新列表
        draftAdapter.submitList(formattedDrafts)
    }

    /**
     * 删除草稿
     */
    private fun deleteDraft(draftItem: TravelLogItem) {
        // 更新状态
        dismissAllDialogs()

        viewModel.travelDel(draftItem.id.toString(), travelDelState)
    }

    /**
     * 监听所有状态流
     */
    private fun observeAllStates() {
        lifecycleScope.launch {
            travelLogState.drop(1).collect { state ->
                when(state) {
                    is ApiState.Success -> {
                        state.data?.let {
                            handleTravelLogSuccess(state.data)
                        }
                    }
                    is ApiState.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMsg = state.msg
                            )
                        }
                    }
                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            travelDelState.drop(1).collect { state ->
                when(state) {
                    is ApiState.Success -> {
                        // 删除成功，更新本地列表
                        val currentDrafts = _uiState.value.drafts.toMutableList()
                        currentDrafts.removeIf { it.id == state.data }
                        _uiState.update {
                            it.copy(
                                drafts = currentDrafts,
                                errorMsg = ""
                            )
                        }
                        showToast("删除成功")
                    }
                    is ApiState.Error -> {
                        _uiState.update {
                            it.copy(errorMsg = state.msg)
                        }
                        showToast("删除失败：${state.msg}")
                    }
                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            // 监听UI状态
            uiState.drop(1)
                .collect { state ->

                // 更新上一次记录UI
                updateLastRecordUI(state.lastRecord)

                // 显示/隐藏草稿箱容器
                val visibility = if (state.drafts.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                binding.draftsTitle.visibility = visibility
                binding.rvDrafts.visibility = visibility

                // 显示错误信息
                if (state.errorMsg.isNotEmpty()) {
                    showToast(state.errorMsg)
                }
            }
        }

        lifecycleScope.launch {
            // 监听草稿箱满弹窗
            draftFullDialog.drop(1)
                .collect { show ->
                binding.dialogDraftFull.root.visibility = if (show) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }

        lifecycleScope.launch {
            // 监听删除确认弹窗
            deleteConfirmDialog.drop(1)
                .collect { show ->
                if (show) {
                    _currentDeleteDraft.value?.let { draft ->
                        binding.dialogDeleteConfirm.tvContent.text =
                            "确定要删除${draft.updatetime}该草稿吗？"
                    }
                }
                binding.dialogDeleteConfirm.root.visibility = if (show) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }

    }

    /**
     * 更新上一次记录UI
     */
    private fun updateLastRecordUI(lastRecord: TravelLogItem) {
        if (lastRecord.updatetime.isNotEmpty()) {
            binding.tvLastTime.text = lastRecord.updatetime
            binding.tvLastCarNum.text = lastRecord.carnum
            binding.layoutLastRecord.visibility = View.VISIBLE
        } else {
            binding.layoutLastRecord.visibility = View.GONE
        }
    }

    /**
     * 草稿箱项点击事件
     */
    private fun onDraftItemClick(draft: TravelLogItem) {
        SPUtils.save(KEY_DRAFT, Gson().toJson(draft)) // 实际项目建议用Gson转JSON

        // 跳转到编辑页面
//        startActivity(Intent(this, DriveStageActivity::class.java))
    }

    /**
     * 草稿箱删除点击事件
     */
    private fun onDraftDeleteClick(draft: TravelLogItem) {
        _currentDeleteDraft.value = draft
        _deleteConfirmDialog.value = true
    }

    /**
     * 跳转到上一次记录详情
     */
    private fun goToLastRecordDetail(lastRecord: TravelLogItem) {
        SPUtils.save(KEY_LAST_RECORD, Gson().toJson(lastRecord))
        startActivity(Intent(this, LastDriveLogRecordActivity::class.java))
    }

    /**
     * 跳转到新增日志页面
     */
    private fun goToDriveStage() {
//        startActivity(Intent(this, DriveStageActivity::class.java))
    }

    /**
     * 关闭所有弹窗
     */
    private fun dismissAllDialogs() {
        _draftFullDialog.value = false
        _deleteConfirmDialog.value = false
        _currentDeleteDraft.value = null
    }

    /**
     * 检查是否可以新增日志（草稿箱<3条）
     */
    fun canAddNewLog(drafts: List<TravelLogItem>): Boolean {
        return drafts.size < 3
    }

    /**
     * 解析最后一条记录
     */
    fun parseLastRecord(rowsStr: String?, drafts: List<TravelLogItem>): TravelLogItem {
        return if (!rowsStr.isNullOrEmpty()) {
            // 根据实际rows字段格式解析，这里先用列表第一条示例
            drafts.firstOrNull() ?: TravelLogItem()
        } else {
            TravelLogItem()
        }
    }

}