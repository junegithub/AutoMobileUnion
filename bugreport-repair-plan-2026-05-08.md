# 易车联 APP 反馈问题列表与修复计划

整理时间：2026-05-08 14:52 CST

原始表格：`/Users/june/Downloads/bugreport/易车联APP反馈收集表.xlsx`

对齐参考：`/Users/source/ytcar-app`（用户描述里的 `ytcat-app` 按实际兄弟目录 `ytcar-app` 处理）

归因矩阵：`ytcar-alignment-triage-2026-05-08.md`

## 处理口径

- 表格有效反馈共 43 条，来自 `反馈收集表` sheet。
- 优先处理“二次测试结果”为“没解决 / 闪退 / 显示错误 / 自动刷新异常”的项。
- 修复时以当前 Android 原生实现为落点，以 `ytcar-app` 对应页面的接口参数、跳转、缓存、提示文案、空态/异常态为逻辑参考。
- 所有问题先按 `ytcar-alignment-triage-2026-05-08.md` 判断：`Android 偏离` 直接进 Android 修复；`两端同源风险` 先确认产品/接口口径；`待数据确认` 先补日志和账号复现；`服务端/接口优先` 先保留请求响应证据。
- 已存在的映射文档：`yrcar-page-mapping.md`；已存在的历史整理：`feedback-readable-summary-2026-04-23.md`。本文件是按本次下载目录反馈表重新整理的执行计划。

## 模块汇总

| 模块 | 数量 | 重点问题 |
|---|---:|---|
| 查车/地图/车辆搜索 | 9 | 地图缩放保持、历史/常搜车辆详情、底部查车 tab 回首页、搜索冲突、过期车辆提示、组织树/车牌选择异常 |
| 轨迹回放 | 4 | 回放卡顿、车头方向、停车点偏差、图标尺寸、播放控件和文案 |
| 视频 | 2 | 实时视频/回放加载失败、查车登录页体验账号 |
| 培训/考试/会议/证书/支付/用户 | 19 | 登录态提示、培训首页报错、会议签名/拍照、人脸识别、证明布局、支付、题库/考试、责任书/承诺书 |
| 行车日志/安全检查/隐患排查 | 6 | 检查项提示、签名提交、键盘遮挡、车牌回填、图片上传、隐患签名样式 |
| 报表/报警/运营分析 | 3 | 运营图表重叠、报表分页、报警筛选 UI |

## 问题列表

| ID | 模块 | 原反馈 | 页面 | 二测/备注 | 优先级 |
|---:|---|---|---|---|---|
| 1 | 查车地图 | 定位某一辆车，页面刷新后，地图缩放到原始等级 | `pages/map/mapHome.nvue` | 放大后刷新不应改变缩放级别 | P1 |
| 2 | 轨迹回放 | 回放卡顿，车头方向有误 | `pages/trajectory/index` | 车头方向有误 | P1 |
| 3 | 轨迹回放 | 停车点开关状态加标识，停车点位显示有偏差，起点/终点/停车点图标偏小 | `pages/trajectory/index.vue` | 停车点标识和偏差没解决 | P1 |
| 4 | 视频 | 实时视频、视频回放加载失败 | `pages/v2/video/videoPlayback/videoPlayback`、`pages/v2/video/liveBroadcast/liveBroadcast` | 没解决 | P1 |
| 5 | 查车地图 | 历史车辆查询一直加载 | `pages/map/mapHome.nvue` | 自动刷新后车标消失 | P1 |
| 6 | 查车入口 | 任意页面点“查车”需回首页，当前未切换页面 | `pages/map/mapHome.nvue` | 没解决 | P1 |
| 7 | 车辆搜索 | 车辆搜索提示冲突 | `pages/realtime-search/index` | 没解决 | P1 |
| 8 | 培训入口 | 切换培训页签先不提示登录，点击学习选项后再提示 | `pages/train/index` | 登录后切到查车页签 | P1 |
| 9 | 查车地图 | 点击底部常搜车辆，车辆详情加载不出来 | `pages/map/mapHome.nvue` | 表格未填二测 | P1 |
| 10 | 培训首页 | 安全培训首页报错，账号 qyh | `pages/train/index` | 没解决 | P1 |
| 11 | 安全会议 | 安全会议签名失败 | `pages/train/meeting/index` | 没解决 | P1 |
| 12 | 考试 | 从业资格考试报错 | `pages/train/exam/testBase/testBase` | 闪退 | P0 |
| 13 | 培训首页 | 三个页面闪退、布局问题 | `pages/train/index` | 闪退解决，布局没解决 | P1 |
| 14 | 运营分析 | 图表数据重叠 | `pages/v2/analysisChart/analysisChart` | 没解决 | P2 |
| 15 | 轨迹回放 | 车辆图标需与精致箭头图标一致 | `pages/trajectory/index.vue` | 表格未填二测 | P2 |
| 16 | 登录态 | 账号在别的设备登录，所有 401 页面需友好提示重新登录 | `pages/train/index` 等 | 需提示“登录状态已过期...” | P0 |
| 17 | 责任书 | 打开培训页未弹责任书签字页；签后不显示；重置/保存按钮错位 | `pages/train/driverBook/index` | 签字回显特别小 | P1 |
| 18 | 人脸识别 | “翻转摄像头”文字不全，拍照后找不到人脸或图片参数异常 | `pages/train/faceCheck/index` | 没解决 | P1 |
| 19 | 安全会议 | 拍照后页面空白 | `pages/train/meeting/index` | 没解决 | P1 |
| 20 | 学习证明 | 部分内容显示不全 | `pages/train/prove/index` | 列表按钮/时间遮挡；详情下载按钮显示不全 | P2 |
| 21 | 车辆搜索 | 全部车辆选择车辆闪退或报错 | `pages/realtime-search/index` | 没解决报错 | P0 |
| 22 | 支付 | 年度支付报错 | `pages/train/pay/index` | 没解决 | P1 |
| 23 | 用户资料 | 修改昵称失败 | `pages/edit-userinfo/index` | 没解决 | P2 |
| 24 | 过期车辆 | 选择过期车辆提示错误 | `pages/map/mapHome.nvue` | 没解决 | P1 |
| 25 | 行车日志 | 选择行车前/中/后检查项目提示异常 | `pages/driveCompany/driveDaily/driveStage/driveStage` | 建议不弹提示选中项 | P2 |
| 26 | 行车日志 | 提交后后台看不到驾驶员签名，部分信息没提交，前后端显示不匹配 | `pages/driveCompany/driveDaily/driveStage/driveStage` | 没解决 | P1 |
| 27 | 行车日志 | 输入内容时键盘遮挡 | `pages/driveCompany/driveDaily/driveStage/driveStage` | 没解决 | P2 |
| 28 | 安全检查 | 车牌搜索选中后未显示到对应位置 | `pages/driveCompany/driveCheck/checkStage/checkStage` | 没解决 | P1 |
| 29 | 安全检查 | 图片两种上传方式都失败 | `pages/driveCompany/driveCheck/checkStage/checkStage` | 没解决 | P1 |
| 30 | 证书 | 安全教育、继续教育证书红章样式错位 | `/pages/train/prove/index` | 没解决 | P2 |
| 31 | 隐患排查 | 上传照片异常；签名按钮文字改颜色 | `pages/driveCompany/dangerCheck/detail/detail` | 没解决 | P1 |
| 32 | 日常培训考试 | 未学习完就参加考试，提示错误 | `pages/train/test/index` | 提示后返回安全培训页 | P2 |
| 33 | 日常培训支付 | 账号 wj 需要个人支付金额，未进入支付页，直接扫脸学习 | `pages/train/faceCheck/index` | 没解决 | P1 |
| 34 | 岗前培训考试 | 培训都学完仍无法考试，网络请求失败 | `pages/train/test/index` | 开始考试按钮跳到培训页 | P1 |
| 35 | 两类人员真题 | 两类人员真题无法做题 | `pages/train/exam/testBase/testBase` | 没解决 | P1 |
| 36 | 两类人员真题 | 真题异常，空白页后返回主页 | `pages/train/exam/testBase/testBase` | 没解决 | P1 |
| 37 | 报表 | 上拉加载更多，找不到上一页数据 | `pages/report/index` | 没解决 | P1 |
| 38 | 轨迹回放 | tab 标签排版；切换播放时长后播放停止；开始/暂停无效；“播放时长”改“分享时间” | `pages/trajectory/index` | tab 排版没解决 | P1 |
| 39 | 过期车辆 | 过期车辆应有提示信息 | `pages/map/mapHome.nvue` | 表格未填二测 | P2 |
| 40 | 视频登录 | 查车登录页面缺少体验账号按钮 | `pages/v2/videoLogin/videoLogin` | 表格未填二测 | P2 |
| 41 | 搜索车辆 | 搜车牌不定位/不显示车牌；组织树点车牌闪退、白屏、提示错误 | `pages/map/mapHome.nvue` | 表格未填二测 | P0 |
| 42 | 报警 | 日期选择没有清空，公司名称覆盖车牌号 | `pages/v2/warningList/warningList` | 表格未填二测 | P2 |
| 43 | 承诺书 | 除山东、安徽地区外显示驾驶员承诺书；首次登录自动签责任书和承诺书；每自然年一签，涡阳县不每年签 | `pages/train/index` | 表格未填二测 | P1 |

## 修复计划

## 本轮已执行修复

- 培训页签登录提示：进入培训页签时，签署状态检查失败不再立刻弹培训登录框；仍在点击具体学习/考试/功能入口时提示登录。
- 责任书/承诺书规则：山东、安徽地区不仅隐藏“驾驶员承诺书”入口，自动签署检查链路也跳过承诺书，避免已签责任书后继续弹承诺书。
- 过期车辆提示：查车详情接口返回 `expired=true` 时保留车辆详情和定位，并提示“车牌已过期 + 机构/联系人”，对齐 `mapHome.nvue`。
- 报警日期筛选：设备报警默认展示“开始日期至结束日期”，请求仍按 `ytcar-app` 默认今天到明天范围；选择日期后展示具体范围，并提供清空日期按钮。
- 报警列表布局：车牌号和公司名改为横向约束，公司名单行省略，避免公司名称覆盖车牌号。
- 人脸识别按钮：加宽“翻转摄像头”按钮并限制单行，避免文字显示不全。
- 行车日志检查项：单个检查项勾选不再弹“已选中 N 项”，保留必要校验和全选提示。
- 查车登录体验账号：普通查车登录页补充“体验账号”入口，对齐 `ytcar-app/pages/v2/videoLogin/videoLogin.vue`，点击后清空查车 token 并回查车 tab。
- 培训登录返回：培训登录成功或返回时带 `MainActivity.TAB_TRAINING`，避免登录后落到查车页签。
- 考试题库参数：题库分类 id、考试 id 全链路按字符串传递，修复 Android `putExtra(Int)` 与答题页 `getStringExtra()` 不匹配导致的空 id/闪退风险。
- 视频回放加载：回放查询前校验通道、SIM、起止时间；SIM 只在缺少前导 0 时补 0；WebView 无响应时 12 秒后提示加载失败，空结果只提示“暂无内容”。
- 查车回首页：底部“查车”点击/重选时关闭车辆详情、清空选中态并恢复全车地图，对齐任意页面回查车首页的产品要求。
- 组织树选车：车辆叶子节点优先使用接口 `realId` 作为车辆 id；id 缺失但车牌存在时按车牌查询实时详情，避免组织树点车牌直接提示异常。
- 报表分页：里程、加油、漏油报表列表项 diff 增加时间戳维度，避免追加页里相同车辆覆盖上一页数据。
- 安全会议签名/拍照：会议签名和拍照提交改走 `api/training/singpost`，对齐 `ytcar-app/pages/train/meeting/index.vue` 和 `faceCheck/index.nvue`；上传 URL 做完整地址归一化，拍照权限只请求相机权限。
- 日常培训支付：日常培训支付检查改走 `api/dailysafety/orderisPay`，支付下单接口改为 GET QueryMap，对齐 `ytcar-app`；修复金额和 `usualpaytype` 丢失导致支付/企业付分支异常。
- 安全检查车牌/图片：安全检查选车回填兼容 `carNum/carnum` 两种返回键；普通图片和签名上传返回路径统一做完整 URL 归一化，避免完整 URL 或前导斜杠导致拼接异常。
- 隐患排查照片/签名：隐患排查 12 图上传和通用签名页统一复用完整 URL 归一化策略，避免上传接口返回完整 URL 或前导斜杠时拼接异常；隐患排查“去签名”按钮文字改为蓝色。
- 行车日志签名/选车：行车日志签名上传 URL 统一归一化；首步校验按 `ytcar-app` 只要求驾驶员、车牌号和货物类型，不再因 `car_id` 缺失阻断手输或回填车牌提交。
- 年度支付：年度 APP 支付请求统一按 `year_id/type/method` 组参，个人中心和支付详情页复用同一策略；微信年度支付不再依赖登录 `code`，避免误入小程序支付分支。
- 行车日志键盘遮挡：行车日志输入框聚焦后延迟滚动到安全位置，滚动容器保留裁剪外空间，减少键盘和底部“下一步”按钮遮挡当前输入项。
- 实时视频加载：实时视频 WebView 回调统一解析 `action` 字段后再交给播放器，避免把 `{"action":"rtmp..."}` 整段 JSON 当作视频地址；视频回放列表复用同一解析策略。
- 培训首页 qyh：`userotherinfo` 用户扩展信息的时间/数字空串字段改为字符串承接，保留 `category_id/yzstatus` 判断，避免 qyh 等账号因接口返回空字符串导致 Gson 解析失败。

### 阶段 1：P0/P1 崩溃、登录态、地图主链路

目标：先消除闪退、空白、无限加载、401 无提示、查车核心流程不可用。

1. 复核全局 401 处理：对齐 `ytcar-app` 训练端 token 失效后的提示文案和留在当前页/重新登录行为；覆盖培训所有接口。
2. 复核车辆搜索与选车：对齐 `pages/realtime-search/index`、`pages/map/mapHome.nvue` 的 `carId/carNum` 兜底、组织树选车、空结果提示分支，避免空 `carId` 进入地图详情。
3. 复核地图刷新：对齐 `mapHome.nvue` 的选中车辆状态，刷新实时位置时保留用户缩放级别和当前选中 marker；历史/常搜入口失败必须关闭 loading。
4. 复核 tab 跳转：底部或任意入口点击“查车”统一回 `MainActivity` 查车 tab，不清空必要地图状态。
5. 复核考试/题库闪退：对齐 `pages/train/exam/testBase/testBase` 的题库分类解析，所有接口空数组、空对象、字符串/数字混合字段都做兼容。

### 阶段 2：轨迹、视频、培训闭环

目标：把高频业务闭环恢复到可回归状态。

1. 轨迹回放：对齐 `pages/trajectory/index` 的 `direction - 90`、停车点坐标/锚点、起终点/停车点尺寸、分享时间与播放速度解耦、完成后可重新播放。
2. 视频：对齐 `pages/v2/video/liveBroadcast/liveBroadcast`、`videoPlayback` 的通道参数、WebView JS bridge 名称、混合内容、媒体自动播放、销毁清理和错误提示。
3. 培训入口：对齐 `pages/train/index` 登录校验时机，培训 tab 可进入，点击学习类入口时再要求培训登录；登录成功后回到原培训入口而不是跳查车。
4. 支付：对齐 `pages/train/pay/index`、`pages/train/pay/daily`、`payOrder` 的 `year_id/id/type/method/usualpaytype` 组参；日常培训先查支付状态，再决定支付或人脸。
5. 责任书/承诺书：按 `ytcar-app` 的签署状态、地区规则和自然年规则实现；签名图片上传后保存完整 URL 并刷新状态。

### 阶段 3：三模块表单与证书/报表 UI

目标：处理提交字段、上传、键盘遮挡和视觉错位。

1. 行车日志：对齐 `driveStage.vue` 的检查项保留、签名上传顺序、`ysingimg/gettime` 等提交字段；输入页设置 `adjustResize` 或滚动定位。
2. 安全检查：对齐 `checkStage.vue` 的车牌回填字段、图片上传字段、完整 URL 拼接和提交时机。
3. 隐患排查：对齐 `detail.vue` 和 `uploadPhotos.vue` 的 12 图缓存、`fileimg` 拼接、签名按钮样式和上传异常提示。
4. 学习证明/证书：对齐 `prove/index` canvas 版式，优先保证列表按钮不遮挡、详情底部按钮完整、红章不压正文。
5. 报表/报警：对齐 `report/index` 分页结束条件；报警页日期清空逻辑、公司名/车牌号布局分栏，避免文本覆盖。

## 验证计划

- 构建验证：`./gradlew :app:compileDebugKotlin`。
- P0/P1 真机回归：查车登录/未登录、车辆搜索、组织树选车、历史/常搜车辆、轨迹回放、考试题库、401 过期。
- 附件回归：逐条播放表格中关联视频，按视频复现路径验收。
- 账号回归：至少覆盖 `qyh`、`wj`、两类人员账号、异地/涡阳县/山东/安徽学员规则账号。
- 接口日志：对所有“没解决”的支付、考试、图片上传、证书问题保留请求参数和服务端响应，区分客户端组参问题与服务端数据问题。
