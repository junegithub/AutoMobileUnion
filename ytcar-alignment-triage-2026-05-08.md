# 反馈问题 ytcar-app 对齐归因矩阵

整理时间：2026-05-08

原始表格：`/Users/june/Downloads/bugreport/易车联APP反馈收集表.xlsx`

参考目录：`/Users/source/ytcar-app`（用户写的 `ytcat-app` 按实际兄弟目录 `ytcar-app` 处理）

Android 目录：`/Users/source/AutoMobileUnion`

## 归因口径

- `Android 偏离`：`ytcar-app` 有明确逻辑，Android 当前实现不同或历史反馈指向原生迁移差异，优先改 Android。
- `两端同源风险`：`ytcar-app` 本身也能看到类似风险、缺少兜底或逻辑与反馈现象一致，不能只按 Android bug 处理。
- `待数据确认`：两端逻辑都需要真实账号、接口响应、视频/图片附件或后端数据才能定责。
- `服务端/接口优先`：客户端参数大体一致，需先看接口返回、后端状态或第三方能力。

## 关键对齐结论

- 地图/搜索：`ytcar-app/pages/map/mapHome.nvue` 对历史/常搜车辆会按 `car_id/carnum` 拉详情、补 marker、过期车 toast；Android 已有类似补点与空 `carId` 防护，但缩放、刷新与详情 loading 仍需用视频复现确认。
- 轨迹：`ytcar-app/pages/trajectory/index.nvue` 使用 `direction - 90`、停车点开关、`分享时间`、播放速度分离；Android `TrackPlayActivity.kt` 当前也已有这些对齐痕迹。剩余更像真机表现/布局/数据偏差复测项。
- 401：`ytcar-app/config/common.js` 统一 toast “登录状态已过期...”；Android `BaseViewModel.kt` 已区分培训 token 并清登录态，但具体页面是否重复弹登录框仍需看调用链。
- 考试题库：`ytcar-app/testBase.vue` 动态遍历 `category_list` key；Android `ExamPracticeActivity.kt` 也改成 `Map<String, ...>` 遍历。若仍空白/闪退，优先查接口返回和 Android 类型转换边界。
- 责任书/承诺书：`ytcar-app` 与 Android 都存在地区规则需要复核的问题。`ytcar-app` 注释了“山东、安徽取消承诺书”的提前 return；Android 虽隐藏承诺书入口，但自动检查链路仍可能继续进入承诺书判断。

## 逐项归因矩阵

| ID | 问题简述 | ytcar-app 对应逻辑 | Android 初步状态 | 初步归因 |
|---:|---|---|---|---|
| 1 | 定位车辆刷新后地图缩放回原始等级 | `mapHome.nvue` 选车会取当前 scale 但随后设 16；定时 `carInfoUpdateBycarId` 只移动 marker | Android 有 `refreshSelectedMarkerAfterMapReload()`，但 `showSingleMarker()` 固定 zoom 15 | 待视频确认；若是定时刷新触发，偏 Android；若选车即重置，两端都有类似策略 |
| 2 | 轨迹卡顿、车头方向错 | `trajectory/index.nvue` 使用 `direction - 90`，移动 marker 不重绘整线 | Android 已有 `toMapMarkerAngle(direction - 90)`，且注释说明去掉 tick 重设 polyline | 方向若仍错属数据/坐标系待确认；卡顿偏 Android 性能复测 |
| 3 | 停车点标识/偏差/图标小 | `trajectory/index.nvue` 停车点开关、24 图标、callout 明细 | Android 有 24 停车点、起终点 30、停车点显隐 | 待数据确认；更可能是坐标/锚点/图标资源差异 |
| 4 | 实时视频/回放加载失败 | `liveBroadcast`/`videoPlayback` 走 uni 页面与 H5/视频参数 | Android 原生 WebView/JS bridge 承接，平台差异大 | Android 偏离或设备数据问题；先抓 WebView url、bridge、通道响应 |
| 5 | 历史车辆查询一直 loading，刷新后车标消失 | `mapHome.nvue` 历史车辆会补 marker、请求详情，失败路径不完全清晰 | Android 有空详情关闭 loading、补点逻辑 | 待附件复现；Android 刷新 marker 列表与选中态优先检查 |
| 6 | 任意页面点查车未回首页 | `ytcar-app` tab/switchTab 自然回查车页 | Android 通过 `MainActivity.EXTRA_SELECTED_TAB` 实现 | 若仍复现为 Android 入口遗漏 |
| 7 | 车辆搜索提示冲突 | `realtime-search/index.vue` 车牌搜索与组织树提示分支分离 | Android `TreeListActivity` 同时查 tree/search，已有空 `carId` 防护 | Android 偏离或交互提示遗漏 |
| 8 | 培训 tab 不应立即登录提示，点击学习再提示 | `train/index.vue` 页面进入先判断 token/签署状态，点击入口也会校验 | Android `TrainingFragment` 当前错误时会弹培训登录框 | 两端逻辑不完全一致；按产品要求应改 Android 交互，ytcar 也需确认是否页签进入即跳登录 |
| 9 | 底部常搜车辆详情加载不出 | `mapHome.nvue` `getInfoByCarNum` 通过车牌查详情并补 marker | Android 有 label/history fallback 和 `EVENT_LABEL_DETAIL` 通过车牌查实时地址 | 待数据确认；若接口空返回属服务端/数据，否则 Android loading/补点 |
| 10 | 安全培训首页 qyh 报错 | `train/home/index.vue` 按用户扩展信息、培训类型加载不同列表 | Android `TrainingFragment/TrainListActivity` 已有账号类型判断痕迹 | 待账号 qyh 接口确认；可能 Android 时序问题 |
| 11 | 安全会议签名失败 | `meeting/index.vue` 上传后 `singpost({signfile,url,id,type:'0'})` | Android `MeetingDetailActivity` 上传后 `singPost`，会刷新详情 | 待接口返回确认；优先比对上传 URL 是否完整 |
| 12 | 从业资格考试报错/闪退 | `testBase.vue` 动态遍历 `category_list` key | Android 已用 `Map<String, QuestionCategoryDetail>` | 若仍闪退，Android 类型转换/空值处理优先 |
| 13 | 培训三个页面布局问题 | `train/index.vue` 为 uni 布局 | Android 原生 fragment/list/card 布局 | Android UI 偏离 |
| 14 | 运营分析图表重叠 | `analysisChart` 用前端图表 | Android `OperationAnalysisActivity` 使用原生图表/图例 | Android UI 偏离；若数据项过多，两端都有展示压力 |
| 15 | 轨迹车辆图标需一致 | `trajectory/index.nvue` 用车辆类型图标并旋转 | Android `VehicleImageProvider` 有对齐说明 | Android 资源/尺寸复核 |
| 16 | 401 友好提示 | `config/common.js` 清缓存并 toast 指定文案 | `BaseViewModel` 处理 401，但页面也可能再弹登录框 | Android 页面级体验需复核；基础逻辑已接近 |
| 17 | 责任书弹出/回显/按钮错位 | `driverBook/index.vue` 签署状态、涡阳规则、签名上传 | Android `DriverBookActivity` 原生签名和回显 | Android UI/回显偏离；规则需继续与接口确认 |
| 18 | 人脸识别文字不全/图片参数异常 | `faceCheck/index.nvue` live-pusher 截图上传，按 type 调不同接口 | Android CameraX/上传链路完全不同 | Android 偏离或第三方/接口参数问题 |
| 19 | 安全会议拍照后空白 | `faceCheck` meeting 分支 `singpost(type)` 后返回/跳转 | Android `MeetingDetailActivity` 图片上传后刷新详情 | Android 页面状态刷新优先查；也需看接口返回 |
| 20 | 学习证明内容显示不全 | `prove/index.vue` canvas 固定绘制，本身有长文本风险 | Android 原生证书/列表也有长文本风险 | 两端同源 UI 风险，Android 需按真实长字段修 |
| 21 | 全部车辆选择闪退/报错 | `realtime-search` 直接 `goAbout(item.carId,item.carNum)`，对异常 carId 防护有限 | Android `switchMapDetail()` 已拦空/0 carId | 若仍报错，可能 Android 其他入口；ytcar 也有异常数据风险 |
| 22 | 年度支付报错 | `pay/index.vue/payOrder.vue/daily.vue` 使用 `payInfo.id` 作为 `year_id` | Android 支付页需使用 `year.id` | Android 偏离优先；若接口拒绝则服务端/支付配置 |
| 23 | 修改昵称失败 | `edit-userinfo` 走 `system/app/user/profile?nickname` | Android 已有同接口对齐记录 | 待接口返回确认；可能服务端权限/参数 |
| 24 | 选择过期车辆提示错误 | `mapHome.nvue` 过期车仍显示详情，并 toast `已过期 + deptName` | Android 应打开详情并友好提示，不应报错 | Android 偏离或异常数据 |
| 25 | 行车日志检查项提示异常 | `driveStage.vue` 保留选中项，提示分支较弱 | Android 表单原生实现 | Android 偏离；按 ytcar 提交值和提示分支修 |
| 26 | 行车日志签名/部分信息未提交 | `driveStage.vue` 提交 `dsingimg/ysingimg/gettime` 等 | Android `DriveLogStageActivity` 已补双签与字段 | 待后台实际记录确认；优先查请求体 |
| 27 | 行车日志键盘遮挡 | uni 页面天然 resize/scroll | Android manifest 已有 `adjustResize` | 若仍复现，Android 布局滚动容器问题 |
| 28 | 安全检查车牌回填失败 | ytcar 选车回跳主要带 `carNum` | Android 选车可能需要兼容 `carNum/carid` | Android 偏离 |
| 29 | 安全检查图片上传失败 | ytcar 直接 `uni.uploadFile` 到 `/api/user/newuplode`，拼完整 URL | Android 使用 multipart + 上传 URL | Android 参数/URL 拼接优先；接口也需看返回 |
| 30 | 证书红章错位 | `prove/index.vue` canvas 固定坐标，本身有错位风险 | Android 原生/图片绘制也有长字段错位风险 | 两端同源 UI 风险，按真实截图修 Android |
| 31 | 隐患上传照片/签名按钮颜色 | `dangerCheck/detail/uploadPhotos` 依赖缓存和 `fileimg` 拼接 | Android 原生缓存/上传流程不同 | Android 偏离优先 |
| 32 | 未学习完参加考试提示错误 | `train/home/index.vue` 考试入口主要看 `training_exams_id`，未见本地进度硬拦 | Android 若有本地进度硬拦则偏离 | Android 偏离 |
| 33 | 日常安全需支付却直接扫脸 | `home/index.vue` 先 `orderisPay`，成功进入支付/企业付/再扫脸 | Android `TrainListActivity` 已有“orderisPay 成功即进支付页”注释 | 若仍复现，Android 分支遗漏或账号接口返回需查 |
| 34 | 岗前培训学完仍无法考试 | ytcar 通过 before 流程、人脸结束后按 `training_exams_id` 跳考试 | Android 多页面传参，易丢 `type/id` | Android 偏离或接口返回待确认 |
| 35 | 两类人员真题无法做题 | ytcar `from=twoList` 走 `payIfTwo` 和不同答题接口 | Android 有 twoList 分支和动态分类 | 待账号/接口确认；Android 类型/参数优先复核 |
| 36 | 真题空白后返回主页 | ytcar `testBase.vue` 空列表显示暂无，不应空白一分多钟 | Android 空态/接口异常处理优先 | Android 偏离或接口超时 |
| 37 | 报表加载更多找不到上一页 | ytcar `report/index` 按 page/total 追加 | Android `ReportActivity` 现在把 `total` 当总页数处理 | Android 需确认接口 `total` 含义；可能已修也可能仍错 |
| 38 | 轨迹 tab 排版/播放控件/分享时间 | ytcar 已显示“分享时间”，但模板 inline 布局也有窄屏挤压风险 | Android XML 已是“分享时间”，控件逻辑已有暂停/恢复 | 两端 UI 都需窄屏复测；Android 优先修反馈机型 |
| 39 | 过期车辆应有提示信息 | ytcar 过期车 toast | Android 需同样 toast/弹层 | Android 偏离 |
| 40 | 查车登录页缺体验账号 | `videoLogin.vue` 非培训 userType 显示“体验账号” | Android 普通登录/未登录虚拟车逻辑替代，但登录页按钮可能缺失 | 产品差异；若要求登录页按钮，Android 偏离 |
| 41 | 搜车牌不定位/组织树闪退白屏 | ytcar 搜索后补点、定位、过期提示；异常 carId 防护有限 | Android 已通过 EventBus 跳回地图并查详情，但依赖 marker/请求时序 | Android 高优先级复核；ytcar 也有异常数据风险 |
| 42 | 报警日期不清空，公司名覆盖车牌 | ytcar warningList 页内维护日期/筛选和列表布局 | Android `AlarmAdapter`/`WarningDetailAdapter` 有车牌+部门拼接 | Android UI 偏离；日期清空逻辑需对齐 ytcar |
| 43 | 责任书/承诺书地区与自然年规则 | ytcar 当前注释掉山东/安徽跳过承诺书，涡阳不每年签 | Android 隐藏入口但自动检查链路仍可能跑承诺书 | 两端同源规则风险；先按最新产品规则统一两端口径，Android 实现再落地 |

## 建议执行顺序

1. 先用附件视频复现 P0/P1，并为每条记录保存 Android 日志、请求参数、响应体。
2. 对 `Android 偏离` 项直接修 Android，不再等待 ytcar。
3. 对 `两端同源风险` 项先明确产品规则或后端数据口径，避免 Android 修成和当前 `ytcar-app` 不一致但仍不符合业务。
4. 对 `待数据确认` 项先补日志和账号复测结论，再进入代码修复。

## 2026-05-08 修复落点

- 已处理 `#8`：培训页签进入不再因签署状态接口失败主动弹登录框，登录提示收敛到功能入口点击。
- 已处理 `#24/#39`：过期车辆详情保留并增加过期提示。
- 已处理 `#25`：行车日志检查项单选不再弹选中数量提示。
- 已处理 `#18` 的静态 UI 部分：人脸识别“翻转摄像头”文字不全。
- 已处理 `#42`：报警日期可清空，车牌号/公司名称布局避免覆盖。
- 已处理 `#43` Android 自动检查链路：山东、安徽跳过承诺书检查。
- 已处理 `#40`：普通查车登录页增加体验账号入口，并按 ytcar-app 逻辑清空查车登录态后回查车。
- 已处理 `#8` 补充项：培训登录成功后回培训页签，不再默认回查车。
- 已处理 `#12/#35/#36` Android 类型转换风险：题库分类 id 与考试 id 保持字符串传递，避免答题页取不到参数。
- 已处理 `#12/#35/#36` Android 答题入口保护：答题页刷新/开始答题前校验题库 id 与角色 id，异常参数不再触发 `toInt()` 闪退；普通角色解析过滤非法 `stype`，两类人员默认角色显式使用接口请求 id。
- 已处理 `#4` 回放保护项：视频回放查询增加通道/SIM/时间校验和 WebView 响应超时提示；实时视频仍需真机和设备数据继续验证。
- 已处理 `#6`：底部“查车”点击/重选触发查车首页复位，关闭详情并显示全部车辆。
- 已处理 `#41` 部分 Android 偏离：组织树车辆节点优先用 `realId`，id 异常时按车牌兜底查实时详情，减少白屏/错误提示。
- 已处理 `#37` Android 列表展示风险：报表追加页的 diff key 增加时间戳，避免同车多条报表互相覆盖。
- 已处理 `#11/#19` Android 接口偏离：会议签名/拍照提交改用 `api/training/singpost`，拍照上传成功后等服务端提交成功再刷新会议详情。
- 已处理 `#33` Android 接口偏离：日常培训支付检查改用 `api/dailysafety/orderisPay`，日常下单改为 GET QueryMap，保留服务端返回的金额和 `usualpaytype`。
- 已处理 `#28/#29` Android 偏离：安全检查选车回填兼容 `carNum/carnum`，图片和签名上传 URL 统一归一化后写入表单。
- 已处理 `#31` Android 偏离：隐患排查 12 图上传、通用签名上传 URL 统一归一化；“去签名”按钮文字颜色调整为蓝色。
- 已处理 `#26` 部分 Android 偏离：行车日志签名上传 URL 统一归一化，首步表单校验不再强依赖 `car_id`。
- 已处理 `#22` Android 偏离：年度 APP 支付参数统一为 `year_id/type/method`，个人中心和支付详情页共用策略，微信年度支付不再额外请求并提交登录 `code`。
- 已处理 `#27` Android UI 偏离：行车日志输入框聚焦后主动滚动到键盘上方安全位置，避免底部固定按钮和软键盘遮挡当前输入项；仍建议真机回归多屏幕尺寸。
- 已处理 `#4` 实时视频 Android 偏离：实时视频 WebView 回调按 `action` 字段解析最终播放地址，避免 JSON 消息直接传给播放器；回放列表播放地址解析同步复用该策略。
- 已处理 `#10` Android 解析边界：`userotherinfo` 用户扩展信息兼容空字符串时间/数字字段，避免 qyh 等账号进入安全培训首页时因 Gson 类型转换失败报错。
- 已处理 `#34` Android 岗前流程偏离：岗前课程完成上报对齐 `beforestudy`，保存返回的 `training_exams_id`，结束人脸/岗前签字后按有效考试 id 和岗前培训 id 进入考试，避免 start 人脸误用旧考试 id。
