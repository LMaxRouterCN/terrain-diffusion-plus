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
- [~] 实测清单 8 项 (2026-11-08 max 首测: 1远古城市✓ 立案起因解决; 2村庄✓其余未测; 3大洞穴✓ 4岩浆河✓ 5灌水✓ 地下观感=fork设计如此; 6locate biome报错-搜脚下群系也找不到,嫌疑Holder身份比较(direct vs registry ref)待代码验证; 7is_overworld未测[仓库侧查tag文件] 8BOP未测; 附: locate structure结果正确但卡约2分钟[性能雷,嫌疑coarse map未覆盖vanilla jigsaw], 洞穴密度过高[任意处5x5到底必遇洞,调cave_density掩膜系数,决策留max])
- [ ] 远古城市根因修复
- [ ] locate 修复 + PR 回馈

## §12 侦察结论 (2026-11-08)

### 山高/世界缩放 (max 问询: 有无横竖分离配置)
- 旋钮 = WorldScaleManager.scale, int 1-6 默认 2, per-world SavedData 落 saves/<世界>/data/terrain_diffusion_world_settings.dat
- 换算: blockY = 海拔米数 × scale ÷ 30 + 63; 负海拔走 sqrt 压缩分支与 scale 无关 (只拉伸地上)
- 入口: CreateWorldScreen World 标签页 mixin 注入的 WorldScaleSettingsScreen (建世界时 GUI)
- 旧世界改 NBT 可行但有接缝风险 (fork 自警告 keep for world lifetime)
- **无横竖分离**: 单 int, HeightConverter(垂直)/SurfaceStamp(水平间距 30/scale 米)/LocalTerrainProvider(采样) 同吃一值; 真分离需代码级拆 ≥3 消费点, 中等工作量, 留 max 决策

### locate 结构 (性能)
- far lookup 高速路: ChunkGeneratorMixin 钩 findNearestMapStructure(HEAD/RETURN 开关 scope) + FarLookupHeightMixin 拦 iterateNoiseColumn 改读 heightmap; 仅 biome 搜索 stub 未接
- locate structure 卡 ~2min = coarse 塔冷启动 + 推理线程与 chunk 生成抢 INFERENCE_EXECUTOR; 二次 locate 应显著快 (max 可验证)
- 既有瑕疵: mixin HEAD/RETURN 非 finally 语义, 异常路径漏关 scope 靠 120s stale 自愈; 可改 HEAD+THROW+RETURN 三点注入, 一行成本, 留 max 决策

### 洞穴密度
- cave_density.json L23=2.5 / L32=5.0 / L83=2.5 (深度带掩膜系数), 数据驱动, 调不调留 max

### 其他
- 1.21.1/1.21.11 版本存在同样 BiomeSource stub, 本次只修 1.20.1 主线
- 模型目录冗余: 同 base 模型三份 (原名 1935MB + unknown/unspecified 变体各 968MB, ~3.8GB), fork 校验机制副作用, 磁盘卫生项

### 提案可行性判定 (2026-11-08 javap 实锤)
- vanilla BiomeSource (forge-1.20.1-47.4.10_mapped_official jar, 与测试实例 Forge 同版): findBiomeHorizontal 6参/8参两重载与 findClosestBiome3d 均为 concrete 默认实现 (javap 签名无 abstract 前缀), super 透传编译期安全
- fork stub 的是 8 参完整版 (带 blockCheckInterval), 6 参便捷版内部委托 8 参版; 修复只需透传 8 参一处, /locate 与地图类生态调用两路全通
- stub = 作者主动阉割可选 override, 非编译义务
## 2026-11-08 晚: locate 修复落地 / THROW 注入排雷 / 洞穴方案转向
- locate biome 根因与修复: BiomeSource 两 stub 返回 null → super 螺旋透传 + far-lookup scope 包裹 (唯一代码改动, versions/1.20.1/.../world/TerrainDiffusionBiomeSource.java, +16 行)
- ChunkGeneratorMixin THROW 注入: 已回退。findNearestMapStructure 方法体 0 条 athrow + defaultRequire=1 → 零注入点 = 启动必崩; 异常经异常表穿过宿主帧、不经宿主 athrow, mixin 0.8.5 无干净写法 (作者 120s stale 自愈即为此兜底)
- 洞穴: 决定不关捆绑 Better Caves (作者 auto 语义 = BC 优先; BC 在场时 mod 自己的噪声洞穴 json 是死配置); 岩浆河定性为 BC-off 新世界形态 (噪声洞穿基岩渐变壳 + vanilla 含水层岩浆线灌浆), BC-on 预期消失, 待游戏验证
- 实例: 新 jar (含 locate 修复) 已装; 回滚锚 mods/terrain-diffusion-mc-forge-1.20.1-2.3.0-windows.jar.bak-locatefix (11-07 构建, 无修复)
- 待办: 游戏验证 → git commit 过目 → 第二轮杠杆 (caves.density_compensation / OpenLoader 覆盖 BC carver json / 峡谷 liftAll 5 行分支)


### 游戏验证结果 (2026-11-08 深夜, 新世界 BC-on)
- locate biome: 雪原/平原/沙漠/蘑菇岛正常; 沙漠与蘑菇岛 4700+ 格外秒出 (coarse 远端采样生效, 修复有效); cherry_grove 仍找不到 — 悬案: 头号嫌疑 BiomeCatalog 未收录 (世界里不生成, 属正确找不到), 次嫌疑 niche 稀有超出搜索半径, 待查代码
- locate structure: 古城正常; 要塞可定位但首次明显等待 (vanilla 环形搜索触发真实 chunk gen, TD 生成开销放大, 属预期非回归)
- 世界底部: 岩浆河未捅穿基岩, 地板密封验收通过; 岩浆嵌基岩层 = BC liquid regions 设计形态且与原版含水层底部同构 (液面 -55 vs 基岩顶 -60, 中间 5 格), 世界深度为 MC 物理极限 -64 非被截短, 定性不修
- 地面破口: 峡谷占大头 → 第二轮杠杆主项重排: 峡谷治理优先于 surface_cave; 待验 density_compensation 覆盖面 (若含 canyon 则一开关管全部, 否则走 liftAll 5 行分支)

### 调查案卷: 江河湖与 World Scale (2026-11-08 深夜)

#### World Scale 数学 (已实锤)
- README_OLD L352: `scale=1 => 30m/block, scale=2 => 15m/block` — 线性 30/scale, 随世界存档保存, 并决定新世界高度上限与 GPU/CPU 性能配比
- WorldScaleManager: DEFAULT_SCALE=2, MIN=1, MAX=6 (UI 与 clamp 双重封顶); 垂直海拔以上区域 ×scale 拉伸, 海平面以下任何 scale 不拉伸 (README_OLD L157-158)
- LocalTerrainProvider L1023: `pixelSizeM = NATIVE_RESOLUTION / scale` — 水文分析 1px = 30/scale 米 = 1 游戏格 (hydrology tile 2048 格 = 2048px 分析网格; TileKey 含 scale)
- 推论: 1m/格 = scale30 不可达 (3000m 山 > 2032 格维度上限, 物理装不下); 本 mod 分辨率地板 = scale6 = 5m/格

#### 江河湖机制 (FluvialRiverNetwork.java 2608行精读)
- 单位体系: 物理量 (流量km²/高程m/湖面积m²/meander波段m) 按现实米; 通道几何按分析px (1px=1格)
- 河宽: radiusPixels(流量, 海拔) 幂律 (L1167-1187), 流量不随 scale 变 -> 河宽格数恒定, scale 红利为零; 泰晤士级流量算出~64px 也被 min(16px) 封顶, 砍掉的宽度转深度
- 山地挤压: CONFINEMENT_FRACTION=0.72 (河最多占谷底72%), 谷底=两侧升高<18m 区域, 陡坡谷底一两百米宽 -> TD 山地河天然溪化; 大河只能在宽谷平原 (需万km²级盆地)
- 湖: 浅(<15m)+小(<135000m², scale2下约600px²/22x22格)+无河流补给 = puddle 删除; 有效湖有碗状加深 (2-12格深, 4-40px半径) — lake bowls 机制
- meander: 幅度=2.4x河宽 (78m起), 波段240/720/2160m, scale2下720m波段=48格波长
- 判词: 无bug, 全有意设计. 玩家未见大河 = 探索半径不够(大盆地远) + 山地谷窄. 实测路: /td-explore web地图 (端口19801) 俯瞰河网

#### cherry_grove 终判 (biome_catalog.json L4131)
- 收录: 是 (3条 mountain 规则: 雨量360-1100/1100-2600两段+海拔1000上下分段, 温度窗6-18.5C, 坡<=0.52, 湿0.58-0.9)
- 稀有机制: climate 窗命中 -> cherryNoise 噪声闸 (>0.15-0.18) -> rarity 10.0 (RARE档, 权重<1+更严噪声阈值, 见 BiomeCatalogSmokeTest) 三重门
- 判词: 存在但极稀有, /locate biome 找不到 = 搜索半径内无样本, 属正确行为非bug

#### 抬 scale 的真实代价账
- 不变的: 推理成本, 河宽格数, 湖格数, 玩家跑图格数成本
- 变的: 同格数覆盖的现实地貌 ÷scale² (多样性缩水); vanilla 装饰现实密度 ×scale² (树/草 authored 按格, scale6 森林×9密度)
- 1px=1格推论: scale 对江河形态无影响, 想看大河与 scale 无关
