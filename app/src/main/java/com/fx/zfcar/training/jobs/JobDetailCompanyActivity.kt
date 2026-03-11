package com.fx.zfcar.training.jobs

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fx.zfcar.databinding.ActivityJobDetailCompanyBinding
import com.fx.zfcar.net.CompanyListRow
import com.fx.zfcar.net.JobViewData
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.JsonUtils
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.util.ProgressDialogUtils
import com.fx.zfcar.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlin.getValue

/**
 * 职位详情页（完整实现UniApp逻辑）
 */
class JobDetailCompanyActivity : AppCompatActivity() {

    // ViewBinding实例
    private lateinit var binding: ActivityJobDetailCompanyBinding

    // 职位数据模型
    private lateinit var jobDetailModel: CompanyListRow

    private val infoViewModel by viewModels<SafetyTrainingViewModel>()
    private val jobDetailStateFlow = MutableStateFlow<ApiState<JobViewData>>(ApiState.Loading)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJobDetailCompanyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        initData()
        setBackClickListener()
        observeStateFlows()
        loadJobDetail()
    }

    private fun initData() {
        val jsonData = intent.getStringExtra("data")
        if (jsonData.isNullOrEmpty()) {
            showToast("未获取到职位数据")
            finish()
            return
        }

        jobDetailModel = JsonUtils.fromJson(jsonData)
    }

    private fun observeStateFlows() {
        lifecycleScope.launch {
            jobDetailStateFlow.drop(1)
                .collect { uiState ->
                    when (uiState) {
                        is ApiState.Loading -> {
                            ProgressDialogUtils.show(this@JobDetailCompanyActivity)
                        }
                        is ApiState.Success -> {
                            ProgressDialogUtils.dismiss()
                            uiState.data?.let {
                                // 更新UI
                                updateUI(uiState.data)
                            }
                        }
                        is ApiState.Error -> {
                            ProgressDialogUtils.dismiss()
                            showToast("请求失败：${uiState.msg}")
                        }
                        else -> {}
                    }
                }
        }
    }

    private fun loadJobDetail() {
        infoViewModel.getJobView(jobDetailModel.id, jobDetailStateFlow)
    }

    /**
     * 更新UI数据
     */
    private fun updateUI(data: JobViewData) {
        binding.tvTitle.text = data.title
        binding.tvContent.text = data.content
        binding.tvCompany.text = data.company

        binding.tvStarttime.text = data.starttime
        binding.tvEndtime.text = data.endtime
    }

    private fun setBackClickListener() {
        PressEffectUtils.setCommonPressEffect(binding.ivBack)
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ProgressDialogUtils.dismiss()
    }

    /**
     * 启动页面的静态方法
     */
    companion object {
        fun start(context: android.content.Context, jobModel: CompanyListRow) {
            val intent = Intent(context, JobDetailCompanyActivity::class.java)
            intent.putExtra("data", JsonUtils.toJson(jobModel))
            context.startActivity(intent)
        }

        // 重载：直接传递JSON字符串
        fun start(context: android.content.Context, jsonData: String) {
            val intent = Intent(context, JobDetailCompanyActivity::class.java)
            intent.putExtra("data", jsonData)
            context.startActivity(intent)
        }
    }
}