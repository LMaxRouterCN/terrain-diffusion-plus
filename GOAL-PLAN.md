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
- 河宽(勘误2026-11-09,原判词两处误): MAX_CHANNEL_RADIUS_PX=16.0f是半径→全宽封顶32格(原"封顶16px=16格"语义笔误); accumulation=localRunoff×cellAreaKm2(L2226,accumulateRunoff传入pixelSizeM)面积补偿→流量为物理量与scale无关(原判词此半句正确);
-   但confinement/谷底扫描全按格计→scale1(30m/格):扫描16px/侧,allowed≈12px半径,全宽~24格=720m,confinement墙先拦,16墙摸不到; scale6(5m/格):扫描clamp48px/侧,16墙生效,全宽32格=160m; 格数24→32不恒定,物理米宽反缩4.5倍,scale红利为负非零; 泰晤士级(万km²盆,flow≈1e4)wanted≈60-64px被墙砍,宽度转深度(boost≤2x)
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

### 调查案卷: onnx-cache 双变体与磁盘膨胀 (2026-11-08)

- 现象: onnx-cache 2.17GB, 每个模型存两份 (base-unknown-*/base-unspecified-*), hash 相同
- 成因: 缓存文件名 = name + ORT版本标签 + 模型SHA-256前16位 (OnnxModel L270 resolveOptimizedModelPath).
  unknown = getImplementationVersion() 返回 null; unspecified = 某版本 ORT manifest 字面量.
  双份 = 历史跑过两个 ORT 版本 (各生成一套), 旧条目无清理机制 -> 慢性泄漏, 每换一次 ORT 版本 +约1.08GB
  (EXTENDED_OPT 优化后 base 1936MB->967.9MB, 减重一半, 每套变体约 1.08GB)
- 优化在构造函数无条件执行 (先于 GPU/CPU 分支), GPU 模式同样写缓存
- AX 误判修正: build.variant (windows) 与缓存文件名无关, 文件名段是 ORT 版本标签
- 现役判定: 两 jar 均 build.variant=windows 但 cache 无第三种标签 -> 现役 ORT 复用 unknown/unspecified 之一
  (以 mtime 为准, 见下), 删错那份代价 = 下次启动重新优化几分钟, 无害
- 清理账: onnx-cache 2.17GB (一半是化石); terrain-diffusion-cache 地形盘缓存 7.69GB (全可再生,
  删除=已探索区域重新推理); 回滚锚 jar.bak-locatefix 7.8MB (验收已过); 空模型目录 4 处


### 峡谷豁免案: B 杠杆落地 (2026-11-09)
- 根因(代码实锤): vanilla canyon(原生 p=0.01, y 10..67) 被 reach_summits 拉顶(67>SEA_LEVEL 63 误判"应达地表") + density_compensation 按拉伸 span 放大概率(0.125~0.34, level build height 800 vs noise_settings 2032 不一致所致) -> 高频穿山天窗; BC 只换洞穴 carver 不碰峡谷(ScaledCarvers L150 注释), scale1 同中招(rewritingBands 默认 true)
- max 口谕: B 进模组默认 + "不硬编码:没文件落盘默认,有文件只读文件" -> 播种式配置: 首运行写 config/terrain-diffusion-mc/carver_altitudes.json(种子= bettercaves 高度键 + minecraft:canyon 豁免), 之后文件即唯一事实源; 坏文件只报错不覆盖, 内存兜底种子
- 语义变更: 文件缺 bettercaves 条目 = BC 不 lift(山实心); README_OLD "空列表禁用内置条目"失效; fork README.md 本就缺 carver_altitudes 文档段(文档债待补)
- commit: 0720001(硬编码式) -> 本轮播种式替代; 未 push
- 待: 过目 -> 构建(maven.neoforged 网络坑备案) -> 替换实例 jar -> 新世界实测峡谷频率
- 构建验收(2026-11-09): buildForgeWindows exit=0, CarverAltitudeRules.class 已入产物, 验尸全绿; 实例现役 jar 已替换, 回滚锚 .bak-canyonfix (旧锚 locatefix 保留)
- 下一步: 开新世界实测峡谷频率 (BC-on, 任意种子; 观察点: 山面破口应归零, 峡谷回稀有地下形态; 首次启动会在 config/terrain-diffusion-mc/ 落盘 carver_altitudes.json)
- 峡谷豁免案结案 (2026-11-09 max 实测): 基本通过, 峡谷改动生效。B 杠杆(播种式 carver_altitudes.json, minecraft:canyon 默认豁免)落地闭环: 根因→改码→过闸→构建→验尸→替换→实测。播种路径(首启动落盘)随本次实测一并验证
- 悬案清账 (2026-11-09, max 授权"随便"): 0720001+aaa9eb1 squash 为单 commit; javac 编码钉 UTF-8(三版本根, 52 条 GBK 警告绝根, 全量构建复验中); README 补 carver_altitudes 文档段; 案卷归档; push 仍未动
## 2026-11-09 凌晨: 洞穴线重开 (max 三指标) 侦察收官
- 指标: 洞穴禁穿地表 / 地表下保厚禁洞(可配置) / 总密度降一档
- 穿面四力: surface_cave(补偿0.15→0.65,无cutoff) > mountain层(177~735大腔贴山面) > winding(top_y80被声明式拉顶,cutoff15护) > (已豁免canyon同款机制)
- 全部前提验证: BC写入=ChunkAccess.setBlockState汇点(duck包非旁路); carve()bci311括号挂点与现有redirect共存; setBlockState自维护高度图(O(1)尺子); 1.0carver=better_cave本体(基类默认prob1.0,y-56..180,JSON不写基类字段); scale2+worldMaxY7535+735(792/237=3.341772闭环); 河流=模型层float[]不进carver阶段
- 决策矩阵已呈: γ(推荐)=括号壳mixin(OCEAN_FLOOR_WG,thickness可配,豁免播种canyon)+手术(surface_cave豁出lift+mountain chance30→15); β=纯数据(mountain照穿面); γ-=废案
- 叠加交互: 壳挡surface_cave低地洞口(与#2自洽,BC天然洞口卖点仅剩侧壁口)
- 实例json已播种,手术②落地需手加surface_cave进excluded或删文件重播种
- 等max拍板: A方案 B thickness C 顶口全灭确认 D 版本顺序(1.20.1先行,1.21.x移植)
## 2026-11-09 晨: CD 终验 + 壳设计修正
- CD1 实锤: CARVERS 状态 heightmapsAfter = POST_FEATURES (非 PRE_FEATURES) → carve 期 setBlockState 实时维护 OCEAN_FLOOR/WORLD_SURFACE, OCEAN_FLOOR_WG 是死表
- 壳尺子改用 OCEAN_FLOOR: 陆地地表/海底语义统一; 自稳闭环(挡写→地表挖不掉→map不漂移→尺子自钉); 兜底=getOrCreateHeightmapUnprimed+首触列下扫(若 map 缺失)
- 壳只拦 air 放行液体(aquifer 灌水/岩浆线为洞内正常物理,拦液体造干层)
- CD2: CarverAltitudeRules 全文已读,手术②=DEFAULT_EXCLUDED 加 bettercaves:surface_cave 一行+注释; 现役实例 json 手编或删重播种
- CD4: 壳过滤 mixin 落位 versions\1.20.1\common\...\mixin\ (现 8 类, json 同名注册)
- 侧壁口语义精确化: 崖列禁区=[崖顶-30,崖顶], 开口贴近崖底(矩阵 C 点的量化版)
- 侦察全线收官, 等拍板 A/B/C/D
## 2026-11-09 晨: CH 终钉 — 谓词链闭环, 侦察终止
- CH1 实锤: MATERIAL_MOTION_BLOCKING = BlockStateBase::blocksMotion 方法引用 (BootstrapMethods #2, REF_invokeVirtual)
- CH2 实锤: blocksMotion = 非COBWEB && 非BAMBOO_SAPLING && isSolid()
- 水的 isSolid()=false: 常识级运行时事实(走进水不撞墙), 字节码末环未追, 留实测轮日志验证
- 终论: OCEAN_FLOOR = 海底语义, 壳水下分支 = 海底下 30 格, 潜艇洞封于海底壳内, 浅海底下有壳
- CG 修正入档: lambda$static$0 = NOT_AIR (非 MMB); clinit 双 indy 序配: bci6→NOT_AIR, bci14→MMB
- 壳链 8 环全景: carve()bci311括号点 / setBlockState写入汇点(vanilla+BC) / CARVERS→POST_FEATURES四成员 / 首写prime后增量 / OCEAN_FLOOR谓词=MMB / MMB=blocksMotion / blocksMotion=isSolid / 水isSolid=false(常识级)
- 侦察阶段终止 (BV→CH), 自动轮链条终结, 纯待命等 max 拍板 A/B/C/D

## 2026-11-09 夜: γ 壳落地, 构建配方找回, 部署闭环
- 代码 (D1a 五件 + D1b 三件): SurfaceShell 线程本地 session (chunk+thickness; shouldBlock = 仅空气 + 本 chunk + session 内 + y >= surface - thickness; OCEAN_FLOOR 尺子, getHeight 自 prime, 无表面列放行); ProtoChunkMixin 在 setBlockState 头部取消壳内写入 (carver 全走此汇点且弃返回值, 拒写不可见); NoiseBasedChunkGeneratorMixin @Redirect applyCarvers 的 carve 调用包 begin/finally end (生效 = TerrainDiffusionBiomeSource + scale>1 + 配置开 + 非 shellExempt); ScaledCarvers 增 ORIGIN_BY_LIFTED 反查 + configuredCarverId; CarverAltitudeRules DEFAULT_EXCLUDED + bettercaves:surface_cave, 新 shellExempt 键 (默认 minecraft:canyon, 壳会削平峡谷地板), 播种含双列表; Config: caves.surface_shell=true / thickness=30 (1..256), mountain_caverns.chance 30->15
- 手术②: surface_cave 豁出 lift (留作者原样 = 稀有低地洞口制造者, 壳再封其低地口); 手术③: 山层腔密度减半
- D2 构建会话复盘 (12 回合, 教训 = 配方在案卷而我没检索): :forge:build 未设 TD_MIRROR -> init.d 旁路休眠 -> NFRT 404; 侦查链 = 三渠道 DNS 污染实锤 (根服务器 IP 表 / Dropbox / Facebook 段) -> SNI 封锁复证 (记忆 032) -> CF 跳板 / 8443 / bmclapi / central 全试 -> 记忆检索命中 032/034/035 -> 配方找回 -> 预检绿 -> buildForgeWindows 56s exit=0
- build.gradle: yungs 无版本坐标迫查 Modrinth maven 版本列表 (本机挂死), strictly 已锁单版, 钉坐标等价且离线确定; 修饰修正 = 尾换行补齐 + 注释缩进归位 + WARNED javadoc 归位
- 产物: terrain-diffusion-mc-forge-1.20.1-2.3.0-windows.jar 8,167,739B, jarJar 三捆绑 (onnxruntime-dml / yungs-api / yungs-better-caves), 验尸绿 (mixins 注册文件名 terrain-diffusion-mc.forge.mixins.json, D2-S 猜错名系虚惊)
- 部署: 实例现役 jar (11-09 03:07 版) 备份 .bak-caveshell 后替换; 实例 carver_altitudes.json 备份 D:\temp\carver_altitudes.json.bak-20261110 后删除, 首启动以新种子 (surface_cave 豁免 + shellExempt) 重播种
- 待实测 (max): 新世界看三指标 (洞不穿地表 / 地表下保厚 / 密度降档), 观察点 = 平地洞口/山面破口/海床洞 归零, 崖壁低位口应留存; 首启动 config 落盘的 carver_altitudes.json 应含 excluded:[canyon, surface_cave] + shellExempt:[canyon]
### 日期勘误 (2026-11-09 夜)
- 本文件 2026-11-10 凌晨/晨/夜四处标题日期有误: 会话实际发生在 2026-11-09 (凌晨 03:07 构建、晨侦察、夜落地, 文件系统时间戳与系统时钟为证), 已统一修正为 11-09。
## 2026-11-10 凌晨: 满水洞穴穿面修复, 配置归拢, 种子/数据包两案定罪

- 修复: 满水封闭洞穴 carve 全程零空气写入, 壳 (只拦空气) 全程旁观照样穿面 -> 谓词扩为空气或流体一并拒 (getFluidState 非空), 壳带内保持原地形; 高洞遇潜水面留石盖封水, javadoc 与 properties 注释同步。
- 配置: properties 迁入 config/terrain-diffusion-mc/ 子目录 (与 carver_altitudes.json 同居), 首启自动从 config 根搬入, 已调值不丢。
- 种子案定罪: init(world.getSeed()) 在 TerrainDiffusionLifecycle.onWorldLoad 接线正常, 接口层吃 world seed; 但 setSeed 只重建合成图工厂 (气候/群系噪声 per-seed), 主地形 elevation 由 ONNX 从 latent 解码, latent 若不吃 seed 则同坐标同山 -- 与实测地形复现 (冰湖指纹) 吻合, 山复现/群系变化分裂在此线上。属上游架构问题, 修复 (latent 掺 seed) 待 max 拍板。
- 数据包损坏案: new TerrainDiffusionBiomeSource 全仓零命中, "运行时 new"假说排除; 高置信新假说 = 注册表按对象身份反查 codec, 而 MapCodec.codec() 每次调用返回新包装实例, 注册实例与保存时 codec() 返回实例身份不等 -> Unknown registry element。修复方向 = Codec 单例化 (static final, 注册与 codec() 同一对象), DFU 反查机制约七成把握, 动手前需反编译验证或建世界实测存读。
## 2026-11-10: codec 单例修复 (max 批准), 种子案 B 立案 (latent 掺 seed)

- codec 修复: TerrainDiffusionBiomeSource.CODEC_INSTANCE 静态单例, codec() 与注册点同用一对象; 根因 = 注册表身份反查 vs MapCodec.codec() 每次新实例 (长期记忆 047)。验证: 建世界进退重进, log 无 Unknown registry element 且 seed 非空。
- 种子案 B (max 拍板, 必修): latent 构造链侦查在途 (生成点 / 缓存 key 是否带 seed / 随机源), 掺 seed 施工待链路清楚后进行。已知代价: 掺 seed 后换 seed 跨界区块有地形接缝, 已生成的区块不受影响。
- DF 同病检查: TerrainDiffusionDensityFunction / NoiseCavesFlagDensityFunction 的 CODEC 模式侦查结果见 D5-U1 回执。
## 2026-11-10 03:5x: 种子案 B 结案 (嫌疑归零), 两案合并为 codec 根因

- 复核 WorldPipeline 三阶段全文: coarse 条件噪声 generateInto(seed) / 初始采样 generateInto(seed+1) / latent 初始 5819 与步进 5820 / decoder 5819 —— 全部为"管线种子 + 偏移", 全链条吃 seed, 接线健康。5819/5820 是噪声用途偏移而非硬编码种子 (D5-U1 初判有误, 修正)。
- 地形复现的真嫌疑: level.dat 写残 (codec 案) 之下新世界 seed 未能真正随机/落盘, 复用旧值 → 同一噪声链 → 同一地形。两案合并, codec 修复 (f58966e) 极可能附带结案种子案。
- 磁盘缓存雷排除: HydrologyProvider/CoarseDrainageProvider 的 TileKey 含 seed 字段, 盘缓存路径 seed-<hex> 分目录, 换种子不污染 (L164 注释明示 persist 后跨种子保留)。之前"namespace 疑不含种子"的担忧不成立, 收回。
- 待 max 实测: 建世界 → 进 → 退 → 重进 (codec 案判决), 同时观察新世界地形是否变化 (种子案判决)。
## D6 (2026-11-10 晨) - 种子封卷 / 洞穴密度 / DH 评估

- [x] 种子案封卷: max 连建多世界地形各异, 链路健康无 bug (上轮"两案合并"推定被实测推翻, 重开重验后以新证据封卷)
- [x] codec 案: max 实测存读不崩, 结案
- [x] 洞穴密度: 杠杆 = BC jar 内 better_cave.json 的 cave_spawn_chance (区域级洞穴生成率, TD 密度补偿不触及它; vanilla probability 是死旋钮 - x3.34 补偿封顶)
  - 落地: 实例 config\openloader\data\z-bc-cave-density\ 数据包, 100.0 -> 70.0 (-30%, "降低一点"档)
  - 仅新世界生效 (worldgen 注册表建世界时序列化进 level.dat); 测试法 = 新世界挖竖井
  - 侦查教训: BC 以 jarJar 捆在 TD 主 jar META-INF/jarjar/ (Forge 路径), 非 META-INF/jars/ (Fabric 路径) - 三轮扑空的根因, 记忆已档
- [x] DH 加速 LOD 生成可行性评估: 报告 D:\Documents\my\日常\DH加速LOD生成-可行性评估-2026-11-10.md
  - 结论: 可行, 官方 worldGenOverrides 入口, 典型 2-4 天, 最大风险 = wrapper 获取
  - 待 max: 做不做 / 单机或联机范围 / 依赖策略
- [ ] 本轮: commit (本文件) + push (github 直连恢复窗口待定)
