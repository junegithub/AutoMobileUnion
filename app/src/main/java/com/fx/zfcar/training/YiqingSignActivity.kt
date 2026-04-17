package com.fx.zfcar.training

import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.fx.zfcar.databinding.ActivityDriverSignBinding
import com.fx.zfcar.net.EpidemicViewData
import com.fx.zfcar.net.UploadFileData
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.training.viewmodel.NoticeViewModel
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.BitmapUtils
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.util.ProgressDialogUtils
import com.fx.zfcar.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.*

class YiqingSignActivity : AppCompatActivity() {
    // 视图绑定
    private lateinit var binding: ActivityDriverSignBinding

    // ViewModel
    private val trainingViewModel by viewModels<SafetyTrainingViewModel>()
    private val noticeViewModel by viewModels<NoticeViewModel>()

    // 页面参数
    private var companyName = ""
    private var categoryId = ""
    private var areaCode = ""
    private var hasSign = false
    private var signImageUrl = ""
    private var signTime = ""

    // StateFlow
    private val signStatusFlow = MutableStateFlow<ApiState<EpidemicViewData>>(ApiState.Loading)
    private var uploadStateFlow = MutableStateFlow<ApiState<UploadFileData>>(ApiState.Idle)
    private val submitFlow = MutableStateFlow<ApiState<Any>>(ApiState.Loading)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverSignBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取传递的公司名称
        companyName = intent.getStringExtra("name") ?: ""

        // 初始化视图
        initView()

        // 监听 StateFlow
        observeStateFlows()

        // 检查签署状态
        checkSignStatus()
    }

    // 初始化视图
    private fun initView() {
        binding.titleLayout.tvTitle.text = "驾驶员承诺书"
        PressEffectUtils.setCommonPressEffect(binding.titleLayout.tvTitle)

        // 返回按钮
        binding.titleLayout.tvTitle.setOnClickListener { finish() }

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

        // 初始化承诺书内容（默认隐藏，等待接口返回）
        binding.layoutContent.visibility = View.GONE
    }

    // 监听 StateFlow
    private fun observeStateFlows() {
        // 监听签署状态
        lifecycleScope.launch {
            signStatusFlow.drop(1)
                .collect { state ->
                when (state) {
                    is ApiState.Loading -> {
                        binding.loadingView.visibility = View.VISIBLE
                    }
                    is ApiState.Success -> {
                        binding.loadingView.visibility = View.GONE
                        state.data?.let {
                            val data = state.data

                            // 更新状态
                            categoryId = data.category_id.toString()
                            areaCode = data.areacode.substring(0, 4)
                            hasSign = data.epidemictype == "1"
                            signImageUrl = data.epidemicfile
                            signTime = data.epidemictime

                            // 检查是否需要重新签署
                            val nowYear = Calendar.getInstance().get(Calendar.YEAR).toString()
                            val signYear = signTime.substring(0, 4)
                            if (hasSign && signYear < nowYear && data.areacode != "341621") {
                                hasSign = false
                                signImageUrl = ""
                            }

                            // 显示承诺书内容
                            showSignContent()

                            // 显示签署状态
                            if (hasSign) {
                                // 已签署 - 显示图片
                                binding.signatureView.visibility = View.GONE
                                binding.layoutButtons.visibility = View.GONE
                                binding.ivSignImage.visibility = View.VISIBLE
                                Glide.with(this@YiqingSignActivity)
                                    .load(signImageUrl)
                                    .into(binding.ivSignImage)

                                // 显示签署时间
                                showSignTime()
                            } else {
                                // 未签署 - 显示画布
                                binding.signatureView.visibility = View.VISIBLE
                                binding.layoutButtons.visibility = View.VISIBLE
                                binding.ivSignImage.visibility = View.GONE
                            }
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

        // 监听图片上传
        lifecycleScope.launch {
            uploadStateFlow.drop(1)
                .collect { state ->
                when (state) {
                    is ApiState.Loading -> {
                        ProgressDialogUtils.show(this@YiqingSignActivity, "上传中...")
                    }
                    is ApiState.Success -> {
                        ProgressDialogUtils.dismiss()
                        state.data?.let {
                            val uploadData = state.data
                            // 上传成功，提交签署信息
                            submitSign(uploadData.url)
                        }
                    }
                    is ApiState.Error -> {
                        ProgressDialogUtils.dismiss()
                        showToast("图片上传失败：${state.msg}")
                    }
                    else -> {}
                }
            }
        }

        // 监听提交签署
        lifecycleScope.launch {
            submitFlow.drop(1)
                .collect { state ->
                when (state) {
                    is ApiState.Loading -> {
                        showToast("正在提交签署信息...")
                    }
                    is ApiState.Success -> {
                        showToast("提交成功")
                        // 返回培训首页
                        finish()
                    }
                    is ApiState.Error -> {
                        showToast("提交失败：${state.msg}")
                    }
                    else -> {}
                }
            }
        }
    }

    // 检查签署状态
    private fun checkSignStatus() {
        trainingViewModel.epidemicView(signStatusFlow)
    }

    // 根据 categoryId 显示不同的承诺书内容
    private fun showSignContent() {
        binding.layoutContent.visibility = View.VISIBLE
        val contentBuilder = StringBuilder()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        when (categoryId) {
            "3031" -> {
                // 安全生产承诺书
                contentBuilder.apply {
                    append("<h1 style='text-align:center;font-weight:bold;font-size:18sp;margin-bottom:16dp;'>承诺书</h1>")
                    append("<p style='font-size:16sp;line-height:1.6;'>根据《安全生产法》、《山东省安全生产条例》，交通法规和公司安全生产管理制度，为全面履行安全生产责制任，做好驾驶员的本职工作，特向公司安全部门郑重承诺如下:</p>")
                    append("<p style='font-size:16sp;line-height:1.6;'>一、认真遵守法律法规和公司的各项管理制度，严格执行道路货物运输、装卸标准和管理办法，持证上岗。年固树立“安全第一，预防为主”的思想，对运输安全负责。确保行车安全无事故。</p>")
                    append("<p style='font-size:16sp;line-height:1.6;'>二、履行岗位职责，严格执行《驾驶员操作规程》，努力学习业务，不断提高理论水平和驾驶职能，积极参加公司组织的各种培训教育活动、安全生产例会、专题会，掌握道路货物运输基本特点，注意事项和应急处理办法。 </p>")
                    append("<p style='font-size:16sp;line-height:1.6;'>三、禁止乱开乱动一切机动车，未经批准不准将车交给无上岗证资格的人员驾驶，不搭载无关人员。认真遵守驾驶员《安全行车十条禁令)，不得超载、超速行驶、不得酒后驾驶、不得疲劳驾驶。</p>")
                    append("<p style='font-size:16sp;line-height:1.6;'>四、做到出车前、行驶中、收车后的“三检”工作，不带病行车，保持车辆技术状况良好。</p>")
                    append("<p style='font-size:16sp;line-height:1.6;'>五、做到“三懂、四会”，三懂即;懂车辆技术性能。懂事故处理方法、懂货运基本知识。四会:会使用消防器材会工艺操作、会处理事故隐患、会维护保养。</p>")
                    append("<p style='font-size:16sp;line-height:1.6;'>本承诺书有效期自${currentYear}年1月1日起至${currentYear}年12月31日止。</p>")
                    if (hasSign) {
                        append("<p style='font-size:15sp;font-weight:bold;margin:10dp 0;'>签字日期：$signTime</p>")
                    }
                    append("<p style='font-size:15sp;font-weight:bold;'>承诺人签字：</p>")
                }
            }
            "6109", "5951", "6110", "5914" -> {
                // 疫情防控承诺书
                contentBuilder.apply {
                    append("<h1 style='text-align:center;font-weight:bold;font-size:18sp;margin-bottom:16dp;'>货车司机疫情防控承诺书</h1>")
                    append("<p style='font-size:16sp;line-height:1.6;text-indent:2em;'>根据当前疫情防控形势，为坚决有效阻断疫情传播，进一步加强货车司机疫情防控，本人郑重承诺：</p>")
                    append("<p style='font-size:15sp;line-height:1.6;text-indent:2em;'>一、落实提前报备</p>")
                    append("<p style='font-size:16sp;line-height:1.6;text-indent:2em;'>货车司机及同乘人员由常态化防控区域返嘉，提前至少2天向单位、社区（村）报备，同时持有48小时核酸检测阴性证明及国务院行程卡、山东省健康码（绿码）。</p>")
                    append("<p style='font-size:16sp;line-height:1.6;text-indent:2em;'>货车司机及同乘人员由高中低风险区所在城市返，在抵前至少提前2天向单位、社区（村）报备。抵后配合社区（村）按照分级分类管理，尽快落实核酸检测、健康监测、隔离管控等各项防控措施。</p>")
                    append("<p style='font-size:15sp;line-height:1.6;text-indent:2em;'>二、落实货车司机个人防控责任</p>")
                    append("<p style='font-size:16sp;line-height:1.6;text-indent:2em;'>货车司机是疫情防控的第一责任人，必须履行疫情防控的责任和义务：(1）每周进行2次核酸检测，在外省市的要把核酸检测阴性证明截图发所属企业；(2）按规定完成疫苗接种；(3）出返本县（市、区）必须落实提前报备；(4)落实车辆消杀、人员防护等各项措施。<font color='red'>(5)路上做好自我防护，到目的地尽量不要下车。(6)尽量减少到公共场所去,进出公共场所要扫场所码、测温、登记。(7)发现自己是红黄码的不要惊慌不要直接就走，原地不动，在外地时通知当地社区、企业，在本县时上报所属社区、企业进行处理。</font></p>")
                    append("<p style='font-size:15sp;line-height:1.6;text-indent:2em;'>三、返后管控</p>")
                    append("<p style='font-size:16sp;line-height:1.6;text-indent:2em;'>货车司机及同乘人员要24小时内进行一次核酸检测，核酸检测结果未出之前不乘坐公共交通工具、出入公共场所。进行7天自我健康监测，<font color='red'>自己和家人</font>出现发热、咳嗽、咽痛、乏力、嗅觉味觉减退和腹泻等症状，应做好防护及时到就近医疗机构发热门诊就诊（不乘坐公共交通工具）<font color='red'>或上报社区等社区安排人员来处理</font>，并如实报告旅居史和接触史。</p>")
                    append("<p style='font-size:15sp;line-height:1.6;text-indent:2em;'>四、途经停留疫情流行地区管控</p>")
                    append("<p style='font-size:16sp;line-height:1.6;text-indent:2em;'>途经停留疫情流行地区和中高风险地区时，按照《关于加强跨省市运输疫情防控和重点物资运输保障的紧急通知》（济指办发〔2022〕95号）精神执行。无需住宿时，尽量不下车、不接触，装卸完毕、车辆消杀后快速驶离；确需住宿时，配合当地疫情防控，实行闭环管理，每天的核酸检测配合属地安排进行采样检测，严格避免和本地人员接触；最后一次离开疫情流行地区并完成7天集中管理，且核酸检测阴性后，才能与社会面接触，纳入社区管理。</p>")
                    append("<p style='font-size:16sp;line-height:1.6;text-indent:2em;'>本人郑重承诺：如违法各项疫情防控措施，造成疫情传播等不良后果，本人愿承担所有责任。</p>")
                    append("<p style='font-size:15sp;font-weight:bold;'>承诺人：（签字）</p>")
                    if (hasSign) {
                        append("<p style='font-size:15sp;font-weight:bold;margin:10dp 0;padding:20px;'>签字日期：$signTime</p>")
                    }
                }
            }
            else -> {
                // 安全文明驾驶承诺书
                contentBuilder.apply {
                    append("<h1 style='text-align:center;font-weight:bold;font-size:18sp;margin-bottom:16dp;'>安全文明驾驶承诺书</h1>")
                    append("<p style='font-size:16sp;line-height:1.6;'>为保障自己与他人的生命财产安全，作为一名驾驶人，我自愿遵守各项交通安全法律法规，保证做到以下事项:</p>")
                    append("<p style='font-size:15sp;line-height:1.6;'>一、认真学习落实道路交通安全法律法规和单位(公司)各项安全管理制度。积极参加单位(公司)组织的交通安全教育培训，接受相关考核并确保达标。</p>")
                    append("<p style='font-size:15sp;line-height:1.6;'>二、加强车辆日常管理维护。按照国家规定定期对车辆进行安全技术检验和年检年审,及时处理车辆交通违法记录，不非法改装车辆，确保车辆安全状况良好。</p>")
                    append("<p style='font-size:15sp;line-height:1.6;'>三、遵守交通法规，安全文明出行。不无证驾驶车辆、不开与准驾车型不符的车辆;不使用假牌套牌、不遮挡污损号牌;不开非法改装、拼装报废和逾期未检验或检验不合格车辆;不酒后驾驶或醉酒驾驶;不使用货运车辆违法载人;提醒驾乘人员系好安全带。不超速、超员、超载和疲劳驾驶;不拨打接听手持电话;不逆向行驶、争道抢行、闯信号灯、乱停乱放;驾驶机动车主动礼让行人;严格遵守有关限行规定和大型货车“靠右行驶”“右转必停”等交通管理措施。</p>")
                    append("<p style='font-size:15sp;line-height:1.6;'>四、遇有雾、雨、雪、沙尘等恶劣天气，或在冰雪、泥泞的道路上行驶时，自觉采取减速、控距、亮尾措施，将车速降至每小时 30 公里以下，谨慎、规范驾驶</p>")
                    append("<p style='font-size:15sp;line-height:1.6;'>五、发生道路交通事故时，积极落实报警求助、抢救伤者、保护现场、快处快赔等规定，坚决杜绝发生事故后擅离现场或逃逸行为。以上承诺如有违反，我自愿承担单位处罚和相应的法律后果。</p>")
                    append("<br>")
                    append("<p style='font-size:16sp;'>驾驶人签名:</p>")
                    append("<br><br>")

                    // 根据地区编码显示不同内容
                    if (areaCode == "3704") {
                        append("<p style='font-size:16sp;'>作为单位(公司)法人，我们将积极履行主体责任，切实加强管理，监督驾驶人落实好以上事项。</p>")
                    } else {
                        append("<p style='font-size:16sp;'>作为单位(公司)法人，我们将积极履行主体责任，切实加强管理，监督驾驶人落实好以上事项，如不履行全体责任我自愿承担相应的法律后果。</p>")
                    }

                    append("<br><br>")
                    append("<p style='font-size:16sp;'>单位(公司)名称:$companyName</p>")

                    when (areaCode) {
                        "3714" -> {
                            append("<br>")
                            append("<p style='font-size:16sp;'>法人签名:</p>")
                            append("<br>")
                            if (hasSign) {
                                append("<p style='font-size:15sp;font-weight:bold;margin:10dp 0;'>$signTime</p>")
                            }
                            append("<p style='font-size:16sp;'>本承诺书一式三份，驾驶人、单位（公司）、交警大队各执一份。</p>")
                        }
                        "3704" -> {
                            append("<br>")
                            append("<p style='font-size:16sp;'>特此承诺！</p>")
                            if (hasSign) {
                                append("<p style='font-size:15sp;font-weight:bold;margin:10dp 0;'>$signTime</p>")
                            }
                            append("<p style='font-size:16sp;'>本承诺书一式两份，驾驶人、单位(公司)各执一份。</p>")
                        }
                        else -> {
                            append("<br>")
                            append("<p style='font-size:16sp;'>特此承诺！</p>")
                            if (hasSign) {
                                append("<p style='font-size:15sp;font-weight:bold;margin:10dp 0;'>$signTime</p>")
                            }
                            append("<p style='font-size:16sp;'>本承诺书一式四份，驾驶人、单位(公司)、交警大队、交警支队各执一份。</p>")
                        }
                    }
                }
            }
        }

        // 设置富文本内容
        binding.tvContent.text = Html.fromHtml(contentBuilder.toString(),
            Html.FROM_HTML_MODE_COMPACT)
    }

    // 显示签署时间
    private fun showSignTime() {
        when (categoryId) {
            "6109", "5951", "6110", "5914" -> {
                binding.tvSignTime.visibility = View.VISIBLE
                binding.tvSignTime.text = "签字日期：$signTime"
            }
            else -> {
                binding.tvSignTime.visibility = View.GONE
            }
        }
    }

    // 保存签名并上传
    private fun saveSignature() {
        if (!binding.signatureView.hasSignature()) {
            showToast("请先签名再保存")
            return
        }

        val signatureBitmap = binding.signatureView.getSignatureBitmap()
        val file = BitmapUtils.saveBitmapToFile(this@YiqingSignActivity, signatureBitmap)
        file?.let { uploadSignatureFile(it) }
    }

    private fun uploadSignatureFile(file: File) {
        noticeViewModel.uploadFile(
            MultipartBody.Part.createFormData("file", file.name,
                file.asRequestBody("image/png".toMediaTypeOrNull())),
            uploadStateFlow)
    }

    // 提交签署信息
    private fun submitSign(fileUrl: String) {
        trainingViewModel.epidemicPost(fileUrl, submitFlow)
    }

    override fun onDestroy() {
        super.onDestroy()
        ProgressDialogUtils.dismiss()
    }
}
