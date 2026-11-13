> [!IMPORTANT]
> Please use an LLM instead of a traditional translator to translate this readme, because I've used a lot of colloquial expressions that are hard to translate. I feel that my work isn't quite enough to get people to notice this project yet, but I will improve it in the future ..

感谢xandergos

感谢derekvawdrey

**这个项目实际上fork于 https://github.com/derekvawdrey/terrain-diffusion-plus 绝大部分东西都是他做的**

我觉得我不是很能胜任这个项目的完全开发工作,我感到力不从心,这是实话

这个项目是真的很牛逼,我感觉我能学到很多新东西

我会尽量的专心对待它,尽量的优化(即使我觉得我的技术还不够)

我会定期的审查我写的代码

pr很麻烦,我可能要把我写的注释都转成英文,或者都删掉,这真的很麻烦

我会尽量列一个清楚的改动对比清单,包含derekvawdrey的和我的


derekvawdrey
- 内置了 YUNG's Better Caves 作为洞穴生成, derekvawdrey也自己写了一个偏向"原版"的洞穴生成系统,但是bug很多

我
- 修复了进入世界时的数据包损坏问题
- 峡谷的生成逻辑回归mc原版, 相较于tdp(上游terrain-diffusion-plus)原版来说概率降低,允许高度降低
- 地表30格之内不允许生成洞穴, 这个地表逻辑是从天上往下看,只和y轴相关,也就是说洞穴依然可以在悬崖的侧面造成开口, 和上一条一起看,综合效果是地面几乎不会再有破口
- 调整了ybc(YUNG's Better Caves)的配置,把生成在紧贴基岩层的"河流"的河床高度和河面高度都提高了一点, 现在无论是岩浆河还是水河都不会像以前一样长进基岩里
- 地下河的液体区域占比默认改成水25%岩浆75%(ybc原版40%/60%),更偏岩浆,可用`liquidregions.json`里的`water_region_spawn_chance`覆盖

待办
- 在主世界添加大型岩浆河

## carver_altitudes.json（雕刻器高度规则，用户可编辑）

`config/terrain-diffusion-mc/carver_altitudes.json` 声明哪些雕刻器的配置键是绝对海拔、哪些雕刻器整个豁免重写。首次启动会自动把默认规则写成这个文件；此后文件就是唯一规则来源——改文件（重启生效）、删条目即失效，不用动 jar。

```json
{
  "altitudeKeys": {
    "bettercaves:better_cave": ["bottom_y", "top_y"]
  },
  "excluded": [
    "minecraft:canyon"
  ]
}
```

- `altitudeKeys`：键是雕刻器**类型** id（配置 JSON 的 `type` 字段），值是该配置里表示绝对海拔的键名，这些数值会随世界垂直拉伸被抬升，其余字段一概不碰；没有声明的类型原样直通。
- `excluded`：整个豁免重写的**已配置**雕刻器 id。默认含 `minecraft:canyon`：原版峡谷是稀有近地表地貌（概率 0.01，y 10..67），被 summit 扩展+密度补偿改写会变成高频穿山天窗，故默认放过，只 carve 它原生那一段。
- 文件解析失败时：原样保留、日志报错，内存临时用默认规则顶上，绝不覆盖手改。
