package com.fx.zfcar.training.safetycheck

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fx.zfcar.databinding.ActivityCarSafetyCheckBinding
import com.fx.zfcar.net.CarCheckData
import com.fx.zfcar.net.CarCheckDetail
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.DateUtil
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.util.SPUtils
import com.fx.zfcar.viewmodel.ApiState
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

class CarSafetyCheckActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCarSafetyCheckBinding
    private val viewModel by viewModels<SafetyTrainingViewModel>()
    private val carSafetyCheckFlow = MutableStateFlow<ApiState<CarCheckData>>(ApiState.Loading)
    private val gson = Gson()
    private var carCheckDetail: CarCheckDetail? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarSafetyCheckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置返回按钮点击事件
        PressEffectUtils.setCommonPressEffect(binding.ivBack)
        PressEffectUtils.setCommonPressEffect(binding.cardLastRecord)
        PressEffectUtils.setCommonPressEffect(binding.cardAddNew)
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 上次记录卡片点击事件
        binding.cardLastRecord.setOnClickListener {
            goToLastRecordDetail(carCheckDetail)
        }

        // 监听最新记录数据
        lifecycleScope.launch {
            carSafetyCheckFlow.drop(1)
                .collect { state ->
                    when (state) {
                        is ApiState.Success -> {
                            state.data?.let {
                                // 显示上次记录区域
                                binding.layoutLastRecord.visibility = View.VISIBLE
                                // 设置日期和车牌号
                                binding.tvLastRecordDate.text = DateUtil.timestamp2Date(state.data.rows.updatetime * 1000L)
                                binding.tvLastRecordCarnum.text = state.data.rows.carnum
                                carCheckDetail = state.data.rows
                            }

                        }
                        is ApiState.Error -> {
                            binding.layoutLastRecord.visibility = View.GONE
                        }
                        else -> {}
                    }
                }
            }

        // 新增检查卡片点击事件
        binding.cardAddNew.setOnClickListener {
            showConfirmDialog()
        }

        // 加载检查记录
        viewModel.getCarCheck(carSafetyCheckFlow)
    }

    // 显示确认弹窗
    private fun showConfirmDialog() {
        val dialog = AlertDialog.Builder(this)
            .setMessage("本检查需一次性完成，中途退出需重新检查，是否开启检查。")
            .setPositiveButton("确认") { dialog, _ ->
                goToCheckStage()
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    // 跳转到上次记录详情页
    private fun goToLastRecordDetail(record: CarCheckDetail?) {
        // 保存记录到SP
        SPUtils.save("carCheckRecord", gson.toJson(record))

        // 跳转页面
        val intent = Intent(this, LastRecordActivity::class.java)
        startActivity(intent)
    }

    // 跳转到检查流程页面
    private fun goToCheckStage() {
        val intent = Intent(this, CarCheckStageActivity::class.java)
        startActivity(intent)
    }
}