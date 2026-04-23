package com.fx.zfcar.training

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.fx.zfcar.databinding.ActivityDriverBookBinding
import com.fx.zfcar.net.ApiConfig
import com.fx.zfcar.net.SignViewData
import com.fx.zfcar.net.SingPostRequest
import com.fx.zfcar.net.UploadFileData
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.training.viewmodel.NoticeViewModel
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.BitmapUtils
import com.fx.zfcar.util.DateUtil
import com.fx.zfcar.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class DriverBookActivity : AppCompatActivity() {
    // 视图绑定
    private lateinit var binding: ActivityDriverBookBinding

    // ViewModel
    private val signViewModel by viewModels<SafetyTrainingViewModel>()
    private val noticeViewModel by viewModels<NoticeViewModel>()

    // 页面参数
    private val currentYear = DateUtil.getCurrentYear()
    private var categoryId = ""
    private var areaCode = ""
    private var hasSign = false
    private var signImageUrl = ""
    private var signTime = ""

    // StateFlow
    private val driverBookSignStatusFlow = MutableStateFlow<ApiState<SignViewData>>(ApiState.Loading)
    private val uploadFlow = MutableStateFlow<ApiState<UploadFileData>>(ApiState.Loading)
    private val submitDriverBookFlow = MutableStateFlow<ApiState<String>>(ApiState.Loading)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化视图
        initView()

        // 监听 StateFlow
        observeStateFlows()

        // 检查签署状态
        checkDriverBookSignStatus()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    // 初始化视图
    private fun initView() {
        // 导航栏标题
        binding.tvTitle.text = "驾驶员责任书"

        // 返回按钮
        binding.ivBack.setOnClickListener { finish() }

        // 重写按钮
        binding.btnRedraw.setOnClickListener {
            binding.signatureView.clearSignature()
            hasSign = false
            binding.ivSignImage.visibility = View.GONE
            binding.signatureView.visibility = View.VISIBLE
            binding.layoutButtons.visibility = View.VISIBLE
        }

        // 保存按钮
        binding.btnSave.setOnClickListener {
            saveSignature()
        }

        // 初始化隐藏内容区域
        binding.layoutContent.visibility = View.GONE
    }

    // 监听 StateFlow
    private fun observeStateFlows() {
        // 监听责任书签署状态
        lifecycleScope.launch {
            driverBookSignStatusFlow.drop(1)
                .collect { state ->
                when (state) {
                    is ApiState.Loading -> {
                        binding.loadingView.visibility = View.VISIBLE
                    }
                    is ApiState.Success -> {
                        binding.loadingView.visibility = View.GONE
//                        val response = state.data
//
//                        // 处理500错误码
//                        if (response.code == 500) {
//                            hasSign = false
//                            signImageUrl = ""
//                            showBookContent()
//                            updateSignUI()
//                            return@collect
//                        }

                        state.data?.let {
                            // 更新状态
                            categoryId = state.data.category_id.toString()
                            areaCode = state.data.areacode
                            hasSign = state.data.signtype == "1"
                            signImageUrl = state.data.signfile

                            // 格式化签署时间
                            signTime = DateUtil.timestamp2String(state.data.signtime)

                            // 检查是否需要重新签署（涡阳县341621无需每年签）
                            val signYear = signTime.take(4).toIntOrNull()
                            if (hasSign && signYear != null && signYear < currentYear && state.data.areacode != "341621") {
                                hasSign = false
                                signImageUrl = ""
                            }

                            // 显示责任书内容
                            showBookContent()

                            // 更新签署UI状态
                            updateSignUI()
                        } ?: run {
                            hasSign = false
                            signImageUrl = ""
                            showBookContent()
                            updateSignUI()
                        }
                    }
                    is ApiState.Error -> {
                        binding.loadingView.visibility = View.GONE
                        showToast(state.msg)
                    }
                    else -> {}
                }
            }
        }

        // 监听图片上传（复用原有逻辑）
        lifecycleScope.launch {
            uploadFlow.drop(1)
                .collect { state ->
                when (state) {
                    is ApiState.Loading -> {
                        showToast("正在上传图片...")
                    }
                    is ApiState.Success -> {
                        state.data?.let {
                            val uploadData = state.data
                            // 上传成功，提交责任书签署
                            submitDriverBook(ApiConfig.BASE_URL_TRAINING + uploadData.url)
                        }
                    }
                    is ApiState.Error -> {
                        showToast("图片上传失败：${state.msg}")
                    }
                    else -> {}
                }
            }
        }

        // 监听责任书提交
        lifecycleScope.launch {
            submitDriverBookFlow.drop(1).collect { state ->
                when (state) {
                    is ApiState.Loading -> {
                        showToast("正在提交签署信息...")
                    }
                    is ApiState.Success -> {
                        showToast("提交成功")
                        checkDriverBookSignStatus()
                    }
                    is ApiState.Error -> {
                        showToast("提交失败：${state.msg}")
                    }
                    else -> {}
                }
            }
        }
    }

    // 检查责任书签署状态
    private fun checkDriverBookSignStatus() {
        signViewModel.signView(driverBookSignStatusFlow)
    }

    // 根据category_id和areacode显示不同责任书内容
    private fun showBookContent() {
        binding.layoutContent.visibility = View.VISIBLE
        val contentBuilder = StringBuilder()

        if (categoryId != "3031") {
            // 非3031 - 安全生产责任状/年度目标责任书
            if (areaCode.take(4) == "3412" || categoryId == "13548") {
                // 安徽阜阳/利辛华鑫 - 安全生产责任状
                contentBuilder.apply {
                    append("<h1 style='text-align:center;font-weight:bold;font-size:18sp;margin-bottom:16dp;'>安全生产责任状</h1>")
                    append("<p style='font-size:16sp;line-height:1.6;'>为了贯彻落实安全生产法和安全管理条例的规定,坚持“安全第一，预防为主”的方针，促进安全生产、增进效益、共建和谐社会，为了明确落实安全主体责任，甲、乙双方特定安全生产责任状，内容如下：</p>")
                    append("<p style='font-size:15sp;line-height:1.6;'>一、甲方负责为乙方提供安全生产服务和安全生产指导以及办理各项业务服务。</p>")
                    append("<p style='font-size:15sp;line-height:1.6;'>二、甲方负责对上级政府和业务主管部门的有关安全生产文件和会议精神的传达、宣传、落实。</p>")
                    append("<p style='font-size:15sp;line-height:1.6;'>三、乙方职责：乙方车辆挂靠甲方公司名下营运必须按照甲方的规定办理以下业务：</p>")
                    append("<p style='font-size:16sp;line-height:1.6;'>1.乙方车辆从挂靠甲方那一天起乙方车辆就须按时投保交强险和商业险和危运无忧险。<br>")
                    append("2.乙方车辆要正常购买国家的各项税费。<br>")
                    append("3.乙方车辆要做到按时参加年审和二级维护。<br>")
                    append("4.乙方车辆在营运过程中要按照《中华人民共和国道路交通安全法》的规定办理，不要客货混装、不要超载、不要超速行驶、不要乱停乱放，不要疲劳驾驶等。<br>")
                    append("5.乙方车辆在营运过程中要严格落实北斗监控的规章制度，不得私自破坏、更改北斗的正常使用，严格实行北斗24小时在线。聘请的驾驶员要积极参与每月的安全教育培训及车辆隐患排查治理。</p>")
                    append("<p style='font-size:15sp;line-height:1.6;'>四、乙方聘用驾驶人员技术要过硬同时要求驾驶员的驾驶证、从业资格证两证齐全，还必须做到一年一度的年审和体检。</p>")
                    append("<p style='font-size:15sp;line-height:1.6;'>五、乙方车辆在营运过程中如发生交通事故要及时向当地公安部门报案并向车辆保险公司报案同时向甲方公司报案,不准瞒报和漏报。</p>")
                    append("<p style='font-size:15sp;line-height:1.6;'>六、本责任书有效期一年。自签字日期生效。</p>")
                    if (hasSign) {
                        append("<p style='font-size:15sp;font-weight:bold;margin:10dp 0;'>签字日期：$signTime</p>")
                    }
                    append("<p style='font-size:15sp;font-weight:bold;'>驾驶员签字：</p>")
                }
            } else {
                // 年度安全生产目标责任书
                // 确定省份名称
                val province = when (areaCode.take(2)) {
                    "36" -> "江西"
                    "34" -> "安徽"
                    "53" -> "云南"
                    else -> "山东"
                }

                contentBuilder.apply {
                    append("<h1 style='text-align:center;font-weight:bold;font-size:18sp;margin-bottom:16dp;'>${currentYear}年驾驶员安全生产目标责任书</h1>")
                    append("<p style='font-size:16sp;line-height:1.6;'>为顺利完成公司年度工作目标，认真贯彻执行《安全生产法》《${province}省安全生产条例》以及公司有关安全生产的要求和制度，保证生产过程中的生命和财产的安全，同时将安全生产责任制落实到个人，调度员与驾驶员签订${currentYear}年安全生产责任书。</p>")
                    append("<p style='font-size:15sp;line-height:1.6;'>一、安全生产职责：</p>")
                    append("<p style='font-size:16sp;line-height:1.6;'>1、严格遵守安全生产规章制度和操作规程，服从管理；<br>")
                    append("2、积极参加安全学习及安全培训,掌握本职工作所需的安全生产知识，提高安全生产技能；<br>")
                    append("3、认真开展安全隐患排查，确保本岗位作业区域内相关设备、用电、环境等保持安全状况；<br>")
                    append("4、发生生产安全事故后，事故现场有关人员应当立即报告本单位负责人；发现事故隐患或者其他不安全因素，应当立即向现场安全管理人员或者本单位负责人报告；<br>")
                    append("5、有权对单位安全生产工作中存在的问题提出批评、检举、控告，有权拒绝违章指挥和强令冒险作业；<br>")
                    append("6、熟悉本岗位的安全生产风险和应急处置措施，发现直接危及人身安全的紧急情况时,有权停止作业或者在采取可能的应急措施后,撤离作业现场；<br>")
                    append("7、正确佩戴和使用劳动防护用品；<br>")
                    append("8、熟练掌握应急逃生知识，提高互救自救能力；<br>")
                    append("9、法律、法规、规章以及本单位规定的其他安全生产职责。</p>")
                    append("<p style='font-size:15sp;line-height:1.6;'>二、目标</p>")
                    append("<p style='font-size:16sp;line-height:1.6;'>1、驾驶作业事故发生率为零；<br>")
                    append("2、各种安全教育和培训的到岗率 100%；<br>")
                    append("3、安全管理制度和操作规程掌握率 100%；<br>")
                    append("4、本岗位设备设施、安全消防器材的维护保养合格率 100%；</p>")
                    append("<p style='font-size:15sp;line-height:1.6;'>三、奖惩办法</p>")
                    append("<p style='font-size:16sp;line-height:1.6;'>全面完成年度安全生产目标：全年未发生安全事故；无重大安全隐患，一般隐患整改及时；安全生产责任考核结果80分以上，视情况奖励100-500元。年度安全目标未完成，公司视情况进行处罚，取消其评比先进的资格。</p>")
                    append("<p style='font-size:15sp;line-height:1.6;'>四、附注</p>")
                    append("<p style='font-size:16sp;line-height:1.6;'>1、本责任书期限为${currentYear}年1月1日至${currentYear}年12月31日。<br>")
                    append("2、本责任书一式两份，调度员、驾驶员各执一份，具有同等效力。<br></p>")
                    if (hasSign) {
                        append("<p style='font-size:15sp;font-weight:bold;margin:10dp 0;'>签字日期：$signTime</p>")
                    }
                    append("<p style='font-size:15sp;font-weight:bold;'>驾驶员签字：</p>")
                }
            }
        } else {
            // 3031 - 道路货物运输安全生产责任书
            contentBuilder.apply {
                append("<h1 style='text-align:center;font-weight:bold;font-size:18sp;margin-bottom:16dp;'>道路货物运输安全生产责任书</h1>")
                append("<p style='font-size:16sp;line-height:1.6;'>为认真贯彻“安全第一、预防为主、综合整治、全员参与、持续改进”的安全生产方针，积极落实公司的安全生产制度，遏制较大事故的发生，实行“一岗双责”，抓好道路运输安全生产，按照有关法律法规签订本责任书：</p>")
                append("<p style='font-size:15sp;line-height:1.6;'>甲方：公司安全员</p>")
                append("<p style='font-size:15sp;line-height:1.6;'>甲方安全职责：</p>")
                append("<p style='font-size:16sp;line-height:1.6;'>1、协助制定、执行企业安全生产管理规章制度、操作规程、应急预案、安全生产工作计划、安全措施等，监督检查执行情况，提出改进建议;<br>")
                append("2、组织安全学习、从业人员安全教育培训、应急演练等安全生产活动;新聘从业人员的岗前教育培训、考核;<br>")
                append("3、按规定组织召开安全生产例会、专题会;<br>")
                append("4、做好安全检查和隐患排查及督促整改;<br>")
                append("5、车辆和安全设施设备、劳动防护用品等管理、发放、使用和保养，以及单位相关证照和保险办理等;<br>")
                append("6、事故现场组织施救，协助事故调查、处理、负责事故原因分析与保险理赔;<br>")
                append("7、实施车辆动态监控;<br>")
                append("8、建立完善各种安全生产基础资料档案。<br></p>")
                append("<p style='font-size:15sp;line-height:1.6;'>乙方：公司驾驶员</p>")
                append("<p style='font-size:15sp;line-height:1.6;'>乙方安全职责：</p>")
                append("<p style='font-size:16sp;line-height:1.6;'>1、严格遵守交通法律法规和公司的各项安全管理制度，服从公司安全员的管理；<br>")
                append("2、按时参加公司组织的安全学习和教育培训，不断提高安全理论水平和驾驶技能；<br>")
                append("3、文明驾车、礼貌驾驶，不开“英雄车、赌气车”，严禁酒后开车、超速行驶、违章超车、疲劳驾驶和驾驶中打手机等违法行为。<br>")
                append("4、严格按照《安全生产监督检查制度》维护车辆，严格按照《驾驶员安全生产操作规程》驾驶车辆；爱护车辆，保持车容车貌整洁，在出车前、运行中、收车后对车辆进行一次安全检查，保持车辆性能良好；<br>")
                append("5、车辆必须按公司运输作业计划的要求装载、运输货物，严禁超范围经营、超限、超载，否则一经发现，视情况给与相应处罚。<br>")
                append("6、严禁车辆在厂区和货场内乱停乱放，临时停车要停放整齐，不得妨碍车辆和人员通行。</p>")
                append("<p style='font-size:15sp;line-height:1.6;'>二、目标</p>")
                append("<p style='font-size:16sp;line-height:1.6;'>7、车辆进入公司停车场，限速5km/h，按停车场管理人员制定位置停放，严禁随意停车，乱停乱放，违者给与50元罚款。<br>")
                append("8、如发生事故，应立即向公司及当地公安部门、安全生产部门、环境保护部门、质检部门报告并看护好车辆、货物，采取一切可能的警示、救援措施。<br>")
                append("本责任书一式两份，公司与驾驶员各持一份。<br>")
                append("本责任书有效期自${currentYear}年1月1日起至${currentYear}年12月31 日止。<br>")
                append("（如车辆更换驾驶员须及时重新签订，否则不准驾车营运。）</p>")
                if (hasSign) {
                    append("<p style='font-size:15sp;font-weight:bold;margin:10dp 0;'>签字日期：$signTime</p>")
                }
                append("<p style='font-size:15sp;font-weight:bold;'>责任人签字：</p>")
            }
        }

        // 设置富文本内容
        binding.tvContent.text = android.text.Html.fromHtml(
            contentBuilder.toString(),
            android.text.Html.FROM_HTML_MODE_COMPACT
        )
    }

    // 更新签署UI状态
    private fun updateSignUI() {
        if (hasSign) {
            // 已签署 - 显示图片
            binding.signatureView.visibility = View.GONE
            binding.layoutButtons.visibility = View.GONE
            binding.ivSignImage.visibility = View.VISIBLE
            Glide.with(this)
                .load(signImageUrl)
                .into(binding.ivSignImage)
        } else {
            // 未签署 - 显示画布
            binding.signatureView.visibility = View.VISIBLE
            binding.layoutButtons.visibility = View.VISIBLE
            binding.ivSignImage.visibility = View.GONE
        }
    }

    // 保存签名并上传
    private fun saveSignature() {
        if (!binding.signatureView.hasSignature()) {
            showToast("请先签名再保存")
            return
        }

        val signatureBitmap = binding.signatureView.getSignatureBitmap()
        val file = BitmapUtils.saveBitmapToFile(this@DriverBookActivity, signatureBitmap)
        file?.let { uploadSignatureFile(it) }
    }

    private fun uploadSignatureFile(file: File) {
        noticeViewModel.uploadFile(
            MultipartBody.Part.createFormData("file", file.name,
                file.asRequestBody("image/png".toMediaTypeOrNull())),
            uploadFlow)
    }

    // 提交责任书签署
    private fun submitDriverBook(fileUrl: String) {
        signViewModel.singPost(SingPostRequest(fileUrl), submitDriverBookFlow)
    }
}
