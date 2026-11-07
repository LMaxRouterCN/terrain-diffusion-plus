# GOAL-PLAN.md — LMaxs Terrain Diffusion fork

> 立项 2026-11-07。本文档随任务推进持续更新。

## 1. 项目定位

fork 自 derekvawdrey/terrain-diffusion-plus（其上游为 xandergos/terrain-diffusion-mc，MIT 许可证）。
起因：max 环境中 TD 主世界不生成远古城市（已影响正常游戏）。分析结论与完整案卷见
D:\Documents\my\日常\td修复案-分析报告-2026-11-07.md（本案的根因链、路线对比、fork 覆盖表都在里面）。

上游（xandergos）状态：地形惊艳但生成系统残缺，/locate biome 废掉（findBiomeHorizontal / findClosestBiome3d 返回 null），两月静默。
fork（derekvawdrey）已交付：河流/湖泊、雪深分层、y 路由地下群系、两套洞穴、124 条数据驱动群系 catalog、推理线程池+旁路缓存、模型断点续传等。
本 fork 在此基础上：修远古城市/结构生成问题 + 实测清单全项 + locate 修复。

## 2. Git 拓扑（已就位）

- origin  = https://github.com/derekvawdrey/terrain-diffusion-plus
- upstream = https://github.com/XanderGos/terrain-diffusion-mc
- clone HEAD = 05fdcf5（BOP province catalog 提交，fork 末梢）
- 调查用旧 clone 保留勿删：D:\Documents\my\日常\vs-td-investigation\TD-Plus

## 3. 路线（C 主线，A 已否决，B 备胎）

- C 主线：构建本 fork → 实测 → 修远古城市根因 → locate 修复（findBiomeHorizontal ≈30 行）→ PR 回馈 derekvawdrey
- B 备胎：仅当 C 构建翻车无法救活时，自写独立 mod 挂 TD 之上
- 许可证：跟上游 MIT

## 4. 实测清单（构建成功后逐项验）

1. 主世界远古城市是否生成（/locate structure minecraft:ancient_city + 实际找到城）
2. /locate structure 其他结构抽查（要塞/村庄/林地府邸）
3. 大洞穴是否存在（备忘录槽点：只有成团小洞）
4. 岩浆河
5. 海平面以下洞穴是否被水灌满（备忘录槽点：全灌满，极其影响人工挖矿）
6. /locate biome 是否能用（预期：修复前废）
7. td 群系进 minecraft:is_overworld 标签（备忘录槽点：不进导致部分矿物不生成）
8. BOP 群系抽查（fork 的 BOP catalog 是否真落地）

## 5. 构建与发布惯例（备忘录 fork 规范）

- 目录：LMaxs Terrain Diffusion fork（已就位）
- 版本号：基于原版 2.3.0 → 2.3.0.1+1.20.1+forge 起（原版版本.我的序号）
- 发布文件名：lmaxs-terrain-diffusion-fork-2.3.0.1+1.20.1+forge-时间戳.jar
- branch：1.20.1+forge（未定多加载器策略，先单）
- tag：模组版本号（如 2.3.0.1）
- 更新日志只写增量
- 构建入口（2026-11-07 侦察确认）：forge-only 用 versions\1.20.1 内跑根 gradlew 的 buildForgeWindows；根 buildMc1201Windows 会双 loader（fabric+forge），不必需

## 6. 已知坑（2026-11-07 侦察终局更新）

- [x→绿] Java17：JAVA_HOME=E:\Java\Zulu\zulu-17 直接命中 toolchain，foojay 不联网
- [x→绿] HF API 元数据：直连 854ms/HTTP200；构建期 generateModelAssetManifest 只查元数据（LFS 取 lfs.sha256），2.5GB 本体不进构建
- [x→绿] libs/onnxruntime-dml.jar：存在，构建时复制成本地 maven 桥（com.microsoft.onnxruntime:onnxruntime-dml:1.0.0），纯本地无网络
- [!] gradle 8.14.3 发行版：dists/gradle-8.14.3-bin 是 0 字节残尸（.part+.lck，从未下完）；官方源首试 10s 超时、复测 2161ms 能通（抖动大）；腾讯镜像 232ms/131MB 稳定（mirrors.cloud.tencent.com/gradle/gradle-8.14.3-bin.zip）；阿里镜像 404 无此版本
- [x→绿] C 盘空间：max 已将 ~/.gradle 符号链接搬至 F:\MOVED\.gradle（F 盘 302GB），C 盘 3.1→9.0GB，gradle 全部写入落 F；剩余写入点仅 TEMP（存量 1.4GB），构建命令带会话级 $env:TEMP 重定向 D 盘（不动 AppData 本体）；模型 max 的 MC 实例里已有一份，实测阶段直接复用（实例路径待 max 提供）
- [!] agent 框架任务结束会杀残留进程（含 gradle daemon）：构建必须同步 exec 等待完成；与本项目 daemon=false 天然兼容
- [x→红→绿] 2026-11-07 构建实况三墙与对策:
  - 墙1 maven.neoforged.net SNI 阻断 (curl/Gradle 均死): init.d 全局脚本 F:\MOVED\.gradle\init.d\td-mirror.init.gradle (TD_MIRROR 环境变量闸门, 不设零副作用), 摘死仓 + 头插 TDMirror=D:\temp\td-maven (内含重版本化 NFRT 1.0.13 + GMD CI 权威生成的 minecraft-dependencies:1.20.1)
  - 墙2 huggingface.co 证书吊销检查通道被墙 (OCSP/CRL 拒答, Java 报 PKIX, schannel 报 CRYPT_E_NO_REVOCATION_CHECK; 证书本身是真的, curl --ssl-no-revoke 可 200): conventions/multiloader-loader.gradle L69 apiUrl + L88 resolveUrl 改 hf-mirror.com; 原版备份 D:\temp\td-mll-backup.gradle
  - 墙3 PokerAgent 框架任务结束屠后台进程: 构建挂 schtasks 计划任务 tdbuild20261107 (启动器 D:\temp\td-build-run.bat, 日志 D:\temp\td-b-out/err.log, 完成标记 D:\temp\td-b-exit.txt)
- [ ] 模型 2.5GB：运行期下载（首次 pipelineTest / runClient 时触发），落 D 盘 dev 目录（仓库根 terrain-diffusion-models/）；断点续传 fork 已交付；生产 jar 落 gameDir/terrain-diffusion-models

## 7. 构建拓扑（侦察确认，构建时直接照此执行）

- 入口链：cd versions\1.20.1 → 调根 gradlew.bat 跑 buildForgeWindows（内部 = 根gradlew 执行 :forge:build -PuseDml=true）
- settings.gradle 按任务名懒加载：任务名含 forge → 只配 common+forge，不碰 fabric-loom
- common 子项目也用 legacyforge 插件（编译期需要 forge 依赖）；源码由根 common/ + loaders/forge/ overlay Sync 任务合成
- forge 产物 = reobfJar + jarJar（嵌 onnxruntime-dml + yungs-api + better-caves，玩家侧开箱即用）
- pipelineTest：JavaExec 直接跑 PipelineTest 主类（-Xmx8g，种子 -5408366058459925370，哨兵坐标 -16160/-59510），不启动 MC；运行期会触发模型下载
- 产物路径：versions/1.20.1/forge/build/libs/
- 三个 mixin 配置：terrain-diffusion-mc.mixins.json / client / forge

- **实战构建配方 (2026-11-07 全链打通后)**:
  1. 直接跑 D:\temp\td-build-run.bat 或等效命令 (schtasks /run /tn tdbuild20261107 触发, 或手动 cmd 跑 bat 均可)
  2. 前提: 环境变量 TD_MIRROR=1 (bat 内已 set) + F:\MOVED\.gradle\init.d\td-mirror.init.gradle 在位 + D:\temp\td-maven 完好
  3. 观察: td-b-exit.txt 出现且内容 0 = 成功; 失败看 td-b-out.log 尾部 + td-b-err.log
  4. 首跑 4m59s (NFRT 反编译全链), 修复 HF 后 2m4s; Gradle/NFRT 缓存已热, 再跑更快
  5. 产物: versions/1.20.1/forge/build/libs/terrain-diffusion-mc-forge-1.20.1-2.3.0-windows.jar (7.78MB, 内嵌 jarjar 三件套 + model-assets-manifest.json 指向 hf-mirror)
## 8. 状态

- [x] clone 落位 + upstream remote 挂载 + .agent 薪火相传
- [x] GOAL-PLAN.md 落盘（本文档）
- [x] 只读侦察完成（构建拓扑/依赖拓扑/环境坑位全部确认）
- [x] 构建授权（2026-11-07 max 离场前授权:构建+收尾跑bat;口头"先clone"语义不含构建,后授权以本轮为准）
- [x] 构建验证（2026-11-07 BUILD SUCCESSFUL 2m4s exit0,产物验尸合格:manifest/refmap/mixins×3/mods.toml/jarjar三件套;日志 D:\temp\td-b-out.log）
- [ ] 实测清单 8 项
- [ ] 远古城市根因修复
- [ ] locate 修复 + PR 回馈
