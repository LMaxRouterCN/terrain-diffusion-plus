# GOAL-PLAN.md — LMaxs Terrain Diffusion fork

> 立项 2026-11-07。本文档随任务推进持续更新。

## 1. 项目定位

fork 自 derekvawdrey/terrain-diffusion-plus（其上游为 xandergos/terrain-diffusion-mc，MIT 许可证）。
起因：max 环境中 TD 主世界不生成远古城市（已影响正常游戏）。分析结论与完整案卷见
`D:\Documents\my\日常\td修复案-分析报告-2026-11-07.md`（本案的根因链、路线对比、fork 覆盖表都在里面）。

上游（xandergos）状态：地形惊艳但生成系统残缺，`/locate biome` 废掉（findBiomeHorizontal / findClosestBiome3d 返回 null），两月静默。
fork（derekvawdrey）已交付：河流/湖泊、雪深分层、y 路由地下群系、两套洞穴、124 条数据驱动群系 catalog、推理线程池+旁路缓存、模型断点续传等。
本 fork 在此基础上：修远古城市/结构生成问题 + 实测清单全项 + locate 修复。

## 2. Git 拓扑（已就位）

- origin  = https://github.com/derekvawdrey/terrain-diffusion-plus
- upstream = https://github.com/XanderGos/terrain-diffusion-mc
- clone HEAD = 05fdcf5（BOP province catalog 提交，fork 末梢）
- 调查用旧 clone 保留勿删：`D:\Documents\my\日常\vs-td-investigation\TD-Plus`

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
- 构建入口预期 buildMc1201Windows（具体以 build.gradle/README 实测为准）

## 6. 已知坑（构建前备好预案）

- 模型 revision pin 或重新下载 2.5GB（首次构建可能卡在模型下载）
- libs/onnxruntime-dml.jar 自带与否待验证
- dev 依赖版本与运行时一致性（EpicFight 案教训：编译期校验不可信的场景要警惕）

## 7. 状态

- [x] clone 落位 + upstream remote 挂载 + .agent 薪火相传
- [x] GOAL-PLAN.md 落盘（本文档）
- [ ] 构建授权（等 max 点头，"先clone"语义不含构建）
- [ ] 构建验证
- [ ] 实测清单 8 项
- [ ] 远古城市根因修复
- [ ] locate 修复 + PR 回馈
