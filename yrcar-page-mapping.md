# Android 与 ytcar-app 页面映射

> 说明：`ytcar-app` 工程位于 `/Users/source/ytcar-app`。本文依据 `ytcar-app/pages.json`、`pages/**` 实际文件，以及 Android 侧 `Activity/Fragment` 代码整理。历史沟通里写成 `yrcar-app` 的地方，这里统一按真实目录名 `ytcar-app` 记录。

## 映射状态

- `已确认`：Android 页面职责明确，且已在 `ytcar-app` 找到真实页面路径。
- `容器页`：Android 是 Activity/Fragment 容器，`ytcar-app` 侧通常是 tab 页或若干子页组合。
- `部分对应`：职责能对上，但实现形态不同，或 `ytcar-app` 由多个页面协作承载。
- `待确认`：Android 页面存在，但 `ytcar-app` 是否有独立页面仍不明确。
- `无对应`：当前 `ytcar-app` 未发现直接对应业务页面。
- `原生/三方回调`：SDK 回调、桥接页、系统能力页，一般不对应业务页面。

## 基础与主入口

| Android Activity/Fragment | Android 文件 | ytcar-app 页面 | 状态 | 备注 |
|---|---|---|---|---|
| SplashAdActivity | `app/src/main/java/com/fx/zfcar/pages/SplashAdActivity.kt` | `pages/first-page/index` | 已确认 | 启动/角色入口；Android 侧还承载开屏广告逻辑 |
| AgreementConsentActivity | `app/src/main/java/com/fx/zfcar/pages/AgreementConsentActivity.kt` | 无独立页面 | 无对应 | `ytcar-app` 未见首次协议确认页；协议更多内嵌在版本页或 WebView |
| MainActivity | `app/src/main/java/com/fx/zfcar/pages/MainActivity.kt` | tabBar: `pages/v2/tabOne/tabOne`、`pages/train/index` | 容器页 | Android 底部 Tab 容器，对应查车与培训两大入口 |
| CarFragment | `app/src/main/java/com/fx/zfcar/car/CarFragment.kt` | `pages/v2/tabOne/tabOne`、`pages/map/mapHome` | 已确认 | 查车首页地图；`tabOne` 挂载地图能力 |
| TrainingFragment | `app/src/main/java/com/fx/zfcar/training/TrainingFragment.kt` | `pages/train/index` | 已确认 | 培训首页聚合页 |
| LoginActivity | `app/src/main/java/com/fx/zfcar/pages/LoginActivity.kt` | `pages/monitor-login/index`、`pages/security-login/index`、`pages/login/index` | 已确认 | Android 兼容车载登录/培训登录；`ytcar-app` 保留多套登录页 |
| PolicyContentActivity | `app/src/main/java/com/fx/zfcar/pages/PolicyContentActivity.kt` | `pages/webview/index` | 已确认 | 协议/隐私/外链内容由 WebView 承载 |
| WXEntryActivity | `app/src/main/java/com/fx/zfcar/wxapi/WXEntryActivity.kt` | 无独立页面 | 原生/三方回调 | 微信 SDK 回调入口 |

## 查车与车辆详情

| Android Activity/Fragment | Android 文件 | ytcar-app 页面 | 状态 | 备注 |
|---|---|---|---|---|
| TreeListActivity | `app/src/main/java/com/fx/zfcar/car/TreeListActivity.kt` | `pages/realtime-search/index`、`pages/search-car/index` | 已确认 | 车辆组织树/车辆搜索 |
| CarInfoActivity | `app/src/main/java/com/fx/zfcar/car/CarInfoActivity.kt` | `pages/carinfo/index`、`pages/vehicleinfo/index` | 已确认 | 车辆详情与车辆档案 |
| CarInfoFragment | `app/src/main/java/com/fx/zfcar/car/CarInfoFragment.kt` | `pages/carinfo/index` | 已确认 | 车辆基础信息区块 |
| TerminalInfoFragment | `app/src/main/java/com/fx/zfcar/car/TerminalInfoFragment.kt` | `pages/carinfo/index` | 已确认 | 终端信息在详情页内分区展示 |
| OtherInfoFragment | `app/src/main/java/com/fx/zfcar/car/OtherInfoFragment.kt` | `pages/carinfo/index` | 已确认 | 其他信息在详情页内分区展示 |
| ActivityNavi | `app/src/main/java/com/fx/zfcar/car/ActivityNavi.kt` | `pages/direct/direct` | 已确认 | 地图导航页 |
| TrackPlayActivity | `app/src/main/java/com/fx/zfcar/car/TrackPlayActivity.kt` | `pages/trajectory/index` | 已确认 | 轨迹回放 |
| RealTimeMonitorActivity | `app/src/main/java/com/fx/zfcar/car/RealTimeMonitorActivity.kt` | `pages/v2/video/liveBroadcast/liveBroadcast`、旧页 `pages/realtime-video/index` | 已确认 | 实时视频监控 |
| VideoPlaybackActivity | `app/src/main/java/com/fx/zfcar/car/VideoPlaybackActivity.kt` | `pages/v2/video/videoPlayback/videoPlayback`、旧页 `pages/video-playback/index` | 已确认 | 视频回放 |
| VideoListActivity | `app/src/main/java/com/fx/zfcar/car/VideoListActivity.kt` | `pages/v2/video/videoList/videoList` | 已确认 | 视频文件列表 |
| VideoFullActivity | `app/src/main/java/com/fx/zfcar/car/VideoFullActivity.kt` | `pages/v2/video/videoFull/videoFull` | 已确认 | 全屏视频 |

## 报警、状态与报表

| Android Activity/Fragment | Android 文件 | ytcar-app 页面 | 状态 | 备注 |
|---|---|---|---|---|
| DeviceAlarmActivity | `app/src/main/java/com/fx/zfcar/car/DeviceAlarmActivity.kt` | `pages/v2/warningList/warningList` | 已确认 | 设备报警列表 |
| DeviceStatusActivity | `app/src/main/java/com/fx/zfcar/car/status/DeviceStatusActivity.kt` | `pages/v2/deviceAlert/deviceAlert` | 已确认 | 车辆状态总览 |
| DeviceStatusListActivity | `app/src/main/java/com/fx/zfcar/car/status/DeviceStatusListActivity.kt` | `pages/v2/deviceAlert/carList` | 已确认 | 状态筛选后的车辆列表 |
| ExpireCarActivity | `app/src/main/java/com/fx/zfcar/car/status/ExpireCarActivity.kt` | `pages/v2/deviceAlert/outDate` | 已确认 | 到期车辆 |
| OperationAnalysisActivity | `app/src/main/java/com/fx/zfcar/car/OperationAnalysisActivity.kt` | `pages/v2/analysisChart/analysisChart` | 已确认 | 运营分析图表 |
| ReportActivity | `app/src/main/java/com/fx/zfcar/car/ReportActivity.kt` | `pages/report/index` | 已确认 | 报表首页 |
| ReportAlarmDetailActivity | `app/src/main/java/com/fx/zfcar/car/ReportAlarmDetailActivity.kt` | `pages/report/warm/index` | 已确认 | 报警报表详情 |
| OfflineDetailActivity | `app/src/main/java/com/fx/zfcar/car/OfflineDetailActivity.kt` | `pages/report/offline/index` | 已确认 | 离线明细 |
| ReportWarningDetailActivity | `app/src/main/java/com/fx/zfcar/car/ReportWarningDetailActivity.kt` | 未找到 Android 文件 | 待确认 | 当前仓库未发现对应 Kotlin/Java 文件，疑似历史记录项 |

## 培训首页入口总表

> 以下页面多数由 `TrainingFragment` 入口跳转，原文档只覆盖了部分模块，这里补齐首页聚合到的业务页。

| Android 入口页面 | Android 文件 | ytcar-app 页面 | 状态 | 备注 |
|---|---|---|---|---|
| NoticeActivity | `app/src/main/java/com/fx/zfcar/training/notice/NoticeActivity.kt` | `pages/driveCompany/notice/notice` | 已确认 | 消息公告总入口，含企业通知/公文公告/违章公告三 Tab |
| UserCenterActivity | `app/src/main/java/com/fx/zfcar/training/user/UserCenterActivity.kt` | `pages/user/index` | 容器页 | Android 为 Fragment 容器，`ytcar-app` 为“我的”主页面 |
| TrainListActivity | `app/src/main/java/com/fx/zfcar/training/safetytraining/TrainListActivity.kt` | `pages/train/trainList/index`、`pages/train/trainList/subject`、`pages/train/trainList/before` | 已确认 | 安全培训/安全会议/岗前培训入口 |
| ExamPracticeActivity | `app/src/main/java/com/fx/zfcar/training/exam/ExamPracticeActivity.kt` | `pages/train/exam/order/index`、`pages/train/exam/testBase/testBase` | 已确认 | 两类人员真题入口 |
| ExamManagerActivity | `app/src/main/java/com/fx/zfcar/training/safetytraining/ExamManagerActivity.kt` | `pages/train/test/index` | 已确认 | 岗前培训考试/考试管理 |
| DriveLogActivity | `app/src/main/java/com/fx/zfcar/training/drivelog/DriveLogActivity.kt` | `pages/driveCompany/driveDaily/index/index` | 已确认 | 行车日志首页 |
| CarSafetyCheckActivity | `app/src/main/java/com/fx/zfcar/training/safetycheck/CarSafetyCheckActivity.kt` | `pages/driveCompany/driveCheck/index/index` | 已确认 | 车辆安全检查首页 |
| DangerCheckActivity | `app/src/main/java/com/fx/zfcar/training/dangercheck/DangerCheckActivity.kt` | `pages/driveCompany/dangerCheck/index/index` | 已确认 | 隐患排查首页 |
| DriverBookActivity | `app/src/main/java/com/fx/zfcar/training/DriverBookActivity.kt` | `pages/train/driverBook/index` | 已确认 | 驾驶员责任书 |
| YiqingSignActivity | `app/src/main/java/com/fx/zfcar/training/YiqingSignActivity.kt` | `pages/train/yiqing/index` | 已确认 | 疫情/承诺书签署 |
| JobsActivity | `app/src/main/java/com/fx/zfcar/training/jobs/JobsActivity.kt` | `pages/job/index` | 已确认 | 综合信息/招聘求职入口 |

## 安全培训与考试

| Android Activity/Fragment | Android 文件 | ytcar-app 页面 | 状态 | 备注 |
|---|---|---|---|---|
| TrainListActivity | `app/src/main/java/com/fx/zfcar/training/safetytraining/TrainListActivity.kt` | `pages/train/trainList/index`、`pages/train/trainList/subject`、`pages/train/trainList/before` | 已确认 | 培训列表入口 |
| DailyTrainListActivity | `app/src/main/java/com/fx/zfcar/training/safetytraining/DailyTrainListActivity.kt` | `pages/train/trainList/daily` | 已确认 | 日常培训列表 |
| TrainCourseListActivity | `app/src/main/java/com/fx/zfcar/training/safetytraining/TrainCourseListActivity.kt` | `pages/train/trainDetail/index`、`pages/train/trainDetail/daily`、`pages/train/trainDetail/subject`、`pages/train/trainDetail/before` | 已确认 | 课程列表/培训详情 |
| StudyDetailActivity | `app/src/main/java/com/fx/zfcar/training/safetytraining/StudyDetailActivity.kt` | `pages/train/studyDetail/index` | 已确认 | 学习详情 |
| CourseDetailActivity | `app/src/main/java/com/fx/zfcar/training/safetytraining/CourseDetailActivity.kt` | `pages/train/studyDetail/detail` | 已确认 | 课件详情/视频学习 |
| FaceCheckActivity | `app/src/main/java/com/fx/zfcar/training/safetytraining/FaceCheckActivity.kt` | `pages/train/faceCheck/index` | 已确认 | 人脸核验 |
| ExamManagerActivity | `app/src/main/java/com/fx/zfcar/training/safetytraining/ExamManagerActivity.kt` | `pages/train/test/index` | 已确认 | 考试管理/开始考试 |
| ScoreDetailActivity | `app/src/main/java/com/fx/zfcar/training/safetytraining/ScoreDetailActivity.kt` | `pages/train/checkTest/index` | 已确认 | 成绩详情 |
| ExamPracticeActivity | `app/src/main/java/com/fx/zfcar/training/exam/ExamPracticeActivity.kt` | `pages/train/exam/order/index`、`pages/train/exam/testBase/testBase` | 已确认 | 真题/题库入口 |
| AnswerQuestionActivity | `app/src/main/java/com/fx/zfcar/training/exam/AnswerQuestionActivity.kt` | `pages/train/exam/answerQuesion/answerQuesion` | 已确认 | 答题页 |
| AuthenticationActivity | `app/src/main/java/com/fx/zfcar/training/AuthenticationActivity.kt` | `pages/train/authentication/index` | 已确认 | 培训认证资料 |
| MeetingDetailActivity | `app/src/main/java/com/fx/zfcar/training/MeetingDetailActivity.kt` | `pages/train/meeting/index` | 已确认 | 会议详情、签字、拍照打卡 |

## 通知、公告与签名

| Android Activity/Fragment | Android 文件 | ytcar-app 页面 | 状态 | 备注 |
|---|---|---|---|---|
| NoticeActivity | `app/src/main/java/com/fx/zfcar/training/notice/NoticeActivity.kt` | `pages/driveCompany/notice/notice` | 已确认 | 公告列表主入口，三类公告共用 |
| NoticeDetailActivity | `app/src/main/java/com/fx/zfcar/training/notice/NoticeDetailActivity.kt` | `pages/driveCompany/notice/detail` | 已确认 | 企业通知/公文公告详情 |
| WarningDetailActivity | `app/src/main/java/com/fx/zfcar/training/notice/WarningDetailActivity.kt` | `pages/driveCompany/notice/warningDetail` | 已确认 | 违章公告详情 |
| SignatureActivity | `app/src/main/java/com/fx/zfcar/training/notice/SignatureActivity.kt` | `pages/driveCompany/writePage/writePage`、`pages/driveCompany/whitePage/whitePage` | 部分对应 | Android 是横屏独立签字页；`ytcar-app` 由签字页 + 空白过渡页跳转回原页面 |
| `ytcar-app` 独立违章公告列表 | 无 Android 独立页 | `pages/driveCompany/notice/warning` | 无对应 | Android 直接在 `NoticeActivity` 的第三个 Tab 内呈现，无单独 Activity |

## 支付

| Android Activity/Fragment | Android 文件 | ytcar-app 页面 | 状态 | 备注 |
|---|---|---|---|---|
| PayDetailActivity | `app/src/main/java/com/fx/zfcar/training/pay/PayDetailActivity.kt` | `pages/train/pay/index`、`pages/train/pay/daily` | 已确认 | 培训支付详情；区分日常/岗前/年度支付 |
| PayOrderActivity | `app/src/main/java/com/fx/zfcar/training/pay/PayOrderActivity.kt` | `pages/train/payList/index`、`pages/train/payOrder/payOrder` | 已确认 | 支付订单列表；`payOrder` 偏年度订单支付 |
| PaymentFragment | `app/src/main/java/com/fx/zfcar/training/user/PaymentFragment.kt` | `pages/train/pay/index`、`pages/train/payList/index`、`pages/train/payOrder/payOrder` | 部分对应 | Android 为用户中心内嵌支付页；`ytcar-app` 以独立页面组合承载 |

## 招聘求职 / 综合信息

| Android Activity/Fragment | Android 文件 | ytcar-app 页面 | 状态 | 备注 |
|---|---|---|---|---|
| JobsActivity | `app/src/main/java/com/fx/zfcar/training/jobs/JobsActivity.kt` | `pages/job/index` | 已确认 | 综合信息列表，含“信息广场 / 我的”双 Tab |
| JobDetailCompanyActivity | `app/src/main/java/com/fx/zfcar/training/jobs/JobDetailCompanyActivity.kt` | `pages/job/detail` | 已确认 | 企业招聘详情 |
| MyJobDetailActivity | `app/src/main/java/com/fx/zfcar/training/jobs/MyJobDetailActivity.kt` | `pages/job/jobDetail` | 已确认 | 我的发布/我的求职详情 |
| PublishJobActivity | `app/src/main/java/com/fx/zfcar/training/jobs/PublishJobActivity.kt` | `pages/job/job` | 已确认 | 发布招聘/求职信息 |

## 用户中心

| Android Activity/Fragment | Android 文件 | ytcar-app 页面 | 状态 | 备注 |
|---|---|---|---|---|
| UserCenterActivity | `app/src/main/java/com/fx/zfcar/training/user/UserCenterActivity.kt` | `pages/user/index` | 容器页 | Android 为 Fragment 容器，`ytcar-app` 为“我的”单页 |
| ProfileFragment | `app/src/main/java/com/fx/zfcar/training/user/ProfileFragment.kt` | `pages/user/index` | 已确认 | 我的首页 |
| AccountInfoFragment | `app/src/main/java/com/fx/zfcar/training/user/AccountInfoFragment.kt` | `pages/userinfo/index` | 已确认 | 账号信息/退出登录 |
| ModifyInfoFragment | `app/src/main/java/com/fx/zfcar/training/user/ModifyInfoFragment.kt` | `pages/edit-userinfo/index` | 已确认 | 修改昵称/资料 |
| ChangePasswordFragment | `app/src/main/java/com/fx/zfcar/training/user/ChangePasswordFragment.kt` | `pages/edit-password/index` | 已确认 | 修改密码 |
| AboutAppFragment | `app/src/main/java/com/fx/zfcar/training/user/AboutAppFragment.kt` | `pages/versioninfo/index` | 已确认 | 关于 App、服务协议、隐私政策 |
| StudyDetailsFragment | `app/src/main/java/com/fx/zfcar/training/user/StudyDetailsFragment.kt` | `pages/train/studyDetail/index` | 部分对应 | Android 从“我的”进入学习详情；`ytcar-app` 复用培训学习详情页 |
| StudyRecordActivity | `app/src/main/java/com/fx/zfcar/training/user/StudyRecordActivity.kt` | 无独立页面 | 待确认 | `ytcar-app` 未见同名学习记录页，可能被 `pages/train/studyDetail/index` 内联替代 |
| LearningCertificateFragment | `app/src/main/java/com/fx/zfcar/training/user/LearningCertificateFragment.kt` | `pages/train/prove/index` | 已确认 | 学习证明/证书 |
| PaymentFragment | `app/src/main/java/com/fx/zfcar/training/user/PaymentFragment.kt` | `pages/train/pay/index`、`pages/train/payList/index` | 部分对应 | 用户中心内嵌支付，与培训支付页复用 |
| ScanCodeActivity | `app/src/main/java/com/fx/zfcar/training/user/ScanCodeActivity.kt` | 无独立页面 | 无对应 | Android 原生扫码；`ytcar-app` 当前未见对应扫码页 |
| BaseUserFragment | `app/src/main/java/com/fx/zfcar/training/user/BaseUserFragment.kt` | 无独立页面 | 原生/容器基类 | Fragment 抽象基类 |

## 行车日志

| Android Activity/Fragment | Android 文件 | ytcar-app 页面 | 状态 | 备注 |
|---|---|---|---|---|
| DriveLogActivity | `app/src/main/java/com/fx/zfcar/training/drivelog/DriveLogActivity.kt` | `pages/driveCompany/driveDaily/index/index` | 已确认 | 行车日志首页 |
| DriveLogStageActivity | `app/src/main/java/com/fx/zfcar/training/drivelog/DriveLogStageActivity.kt` | `pages/driveCompany/driveDaily/driveStage/driveStage` | 已确认 | 分阶段填写 |
| LastDriveLogRecordActivity | `app/src/main/java/com/fx/zfcar/training/drivelog/LastDriveLogRecordActivity.kt` | `pages/driveCompany/driveDaily/lastRecord/lastRecord` | 已确认 | 最近/历史行车日志 |
| CarSearchActivity | `app/src/main/java/com/fx/zfcar/training/drivelog/CarSearchActivity.kt` | `pages/driveCompany/searchCarNum/searchCarNum` | 已确认 | 行车日志选车页 |

## 车辆安全检查

| Android Activity/Fragment | Android 文件 | ytcar-app 页面 | 状态 | 备注 |
|---|---|---|---|---|
| CarSafetyCheckActivity | `app/src/main/java/com/fx/zfcar/training/safetycheck/CarSafetyCheckActivity.kt` | `pages/driveCompany/driveCheck/index/index` | 已确认 | 安全检查首页 |
| CarCheckStageActivity | `app/src/main/java/com/fx/zfcar/training/safetycheck/CarCheckStageActivity.kt` | `pages/driveCompany/driveCheck/checkStage/checkStage` | 已确认 | 检查分阶段填写 |
| LastRecordActivity | `app/src/main/java/com/fx/zfcar/training/safetycheck/LastRecordActivity.kt` | `pages/driveCompany/driveCheck/lastRecord/lastRecord` | 已确认 | 最近检查记录 |

## 隐患排查

| Android Activity/Fragment | Android 文件 | ytcar-app 页面 | 状态 | 备注 |
|---|---|---|---|---|
| DangerCheckActivity | `app/src/main/java/com/fx/zfcar/training/dangercheck/DangerCheckActivity.kt` | `pages/driveCompany/dangerCheck/index/index` | 已确认 | 隐患排查首页 |
| DangerCheckDetailActivity | `app/src/main/java/com/fx/zfcar/training/dangercheck/DangerCheckDetailActivity.kt` | `pages/driveCompany/dangerCheck/detail/detail` | 已确认 | 新增/编辑排查详情 |
| DangerHistoryRecordActivity | `app/src/main/java/com/fx/zfcar/training/dangercheck/DangerHistoryRecordActivity.kt` | `pages/driveCompany/dangerCheck/historyRecord/historyRecord` | 已确认 | 历史记录详情 |
| UploadPhotosActivity | `app/src/main/java/com/fx/zfcar/training/dangercheck/UploadPhotosActivity.kt` | `pages/driveCompany/dangerCheck/uploadPhotos/uploadPhotos` | 已确认 | 图片上传 |
| `ytcar-app` 整改补充页 | 无 Android 独立页 | `pages/driveCompany/dangerCheck/rectification/index`、`pages/driveCompany/dangerCheck/rectification/sign` | 无对应 | `ytcar-app` 存在额外整改流程页，Android 当前未拆成独立 Activity |

## 高频页面参数映射

> 这一节先补高频且后续梳接口最需要的跳转参数。只记录已在两边都确认过的主链路。

| 场景 | Android 传参 | ytcar-app 传参/缓存 | 备注 |
|---|---|---|---|
| 公告详情 | `NoticeDetailActivity`: `noticeId`；同时用缓存保存 `noticeInfo`、`noticeId`、`noticeSign` | `pages/driveCompany/notice/detail`: `option.noticeId`；`uni.setStorageSync('noticeInfo')`、`noticeId`、`noticeSign` | 两边都依赖列表项缓存恢复详情与签字状态 |
| 违章公告详情 | `WarningDetailActivity`: `notice` JSON | `pages/driveCompany/notice/warningDetail`: `option.notice` JSON | 详情直接整包透传 |
| 通用签字页 | `SignatureActivity`: `from`、`fill`、`type`、`answer`、`exams_id`、`training_safetyplan_id` | `pages/driveCompany/writePage/writePage`: `from`、`fill`、`type`；签完经 `pages/driveCompany/whitePage/whitePage` 带 `fromPage`、`fromUrl=sign` 回跳 | `ytcar-app` 目前未看到考试答卷签字参数链，更多用于责任书/公告/隐患签字 |
| 综合信息详情 | `JobDetailCompanyActivity` / `MyJobDetailActivity`: `data` JSON | `pages/job/detail` / `pages/job/jobDetail`: `option.data` JSON | 两边都直接透传整行数据 |
| 培训支付详情 | `PayDetailActivity`: `payName` / `payNum` / `usualpaytype`，或 `id` / `name` / `money` / `type` / `usualpaytype` | `pages/train/pay/index`、`pages/train/pay/daily`: 同名 query；年度支付还会用 `uni.setStorageSync('payInfo')` | Android 与 uni-app 参数命名基本一致 |
| 行车日志选车 | `DriveLogStageActivity` 返回 `carNum`、`carId` | `pages/driveCompany/searchCarNum/searchCarNum`: 通过 `from` 回跳，回传 `?carNum=...` | `ytcar-app` 只显式回写 `carNum`，`carId` 更多靠缓存或后续查询 |
| 安检选车 | `CarCheckStageActivity` 同样依赖选车结果 | `pages/driveCompany/searchCarNum/searchCarNum`: 通过 `from` 回跳，回传 `?carNum=...` | 与行车日志共用选车页 |
| 隐患排查详情 | `DangerCheckDetailActivity`: `carNum`、`from`、`fill`、`photosNum`，并配合本地缓存 `inputForm`、`tempSave`、`photos` | `pages/driveCompany/dangerCheck/detail/detail`: `option.carNum`、`option.fromUrl`；大量依赖 `uni.setStorageSync('inputForm')`、`tempSave`、`photos`、`pageTitle` | 这一块是当前缓存最重的流程 |
| 培训列表入口 | `TrainListActivity`: `type`、`title`；课程/支付/考试再传 `id`、`name`、`money`、`usualpaytype`、`training_safetyplan_id` 等 | `pages/train/index` -> `pages/train/home/index` / `pages/train/trainList/**`：主要用 `type`、`title`、`id`、`name`、`training_safetyplan_id`、`fromUrl` | `ytcar-app` 比 Android 多了一层 `pages/train/home/index` 过渡页 |
| 学习/考试详情 | `ExamManagerActivity` / `ScoreDetailActivity`: `id`、`training_safetyplan_id`、`type`、`answer`、`exams_id` | `pages/train/test/index`、`pages/train/checkTest/index`、`pages/train/sign/index`: 同名 query | Android 和 uni-app 参数名高度一致 |

## 查车 / 视频 / 报表参数映射

| 场景 | Android 传参 | ytcar-app 传参/缓存 | 备注 |
|---|---|---|---|
| 车辆详情 | `CarInfoActivity`: `KEY_CAR_ID` | `pages/carinfo/index`: `option.id` | Android 支持 `String/Int` 两种 carId；uni-app 直接用 `id` |
| 实时搜索页 | `TreeListActivity`: `KEY_SEARCH_TYPE`、`KEY_CAR_NUM`、`KEY_CAR_SEARCH` | `pages/realtime-search/index`: `option.type`、`option.carNum`、`option.searchValue`、`option.activeIndex` | uni-app 还支持从地图页带 `activeIndex=tree` 打开机构树搜索 |
| 地图实时视频 WebView | `RealTimeMonitorActivity` / `VideoPlaybackActivity` 内部持有 `carId`、`carNum` | `pages/webview/index`、`pages/webview/videoReplay`: `option.id`、`option.carnum` | `ytcar-app` 通过 `webview` 页面承接老视频实现 |
| 轨迹回放 | `TrackPlayActivity`: `KEY_CAR_ID`、`KEY_CAR_DLTYPE`、`KEY_CAR_STATUS` | `pages/trajectory/index`: 页面内部多依赖全局/查询参数；旧版 `index1.vue` 也存在 | Android 参数更完整，uni-app 更偏页面内部重组 |
| 实时视频 | `RealTimeMonitorActivity`: `KEY_CAR_ID`、`KEY_CAR_NUM`、`KEY_CAR_VIDEO`、`KEY_CAR_ONLINE` | `pages/v2/video/liveBroadcast/liveBroadcast`: `data.carid`、`data.carnum`、`data.videoCar`、`data.online` | 两边都带车辆在线状态和是否视频车 |
| 视频回放查询 | `VideoPlaybackActivity`: `KEY_CAR_ID`、`KEY_CAR_NUM`、`KEY_CAR_VIDEO`、`KEY_CAR_ONLINE` | `pages/v2/video/videoPlayback/videoPlayback`: `data.carid`、`data.carnum`、`data.videoCar`、`data.online` | uni-app 页面自行维护开始/结束时间与筛选项 |
| 视频文件列表 | `VideoListActivity`: `KEY_CAR_NUM`、`KEY_SIM`、`KEY_VIDEO_LIST` | `pages/v2/video/videoList/videoList`: `option.carnum` + 缓存 `videoList`、`videoSim` | Android 直接 Intent 传列表 JSON；uni-app 主要走本地缓存 |
| 全屏视频播放 | `VideoFullActivity`: `KEY_VIDEO_URL`、`KEY_TITLE` | `pages/v2/video/videoFull/videoFull`: 缓存 `videoUrl` | uni-app 不显式传 URL，依赖本地缓存 |
| 报警报表详情 | `ReportAlarmDetailActivity`: `KEY_WARN_REPORT_TYPE`、`KEY_WARN_REPORT_NUM`、`KEY_WARN_REPORT_NAME`、`KEY_WARN_REPORT_TIME_TYPE` | `pages/report/warm/index`: `option.warningType`、`option.count`、`option.timetype` | 参数命名几乎一一对应 |
| 离线详情 | `OfflineDetailActivity`: `KEY_CAR_ID`、`KEY_CAR_NUM`、`KEY_START`、`KEY_END` | `pages/report/offline/index`: `option.carId`、`option.start`、`option.end` | uni-app 未使用 `carNum`，标题直接显示条数 |
| 设备状态总览 | `DeviceStatusActivity`: 无外部参数 | `pages/v2/deviceAlert/deviceAlert` | 都是总览入口页 |
| 状态车辆列表 | `DeviceStatusListActivity`: `KEY_CAR_STATUS_TITLE`、`KEY_CAR_STATUS_TYPE` | `pages/v2/deviceAlert/carList`: `option.type`、`option.totalNum` | uni-app 列表页靠 `type` 决定展示文案 |
| 到期车辆列表 | `ExpireCarActivity`: Android 内部区分即将到期/已到期 | `pages/v2/deviceAlert/outDate`: `queryParms.expired=true/false` | uni-app 用页内 Tab 切换，不依赖外部 query |
| 设备报警列表 | `DeviceAlarmActivity`: 起止时间、报警类型、分页参数 | `pages/v2/warningList/warningList`: 页内维护 `searchType`、`start`、`end`、`pageNum`、`pageSize` | 入口参数少，更多是页内筛选 |

## 高频接口映射

> 这一节先按“页面/流程 -> Android ViewModel 方法 -> ytcar-app 接口函数/URL”整理。Android 侧底层 repository 名称基本一致，因此这里优先记录业务可读层。

| 页面/流程 | Android 调用 | ytcar-app 调用 | 关键参数 | 备注 |
|---|---|---|---|---|
| 公告列表 | `NoticeViewModel.getNoticeInfo(page, index, type)` | `getNoticeInfo` -> `/api/other/notice` | `page`、`index`、`type` | 企业通知/公文公告 |
| 公告已读/签字 | `NoticeViewModel.readNotice(noticeId, signimg)` | `noticeRead` -> `/api/other/readNotice` | `notice_id`、`signimg` | 签字图片为空时表示仅已读 |
| 违章公告列表 | `NoticeViewModel.warningNotice(page, index, type)` | `warningNotice` -> `/api/other/warningNotice` | `page`、`index`、`type` | Android 第三个 Tab；uni-app 也可独立进入 `warning.vue` |
| 违章公告已读/签字 | `NoticeViewModel.readWarningNotice(noticeId, signimg)` | `readWarningNotice` -> `/api/other/readWarningNotice` | `notice_id`、`signimg` | 支持签字回传 |
| 通用图片上传 | `NoticeViewModel.uploadFile(filePart)` | 页面里直接 `uni.uploadFile` 到 `/api/user/newuplode` | multipart `file` | `ytcar-app` 这部分未封装到 `apis.js`，而是在页面直接上传 |
| 用户信息 | `SafetyTrainingViewModel.getUserInfoSafe()` | `getUserInfoSafe` -> `/api/user/info` | 无 | 培训用户主信息、通知数、年费、企业信息等 |
| 修改昵称 | `SafetyTrainingViewModel.editNickname(nickname)` | `editNickname` -> `/system/app/user/profile` | `nickname` | 这里两端看起来存在接口口径差异，需后续再核 Android repository 的真实 URL |
| 修改密码 | `SafetyTrainingViewModel.resetPwd(newpassword, oldpassword)` | `changePasswordSafe` -> `/api/user/resetpwd` | `newpassword`、`oldpassword` | Android 与 uni-app 职责一致 |
| 学习证明 | `getUserStudyProveList(month)` | `userstudyprovelist` -> `api/user/userstudyprovelist` | `month` | 安全教育证书 |
| 继续教育证书 | `getEducationCertificate()` | `educationCertificate` -> `/api/training/educationCertificate` | 无 | 继续教育证书列表 |
| 岗前培训证书 | `getBeforeEducationCertificate()` | `beforeEducationCertificate` -> `/api/before/educationCertificate` | 无 | 岗前证书 |
| 学习详情列表 | `getStudySafetyList(month)` | `studysafetylist` -> `api/user/studysafetylist` | `month` | “我的 -> 学习详情” |
| 学习记录 | `getStudyList(searchname, training_safetyplan_id, page)` | `studylist` -> `api/user/studylist` | `searchname`、`training_safetyplan_id`、`page` | Android 有独立页，uni-app 更像内嵌能力 |
| 综合信息列表 | `getCompanyList(page, type)` / `getMyJobList(page, type)` | `companyList` -> `/api/job/companylist` / `myJobList` -> `/api/job/myjoblist` | `page`、`type` | “信息广场 / 我的” |
| 综合信息详情 | `getJobView(jobid)` | `jobView` -> `/api/job/jobview` | `jobid` | 企业职位详情 |
| 发布职位 | `jobAdd(request)` | `jobAdd` -> `/api/job/jobadd` | 表单字段 + 证件图片 URL | Android 与 uni-app 都包含图片上传后再提交 |
| 行车日志首页 | `TravelViewModel.getTravelLog()` | `getTravellog` -> `/api/other/travellog` | 无 | 返回上次记录 + 草稿箱 |
| 删除行车日志草稿 | `TravelViewModel.travelDel(id)` | `deleteDraft` -> `/api/other/travedel` | `id` | 删除草稿 |
| 提交行车日志 | `TravelViewModel.travelPost(request)` | `postDirveDiary` -> `/api/other/travelpost` | 表单整体 | 日志提交 |
| 车辆安全检查首页 | `SafetyTrainingViewModel.getCarCheck()` | `getCarCheck` -> `/api/other/carcheck` | 无 | 最近检查记录 + 草稿/状态 |
| 提交车辆安全检查 | Android `CarCheckViewModel` / 页面提交 | `postCarCheck` -> `/api/other/carcheckpost` | 表单整体 | Android 具体调用在 `CarCheckViewModel`，uni-app 接口已明确 |
| 隐患排查首页 | `SafetyTrainingViewModel.getDanger()` | `getDanger` -> `/api/other/danger` | 无 | 进行中记录 + 历史记录 |
| 提交隐患排查 | `SafetyTrainingViewModel.dangerPost(request)` | `postDanger` -> `/api/other/dangerpost` | 表单整体 | 提交主记录 |
| 隐患整改通知 | Android 暂未见独立页面 | `dangerNotice` -> `api/other/dangerNotice` | 记录主键等 | `ytcar-app` 独有整改链路 |
| 隐患签字 | Android 通过通用签字 + danger 提交 | `dangerSing` -> `api/other/dangerSing` | 签字相关字段 | `ytcar-app` 独有显式接口名 |
| 车辆搜索（培训三模块） | `SafetyTrainingViewModel.carnumSearch(carnum, page)` | `searchCar` -> `/api/other/carnumSearch` | `carnum`、`page` | 行车日志/安检/隐患共用 |
| 安全培训列表 | `getSafetyList(page, type)` | `safetylist` -> `api/user/safetylist` | `page`、`type` | 培训首页主列表 |
| 岗前培训列表 | `getBeforeList()` | `beforeList` -> `api/before/subjectlist` | 无 | 岗前培训计划 |
| 安全会议列表 | `getMeetingList(page, meetingType)` | `meetinglist` -> `api/training/meetinglist` | `page`、`meetingType` | 安全会议 |
| 继续教育列表 | `getSubjectList(page)` | `subjectList` -> `api/training/subjectlist` | `page` | 继续教育计划 |
| 安全会议详情 | `getMeetingView(id)` | `meetingview` -> `api/training/meetingview` | `id` | 会议详情 |
| 会议签到/签字提交 | `singPost(request)` | `singpost` -> `api/training/singpost` | `id`、签字图、拍照图等 | 会议签字拍照 |
| 日常培训签字提交 | `postSignImg(id, signfile)` | `postSignImg` -> `/api/dailysafety/sing` | `id`、`signfile` | 日常培训签字 |
| 岗前培训签字提交 | `postBeforeSign(id, signfile)` | `postBeforeSign` -> `/api/before/sing` | `id`、`signfile` | 岗前培训签字 |
| 岗前培训考试资格 | `beforeExamInfo()` | `beforeExamInfo` -> `/api/before/examinfo` | 无 | 判断是否需考试 |
| 日常培训支付检查 | `checkSafe(training_safetyplan_id)` | `orderisPay` / `checkSafe` -> `api/dailysafety/orderisPay`、`api/user/payorder` | `training_safetyplan_id` | 两端命名不完全一致，需后续核 Android repository 最终路由 |
| 日常培训个人支付 | `creatPayOrder(params)` / `creatPayOrderAlipay(params)` | `creatOrder` -> `api/dailysafety/creatOrder` | `id/year_id/type/method/code` 等 | Android 拆微信/支付宝两套 |
| 日常培训企业支付 | `companyPay(id)` | `companyPay` -> `api/dailysafety/companyPay` | `id` | 企业代付 |
| 岗前培训个人支付 | `trainPersonPay(params)` / `trainPersonPayAlipay(params)` | `trainPersonPay` -> `api/before/creatOrder` | `id`、`type`、`method` | 岗前培训个人支付 |
| 岗前培训企业支付 | `trainCompanyPay()` | `trainCompanyPay` -> `/api/before/companyPay` | 无或计划 ID | 企业代付 |
| 年度支付 | `yearPay(params)` / `yearPayAlipay(params)` | 页面复用支付页，本地会缓存 `payInfo`；`apis.js` 未单独暴露明显函数 | `year_id`、`type`、`method`、`code` | `ytcar-app` 主要在页面内完成组参 |
| 支付订单列表 | `getOrderList(page)` | `orderlist` -> `api/training/orderlist` | `page` | 支付记录 |
| 认证资料提交 | `submitAuthentication(request)` | `authentication` -> `api/training/subinfo` | 表单整体 | 培训认证资料 |
| 考试视图 | `ExamViewModel.getExamView(params)` | `getquestions` -> `api/user/safekspaperview` 等相关接口 | `id`、`type`、`training_safetyplan_id` | Android 封装更细；uni-app 偏直接调用题目/答题接口 |
| 提交考试 | `ExamViewModel.submitExam(request)` | `answerSbumit` -> `api/user/safekspost` | `answer`、`exams_id`、`training_publicplan_id`、`imgurl` | Android 提交体更完整，含签字图片 |
| 成绩/答案详情 | `getExamResult(params)`、`getQuestionView(params)` | `safecankao` -> `api/user/safecankao`、`questionview` -> `api/user/questionview` | `id`、题目 ID 等 | 成绩详情与查看正确答案 |
| 报警报表 | `ReportViewModel.getWarningReport(...)` | `getWarn` -> `/aggregation/app/work/warning` | `search`、`timetype`、`page` | 报警汇总 |
| 报警报表详情 | `ReportViewModel.getWarningDetail(...)` / `AlarmViewModel.getAlarmDetailsList(...)` | `getWarnDetail` -> `/aggregation/app/work/warningDetail` | `page`、`pageSize`、`timetype`、`type` | Android 还区分详情列表 ViewModel |
| 离线报表 | `getOfflineReport(end, page, pageSize, search, start)` | `getOffLine` -> `/aggregation/app/work/offline` | `start`、`end`、分页 | 离线统计 |
| 离线详情 | `getOfflineDetailReport(carId, end, start)` | `getOffLineDetail` -> `/aggregation/app/work/offlineDetail` | `carId`、`start`、`end` | 日期明细 |
| 里程报表 | `getMileageReport(...)` | `getMileage` -> `/aggregation/app/work/mileage` | `search`、`timetype`、分页 | 里程统计 |
| 过期车辆 | `getExpiredCars(...)` | `getCaroverlist` -> `/aggregation/app/work/caroverlist` | `search`、分页 | 过期查询 |
| 安全查询 | `getActiveWarning(...)` | `getSafety` -> `/aggregation/app/work/activeWarning` | `search`、`timetype`、分页 | 安全类报表 |

## 查车 / 视频 / 报表接口映射

| 页面/流程 | Android 调用 | ytcar-app 调用 | 关键参数 | 备注 |
|---|---|---|---|---|
| 车辆详情 | `CarInfoViewModel.getCarInfo(carId)` | `carinfo` -> `/car/app/car/carinfo` | `id/carId` | 车辆基础信息、终端信息、其他信息 |
| 地图位置点 | `CarInfoViewModel.getMapPositions(size)` | `getMapPunctuationData` -> `/aggregation/app/dashboard/maorealtime`、`getAllcars` -> `/aggregation/app/position/getTop`、`getMapPunctuationDataApi` -> `/api/user/realtime` | `size`、区域/权限相关参数 | uni-app 查车页里新旧接口并存 |
| 实时地址 | `CarInfoViewModel.getRealTimeAddress(carId, carnum)` | `getMapCatInfo` -> `/aggregation/app/position/realtimeaddress` | `carId`、`carnum` | 地图详情弹层/定位地址 |
| 树形组织查询 | `SearchViewModel.getTree(...)`、`getTreeBlurry(...)` | `getCarTreeInfo` -> `/car/app/tree/getTree`、`searchByDept` -> `/car/app/tree/getTreeBlurry` | `ancestors/blurry`、`tree`、`pos` | 机构树与模糊查询 |
| 车牌模糊搜索 | `SearchViewModel.searchCarByType(search, tree, type, pageSize, pageNum)` | `getSearchCarType` -> `/aggregation/app/dashboard/appSearchCarType`、`searchByCarName` -> `/car/app/car/allCarListPage` | `search`、`type`、`pageSize`、`pageNum` | uni-app 先查类型计数，再查明细 |
| 搜索历史 | `CarInfoViewModel.addSearchHistory(...)`、`getSearchHistory()` | `addHistory` -> `/car/app/history`、`getHistory` -> `/car/app/history` | 搜索词、车牌、用户上下文 | uni-app 保留历史搜索接口 |
| 轨迹回放 | `CarInfoViewModel.getTrackInfo(carId, endtime, starttime)` | `getTrackInfoApi` -> `/aggregation/app/position/track` | `carId`、`starttime`、`endtime` | 轨迹点查询 |
| 轨迹分享 | `shareTrack(request)` / `shareTrackV2(request)` | `getShareToken` -> `/aggregation/app/position/shareTrack`、`getShareTokenV2` -> `/aggregation/app/position/shareTrack/V2` | `carId`、时间范围 | 长时长走 V2 |
| 位置分享 | `shareLastPosition(carId)` | `sharePosition` -> `/aggregation/app/position/shareLastPosition`、`sharePositionV2` -> `/aggregation/app/position/shareLastPosition/V2` | `carId` | 当前位置分享 |
| 文本下发 | `CarInfoViewModel.sendContent(carId, content)` | `sendcontent` -> `/jt808/app/jt808/sendcontent` | `carId`、`content` | 指令下发 |
| 拍照指令 | `CarInfoViewModel.takePhoto(carId)` | `takePhoto` -> `/jt808/app/jt808/photos` | `carId` | 抓拍 |
| 实时视频能力查询 | `CarInfoViewModel.getVideoInfo(carId)` | `videoInfoBycarId` -> `/aggregation/app/video/videonew`、旧接口 `getVideoApi` -> `/api/user/video` | `carId` | 返回通道、sim、在线状态、播放参数 |
| 实时视频页 | `RealTimeMonitorActivity` 内调用 `getVideoInfo()` 并基于 WebView/播放器播放 | `pages/v2/video/liveBroadcast/liveBroadcast` 调 `videoInfoBycarId` | `carid` | 新版实时视频主链 |
| 视频回放能力查询 | `VideoPlaybackActivity.getVideoInfo(carId)` | `pages/v2/video/videoPlayback/videoPlayback` 调 `videoInfoBycarId` | `carid` | 先拿视频能力和通道，再查录像 |
| 视频回放文件查询 | Android 页面通过本地 `rtvsVideoBack.html` JS 回调生成列表 | `videoplaylist` -> `/api/user/videoplaylist`、`historyplay` -> `/api/user/historyplay`、`playcontrol` -> `/api/user/playcontrol` | `sim`、`channel`、`startTime`、`endTime`、码流/存储/资源类型 | uni-app 暴露了更完整的回放接口集；Android 当前更多走内嵌网页桥 |
| 视频文件列表页 | `VideoListActivity` 接收 `KEY_VIDEO_LIST` 后本地播放历史视频 | `pages/v2/video/videoList/videoList` 读取缓存 `videoList`、`videoSim`，调用隐藏 WebView 获取播放地址 | `carnum`、`videoList`、`videoSim` | 两边都通过隐藏 WebView/JSBridge 拿最终 URL |
| 全屏视频播放 | `VideoFullActivity` 直接播放 `videoUrl` | `pages/v2/video/videoFull/videoFull` 读取缓存 `videoUrl` | `videoUrl` | 终态播放页 |
| 报表首页 | `ReportViewModel` 根据不同 Tab 调不同方法 | `pages/report/index` 调 `getMileage`、`getWarn`、`getSafety`、`getPhotos`、`getCaroverlist`、`stopCarList`、`addOil`、`dayOil`、`leakOil`、`getOffLine` | `page`、`searchText`、`timetype`、`type` | uni-app 用 `type=currentNavIndex+1` 切换报表类型 |
| 报警报表详情 | `ReportViewModel.getWarningDetail(...)` / `AlarmViewModel.getAlarmDetailsList(...)` | `getWarnDetail` -> `/aggregation/app/work/warningDetail` | `page`、`timetype`、`type` | 紧急报警/报警明细 |
| 离线详情 | `ReportViewModel.getOfflineDetailReport(...)` | `getOffLineDetail` -> `/aggregation/app/work/offlineDetail` | `carId`、`start`、`end` | 明细日期列表 |
| 设备状态总览 | `CarInfoViewModel.getCarStatusList()` | `getStatusList` -> `/aggregation/app/carStatus/statusList` | 无 | 行驶/静止/离线/超速/疲劳/到期数量 |
| 状态车辆列表 | `CarInfoViewModel.getCarStatusByType(carType, pageNum, pageSize)` | `getStatusListByType` -> `/aggregation/app/carStatus/listByType` | `carType`、`pageNum`、`pageSize` | 分类车辆明细 |
| 到期车辆列表 | `CarInfoViewModel.getOutdate(expired, pageNum, pageSize)` | `getOutdate` -> `/aggregation/app/carStatus/listByExpired` | `expired`、`pageNum`、`pageSize` | 即将到期/已到期 |
| 设备报警列表 | `DeviceAlarmActivity` 侧对应报警列表请求 | `getWarningList` / `getWarningListByParms` -> `/aggregation/app/work/warningList`、`getWarningType` -> `/system/app/dict/data/list` | `start`、`end`、`pageNum`、`pageSize`、`warningType/searchType` | 报警页筛选条件较多，uni-app 主要在页面层组参 |

## 缓存键映射

> 这一节只补两边都已在代码里确认过、且确实参与页面流转恢复的缓存键/全局状态。纯局部变量不列。

| 场景 | Android 缓存/状态 | ytcar-app 缓存/状态 | 备注 |
|---|---|---|---|
| 登录态（监控端） | `SPUtils.saveToken(...)`、`MyApp.isLogin`、`MyApp.userInfo` | `token`、`loginState`、`userState` | Android 偏 `SPUtils + MyApp` 双态；uni-app 以 `storage` 为主 |
| 登录态（培训端） | `SPUtils.saveTrainingToken(...)`、`SPUtils.saveTrainingLoginUser(...)`、`MyApp.isTrainingLogin`、`MyApp.trainingUserInfo` | `trainToken`、`trainLogin` | 两边都区分培训登录态；uni-app 额外通过 `requestType=train` 切请求头 |
| 全局请求环境 | `SPUtils.save("requestType", "train")`、`MyApp.isLYBH` | `requestType`、`baseURL`、`testCheck` | uni-app 还把域名和认证状态一并放进缓存 |
| 用户/企业/车辆基础信息 | `userInfo`、`companyInfo`、`carInfo` | `userInfo`、`companyInfo`、`carInfo` | 命名基本完全一致 |
| 协议/隐私确认 | `savePolicyAccepted(true)` | `yinsiState` | 键名不同，但职责一致，都是首登协议确认 |
| 账号记忆 | Android 未见同等缓存链 | `username`、`password`、`rememberPassword` | `ytcar-app` 独有“记住密码”实现 |
| 公告详情与签字 | `noticeInfo`、`noticeId`、`noticeSign` | `noticeInfo`、`noticeId`、`noticeSign` | 一一对应，详情页和签字回跳都依赖这组键 |
| 培训课程当前项 | `item`、`id`、`tempTrainItemId`、`tempTrainItemName` | `item`、`id` | uni-app 当前主要保留 `item/id`，Android 多了临时课程标识 |
| 日常培训签字链 | `needSign`、`dailyName`、`dailyId`、`dailySign`、`pageScoll`、`course_scroll_$courseId` | `needSign`、`dailyName`、`dailyId`、`dailySign`、`pageScoll`、`watchTime` | 两边都靠缓存恢复签字/学习进度；uni-app 还记录播放时长 |
| 岗前培训签字链 | `needBeforeSign`、`beforeName`、`beforeId`、`beforeSign`、`beforeExamsId` | `needBeforeSign`、`beforeName`、`beforeId`、`beforeSign`、`beforeExamsId` | 主链一致；但 uni-app 部分页面仍读取 `id`，存在键名混用风险 |
| 培训首页恢复状态 | Android 未见统一首页缓存键 | `activeIndex`、`activeTab`、`avatar` | `ytcar-app` 独有，用于培训聚合页和实名/头像流程恢复 |
| 支付流程 | `payInfo` | `payInfo` | 年费/培训支付上下文对象，两边都走本地缓存兜底 |
| 行车日志 | `draft`、`lastRecord`、`KEY_SELECTED_CAR_NUM` | `draft`、`lastRecord` | 主体草稿键一致；Android 选车额外有常量键，uni-app 更多通过回跳 query |
| 车辆安全检查 | `carCheckRecord` | `carCheckRecord` | 首页记录恢复一一对应 |
| 隐患排查主表单 | `hisItem`、`pageTitle`、`inputForm`、`tempSave` | `hisItem`、`pageTitle`、`inputForm`、`tempSave` | 这条链两边最接近，都是“主页选记录 -> 详情页补录 -> 草稿暂存” |
| 隐患排查图片与签字 | `photos`、`dirversign_img`、`checksign_img` | `photos`、`dirversign_img`、`checksign_img` | 命名完全一致；签字页回跳后直接写回缓存 |
| 查车跨页选择态 | Android 主要走 `Intent extra` / `ActivityResult` | `getApp().globalData.searchId/searchName/searchType` | uni-app 更依赖全局变量回填地图、树、视频列表等入口 |
| 地图统计与 marker 缓存 | Android 地图页以内存态为主 | `allCarsNum`、`mapMarkers`、`mapMarkersId`、`myLocation`、`${carId}` | uni-app 为减少重复请求，额外做了本地点位缓存 |
| 视频流程 | Android 主要走 `Intent extra`，少量内存态 | `videoToken`、`videoList`、`videoSim`、`videoUrl` | uni-app 的视频链明显更依赖缓存中转；另有个别页面出现 `videToken` 拼写 |
| 报表页恢复状态 | Android 未见对应本地键 | `currentNavIndex`、`type` | `ytcar-app` 用于从首页/统计入口回到指定报表 Tab |

## 测试 / 试用 / 体验账号逻辑

| 场景 | Android 实现 | ytcar-app 实现 | 备注 |
|---|---|---|---|
| 监控端体验入口 | 查车首页 `CarFragment` 未登录时填充虚拟车辆 `临Y88888` / `88888`，显示 `全部1辆车`、地图 marker 和车牌标签；点击 marker/标签打开本地虚拟车辆详情 | `pages/v2/videoLogin/videoLogin.vue` 在 `userType !== 2` 时展示“体验账号”；`goSuffer()` 清空 `videoToken` 后返回 `pages/v2/tabOne/tabOne`，随后 `pages/map/mapHome.nvue` 的 `loginIf()` 使用 `virtual.lastPosition` 展示 `临Y88888` | 体验入口真正落点是查车首页未登录虚拟车，不走登录接口 |
| 已登录监控账号退出后体验 | `LoginActivity` 已登录态显示用户信息和“退出登录”；退出走服务端登出并清空本地登录态，回到查车首页后由未登录虚拟车逻辑接管 | `pages/v2/videoLogin/videoLogin.vue` 已登录态显示用户信息和“退出登录”；`exitLogin()` 直接复用 `goSuffer()` 清空 `videoToken` 并返回 | uni-app 的退出更轻量，Android 保留服务端登出 |
| 培训测试账号 | `FaceCheckActivity.checkTestAccount()` 从 `SPUtils.get("userInfo")` 解析 `UserInfoDetail.username`，命中 `safe` 或 `cece` 时展示测试账号快捷入口 | `pages/train/faceCheck/index.nvue` 从 `uni.getStorageSync('userInfo')` 解析 `username`，命中 `safe` 或 `cece` 时 `issafe=true` | 这是明确写死的测试账号名单 |
| 测试账号跳过人脸拍照 | `activity_face_check.xml` 的 `llTestEntry` 提供“去学习继续教育 / 去学习日常培训 / 岗前”三入口；`FaceCheckActivity.initTestEntryClick()` 分别跳 `TrainCourseListActivity`、`DailyTrainListActivity`、`TrainCourseListActivity(type=before)` | `pages/train/faceCheck/index.nvue` 在 `issafe` 时展示三个 `navigator`，分别跳 `pages/train/trainList/subject`、`daily`、`before` | 仅绕过拍照核验入口，后续学习页仍按各自培训类型进入 |
| 培训登录页体验入口 | Android 无；培训登录由 `LoginActivity.LOGIN_TYPE_TRAINING` 强制登录 | `pages/v2/videoLogin/videoLogin.vue` 中 `userType === 2` 时隐藏“体验账号” | 两端一致：培训端没有体验账号入口 |

## ytcar-app 侧暂未在 Android 找到直接对应、且确认有用户入口的页面

| ytcar-app 页面 | 状态 | 备注 |
|---|---|---|
| `pages/train/home/index` | ytcar-app 独有 | 已确认由 `pages/train/index` 多处 `navigateTo` 进入，作为培训二级聚合页；Android 当前更多直接进 `TrainListActivity` |
| `pages/v2/videoLogin/videoLogin` | ytcar-app 独有 | 已确认可由 `pages/train/index`、`pages/map/mapHome.nvue`、`pages/home/index` 进入；Android 当前由原生视频流程承接 |
| `pages/map/index`、`pages/map/map` | 部分重合 | 已确认 `search-car/index.vue`、`home/index.vue` 可跳转进入；Android 虽有地图能力，但未拆成这两个 uni-app 页面形态 |

## 已排除的 ytcar-app 页面

> 下面这些页面在 `ytcar-app` 虽然存在，但当前未确认有正常用户入口，或者只出现在旧逻辑/注释代码中，因此不再列入“独有页面”清单。

- `pages/home/index`
- `pages/message-page/index`
- `pages/news-setting/index`
- `pages/operation/index` 及 `pages/operation/**`
- `pages/bill-search/**`
- `pages/v2/tabTwo/tabTwo`
- `pages/popup/index`
- `pages/user/testUser.vue`

## 后续校准建议

1. 继续把“页面参数映射”从高频链路扩到查车、报表、视频、考试全链路。
2. 给每个页面补接口映射：接口名、请求参数、关键字段、缓存键（如 `SPUtils` / `uni.setStorageSync`）；这一轮已补到高频缓存键，但还没覆盖登录边角、地图搜索历史、考试答卷草稿。
3. 对 `部分对应` 和 `待确认` 项补“页面流转图”，尤其是签字、支付、学习记录、扫码。
4. 单独整理“通用能力映射”：WebView、签字板、拍照上传、视频播放、微信/支付宝支付、扫码。
