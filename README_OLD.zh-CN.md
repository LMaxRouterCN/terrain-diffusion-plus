# README_OLD.md 中文全译

> 本文件是 `README_OLD.md`（上游 terrain-diffusion-plus 的英文原版 README，591 行，2026-11-07 存档）的逐段中文翻译。
> 你 fork 之后的改动说明在现行 `README.md`，不在本文件范围。
> 原文开头那句 "Please use an LLM instead of a traditional translator to translate this readme" —— 本翻译照办。
>
> 翻译约定：① 逐段忠实翻译，不概括、不跳句 ② 代码块内的命令与输出保持原样 ③ 配置示例（TOML）中 `#` 注释翻译为中文，键名与取值原样保留 ④ 专有名词（DirectML / CUDA / chunky 等）保留英文，需要时附中文说明 ⑤ 行内代码与链接保持原格式。

---
# Terrain Diffusion Plus

[Terrain Diffusion](https://github.com/xandergos/terrain-diffusion) 模组的一个 fork，在这个基础上实现了动态生物群系选择、群系配置、提速、河流和洞穴。它没做性能优化，建议你用 chunky 预生成世界并设好世界边界。扩散模型本身跑得挺快，但河流网络的计算每个超区块（super-tile）需要 30-60 秒。

## 这个模组做了什么

它把 Minecraft 的主世界生成器换成一个在真实世界地形上训练出来的扩散模型。模型不靠组合噪声函数，而是生成一张高程图加上四个气候变量——温度、温度季节性、降水、降水变率——世界就从这些数据构建出来。

光这一个改动就牵动了后面一整套系统：

- **真实地貌。** 山脉、河谷、沿海陆架、高原都具有真实地形的那种大尺度结构，因为模型是从真实地形里学来的，而不是从一层层噪声 octave 里叠出来的。
- **气候驱动的生物群系。** 群系不是涂上去的，而是通过数据驱动的规则目录（`biome_catalog.json`）从模型产出的高程和气候推导出来的。沙漠落在雨影区，针叶林跟着纬度和海拔走，群系边界落在气候真正发生变化的地方。
- **积雪深度。** 按模型自己的地面温度，越冷雪越厚：刚能积雪的地方只有薄薄一层，-3 到 -7 C 之间一到七层，再冷到 -12 C 就是一个雪块加若干层。树冠积雪也会让树下地面跟着积雪。`snow.depth_scaling=false` 恢复原版单层。
- **水文。** 河流是在生成好的高程上由河流网络追踪出来的，所以顺坡而下、沿途汇入支流、最终入海。每一段河道都贴合它所在的河谷成形：水道最多占谷底宽度的 72%，在受夹束的地方加深；在泛滥平原上蜿蜒，蜿蜒幅度与河宽挂钩，到谷壁抬升处停止蜿蜒；水位停在两岸刚好能托住的高度，且沿程向下游保持连续的纵剖面。湖泊填进排水网络找到的洼地，湖床从岸边向内成碗形加深；而那些又小又浅、不靠河流补水的坑洼保持干涸。
- **高世界。** 地形使用远超原版的建设高度，由 `World Scale`（世界尺度）设置缩放——最高到 2032 格。
- **地表结构。** 巨砾、石柱、天然拱、海蚀柱这类程序化结构，都按它们脚下地形的真实坡度和材质来放置。
- **贯穿山体的洞穴。** 1.20.1 和 1.21.1 构建内置 YUNG's Better Caves；所有构建上，每个雕刻器的高度和生成率都被搬进更高的世界里，让洞穴填满整座山，而不是只在基岩附近留一条带。见[洞穴](#caves)。
- **地形浏览器。** `/td-explore` 打开一个浏览器里的世界地图，用来侦察和调试。

生成过程跑的是神经网络，所以需要一块 GPU 才舒服——见[需求](#requirements)。

## 我该用哪个版本？

[Releases](https://github.com/xandergos/terrain-diffusion-mc/releases) 页面提供三种运行时构建。

**CPU 构建很慢，除非你在 macOS 上。**

| 构建 | 支持环境 | 需要的安装 |
|---|---|---|
| **Windows**（推荐） | 装有任意现代 GPU 的 Windows | 无 |
| **CUDA** | NVIDIA GPU | [CUDA + cuDNN 安装](CUDA_INSTALL.md) |
| **CPU** | 其他所有环境 | 无 |

> **Mac 用户：** CPU 构建在 Apple Silicon 上会自动使用 CoreML 硬件加速，无需额外设置。

只有在 Linux 上，或者你用 NVIDIA GPU 且偏好 CUDA 时，才用 `-cuda` 构建。

## 支持的 Minecraft 版本与加载器

| Minecraft | Java 版本 | Fabric | Forge | NeoForge |
|---|---|---|---|---|
| **1.20.1** | Java 17 | 是 | 是 | 否 |
| **1.21.1** | Java 21 | 是 | 否 | 是 |
| **1.21.11** | Java 21 | 是 | 否 | 是 |

## 需求

- 强烈推荐带 GPU 的 Windows 或带 NVIDIA GPU 的 Linux。CPU 推理能用，但很慢。
- 所需显存（GPU 内存）：约 1.5 GB。
- 所需内存：约 2.5 GB，你可能需要调大 Minecraft 的内存分配。

以下三者任选其一：

- 装了 [Fabric](https://fabricmc.net/) 和 [Fabric API](https://modrinth.com/mod/fabric-api) 的 Minecraft。
- 装了 [Forge](https://files.minecraftforge.net/) 的 Minecraft，仅限 1.20.1。
- 装了 [NeoForge](https://neoforged.net/) 的 Minecraft，仅限 1.21.x 及以后版本。

## 用法

**用 CUDA 构建的话：** 先看 [CUDA_INSTALL.md](CUDA_INSTALL.md)。

1. 从 [Releases](https://github.com/xandergos/terrain-diffusion-mc/releases) 下载匹配你 Minecraft 版本和加载器的模组 jar，放进 Minecraft 的 `mods/` 文件夹。确认 Minecraft 版本对应。
2. 至少联网启动一次 Minecraft 来下载 Terrain Diffusion 模型。首次下载量很大，总计约 2.5 GB。
3. 创建世界，选择 **Terrain Diffusion** 世界类型。点 **Customize**（自定义）设置 `World Scale`。
4. 模组会在世界原点附近自动找一块陆地当出生点。如果 `(0, 0)` 周围全是海，找陆地可能要花一点时间。
5. 用 `/td-explore` 在浏览器里侦察生成的世界；用 `/td-status` 看模组正在干什么：模型落在哪个推理设备上、世界尺度、多少地形已缓存或正在生成、哪些设置会影响生成的地形。提 bug 时把它的输出贴上。

### 手动安装模型

模型在首次启动时从 Hugging Face 下载到游戏目录（`mods/` 旁边）的 `terrain-diffusion-models/` 里。连接中断时，已收到的内容留在 `.tmp` 文件中，下次尝试从那里续传，还有几次自动重试，所以网络慢或者不稳定，只需要让游戏挂着或者重开。在网络碰不到 Hugging Face 的环境下，按日志里 *Direct download* 行指定的 revision 自己下载那五个文件，放进这个文件夹：

```
terrain-diffusion-models/
  base_model.onnx
  coarse_model.onnx
  decoder_model.onnx
  pipeline_data.json
  world_pipeline_config.json
```

每次启动都会做 SHA-256 校验（`validate_model=true`），所以文件错了或者截断了会重新下载，而不是将就着用。

结构搜索——`/locate structure`、制图师的探险家地图、末影之眼——要在没人生成过的地形上逐区块探测。这些探测用模型的粗粒度地图来回答（`World Scale` 2 时每 512 格一个值），而不是真去生成每个图块，所以一次搜索一瞬间就完成，不会冻住服务器；代价是偶尔指到的位置，精确地形其实并不适合那个结构。

## 探索世界

模组内置一个地形浏览器 Web UI。在游戏里运行 `/td-explore`，它会打印一个可点击的链接，比如 `http://localhost:19801`，在浏览器里打开交互式地图。

点左边的地图打开详细视图；点详细视图可以在左下角拿到坐标。还可以按气候筛选。

## 模组兼容性

其他模组在运行时被检测，而且永远是可选的——不捆绑任何东西，也不需要预装什么。你装上想要的模组，群系目录自己就会适配。

### 生物群系模组

内置目录把所有受支持的整合都放在一个文件里，每个条目声明自己需要哪个模组。对应模组不在的条目在加载时被丢弃，所以同一个 jar 适配所有安装组合：

| 已安装 | 参与的群系 |
|---|---|
| 无额外 | 原版 65 个 |
| [Biomes O' Plenty](https://modrinth.com/mod/biomes-o-plenty) | 124 个 |

装了 Biomes O' Plenty 时，世界被一个 5 km 尺度的区域场划分成三个大陆省份，相关群系作为邻居一起生成，而不是随机混在一起：一个北方与旧大陆省（苔原、积雪针叶林、泥沼、荒沼与草沼、桦木林、草地、疏林、地中海森林、高地与峭壁、荒地），一个新大陆省（muskeg 冻原沼泽、松树针叶林、冷杉疏林、季节性森林、白杨疏林、普列利草原、恶地、巴尤沼泽、雨林、红杉、繁茂稀树草原），和一个东方与太平洋省（寒漠、枫树林与雪华树林、果园、旱地、竹林丛林、真菌丛林、热带、火山与玉崖）。每个省内部，各群系拿到的都是模型实际为它产出的气候窗口；通用基线群系（平原、森林、针叶林、沙漠、稀树草原、丛林、沼泽，加积雪的一对）不设门槛，保证每种气候至少有两个候选；稀有点缀（蓝花楹疏林、薰衣草田、南瓜地、不祥树林、神秘树林、起源谷）则是噪声门控下的零星发现，在气候合适的任何地方出现。每一套规则集都用 `tools/biome-lab` 对照模型的真实气候分布核验过：没有死条件，没有永远刷不出来的群系，BOP 群系除两个额外沙滩外全部高过遭遇率门槛。

想加自己的，就往 `config/terrain-diffusion-mc/` 放一个 `biome_catalog.json`——它会替换内置那份。条目可以写 `"requiredMods": ["some_mod"]`，用同样的方式给自己设门。

### 全面改造型模组

给整个游戏换皮的主题模组，通常直接在原地重定义原版群系。这在模组里不用任何配置就能工作：它们的树、颜色、生物和结构经由群系注册表进来，所以地形是这个模组的，外观是它们的。

有一件事被自动处理：

- **主世界夺回。** 这类模组经常自带 `minecraft:dimension/overworld`，正常情况下它会压过玩家选的任何世界类型——主世界会被静默打回它们的生成器。如果一个世界是用 Terrain Diffusion 世界类型创建的，加载时会把它的主世界重新夺回来。用其他世界类型创建的世界不受影响。

不复制、不再分发其他模组的任何内容；模组只读取游戏里已经加载的东西。

注意：这类模组自己的地表规则写在本模组所替换的原版主世界噪声设置里，所以地表方块仍然是这个模组自己的。

### 洞穴

洞穴来自**雕刻器**：每个洞穴模组、群系模组和数据包都会去写的那份按群系划分的列表。这份列表在这里原样运行，所以装一个洞穴模组的效果就是你预期的样子。对它只做两件事，没有别的。
**高度被搬到地形所在的地方。** 雕刻器是为地表大约在 y=64..140 的世界写的，所以它会说"世界底部到 y=180 之间任意位置"这类话。这个模组的维度最高 2032 格，`World Scale` 把海平面以上的一切都拉伸了，所以一个原封不动的雕刻器只会在世界底部附近切出一条带，把它上面的每座山都留成实心。因此每个雕刻器的高度被乘上和地形相同的系数，其余一切——形状、大小、半径、方块选择——都不动。`World Scale` 1 时什么都不会发生，两个高度本来就相同。海平面以下的地下在任何尺度下都不拉伸，所以深部雕刻器保持它们写定的深度，一毫不差。

**洞穴频率按世界的每一层板保持。** 一个以某概率每区块开一个洞穴系统、并把它丢在范围内任意位置的雕刻器，在范围高出数倍之后，产出的洞穴数量不变却摊得更薄——最明显的表现是山壁上的洞口少得多。它的生成概率按范围增长的倍数同步提高。改为按噪声填充区块的雕刻器，比如 YUNG's Better Caves，本来就随它拿到的层带自行缩放，保持原样。

两者都可以在 `config/terrain-diffusion-mc.properties` 里关掉（`caves.lift_carvers`、`caves.density_compensation`）。

**层带直达山顶。** 拉升保持层带的比例，但地形并不保持原版的比例：原版洞穴停在 y=180，Better Caves 的隧道停在 y=80，`World Scale` 2 时这两个顶部落在 y=297 和 y=97，而山顶在 y=400-550 上下，把每座山的大半留成了实心。因此每个写定顶部在海平面以上的层带都被一路跑到世界顶端（概率型雕刻器的密度补偿覆盖了多出来的高度）；留在海平面以下的层带，也就是深部洞窟，保持它被拉升后的顶部。`caves.reach_summits=false` 让洞穴停在拉升后层带的终点。

**山体里的洞窟。** Better Caves 把它的大型洞窟留在世界底部附近，所以就算隧道通到了山顶，一座山里也只有隧道，没有更大的东西。它的雕刻器被多给了一层洞窟层：它自己第一层洞窟层的克隆，所以洞窟保持该模组（或整合包）的调校，层带搬到大约 y=177（`World Scale` 2 时）直到山顶。低地地形永远够不到这个层带。`caves.mountain_caverns=false` 关掉它；`caves.mountain_caverns.chance`（0..100，默认 30）是该层中持有洞窟的洞窟区域占比。

#### YUNG's Better Caves

1.20.1 和 1.21.1 构建以 jar-in-jar 内置 [YUNG's Better Caves](https://modrinth.com/mod/yungs-better-caves)，所以大改洞穴、地下湖和岩浆海开箱即有——不用装任何东西。什么都不被覆盖：整合包或玩家为它配置什么，就生成什么，它的洞穴与洞窟层带是在运行时被搬进我们的高世界里，而不是靠在它的配置上盖一个文件。1.21.11 没有 Better Caves 的发布版，所以那些构建用原版雕刻器，除非你自己装一个洞穴模组。

因为它是内置的，没法从 mods 文件夹移除。想跑另一个洞穴大改模组，设 `caves.bundled_cave_mod=false`：它的雕刻器从每个群系里被丢弃，原版洞穴被放回来——除非另一个洞穴模组的雕刻器已经在那儿了，那种情况下生成的是那个模组的洞穴。

在 1.21.1 上，模组还让 Better Caves 的含水层上下文保持存活：它的水和岩浆区域从一个按线程的上下文放置，而这个维度的许多区块同时开始生成时，该上下文会间歇性缺失（日志记为 *Failed to fetch the AquiferContext*），那些区块就没有液体区域。现在一次取不到上下文的填充会把它重新建立起来。

> 在 Minecraft 1.20.1 配 [C2ME](https://modrinth.com/mod/c2me-fabric) 时，在 C2ME 配置里设 `threadedWorldGen.enabled=false`。Better Caves 放置水和岩浆区域所依据的状态会被多线程区块生成竞争，症状是水区域里出现岩浆袋。1.21.11 不受影响。

#### 把你的雕刻器告诉这个模组

配置是模组自有类的雕刻器原样直通，因为没有通用的办法知道它的哪些数字是高度。一个模组或整合包可以自己声明，从而拿到和原版雕刻器相同的高度拉升，办法是放一个 `config/terrain-diffusion-mc/carver_altitudes.json`：

```json
{
  "altitudeKeys": {
    "somemod:crystal_cavern": ["min_height", "max_height"]
  },
  "excluded": [
    "othermod:vertical_shaft"
  ]
}
```

`altitudeKeys` 按雕刻器**类型** id 索引——即已配置雕刻器 JSON 的 `"type"`——并列出该类型配置中持有绝对 y 值的键。任何这样的键，无论在配置的哪一层深度，都会被拉升；其余每个数字都保持写定的原样，因为配置是由模组自己的 codec 重建的，而不是靠戳字段。空列表可以禁用一个内置条目。模组自带一个条目，给 Better Caves 的 `bottom_y`/`top_y`。

`excluded` 列出完全不被改写的**已配置**雕刻器 id，连原版形状的也算——这是给已经为高世界调好雕刻器的模组或整合包留的逃生门。

一个直接接管雕刻步骤的模组不会被这一切碰到：它已经接管了洞穴生成，这个模组不挡它的路。

#### 噪声洞穴

原版的洞穴不是雕刻器：自 1.18 起，奶酪洞窟、意面隧道、细面洞穴和洞口都是在 `minecraft:overworld` 的密度函数里切出来的，而这个维度不用它。本模组在它自己的密度函数里装了同样的洞穴，组合方式和原版完全一致，其中细面条带从原版的 y=320 扩展到这个世界的全高，所以它们能到达山顶。`caves.noise_caves=auto` 在 YUNG's Better Caves 不在场或未启用时把它们打开，因为两者同时在场会把世界掏空；在没有 Better Caves 的 1.21.11 上，大型洞穴就来自这里。`true` 和 `false` 强制开关。

编辑 `minecraft:overworld` 噪声设置的洞穴大改模组哪条路都到不了这个维度，但有一个地方可以放。本模组的 `final_density` 以一个具名密度函数收尾：

```
final_density = (terrain + beardifier) + terrain-diffusion-mc:cave_density
```

在数据包里覆盖 `data/terrain-diffusion-mc/worldgen/density_function/cave_density.json`，你放进去的东西会被到处加到地形上——想要空气的地方就放负值。这是一个小文件，不是整份噪声设置的拷贝，所以本模组的地形变化时它依然有效。自带的文件值得当模板读一读：它引用了 `terrain-diffusion-mc:terrain_diffusion`（地形自身的带符号密度，地下深处 +1，空中 -1，在表面过零），这样洞穴就能像原版那样用 `min` 与地形组合；它还由 `terrain-diffusion-mc:noise_caves_enabled` 开关。

#### 洞穴群系

洞穴群系在 Minecraft 里不是一个独立的系统——繁茂洞穴、溶洞群系和深暗之域是和地表群系同一张气候表里的普通行，只是深度是本模组自己的群系规则永远不会产出的那种。与其瞎猜，不如直接问整合包加载的无论哪个主世界群系源要地下，所以模组自己的洞穴群系零配置就位，层带跟随头顶的地形。

## 配置

编辑 `config/terrain-diffusion-mc.properties`，首次启动自动创建：

```properties
# Terrain Diffusion MC 配置

# 推理设备："cpu"、"gpu" 或 "auto"。
# "gpu" 在 Windows 构建上用 DirectML，在 CUDA 构建上用 CUDA。
# GPU 构建默认 "gpu"，这样检测不到 GPU 时启动会大声报错。
# CPU 构建默认 "auto"：macOS 上用 CoreML，否则用 CPU。
inference.device=gpu

# 非活跃模型是否在流水线阶段之间从显存卸载。
# "auto" 在显存够时（约 2.5 GB）让三个模型常驻，不够时卸载；
# 卸载意味着每次阶段切换都重新加载一个模型，每个生成的图块约多花 0.8 秒。
# "true" 总是卸载（峰值 1.5-2 GB），"false" 从不卸载。
inference.offload_models=auto

# 相邻模型窗口之间的重叠："full"（默认）把每个解码器像素生成约两遍并混合，
# "reduced" 加大解码器步幅，生成更快，但留给混合去遮掩窗口边界的余量更小。
# 这会改变生成的地形，所以建世界前选好并保持。
inference.window_overlap=full

# 对 .minecraft/terrain-diffusion-models 里的已有文件校验 SHA-256。
# 如果你想提供自定义模型/配置文件而不做哈希校验，设为 false。
validate_model=true

# 本地地形浏览器 Web UI（/td-explore）的端口。
explorer.port=19801

# 出生点搜索：在 (0, 0) 附近找陆地出生点用的粗粒度像素区域尺寸。
# 从 initial_size x initial_size 开始，每步扩大 8，直到 max_size x max_size。
# 每个粗粒度像素覆盖很大面积，所以 16-128 通常就够了。
spawn_search.initial_size=16
spawn_search.max_size=128

# 放置在地形之上的程序化地表结构（巨砾、石柱、天然拱……）。
# 只想要原版地貌的话就关掉。
surface_features.enabled=true

# 温暖山地带：在约五分之一的世界里，海平面以上的温度直减率被放宽到
# lapse_c_per_km（正常约 -6.5），所以那里高大的山脉保持温带或炎热，
# 而不是总是变雪。只改变带内的群系与河流雕刻；建世界前选好并保持。
climate.warm_mountains.enabled=true
climate.warm_mountains.lapse_c_per_km=-1.5

# 由地面温度决定的积雪深度（1-7 层，最冷处一个雪块加若干层）。
# 纯装饰；改动只影响新区块。
snow.depth_scaling=true
snow.max_layers_over_vegetation=6

# 洞穴。各项作用见上文 "洞穴" 一节；它们全都改变生成哪些洞穴，
# 所以建世界前选好。
caves.lift_carvers=true
caves.density_compensation=true
# 把每个到达地表的洞穴层带一路跑到山顶，而不是停在拉升后的顶部
# （世界尺度 2 时 Better Caves 的隧道停在 y=97，而山顶在 y=400-550 上下）。
caves.reach_summits=true
# 仅限 1.20.1 和 1.21.1，YUNG's Better Caves 内置于这两个版本的 jar。
caves.bundled_cave_mod=true
# 给 Better Caves 在山体里加一层洞窟（尺度 2 时从约 y=177 到山顶）；
# chance 是持有洞窟的洞窟区域占比，0..100。
caves.mountain_caverns=true
caves.mountain_caverns.chance=30
# 密度函数里的原版风格噪声洞穴，扩展到高世界。"auto" = Better Caves
# 不在场或未启用时开启；1.21.11 的大型洞穴就来自这里。
caves.noise_caves=auto
```

### 按世界设置

对 Terrain Diffusion 世界，在创建世界时点 **Customize**（自定义）并设置：

- `World Scale`（世界尺度），整数 `1..6`。

这个值随世界存档保存，并影响：

- 每格方块代表多少真实世界米数（`scale=1` => `30m/格`，`scale=2` => `15m/格`，依此类推）；
- 新建世界的最大高度；
- GPU 推理与 CPU 区块生成之间的性能平衡。

推荐 `2`，尺度与可玩性兼顾。想要更小、更压缩的世界就用 `1`。值越低对 GPU 压力越大，因为 Terrain Diffusion 跑得更频繁；值越高对 CPU 压力越大，因为世界高度更大。

## 从源码构建

构建期间需要联网，用来拉取 Minecraft、加载器依赖，以及 Hugging Face 上钉定版本的模型清单元数据。

Windows/DirectML 构建需要 `libs/onnxruntime-dml.jar`，它作为本仓库的一部分提供。从源码构建它见[用 DirectML 构建 onnxruntime](#building-onnxruntime-with-directml)。

### 工作区布局

本仓库是一个多版本 Gradle 工作区。

```text
common/                  跨 Minecraft 版本稳定的共享 Java 代码
loaders/fabric/          共享的 Fabric 加载器代码与元数据
loaders/neoforge/        1.21.x 的共享 NeoForge 加载器代码与元数据
libs/                    共享的本地运行时 jar，含 onnxruntime-dml.jar
gradle/conventions/      共享的 Gradle 约定脚本
terrain-diffusion-models/ 开发运行时自动创建的共享模型资产
versions/1.20.1/         Minecraft 1.20.1：common + fabric + forge
versions/1.21.1/         Minecraft 1.21.1：common + fabric + neoforge
versions/1.21.11/        Minecraft 1.21.11：common + fabric + neoforge
```

对 Minecraft API 敏感的文件放在 `versions/<minecraft-version>/common` 下。这是有意的：mixin、世界生成 JSON、维度类型、群系源注册和客户端的世界创建界面，稳定性不足以留在根部的 `common` 文件夹。

所有 Gradle 开发运行共享仓库根部的 `terrain-diffusion-models/`，所以切换 Minecraft 版本或加载器不会重新下载一份模型资产。分发 jar 不吃这个开发期覆盖，生产环境继续用 `<游戏目录>/terrain-diffusion-models`。

### Java 版本

使用与目标 Minecraft 版本匹配的 Java。

| Minecraft | 所需 Java |
|---|---|
| **1.20.1** | Java 17 |
| **1.21.1** | Java 21 |
| **1.21.11** | Java 21 |

各版本的 `gradle.properties` 设了 `java_version`，但你的 JDK/工具链仍然需要对 Gradle 可见。

#### Minecraft 1.20.1

```powershell
cd .\versions\1.20.1
..\..\gradlew.bat :fabric:runClient
..\..\gradlew.bat :forge:runClient
```

干净运行的命令：

```powershell
cd .\versions\1.20.1
..\..\gradlew.bat :common:clean :fabric:clean :fabric:runClient
..\..\gradlew.bat :common:clean :forge:clean :forge:runClient
```

#### Minecraft 1.21.1

```powershell
cd .\versions\1.21.1
..\..\gradlew.bat :fabric:runClient
..\..\gradlew.bat :neoforge:runClient
```

干净运行的命令：

```powershell
cd .\versions\1.21.1
..\..\gradlew.bat :common:clean :fabric:clean :fabric:runClient
..\..\gradlew.bat :common:clean :neoforge:clean :neoforge:runClient
```

#### Minecraft 1.21.11

```powershell
cd .\versions\1.21.11
..\..\gradlew.bat :fabric:runClient
..\..\gradlew.bat :neoforge:runClient
```

干净运行的命令：

```powershell
cd .\versions\1.21.11
..\..\gradlew.bat :common:clean :fabric:clean :fabric:runClient
..\..\gradlew.bat :common:clean :neoforge:clean :neoforge:runClient
```

### 构建单个 Minecraft 版本

在仓库根部，这些任务为单个 Minecraft 版本构建所有已配置的加载器和运行时变体：

```powershell
.\gradlew.bat buildMc1201
.\gradlew.bat buildMc1211
.\gradlew.bat buildMc12111
```

### 按运行时变体构建单个 Minecraft 版本

在仓库根部，这些任务为单个 Minecraft 版本和单个运行时变体构建所有已配置的加载器。

#### Windows / DirectML

```powershell
.\gradlew.bat buildMc1201Windows
.\gradlew.bat buildMc1211Windows
.\gradlew.bat buildMc12111Windows
```

#### CUDA

```powershell
.\gradlew.bat buildMc1201Cuda
.\gradlew.bat buildMc1211Cuda
.\gradlew.bat buildMc12111Cuda
```

#### CPU / CoreML

```powershell
.\gradlew.bat buildMc1201Cpu
.\gradlew.bat buildMc1211Cpu
.\gradlew.bat buildMc12111Cpu
```

### 在版本文件夹内构建

在某个版本文件夹里，用该版本自己的 Gradle 任务。

#### 1.20.1：Fabric + Forge

```powershell
cd .\versions\1.20.1
..\..\gradlew.bat buildAllVariants
..\..\gradlew.bat buildWindows
..\..\gradlew.bat buildCuda
..\..\gradlew.bat buildCpu
..\..\gradlew.bat buildFabricWindows
..\..\gradlew.bat buildFabricCuda
..\..\gradlew.bat buildFabricCpu
..\..\gradlew.bat buildForgeWindows
..\..\gradlew.bat buildForgeCuda
..\..\gradlew.bat buildForgeCpu
```

#### 1.21.1 和 1.21.11：Fabric + NeoForge

```powershell
cd .\versions\1.21.1
..\..\gradlew.bat buildAllVariants
..\..\gradlew.bat buildWindows
..\..\gradlew.bat buildCuda
..\..\gradlew.bat buildCpu
..\..\gradlew.bat buildFabricWindows
..\..\gradlew.bat buildFabricCuda
..\..\gradlew.bat buildFabricCpu
..\..\gradlew.bat buildNeoForgeWindows
..\..\gradlew.bat buildNeoForgeCuda
..\..\gradlew.bat buildNeoForgeCpu
```

```powershell
cd .\versions\1.21.11
..\..\gradlew.bat buildAllVariants
..\..\gradlew.bat buildWindows
..\..\gradlew.bat buildCuda
..\..\gradlew.bat buildCpu
..\..\gradlew.bat buildFabricWindows
..\..\gradlew.bat buildFabricCuda
..\..\gradlew.bat buildFabricCpu
..\..\gradlew.bat buildNeoForgeWindows
..\..\gradlew.bat buildNeoForgeCuda
..\..\gradlew.bat buildNeoForgeCpu
```

## 常见问题

**动态链接库（DLL）初始化例程失败**

某些较老的 Java 版本会出这个问题。使用你的 Minecraft 目标版本所要求的 Java：

- Minecraft 1.20.1 用 Java 17。
- Minecraft 1.21.x 用 Java 21。

已知 [最新版 Microsoft OpenJDK 17 和 21](https://learn.microsoft.com/en-us/java/openjdk/download) 可用。

**LoadLibrary 失败，错误码 126** *仅限 CUDA 构建*

通常是 CUDA 或 cuDNN 安装不当。排查步骤见 [CUDA_INSTALL.md](CUDA_INSTALL.md)。

**java.lang.IllegalStateException: Failed to load terrain-diffusion models**

这通常意味着内存不足，或者加载器在运行时看不到 ONNX Runtime 依赖。Terrain Diffusion 的模型约占用 2.5 GB 内存，确保分配够了。

**如果你的问题仍未解决，请[在这里提 issue](https://github.com/xandergos/terrain-diffusion-mc/issues/new)。**

## 用 DirectML 构建 onnxruntime

**要求**

- [Windows 10 SDK (10.0.17134.0)](https://developer.microsoft.com/en-us/windows/downloads/sdk-archive/index-legacy) —— 对应 Windows 10 版本 1803 或更新
- Visual Studio 2017 工具链 —— 从 VS Installer 安装 *Desktop development with C++*（使用 C++ 的桌面开发）
- Visual Studio 2022 工具链 —— 同上
- Python 3.10+：[https://python.org/](https://python.org/)
- CMake 3.28 或更高

两条 VS 工具链都保持更新。完整细节见 [ONNX Runtime 构建文档](https://onnxruntime.ai/docs/build/inferencing.html) 和 [DirectML EP 要求](https://onnxruntime.ai/docs/execution-providers/DirectML-ExecutionProvider.html#build)。

**步骤**

所有命令都在 **VS 2022 的 Developer Command Prompt** 里运行。

**步骤**

所有命令都在 **VS 2022 的 Developer Command Prompt** 里运行。

```powershell
git clone --recursive https://github.com/Microsoft/onnxruntime.git
cd onnxruntime
.\build.bat --config RelWithDebInfo --build_shared_lib --parallel --compile_no_warning_as_error --skip_submodule_sync --use_dml --build_java --build
```

构建出的 jar 出现在 `java/build/`。把它改名为 `onnxruntime-dml.jar`，放进本仓库的 `libs/`。

## 给模组开发者的说明

AI 地形本身改起来相当复杂，但与 Minecraft 生物群系的整合是被刻意做得容易上手的。模型输出高程 + 4 个气候变量，把它们变成 Minecraft 群系的是**一份数据文件，不是代码**：[`biome_catalog.json`](versions/1.21.1/common/src/main/resources/biome_catalog.json) 列出每个群系以及它可以出现的气候窗口，外加一个权重，说明它该占重叠区域的多少。你可以一次编译都不用重做，就把全世界的群系布局重新调好——把改过的副本丢进 `config/terrain-diffusion-mc/` 即可。

值得了解的几块：

- [`BiomeClassifier`](common/src/main/java/com/github/xandergos/terraindiffusionmc/pipeline/BiomeClassifier.java) 逐像素推导气候变量并对噪声场采样。
- [`BiomeRuleEngine`](common/src/main/java/com/github/xandergos/terraindiffusionmc/biome/BiomeRuleEngine.java) 在一个像素上所有够格的群系里按 `rarity` 加权挑选，使用一个空间上连贯的噪声场，所以结果是真实的成片分布，而不是逐像素的五彩纸屑。
- [`tools/biome-lab`](tools/biome-lab) 是一个独立的 Python 工具，用蒙特卡洛方法把一份目录对着真实气候分布做模拟。它存在的原因是这里的失败是无声的：一条规则可以微妙地自相矛盾，让某个群系永远刷不出来，却没有报错、崩溃或警告。任何目录改动之后都跑一下它——它会报告死条件、永不生成的群系和有效群系数量。

地形多样性的上限远超群系多样性，把这段差距填上是个真实的机会。我希望有人能在这上面玩出花来。
