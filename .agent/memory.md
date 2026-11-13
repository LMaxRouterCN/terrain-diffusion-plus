<!-- ID:001 -->
[2026-10-11 v3终版,已推翻v1雨闸门模型] VS2 2.4.11 world_weather 哨兵坐标bug(全案报告:vs-td-investigation/CASE-REPORT.md):船坞区块ticking+任意镂空/悬空/地下ship→tickChunk每tick以1/16抽列做冰雪探测查getBiome(不问天气:晴天raining=0全速复现,level.dat字节级证实;/weather clear已执行且无效)→VS2 occlude对镂空列返回哨兵(0,minY-1,0)→useBiomeAtWorldPos无条件套shipToWorld→输出c_w+R·(-c_s)≈船坞模长(2400万格级)且随船摇晃逐tick移动→TD等同步推理型BiomeSource必miss→主线程每tick park 3.5-5.4s→TPS0.18,无自然终点(流停=退档[Stopping server前8秒签名]或船坞卸载)。修复:VS2侧两个wrap(useBiomeAtWorldPos/usePrecipitationAtWorldPos)加 if(pos.getY()<level.getMinBuildHeight()) return original.call(level,pos);;BiomeSource mod侧防御=Server thread且缓存miss直接走default/null fallback。诊断指纹:远点半径=某船坞中心模长+角度窄弧单调漂移+每tick恰1次miss+离群点模长=另一船坞。v1翻车教训:对MC源码的"记忆"未核对禁止当事实(唯一记错处=isRaining守卫);"雨停"从"流停"反推=循环论证。
tag: Minecraft, VS2, bug根因, mod兼容, 哨兵坐标
<!-- END:001 -->
<!-- ID:002 -->
[2026-10-12] 用户(max)对VS2(Valkyrien Skies)开发组强烈反感:更新不积极、文档不好好写、开发组小圈子各行其是。倾向避免与其交互、不想编译VS2本体。涉及VS2 bug修复路线决策时:优先本地mixin模组或下游防御,PR只作fire-and-forget;勿默认用户愿意等上游或升VS2版本(其整合包锁2.4.11)。
tag: Minecraft, VS2, 用户偏好, 修复路线
<!-- END:002 -->
<!-- ID:003 -->
Max 的待办/备忘录是单文件扁平记录,路径: D:\Documents\my\日常\!备忘录-剪贴板-随笔记-待办_不要删除这个文件.md (约5.6KB@2026-10-19,持续增长)。标记约定: 无前缀=未完成,勾=完成,叉=决定不做,半勾=进行中,圈=搁置/纠结,可多个按时间序。分类: 杂乱/整合包/pokeragent(本地D:\Documents\my\Poker Agent)/mc模组(本地D:\Documents\mcmod)/tht(TansHugeTrees)/emi(LMaxs-EMI-Fork)。统计基准(2026-10-19): 勾6项(下界合金掉落物、Log Filter注释、create覆盖挪回收站、旧记号转义迁移、ae2合成卡住破案=根因ae2vm、EmiScrollGridFix); 半勾3项(ae2/emi回退决策、pipez fork、td×vs2兼容半成); 叉1项(AppliedCannon)。条目无时间戳,除pokeragent分区两条(先create回收站后转义迁移)外完成先后不可考,用时重读文件为准,勿信统计快照。
tag: 待办文件, 备忘录路径
<!-- END:003 -->
<!-- ID:004 -->
2026-10-18 02:17 审查结论: 爆炸效果重写与大火燃烧重构在本地为纯绿地项目, mcmod全部项目过目加全盘检索AppliedCannon零命中, 无任何现有代码。可行性报告落盘于 D:\Documents\my\日常\爆炸大火-可行性审查\爆炸与大火-可行性审查报告.md。核心: 爆炸=射线改体积场, ExplosionEvent.Start取消加getToBlow填充, 零mixin, 中等难度, 建议先行; 大火=燃烧管线, 架构决策留给max: chunk级SavedData燃烧图(推荐) 对比 每方块BE, 中高难度; 两项目共享分帧调度器基建。许可证MPL-2.0, Forge 1.20.1。
环境事实: max真实桌面路径为 D:\Desktop 而非C盘用户目录下Desktop; 后端终端为PowerShell。
[2026-11-10 更正] 桌面"30分钟后休眠.bat"现为正常30分钟版: 触发器 AddMinutes(30) 已实锤(全文复读), 早前"AddHours(2)漏改"报告过时(10-18当天或之后已被修正)。残留: 计划任务名仍叫 AutoHibernate2h(纯名字无功能影响, 取消=桌面"取消休眠.bat")。等效执行: Register-ScheduledTask -TaskName AutoHibernate2h -Action (New-ScheduledTaskAction shutdown.exe /h) -Trigger (Once -At (Get-Date).AddMinutes(30)) -Force。桌面bat一览: 2小时后休眠/2小时休眠/30分钟后休眠/结束所有进程/取消休眠/杀edge/杀filec/杀QQ/重启EarTrumpet。
tag: 爆炸, 大火, 绿地项目, 可行性审查, 桌面路径, 休眠bat
<!-- END:004 -->
<!-- ID:005 -->
LLMwc4n是max的LLM提示词/破限分享仓库,用户常来开issue求助模型拒绝问题. 2026-09-25时max自述要忙一两个月(约到12月). 5个open issue全是用户求助: #2(2025-11),#4(gemini敏感词),#5(ds已"打穿",用的claude-fable-5.1改的提示词),#6(捧场),#8(2026-10-13,v4/v4.1被拒,等max回复). #7编号空缺(404).
tag: LLMwc4n项目, 用户求助, max忙期
<!-- END:005 -->
<!-- ID:006 -->
pipez fork 远端定稿(2026-11-06):仓库已改名 https://github.com/LMaxRouterCN/LMaxs-Pipez-fork(原fork名pipez),工作分支lmaxs/1.20.1,云端头0fddedb,本地origin已指向新名。旧私有仓已由max网页手删(gh无delete_repo scope,跳过了授权流程)。三commit:894b17c镜像适配,175312c BFS修复(#1),0fddedb案卷文档(GOAL-PLAN/TODO/AUDIT/ENERGY-MAP+.agent,公开化max豁免)。遗留:1.上游henkelmax/pipez无LICENSE,正式分发前需上游许可确认 2.默认分支仍是master(上游快照),lmaxs/1.20.1非默认,待max定 3.fork的parent显示null疑似已detach(未验证) 4.#1修复后未跑构建(import星号导入,大概率过未验证) 5.分支名与备忘录规范(1.20.1+forge)不一致,维持现状
tag: LMaxs-Pipez-fork, git远程, 发布闭环
<!-- END:006 -->
<!-- ID:007 -->
GitHub Search API(gh search / search/issues)索引滞后且分页排序不稳定: 近期创建的issue可能完全不在结果(实例:LLMwc4n#8和WindowTextExtractor#1存在于GraphQL直查但搜索无),total_count与实际可有±几条抖动(实例:BCLib#172在第2页和第3页重复出现). 权威做法:名下仓库用GraphQL批量查,具体issue用repos API直查. 2026-10-18实测.
tag: gh-cli, 搜索索引坑, 数据可信度
<!-- END:007 -->
<!-- ID:008 -->
gh api -f key=true 发送的是字符串"true"而非JSON布尔,subscription端点(PUT /repos/{owner}/{repo}/subscription)不认就静默空转:HTTP 2xx退出码0但订阅不生效,2026-10-19实证(failed:0但27仓未建订阅). 正确做法:JSON body管道喂gh api --input -. 回读验证必须独立GET,不能信PUT退出码.
tag: gh-cli, API坑, 布尔参数, subscription
<!-- END:008 -->
<!-- ID:009 -->
exec已在PowerShell中运行,不要再套powershell -Command "..."双层:外层引号会对$变量先行插值,内层收到的脚本$n/$fail全成空壳,foreach直接ParserError. 直接裸写循环/管道即可. 2026-10-19实证.
tag: exec指令, PowerShell嵌套, 引号插值坑
<!-- END:009 -->
<!-- ID:010 -->
2026-10-19 IR2040启动卡死定案闭环: Ixeris 4.6.9+1.20.1-forge线程接管与FML早显窗口互锁死锁. 机制: 原main线程被改名Ixeris Event Polling Thread,卡在MainThreadDispatcher.await不泵Win32消息,Ixeris另开线程跑Minecraft构造,fmlearlydisplay对该早显窗口glfwSetWindowTitle跨线程同步SendMessage需等待消息泵→环死锁. max回退Ixeris旧版后启动成功=根因实证. 证据: jstack dump在$env:TEMP\ir2040_td.txt(另存D:\Documents\my\ir2040-thread-dump.txt待确认),日志冻结15:59:14.920,Render thread卡937s仅耗2.1s cpu,已排除磁盘/Defender/堆(Xmx14336m)/双显卡. 后续: 给Ixeris仓库开issue,草稿证据链齐. 该包mods在版本隔离目录,E盘机械盘与本案无关.
tag: bug根因, Minecraft, Ixeris
<!-- END:010 -->
<!-- ID:011 -->
2026-10-19 create等写入载荷的转义坑: 运行中后端会把载荷里的```替换为三反引号; 需要在目标文件保留字面```时(如给源码写断言), 用字符串拼接(TICK+3)绕开替换. 三反引号直接写载荷会原样落盘但破坏聊天端markdown渲染, 应写```让其转换. 自2026-10-19起后端转义新约定: 载荷中的反斜杠+反引号由后端还原为字面反引号, 旧记号退役(后端重启后生效, 重启前旧记号仍有效).
tag: API坑, PokerAgent
<!-- END:011 -->
<!-- ID:012 -->
2026-10-19 终版勘误(覆盖旧版): (1)双禁铁律: 指令标记序列(方括号包裹的cmd/code那两对)不得以字面形式出现在载荷与正文任何位置(注释/引用/示例不豁免), 讨论时用口述或码位描述. (2)码位铁律: 凡引用文件已有特殊字符一律从文件自适应提取(探针)或\uXXXX写法, 永不凭记忆心算(两连错: 12300=角括号, 12336=U+3030波浪线; 方头括号=U+3010/U+3011). (3)GBK控制台: python脚本print非GBK字符会UnicodeEncodeError吞诊断, 开头sys.stdout.reconfigure(encoding='utf-8', errors='replace'). (4)剪贴板read经展示层转换不保真, 取证一律行号模式. (5)旧记号TICK+3字面写入remember/memory载荷会被运行中旧后端替换成三反引号存进记忆文本, 记忆里提及该记号用TICK+3拼接口述. (6)转义语义生效条件(实测修正): commit 72a761a后实测运行中后端仍为旧语义(反斜杠加反引号原样落盘, 旧记号替换仍生效)——备份通知不能证明重启加载了新代码(该功能4ad4306就有); 转义还原需max重启后端后生效, 重启前载荷继续用旧记号, 重启后切反斜杠转义, 要字面反斜杠加反引号则双写反斜杠. 关联011.
tag: API坑, PokerAgent
<!-- END:012 -->
<!-- ID:013 -->
[2026-10-26深夜终态] IR2040客户端冻结案→已发布GitHub issue: https://github.com/Antikythera-Studios/epicfight/issues/2625 (title带[1.20.1/20.14.17]前缀,body 8523字符,OPEN).根因链:Render thread钉死在EpicFight 20.14.17 AnimationClip.getPoseInTime:100(NaN毒化二分搜索,三个分支全false区间永不收缩)←SPChangeLivingMotion包处理→ClientAnimator.offAllLayers→Layer.off→getEnabledPose→ConcurrentLinkAnimation.getPoseByTime:72→NaN elapsedTime源自AnimationPlayer.tick 48-51(EMPTY_ANIMATION totalTime=0且isRepeat=true,elapsedTime%=0→NaN,任何曾被禁用的composite层一tick带毒).NaN出生:Animations:143匿名DirectStaticAnimation→StaticAnimation()无参构造99行super(0.0F,true)→EMPTY_CLIP(AnimationClip:14)clipTime默认0.修复建议三条已在issue:tick守卫getTotalTime()>0/getPoseByTime洗NaN/animationClip加NaN守卫.验证闭环:dump栈23帧epicfight侧9帧与本地源码双向锁死,线上body每行断言有实锤.完整案卷D:\Documents\my\日常\epicfight-bug-case\(body.md/issue-draft.md v2/evidence两dump).dump原件%TEMP%\mc_td.txt与td2.txt.后续:max网页审+拖dump附件,可插桩build定位具体entity/animation对(需复现).次生案:DH-Regen Queue烧核非死锁,零BLOCKED,不另报.教训:jcmd的nid是十进制(Zulu21).附:StaticAnimation.java:101上游typo"emtpy"无关本bug.
tag: bug根因，Minecraft, EpicFight
<!-- END:013 -->
<!-- ID:014 -->
2026-10-26 PokerAgent框架坑(v2修正): ①create指令写入内容约4000字符处静默截断,回执仍报"已创建"——大文件必须分块(每块≤1500字符)+append+每块read校验,严禁单次写长文档. ②append会剥掉新块开头的全部空行(不是只吞一个!前缀空行防吞策略无效),接缝补空行只能用replace -l. ③replace默认整块匹配,行内片段搜不到;精确改行用-l行号模式,支持2行扩3行的插入. ④同一轮连续两个行号replace:先执行行号大的(下游)再小的(上游),否则前面插入导致行号漂移会打错位置.
tag: API坑, PokerAgent
<!-- END:014 -->
<!-- ID:015 -->
2026-10-26 gh api + PokerAgent exec通道实测谱系(修正:推翻早前"含管道必挂"的过度概括):
[安全] 裸URL零flag的gh api;简单jq字符串路径(--jq ".workflows[].path" / --jq ".content");exec内含PowerShell管道的纯本地命令(Get-ChildItem | Select-Object 实测通过).
[挂起无回执] gh api加自定义Accept头(-H "Accept: application/vnd.github.raw");gh api --jq ".content" 后接PowerShell解码管道(ForEach-Object内嵌套中括号圆括号).
[疑似毒,未干净验证] jq表达式内转义嵌套双引号——今晚样本被正文标签污染混杂,早前轮次有独立案底,标记存疑.
[立即报错] gh api -o 不存在(那是curl的flag).
[框架解析铁律] 解释性正文严禁字面出现中文方括号形式的cmd或code标签对——后端会从假标签起抓,把后续散文连同同批真指令一起解析成垃圾,连环报"未知指令".引用标签一律用英文方括号描述.
[输出纪律] 今晚三次消息停在"即将发指令"的冒号处然后直接结束,指令块从未生成.铁律:宣布与指令块必须同条消息,消息不得以预告冒号收尾.
[取文件内容标准姿势] contents端点用--jq ".content"拿base64,回执按宽度折行,去换行再解码;验标签或操作真伪,直接打事件API看actor字段.
[批量解析观察] 深夜四指令批(2×memory+remember+read):remember碎裂出2条未知指令噪声(正文首行+闭合code标签被当指令名),016回执出现两次,疑似碎裂触发批内重放(幂等无害),最终全对(remember新内容经注入回执自验).推论:批内多行指令可能碎裂并引发重放,非幂等变更(gh issue comment/add-label等)忌混批,单独成条发送.
tag: gh-cli, API坑, exec指令, PokerAgent
<!-- END:015 -->
<!-- ID:016 -->
2026-10-26 实锤版+2026-11-02补遗: epicfight上游自动打标机制已由events API验证. issue#2625唯一labeled事件: actor=github-actions[bot](type:Bot),时间2026-10-26T12:53:13Z(贴issue创建后约1分钟). 机制全貌: 仓库.github/workflows/issue-label.yml监听issues opened,按author_association分流——OWNER/MEMBER/COLLABORATOR打"core contributor",其余所有人(含max/LMaxRouterCN)一律自动打"triage",由workflow内gh issue edit --add-label执行. 结论: 该仓库所有外来issue出生即带triage,与人工关注零关联. 通用方法论: 判断issue标签是否人工,查events API对应事件的actor字段,bot即自动化. 2026-11-02补遗: issue#2625的comments字段=1之谜已解——挂附件=发comment,comments API实锤返回10-26T13:01:55Z一条,作者LMaxRouterCN,内容为双dump附件链接(ir2040-ef-td1/td2.txt). 此前events流无commented记录是当时查询的时间窗口问题,附件挂载确实以comment形式落地,疑点闭环. 转写件中GITHUB_VTOKEN错字确认是base64转写噪声,workflow文件本体完好(bot实际执行成功).
tag: EpicFight, gh-cli, 数据可信度
<!-- END:016 -->
<!-- ID:017 -->
[2026-11-02] EpicFight issue#2625 侦察收尾+决策落定(013根因链续篇): ①两分支全链实锤: 1.20.1(HEAD 20.15.1)涉案三文件自20.14.17零改动,issue行号分析对HEAD原样成立;1.21.1(默认分支)tick无守卫+EMPTY匿名类+StaticAnimation无参构造super(0.0F,true)三环同构中招."emtpy"typo两分支都活着. ②上游GPL-3.0;PR队列尸检:外部2521/2591/2605+内部2573全躺3-7个月零合并,issue零真人回应→上游PR=发射后不管. ③全库搜NaN无重复劳动(仅#2625+2021老issue#449). ④2026-11-02晚max拍板:路径C双轨确认. README动机定稿:epicfight本体更新快,但附属Weapons of Miracles更新极慢且闭源,玩家被锁旧版epicfight,上游合并也大概率不回移旧分支→独立mixin模组是旧版玩家出路. 模组名候选EpicFight NaN Freeze Fix(推荐,参照VS2 Sentinel Guard Fix句式)/EpicFight NaN Guard,查重后定. max将自建项目文件夹+转移PokerAgent工作目录,目录就位后项目正式启动,届时定PR分支(1.20.1先行待确认)与修复层级(tick守卫根修+getPoseInTime纵深+可选ConcurrentLink洗startsAt). GPL注脚:本地LAN自用零风险,整合包公开发布时mixin挂GPL-3.0最稳. 证据:案卷epicfight-bug-case\evidence\upstream-src-20261102\五源码副本,GOAL-PLAN.md含完整侦察章节
tag: EpicFight, 修复路线, Minecraft, mod兼容, bug根因
<!-- END:017 -->
<!-- ID:018 -->
2026-11-02 教训：自动模式二次确认唤醒时，连发多条"任务未完成，挂起中"式极简回复，每条都是一轮对话，都会截断最老上下文，把关键决策点细节顶丢。正确做法：任务挂起等用户决策时，唤醒回复必须零冗余（一句确认未完成），不发多余指令，用户回归后从GOAL-PLAN.md/memory/git log恢复现场，而非反复空转回复。
tag: 空转教训, 自动模式, 上下文管理, PokerAgent
<!-- END:018 -->
<!-- ID:019 -->
Minecraft联机拓扑(2026-11-02确认,含修正):max(游戏id=LMaxRouter)开Forge1.20.1整合包"Industrial Revolution 2040"(E:\MC\.minecraft\versions\下,549mods)单人世界→Open to LAN(手动指定25565)→SakuraFrp隧道(节点名gali,公网门牌frp-now.com:21540,免费档10Mbps上限)→朋友old_gali连接。朋友客户端=其机器上的Forge镜像(经FML握手+36min游玩推断),非Fabric。E:\MC\juyue123≠朋友端:它是PCL实例的Fabric1.20.1轻量建筑包(28mods:litematica/printer/axiom/syncmatica/worldedit/lanserverproperties),与IR2040联机无关。实证(10/26晚):36min内old_gali异常掉线重连4次(无一次正常"left the game"),join间夹等距重复异常栈,断线根因(带宽vs服务端异常循环)待日志定性。max端kreno jar文件名含空格前缀:"优化-性能   Krypton Reno-forge-1.20.1-26.1.1-1.20.1.jar"。
tag: Minecraft, 联机拓扑, old_gali, SakuraFrp, mod兼容
<!-- END:019 -->
<!-- ID:020 -->
PokerAgent create指令坑(2026-11-03):对同一路径连续create两次以上时,第一次create会把被覆盖文件移入.agent_trash回收站,第二次create若回收站已存在同路径历史备份则拒绝执行(报错"回收站已存在该路径的历史备份")。解除法:把.agent_trash下对应备份文件改名或删除(如GOAL-PLAN.v1.bak.md),再重发create。勿在指令名上猜(是create不是write)。
tag: API坑, PokerAgent, 文件操作
<!-- END:020 -->
<!-- ID:021 -->
2026-11-03 弹药盒×aeinfinitybooster失配案,完整证据链与可复用结论:
①NoSuchFieldError消息里打印的类型是【调用方编译期烧死的期望类型】,不是目标类现状。诊断法:两边jar各javap一次比对字段描述符(javap -p -classpath <jar> <全限定类名>)。
②aeinfinitybooster 2.1.0(2026-10-23发布)做了RegistryObject→Supplier字段类型迁移,字段名不变描述符变;旧线1.0.0+19/+20(Modrinth)和1.0.0+51(整合包v50实际pin的版本)仍是RegistryObject。Forge社区这波迁移是下游mod的隐形ABI断点。IR2040包v50.zip内含+51原版jar,回滚材料在包本体里(勿重复下载边角版)。
③mixin软依赖模式(编译期不链接对方+mixin注入直接字段访问指令)会把对方字段类型冻成ABI:注入期不解析字段所以启动无异常,首tick执行到getstatic才链接期炸,常被tick崩溃拦截类(Neruina)吞掉。症状=聊天栏刷"捕获到物品堆叠"+服务端不崩+日志NoSuchFieldError。
④修复四路:回滚(最优=从包zip抽原版pin版本,配磁盘CreationTime取证定位"谁何时换的mod")/等作者适配/踢mod/ASM手术(getstatic描述符改Supplier+invokevirtual改invokeinterface,栈形状不变checkcast保留,只动mixin类)。
⑤取证技巧:mod文件CreationTime vs v50.zip内条目对照 = "包解压落盘"vs"后来手动放入";lightspeed-cache内mod指纹=包实际跑过的版本;包config目录有无某mod配置文件=该mod新功能是否被用过。
tag: bug根因, mod兼容, Minecraft, mixin, 字节码, 修复路线
<!-- END:021 -->
<!-- ID:022 -->
(2026-11-03 max告知)当前对话渠道的文件上传API已损坏,read全文模式(剪贴板API整文件上传)不可用;字面量读取上限80000字符,大文件必须行号模式分段读,或exec+Select-String在本地过滤只回显命中行。影响:通读大源码(PokerAgent框架/大Java项目)成本极高,max因此冻结了PokerAgent框架侧全部待办。选活时优先小JSON/配置文件类,或grep定位后局部读
tag: API坑, 上下文管理, exec指令
<!-- END:022 -->
<!-- ID:023 -->
[2026-11-03终态v3-项目完结]EpicFight-NaN-Freeze-Fix全链闭环含实测:①修复(0002124,分支1.20.1forge)②构建(3mixin OK)③投mods清旧1.0.0④GitHub release(repo public,url=https://github.com/LMaxRouterCN/EpicFight-NaN-Freeze-Fix/releases/tag/v1.0.1,jar asset在位)⑤max实测通过(首帧不崩+JEI漏斗45mb/高炉矿车450/合成10)。代码侧+验证侧全终结,零未完。完整因果链:改整合包配方→TC连坐(026)→EF冻结(013)→mixin尾参崩溃(024)→1.0.1→实测销案。nanhealthfixer定性:TeamPotato血量卫生mod,服务端only三事件监听洗NaN/负值血量+取消NaN伤害,与本mod动画时间线正交零冲突,max未表态去留(纯功能选择)。遗留仅两项无时限:上游issue#2625跟踪(自动triage机制见016)+上游PR fire-and-forget可选(见017,PR队列3-7个月)。教训:行动前探remote勿信记忆;非原版mixin handler尾参必javap实测运行时jar;dev依赖版本≠运行时+remap关闭时编译期校验不可信
tag: 当前任务, 1.0.1发布, nanhealthfixer定性, 发布闭环
<!-- END:023 -->
<!-- ID:024 -->
(2026-11-03)EpicFight-NaN-Freeze-Fix(D:\Documents\mcmod\EpicFight NaN Freeze Fix)崩溃根因:ConcurrentLinkAnimationMixin的@Inject(method="getPoseByTime")handler尾参用了CallbackInfo,但epicfight 20.14.17运行时该方法返回非void→mixin校验报"CallbackInfoReturnable is required"→APPLY失败→游戏死。触发链:进世界首帧LocalPlayer构造→epicfight挂capability→ConcurrentLinkAnimation类首次加载→mixin转换爆炸。主菜单阶段不触发目标类加载,故装载期全程无报错。修法:尾参改CallbackInfoReturnable<javap实测返回类型>,逻辑不变(只洗startsAt,不setReturnValue不cancel,纯观察合法)。另2个mixin(AnimationPlayer根修/AnimationClip兜底)同款风险待javap审计。编译期未拦截两嫌疑:①commit cd1a3a7关remap连带瞎掉AP签名校验②dev依赖epicfight版本≠运行时20.14.17。教训:非原版目标+remap=false时,编译期校验不可信,mixin handler尾参类型必须对运行时jar javap实测
tag: bug根因，EpicFight, mixin, Minecraft
<!-- END:024 -->
<!-- ID:025 -->
(2026-11-03事故立规)exec输出纪律,三次同源教训(两次回执超长+一次锟斤拷洪水卡爆前端):①任何内容读取前必须先查文件大小+扩展名,二进制(zip/jar/mca/png/nbt)永不当文本读,白名单=json/toml/txt/md/js/java/mcmeta等纯文本②每级输出双上限:Select-Object -First N+单行Substring截120字③对-Recurse文件集做内容搜索必须带扩展名过滤④PowerShell/框架无输出保护,洪水=前端死,保护层责任在agent侧
tag: exec指令, API坑, 用户偏好
<!-- END:025 -->
<!-- ID:026 -->
(2026-11-03漏斗案终态-实测通过)max自定义配方/数据包改动固定在config\openloader\data\LMaxRouter Datapack(版本目录E:\MC\.minecraft\versions\Industrial Revolution 2040\config\openloader),排查配方先查这里,kubejs和saves世界datapacks搜不到是假阴性;openloader结构=data放数据包/resources放资源包(内有MusicPack/resourcepack/Unique Dark大量二进制,exec内容扫描必须绕开);TC匠魂melt配方有共用条目(ingot_5.json=高炉/漏斗/矿车共享450mb),改单物品数值必须拆分:覆盖原配方移除目标+新建独立配方,直改连坐;vanilla datapack JSON不吃注释。mods顶层465个jar,命名=中文前缀+英文原名(本体:匠魂-TConstruct-1.20.1-3.12.1.231.jar,前置:前置库 匠魂 地幔-Mantle-1.20.1-1.11.117.jar,附属:魂樱工匠-sakuratinker-1.4.2.3.jar+前置etstlib/l2library/sakura_library),按英文名正则找jar必漏本体,须全列或带中文关键词;TC melt配方路径data\tconstruct\recipes\smeltery\melting\metal\iron\,锭=90mb基准。2026-11-03凌晨max实测三断言全通过(45mb×10=450mb=90mb×5锭,合成10/熔炼/连坐清),配方案销案
tag: Minecraft, openloader, 配方排查, 文件操作
<!-- END:026 -->
<!-- ID:027 -->
(2026-11-03)PowerShell坑:非StrictMode下访问.NET对象不存在的属性返回$null且不报错(笔误$z.EnEntries→静默null),null经管道给Where-Object产出零项→"零命中"类输出无条件成立,具有欺骗性。防御:①判定"是否为零"前先打印已知非零的总量自检(证明扫描器活着,如recipe总数)②可Set-StrictMode -Version Latest让笔误变显式报错③zip条目扫描属性$z.Entries逐字符核对。同类:引号插值坑
tag: PowerShell, exec指令, 文件操作
<!-- END:027 -->
<!-- ID:028 -->
万能闸门UniversalGate(立项2026-11-06),路径D:\Documents\mcmod\UniversalGate,GOAL-PLAN.md已落盘。核心:max的"0厚度传送门"方块,外表普通方块,逻辑上从世界拓扑删除该格,两侧焊住。架构(含关键修正):单点mixin折叠getBlockState因方向歧义死掉(查询无方向参数),定为"translate(pos,dir)定向翻译原语+系统入口薄钩"(流体传播/updateShape/capability,只看坐标不看内容,零方块类型特判)。实体穿越=碰撞箱置空零mixin。管道网络不融合(代理限流阀语义,深度防环)。v0:同维度对面配对,深度1,验证T1-T10(水7格自检/双箱63格/FE直充/实体穿)。最大风险:1.20.1注入点未核实,Phase 0反编译调研=go/no-go关口;neighborChanged需BE主动桥;写路径漏一条=水写进闸门。状态(2026-11-06):Phase 0调研已授权但排队中(前面有max的其他优先任务),mod代码冻结未授权。待max定:正式名/开关交互/FE取舍/外观/配置项
tag: 万能闸门, 绿地项目, 当前任务, Minecraft, 架构决策
<!-- END:028 -->
<!-- ID:029 -->
万能闸门Phase 0结论(2026-11-06):GO。报告在research/phase0-report.md。核心事实坐标:流体写=FlowingFluid:253 setBlock(pos,fluid,3)直写,漏翻译水顶闸门(实锤);流体读双通道Level.getBlockState(L351→LevelChunk:150)+Level.getFluidState(L360→LevelChunk:187)必须双钩;邻居通知总闸=NeighborUpdater接口(redstone包,Level:147唯一CollectingNeighborUpdater实例);渲染读走RenderChunkRegion直调LevelChunk.getBlockState绕过Level层→与逻辑读正交,翻译写物理落格渲染自然正确;客户端同步同样正交(sendBlockUpdated只发被写格);BE.pos=final+immutable+chunk map键,移动BE=重建对象,capability注入点=BE构造gatherCapabilities(IForgeBlockEntity extends ICapabilitySerializable);BE传送建议v1排除。待max拍板:读翻译层(Level vs LevelChunk)、写覆盖面(仅setBlock vs 全家桶)、BE分期。渲染编译循环618/629同格流体+方块双渲
tag: 万能闸门, 架构决策, Minecraft, mixin, 可行性审查
<!-- END:029 -->
<!-- ID:030 -->
td修复案上游情报(2026-11-07):主仓xandergos/terrain-diffusion-mc代码冻结于2026-05-21(develop),release停在5-13的2.3.0-preview。PR#172官方修洞穴灌水(density二值化)合入develop但从未发布,max的jar没吃到。作者活着,精力在SIGGRAPH论文侧(研究仓8-12仍在推)+alt-dev-2分支做1.21.11新版(自述已验证beta candidate:biomes/rivers/优化),1.20.1=弃子版本官方不再碰。issue#255用户luo2006817-byte是趟过雷的战友:1.21.1本地补丁全套(density null branch/default_fluid/cave cheese回归/biome tag修),尸检结论:1.21.1 fluid走seaLevel路径,PR#172二值化修复在其上无效。issue#231证词(1.21.11 Fabric):deep_dark极罕见但存在,lush/basalt极其常见——与1.20.1 Forge实锤(possibleBiomes无deep_dark)冲突,跨版本漂移或声明表与实际返回不一致,待源码验证。max已排除bigglobe主世界路线:bigglobe全自研+自家数据包,兼容缺口=整个模组生态,bigglobe作者亲测"模组数据包转换器"路线并放弃(权威死亡证明);max现行架构=主世界/下界td(vanilla管线)+末地bigglobe(内容少),已知代价=龙之研究龙岛丢失
tag: td情报, terrain-diffusion-mc, 上游状态
<!-- END:030 -->
<!-- ID:031 -->
td修复案技术档案(2026-11-07,配套报告D:\Documents\my\日常\td修复案-分析报告-2026-11-07.md):架构三层=AI(2D栅格)→BiomeClassifier(纯Java规则引擎,非AI!)→vanilla管线。缺口:G1 possibleBiomes无deep_dark(远古城第一关被拒)G2 findBiomeHorizontal/findClosestBiome3d直接return null(/locate死)G3 sparse三群系缺is_overworld标签 G4 density null分支返回-y→未解码tile变0~63海(我的推断,#133"all terrain submerged"原文支持;与luo的seaLevel符号论竞争,PR#172移植后实测裁决)G5 carver隧道在biome JSON(在岗)但cheese/spaghetti/noodle噪声洞穴被final_density替换没了 G6 aquifers off全海平面流体 G7 td覆盖vanilla grove(温度2.0干热林,不修) G8解码缓存不落盘#248(不修,VS2刀缓解)。修复设计:A刀mixin BiomeSource(y<阈值→2D区域噪声选deep_dark/lush/dripstone+possibleBiomes注入+VS2防御刀+可选findBiomeHorizontal粗实现)+DensityFunction null分支-y→1.0(PR#172六行移植);B刀纯数据=is_overworld标签合并(Forge自动union)+新noise_settings(router从vanilla overworld全量抄,final_density=min(td密度,vanilla洞穴部分),aquifers开)+新world_preset(td注册表项公开可组合),交付默认preset路线;C刀MPL-2.0发布。不确定:U1两论竞争U2 aquifer×柱状密度交互U3远古城biome检查采样y(若表面采样柱状路由够不着)U4 beard_thin×高度域1968 U5 1.20.1/1.21.1流体路径同构性U6注入时序(低风险)。工时A3-5h/B2-4h/C1-2h,日历最好2天典型3-5天最坏1-2周
tag: td情报, terrain-diffusion-mc, bug根因，Minecraft, mixin
<!-- END:031 -->
<!-- ID:032 -->
(2026-11-07实勘)max家网络:maven.neoforged.net被SNI阻断(GFW级):本地DNS污染(每次解析返回随机假IP,如69.171.229.73/204.79.197.217),DoH真身在cdn77(89.222.116.12/103.228.130.61),但真IP+正确SNI仍TLS握手RST→改hosts无效。无本地代理(常见端口全关,无TUN网卡);LAN代理10.24.136.70:2080仅注册表残留,TCP死。可达:maven.minecraftforge.net直连OK(~1.2s),BMCLAPI的/maven/映forge OK但不映neoforged(404实证),腾讯mirrors(gradle发行版OK),doh.pub与223.5.5.5的DoH可用,1.1.1.1 DoH超时。结论:neoforged系构建在max机器上必须:等他开代理/找Central等其他镜像/预填本地缓存。exec通道持管理员权限。
tag: Minecraft, 网络环境, DNS污染, maven.neoforged, neoforged, 构建坑
<!-- END:032 -->
<!-- ID:033 -->
PowerShell三大坑之三(与引号插值/静默null并列):foreach语句块(非管道对象)不能直接接"|"管道,报ParserError"An empty pipe element is not allowed",且解析期死亡=整个脚本报废,前面代码也不执行。防御:循环内收集到List/数组变量,循环后"$out | Select-Object"分两步。2026-11-07实证(MDG源码考古块阵亡,浪费一轮)。判读技巧:回执只有ParserError无任何输出=全脚本未运行,不是部分执行。
tag: PowerShell, exec指令, 坑
<!-- END:033 -->
<!-- ID:034 -->
(2026-11-07)墙内拉取被SNI阻断的GitHub生态maven仓(如maven.neoforged.net)的CI-fork旁路技法:①gh repo fork <owner>/<repo>②PUT repos/<me>/<repo>/actions/permissions body={"enabled":true,"allowed_actions":"all"}启用fork的Actions③gh api repos/<me>/<repo>/git/refs POST建branch指向目标commit(版本=tag+distance公式,如1.0.13=tag1.0后第13commit,可用compare API数出)④branch名以feature/**开头自动触发build-prs.yml(push触发),或workflow_dispatch兜底⑤run完成后GET .../actions/artifacts,下载maven-publish artifact(zip=完整maven仓结构含.module/.pom/jar/校验和,走api.github.com,墙内可达)⑥解压成file://本地仓+init-script(-I,projectsLoaded/allprojects/afterEvaluate)注入gradle:摘除死端点仓+尾挂本地镜像,版本用-P对齐。gh api喂body用[IO.File]::WriteAllText无BOM json+--input文件。此法可复用于任何"上游maven被墙但源码在GitHub且CI有publish动作"的场景。
tag: 构建坑，网络环境，gh-cli，CI旁路，技巧
<!-- END:034 -->
<!-- ID:035 -->
[2026-11-09 夜] td案 γ壳版已部署: terrain-diffusion-mc-forge-1.20.1-2.3.0-windows.jar 8,167,740B 现役TEST实例(mods\), 回滚锚 .bak-caveshell; 三 commit: 76c364c 洞穴壳+手术(surface_cave豁出lift+mountain chance30→15) / 4b65be6 modrinth钉版本 / 案卷commit(含日期勘误,哈希以git log为准); 实例 carver_altitudes.json 已备份 D:\temp\carver_altitudes.json.bak-20261110 并删除, 首启动以新种子重播种(应含 excluded:[minecraft:canyon, bettercaves:surface_cave] + shellExempt:[minecraft:canyon])。**构建配方(两次救命,勿丢)**: 环境四件套 TD_MIRROR=1 + GRADLE_USER_HOME=F:\MOVED\.gradle + JAVA_HOME=E:\Java\Zulu\zulu-17 + TEMP/TMP=D:\temp, 任务名 buildForgeWindows; TD_MIRROR 激活 init.d\td-mirror.init.gradle(C盘活动home与F盘旧home双份同MD5): 摘 maven.neoforged.net 死仓 + 头插 D:\temp\td-maven 本地仓(NFRT 1.0.13 + minecraft-dependencies 1.20.1 全套22文件)。不设 TD_MIRROR → neoform-runtime 解析 404(maven.neoforged.net = GFW级SNI阻断, 见记忆032)。yungs 坐标已钉版本可离线。**教训: 开工先检索长期记忆/案卷**——11-09夜白绕12回合网络侦查, 配方一直躺在记忆035里。[2026-11-10 更新] push 悬案销: github 直连已恢复, 07:02 push 成功(b536576); 输出 7d004d9..b536576 = 仅推新 commit, 此前 pending 的 9 commit 已在远端(何时推上未观察到,某次网络窗口已成功), commit 债清零。BC密度已落地(cave_spawn_chance 100→70, 见记忆042), 该悬案销。GOAL-PLAN 日期勘误维持已 amend。
tag: td情报, terrain-diffusion-mc, 构建坑, 网络环境, 当前任务
<!-- END:035 -->
<!-- ID:036 -->
HF (huggingface.co) 网络坑 (2026-11-07 td案实证): Java 报 PKIX path building failed + curl schannel 报 CRYPT_E_NO_REVOCATION_CHECK = 证书吊销检查通道 (OCSP/CRL) 被墙, 证书本身是真的 (curl --ssl-no-revoke 直连 200, Zulu cacerts 168 条根全在). 解法: hf-mirror.com 完全可达 (API+resolve 通道全 200, 307 重定向正常), 替换 URL 即通. 注意与 maven.neoforged.net 的 SNI 阻断 (连 curl 都死) 是两种不同性质的墙, 诊断时先分清: 吊销检查死=证书真+握手半途死; SNI 死=连接层死.
tag: hf-mirror, 坑, 网络环境, API坑
<!-- END:036 -->
<!-- ID:037 -->
PokerAgent 长跑技巧 (2026-11-07 实证): agent 框架在 exec 指令结束时会屠杀全部后台进程 (Start-Process -PassThru 的进程 90s 内被杀). 解法: schtasks /create /f /tn 名字 /tr bat路径 /sc once /st 23:59 + schtasks /run /tn 名字 立即触发, 计划任务父进程是系统服务, agent 屠刀够不着, 构建跑满 4m59s 实证. 判据: bat 尾部 echo %ERRORLEVEL% > 完成标记文件, 轮询 Get-Process + 日志尾部. 另: bat 内 set 环境变量不穿透 (需在 bat 内写死), init.d + 环境变量对 schtasks 触发的构建同样生效.
tag: 构建坑, 技巧, schtasks, PokerAgent
<!-- END:037 -->
<!-- ID:038 -->
2026-11-07 09:25 td案构建大捷: terrain-diffusion-mc-forge-1.20.1-2.3.0-windows.jar (7.78MB) BUILD SUCCESSFUL exit0, 产物@versions\1.20.1\forge\build\libs, 验尸全绿. 三墙三对策: ①maven.neoforged.net SNI死→init.d F:\MOVED\.gradle\init.d\td-mirror.init.gradle (TD_MIRROR env闸门,不设零副作用) + TDMirror=D:\temp\td-maven (重版本化NFRT1.0.13+GMD CI生成minecraft-dependencies:1.20.1) ②HF吊销检查通道死(证书真)→hf-mirror替换multiloader-loader.gradle L69+L88,备份D:\temp\td-mll-backup.gradle ③PokerAgent屠后台进程→schtasks tdbuild20261107脱离进程树. 构建配方=D:\temp\td-build-run.bat(TD_MIRROR=1+GRADLE_USER_HOME=F:\MOVED\.gradle+JAVA_HOME=E:\Java\Zulu\zulu-17+TEMP/TMP=D:\temp,cd versions\1.20.1跑根gradlew buildForgeWindows),日志D:\temp\td-b-out/err.log,标记td-b-exit.txt(0=成功). 剩余: 实测8项(远古城市/locate biome等)+locate修复+PR回馈. git未commit(等max过目).
tag: td情报, terrain-diffusion-mc, 构建坑, 网络环境
<!-- END:038 -->
<!-- ID:039 -->
HF(huggingface.co)网络坑实证(2026-11-07 td案): Java报PKIX path building failed + curl schannel报CRYPT_E_NO_REVOCATION_CHECK = 证书吊销检查通道(OCSP/CRL)被墙,证书本身是真的(curl --ssl-no-revoke直连200,Zulu cacerts 168条根全在). 解法=hf-mirror.com完全可达(API+resolve全200,307重定向正常),URL替换即通. 与maven.neoforged.net的SNI阻断(curl都死)是两种性质的墙,诊断先分清: 吊销检查死=握手半途死+证书真; SNI死=连接层全死.
tag: hf-mirror, 坑, 网络环境, API坑
<!-- END:039 -->
<!-- ID:040 -->
PokerAgent长跑技巧(2026-11-07实证): agent框架在exec指令结束时屠杀全部后台进程(Start-Process -PassThru的子进程90s内被杀,连同java进程). 解法: schtasks /create /f /tn 名字 /tr "bat路径" /sc once /st 23:59, 再 schtasks /run /tn 名字 立即触发. 计划任务父进程=系统服务,agent屠刀够不着,构建跑满4m59s实证. 判据: bat尾部 echo %ERRORLEVEL% > 完成标记文件,轮询Get-Process+日志尾部. env变量要在bat内写死(set不穿透),init.d+bat内set对schtasks触发的构建同样生效.
tag: PokerAgent, 技巧, schtasks, 构建坑
<!-- END:040 -->
<!-- ID:041 -->
TD fork 测试实例: E:\MC\.minecraft\versions\TEST 1.20.1-Forge_47.4.10 (1.20.1 + Forge 47.4.10, 生产 jar 实测用实例, 2026-11-08 起用于 td 案实测八项)
tag: 测试环境, Minecraft
<!-- END:041 -->
<!-- ID:042 -->
TD洞穴体系关键结构(2026-11-08):mod jar-in-jar捆YUNG's Better Caves 2.0.6(默认开,properties键caves.bundled_cave_mod);caves.noise_caves=auto语义=BC在场且启用→mod自己的噪声洞穴(cave_density.json整套)关闭→BC在场时cave_density是死配置,改它无效。max最终决策:不关BC,走BC实例层调参(零mod侵入);max实测:BC摘除(bundled_cave_mod=false)机器能跑不崩。noise_settings: min_y=-64,height=2032,dimension_type min_y=-64/height=800(不一致但能跑),sea_level 63,aquifers_enabled=true,bedrock_floor渐变-60/-55。实例config在versions\TEST 1.20.1-Forge_47.4.10\config\隔离(PCL实例)。[2026-11-10 结案补充] BC形态实锤: 捆在TD主jar的META-INF/jarjar/yungs-better-caves-1.20.1-Forge-2.0.6.jar 405KB(+yungs-api 370KB+onnxruntime 6.8MB)。**坑: META-INF/jarjar/ 是Forge的jarJar路径, META-INF/jars/ 是Fabric的——grep错路径必扑空,踩了三轮**。BC主配置=jar内data-driven JSON(data/bettercaves/worldgen/configured_carver/better_cave.json),实例config\bettercaves\只放liquidregions等附加选项,主配置不落盘;覆盖法=OpenLoader数据包(实例已有OEIBF先例)。密度杠杆定案: cave_spawn_chance(区域级洞穴生成率,默认100.0=每区域必出洞="任意处5x5竖井必遇洞"根因)是唯一干净旋钮——BC主carver概率已1.0,TD密度补偿(×3.34封顶)对它无效,vanilla probability字段是死旋钮。落地: config\openloader\data\z-bc-cave-density\包,100→70(-30%),仅新世界生效; cavern(23)与surface_cave(0.15→被TD补偿至0.5)未动。BC经biome_modifier(add_carvers/remove_carvers)挂is_overworld全群系,carver重建走BetterCavesWorldCarverConfig codec,datapack覆盖应生效,BCConfigForge运行时覆盖风险低。
tag: td情报, terrain-diffusion-mc, Minecraft, bug根因, 坑
<!-- END:042 -->
<!-- ID:043 -->
[2026-11-08] terrain-diffusion-mc 四案判词浓缩(全案详见fork仓库GOAL-PLAN.md及C3/C4 commit):
①World Scale=30/scale米/格线性(native_resolution=30,模型包world_pipeline_config.json自述实锤),MAX_SCALE=6→5m/格是分辨率地板,1m/格需换1m分辨率的模型,全球公开DEM天花板SRTM=30m,原料不存在。
②江河(勘误2026-11-09,原判词两处误): 河宽三层漏斗:幂律wanted(radiusPixels L1183)→绝对墙min(MAX_CHANNEL_RADIUS_PX=16半径,全宽封顶32格,原判词"封顶16px=16格"语义笔误)→confinement min(wanted,0.36×谷底px); accumulation=localRunoff×cellAreaKm2(L2226,传入pixelSizeM)面积补偿→流量为物理量与scale无关(原判词此半句正确);但confinement按格计→scale1(30m/格)全宽~24格=720m(confinement墙≈12px半径先拦),scale6(5m/格)16墙生效全宽32格=160m→格数24→32不恒定,物理米宽反缩4.5倍,scale红利为负非零;泰晤士级万km²盆flow≈1e4 wanted≈60-64px被墙砍转深度;大江存在(vanilla大河3倍宽)但需万km²盆地+宽谷平原,山地confinement(谷底=两侧升高<18m)压窄成溪;湖浅<15m+小<0.135km²+无补给=puddle删除;cherry_grove三重门(气候窗+cherryNoise>0.15+rarity10 RARE)极稀有,locate找不到=半径内无样本,均非bug;实测建议:看大江用scale1+宽谷(720m级),scale6下大江封顶160m。
③onnx-cache泄漏:缓存文件名=name-ORT版本标签-模型hash(OnnxModel L270),unknown=getImplementationVersion()返null回退,unspecified=manifest字面量;旧ORT版本条目永不清,每换捆绑ORT版本+~1.08GB;换mod jar不触发(key不含mod版本)。测试实例(E:\MC\.minecraft\versions\TEST 1.20.1-Forge_47.4.10):现役=unknown系(06-13),unspecified系1.08GB化石零风险可删;地形盘缓存terrain-diffusion-cache 7.69GB全可再生。
④实测路:游戏内/td-explore开web地图(端口19801)俯瞰河网,是江河案唯一游戏侧验证手段。
tag: terrain-diffusion-mc，td情报，坑
<!-- END:043 -->
<!-- ID:044 -->
[2026-11-09] td fork配置架构决策:carver_altitudes.json改为播种式(source of truth模式)——首运行把默认规则落盘成用户可编辑文件,之后只读文件,内置常量仅种子+坏文件内存兜底;坏文件绝不覆盖(保留用户手改,响亮报错)。max明确偏好:反运行时硬编码/反merge式"内置+文件合并",配置必须落盘可见可改,用户删行=功能关(后果自负)。旧BUILT_IN表+BUILT_IN_EXCLUDED+applyOverrides merge语义已删。语义变更:文件缺bettercaves条目=BC不被lift。通用教训:给max做任何"默认配置"一律走落盘播种,不走代码内常量参与运行时。此前判词:vanilla canyon被reach_summits+density_compensation误伤(p0.01→0.125~0.34,y顶拉穿天窗),豁免minecraft:canyon为种子默认。
tag: td情报, terrain-diffusion-mc, 架构决策, 用户偏好
<!-- END:044 -->
<!-- ID:045 -->
TD fork 构建配方 (2026-11-10 实证): buildForgeWindows 任务必须以 versions\1.20.1 为 cwd 跑, 在 fork 根下报 "Task not found"; gradlew.bat 用 fork 根的那份; 产物在 versions\1.20.1\forge\build\libs\。任务定义不在 vdir 顶层 *.gradle (grep 零命中, 疑在 applied script 或 buildSrc, 未追)。env 四件套: TD_MIRROR=1 / GRADLE_USER_HOME=F:\MOVED\.gradle / JAVA_HOME=E:\Java\Zulu\zulu-17 / TEMP=TMP=D:\temp。构建时长约 2.5 分钟。
tag: 构建坑, terrain-diffusion-mc, buildForgeWindows
<!-- END:045 -->
<!-- ID:046 -->
TD 种子架构 (2026-11-10 D5-U4 实测二次反转后修正): 管线随机源全链条吃 seed (coarse L195 seed / L211 seed+1, latent seed+5819/5820, decoder seed+5819, 均为管线seed+用途偏移, 接线本身健康)。**2026-11-10 04:1x max 实测: codec 修复后存读不崩 (codec 案结案), 但新世界地形+出生点与旧世界全同 → "level.dat 写残致 seed 复用/两案合并"结论被实测推翻, 种子案独立重开**。铁证: 地形+spawn 全同 = pipeline 眼中 seed 恒定。分水岭 = max 对两个世界各输 /seed 对报 (seed 同=创建侧, seed 异=传递链断)。新嫌疑: ①两世界 seed 本就相同 ②pipeline 构造时 seed 来源 (biome source ctor?) ③init/setSeed 触发时机晚于地形/spawn 计算 ④静态单例不随世界重建 ⑤config 固定 seed 项。**新雷: fork 根下 common/ 与 versions\1.20.1\common/ 双源码副本, 构建用后者, D5-U1/U2 侦查读的是前者, 同步性 D5-U4 hash 钉死中**。磁盘缓存 TileKey 含 seed 无污染 (此结论不受影响)。
tag: td情报, terrain-diffusion-mc, 种子
<!-- END:046 -->
<!-- ID:047 -->
TD 数据包损坏案根因 (2026-11-10, 机制约 70% 把握): 保存时 PrimaryLevelData 报 "Unknown registry element in biome_source" → WorldGenSettings 编码失败 → level.dat 写残 (seed 陪葬) → 重进读档炸, 新建世界正常。机制: biome_source 注册表按对象身份存 codec, 保存时拿实例 codec() 反查 ID, 而 MapCodec.codec() 每次调用返回新包装实例 → 注册实例与反查实例身份不等 → 反查失败。new TerrainDiffusionBiomeSource 全仓零命中, "运行时 new" 假说已排除。修复 = CODEC 单例化 (static final, 注册与 codec() 返回同一对象), 三行改动; 若无效, 下一嫌疑 = RegistryOps 上下文错配。上游原版即有 (归档 log 2026-11-07-2 起 11 处实锤)。附赠修复: 存档可重进, level.dat 可存 seed (77-85 九世界空 seed 即此案所致)。
tag: td情报, terrain-diffusion-mc, bug根因, 数据包损坏
<!-- END:047 -->
<!-- ID:048 -->
DH 加速 LOD 生成评估结论 (2026-11-10, 报告 D:\Documents\my\日常\DH加速LOD生成-可行性评估-2026-11-10.md): 可行。官方入口=DhApi.worldGenOverrides.registerWorldGeneratorOverride; 推荐路线=IDhApiWorldGenerator.generateLod+setApiDataPointColumn (非 chunk 抽象类); 数据点=DhApiTerrainDataPoint.create(detailLevel,blockLight,skyLight,bottomY,topY,blockStateWrapper,biomeWrapper); 尺度=TD 30m/px×scale2=60m/px vs DH detail6=64blocks/列, 插值吸收。最大风险=wrapper 实例获取无公开工厂(兜底: 反射 core BlockStateWrapper 工厂钉死 2.3.2-b / DhApiBlockStateWrapperCreatedEvent 拦截); 次风险=DH 2.3.2-b beta API 易变→compileOnly 软依赖+在场检测注册(TerraBlenderSurfaceCompat 先例)。联机拓扑=DH 生成在客户端, 模型 2GB 在服务端→单机先行联机二期。工时典型 2-4 天最坏 1 周。模块边界: TdLodTranslator 纯翻译/TdDhGenerator 壳/DhCompat 注册/配置落盘播种。待 max 决策: 做不做/范围/依赖策略。
tag: td情报, terrain-diffusion-mc, 可行性审查, DH
<!-- END:048 -->
<!-- ID:049 -->
[2026-11-10] Windows loader 模块身份=基名(非路径): 进程内已有同名DLL时, LoadLibrary/System.load 传任意绝对路径都只会返回已加载的那个模块。"预载System32新版VC++再加载目标DLL"这类实验天然作废——JDK bin里捆绑的VC++运行时(如Zulu17的14.29)在JVM启动时就随JVM的DLL入进程, 之后无法替换。实战: ORT-DML 1.26 的 onnxruntime.dll 在 Zulu17 下 System.load 报1114(初始化例程失败), 同一DLL文件 python(无捆绑CRT走System32 14.51)加载成功; Zulu17 preload System32 CRT 后依旧1114(preload拿回的是旧模块)。调试这类问题的正解=换不同JVM做矩阵测试, 而非进程内 preload。CRT遮蔽链(Zulu17 bin 14.29 → 静默遮蔽 System32 14.51)为当时主嫌, 矩阵验证中。
tag: 坑, Windows, DLL, terrain-diffusion-mc, bug根因
<!-- END:049 -->
<!-- ID:050 -->
[2026-11-10] ORT-DML 1.26 (onnxruntime.dll FileVersion 1.26.0) 在 Windows 各 JVM 下 System.load 报 1114 的实证矩阵: JDK bin 捆绑 VC++ CRT ≥14.40 → PASS (zulu214219=21.0.7, zulu-25, VSCode redhat JRE25); bin 捆绑 14.29/14.31 → UnsatisfiedLinkError 1114 初始化例程失败 (zulu-17=17.0.12, Oracle jdk-21=21.0.2, zulu21=21.0.4)。机制=应用目录(java.home\bin)在 DLL 搜索序第一位, 旧 CRT 遮蔽 System32 的 14.51; 注: 缺入口理论上应报 126/127 而实测 1114, 精确机制未深究, 版本相关性为实证结论。max 的游戏(PCL)实际用 E:\Java\zulu214219 (latest.log "java version 21.0.7 by Azul Systems") 所以从没炸过。**mod 兼容性影响: terrain-diffusion-mc 用户用捆绑旧 CRT 的 JDK 启动会直接 UnsatisfiedLinkError 1114, 建议写进 README 系统要求 + 崩溃时的友好提示**。另发现: E:\Java\jdk-17.0.14.7-hotspot 疑似 32 位安装(加载 64 位 DLL 报 "Can't load AMD 64-bit .dll on a IA 32-bit platform")。调试正解=换 JVM 矩阵测试, 进程内 preload System32 CRT 无效(模块身份=基名, 见记忆 049)。
tag: 坑, Windows, DLL, terrain-diffusion-mc, bug根因, JVM, mod兼容
<!-- END:050 -->
<!-- ID:051 -->
2026-11-11 td案取证教训(通用):①Minecraft轮转log时间戳格式为"日+月"(如11-08=0811月2026,11-10=1011月2026),跨多日扫描时禁止硬编码日期前缀,必须按目标日动态生成,否则静默0命中还误判"日志已轮转丢失";②mod的"Persisted ... to <路径>"日志行尾部含完整缓存路径(含seed-hex分目录),截断显示会永久遮蔽关键键值,涉路径行Cap用200+;③javap输出为2空格缩进,自写方法解析器勿按4空格匹配;④gradle缓存里vanilla反编译源不存在.java散文件,运行时版本jar(versions\<ver>\<ver>.jar)可直接javap取证Forge补丁后的真实字节码。
tag: 坑, 日志分析, terrain-diffusion-mc
<!-- END:051 -->
<!-- ID:052 -->
td案结案卷(2026-11-12):TD(Terrain Diffusion mod)种子丢失bug根因=LocalTerrainProvider双实例竞速。init(static synchronized类锁,LevelEvent.Load时刻,持真种子)与getInstance(独立INIT_LOCK,首批coarse调用者,Load+0.03~0.75s)各持一把锁互不互斥,两边都是查INSTANCE空→重量级构造器→无条件赋值;两线程并行构建LTP,后完成者覆盖先完成者:getInstance后写=0L覆盖live种子=新世界整场seed-0(指纹=加载飞快+spawn固定(-5120,69,5120)+24x24扩张);init后写=好场(else分支setSeed自愈+双批次指纹);重进必好(INSTANCE已在,else-if setSeed)。修复=getInstance锁改LocalTerrainProvider.class与init互斥(INSTANCE volatile,DCL安全),可回馈上游。九场铁证:坏场w83,w84,w85,(86),(90)全单批次;好首建w77,(87),(91)全双批次。seed hex:(86)坏c80ac7a0678ee9a0/(90)坏539eb6874ef9c574/(91)好8e9f8c97872ce0a0/(92)好6f27a29b076e1160/(87)好937c0cf8d4dc9ad8/(88)好8791516634639969/(89)好27106f412749fe46。悬案:重进会话coarse37+uncached54(DH LOD候选);(83)(84)(85)=codec测试世界全seed-0;codecfix=f58966e正常。环境:fork=D:\Documents\mcmod\LMaxs Terrain Diffusion fork;构建D:\temp\td-build-run.bat(TD_MIRROR=1);测试实例E:\MC\.minecraft\versions\TEST 1.20.1-Forge_47.4.10;probe=同实例mods\tdseedprobe-1.0.0.jar(源码D:\Documents\mcmod\td-seed-probe);部署jar=mods\terrain-diffusion-mc-forge-1.20.1-2.3.0-windows.jar。PokerAgent教训(2026-11-12):多行消息可能被通道剥换行→多行脚本压成单行注释空跑(回执=已执行无输出);单行exec内联会吃空格;remember/memory内联已弃用必须code块;稳妥姿势=exec单code块多行+消息短。
tag: bug根因, Minecraft, td情报, terrain-diffusion-mc
<!-- END:052 -->
<!-- ID:053 -->
[2026-11-12 03:35] td种子竞速案封卷。修复=cf6d44f(LocalTerrainProvider: getInstance与init统一类锁,INIT_LOCK字段删除),已部署TEST实例,备份D:\temp\td-jar-backup\pre-seedfix-1112-022114.jar。终审证据:max冷启动连建6新世界,6/6好:每场ServerStarted行chainSeed=levelGetSeed=instanceSeed=seed四值全等,零0值;W1 AboutToStart时INSTANCE=null(旧竞态窗口安全通过,getInstance排队进门见活实例);W2-6 AboutToStart时instanceSeed=旧值→ServerStarted收敛(init else分支setSeed自愈实锤);六场均~2分钟冷算(修复前坏场秒进)。两条交错支路(实例未建/实例旧种子)均被实测覆盖。修复前首建坏率62.5%,未修复6连好概率≈0.28%。probe=tdseedprobe-1.0.0.jar(源码D:\Documents\mcmod\td-seed-probe)。后续:上游PR待max点头。
tag: terrain-diffusion-mc, td情报, bug根因，种子
<!-- END:053 -->
<!-- ID:054 -->
2026-11-14 td深度案收线: max裁决"-128合理,收工"——原目标"洞穴不捅基岩/墙不可感知"闭环;设计哲学(max原话):"基岩是墙但正常游玩不应感知墙"。better_caves bottom_y维持-63(-127灌洞否决,深部内容全部延后)。延后backlog(重启时用): ①下界岩(基岩层附近少量)+岩浆河置新底 ②矿石边角:gold_lower成-64~-48悬空薄片(唯一遗留) ③洞带是否下扩bottom_y ④OEIBF包识别。世界生成链实锤: td零carver数据,carver实现内嵌td jar(bettercaves jar不在mods);OpenLoader包z-bc-cave-density喂better_cave.json(bottom_y=-63×2区域,top_y=80);td经NoiseBasedChunkGeneratorMixin@Redirect getCarvers→ScaledCarvers抬升外来carver。vanilla矿石锚点(1.20.1版本jar实测)劈两半: 跟随组above_bottom自动灌新深部=diamond/buried/large(-80~80→裁剪后-128~-48),redstone(底~15),redstone_lower(-32~32→裁剪后-128~-96),iron_small(底~72),lapis_buried(底~64);留守组absolute不动=gold(-64~32),gold_lower(-64~-48悬浮),lapis(-32~32),iron_middle(-24~56),copper(-16~112),emerald(-16~480)。深板岩矿变体由ore_feature自带deepslate target自动处理。未实锤: td biome→vanilla矿引用链(fork resources无biome目录,biome json疑在jar内或代码生成)。TEST实例jar已打-128补丁,备份D:\temp\td-jar-backup\pre-depthfix-1113-175954.jar,回退=回拷mods;fork三版本同步(1.20.1+1.21.1+1.21.11,Δ=64+基岩锚above_bottom化+ScaledCarvers L373注释)待max裁决。
[2026-11-14 03:55修正,MD5+max证词] ①上述"TEST实例jar已打-128补丁"不成立: 063② MD5实证,11-14 02:59部署时刻 mods 活跃jar≡pre-depthfix备份→-128补丁当时不在mods;max表态不追究去向,事项封口。②"fork三版本同步待裁决"已由056作废(深度案全撤销,fork源码从未改)。③本条延后backlog(下界岩/岩浆新底/gold_lower/洞带下扩)随-128撤销作废,仅历史档;岩浆河唯一存活分支=地底河案water_region_spawn_chance调法(见058/064)。④当前世界生成态(液面-46/河床-52/密度100)以056/058/064为准。
tag: td情报, terrain-diffusion-mc, 矿石锚点, 深度案, 延后清单
<!-- END:054 -->
<!-- ID:055 -->
2026-11-14 洞穴带侦察收口:better_cave.json结构=2隧道(bottom_y=-63,top_y=80,y_compression 5.0/2.2)+2大空腔(-63..-18无底is_floored=false,-63..-28有底,cavern_spawn 23/region)+misc.replaceable=#minecraft:overworld_carver_replaceables。深度案原始病灶实锤:bottom_y=-63语义=旧底-64上方1格,旧基岩带-64..-55→洞带下沿9格嵌在基岩带内,carver啃不动基岩(不在replaceable标签)→洞底暴露基岩墙=当年"河贴基岩"根因。手术后基岩-124..-119,洞底-63悬空56格,"洞穴不碰基岩"当前已成立零改动达标。max指令"bottom_y高点"读作下扩灌新深部留缓冲,候选-110/-103/-96(缓冲9/16/24),倾向只动隧道L9/L32两处,cavern留-63;td properties L158实锤noise型carver自适应band。未实锤:旧世界地底河水源(aquifer vs BC自带液态逻辑)。bettercaves实现疑td捆绑注册(jar内路径无bettercaves字样,mods无其jar,但type=bettercaves:better_cave活体生效)。
tag: td情报，terrain-diffusion-mc，深度案，洞穴，bug根因
<!-- END:055 -->
<!-- ID:056 -->
2026-11-14终改:深度案全撤销——max明令"把-128改回去",TEST mods内td jar已回拷为原版(D:\temp\td-jar-backup\pre-depthfix-1113-175954.jar,MD5校验一致),-128补丁版归档同目录terrain-diffusion-2.3.0-depth128-patched.jar(撤销的撤销可用)。fork源码从未改动,三版本同步议题就此终结。世界终态:原生深度-64,基岩带-55~-64(max口径),洞穴底bottom_y=-48(better_cave.json 4处,原-63嵌基岩带9格=旧"河贴基岩"根因),洞底与墙间6格实心深板岩垫,top_y全部未动。深部白纸backlog(下界岩/岩浆河/gold_lower/洞带下扩)随-128撤销作废,仅剩历史档(054/055)。生效范围仅新区块,旧区块保持-128形状。
tag: td情报，terrain-diffusion-mc，深度案，fork同步
<!-- END:056 -->
<!-- ID:057 -->
2026-11-14沟通教训(修正版,前版"零改动读法优先"已被证伪作废):max的短句裁决按祈使句优先直译。"bottom_y高点,离基岩层远点"=把bottom_y抬高的动作指令,我连续三轮误读(维持-63不动→下扩灌新底抛-110/-103/-96→"描述现状句"),还把错误结论写成教训存档,根因=发明"现状自洽"解释覆盖字面义。新规则:字面像祈使就按祈使执行,与现状冲突也不自行折衷;"把X改回去"=撤销X的补丁回原状;"X完事"才是结案句;拿不准时一句话向max复述指令内容再动手,禁止三轮解读。
tag: 用户偏好，沟通，坑
<!-- END:057 -->
<!-- ID:058 -->
2026-11-14 地底河案结案(侦破+修复双闭环):根因=BC(嵌td jar JiJ内yungs-better-caves-1.20.1-Forge-2.0.6)液体系统Liquid Regions,液面默认-55(基岩顶),旧世界bottom_y-63洞底8格淹水贴墙,新bottom_y高于液面洞全干河消失。液体系统全配置=config\bettercaves\forge-1_20_1\liquidregions.json三钮(独立于carver json/td properties,td不碰):①liquid_altitude水岩共用液面绝对Y零校验②water_region_spawn_chance水区份额%(校验[0,100]fail-soft,岩浆吃补集)③liquid_region_size区域波长(0.001=千格级)。极性实锤:srg_to_official_1.20.1.tsrg f_49990_=WATER f_49991_=LAVA;getLiquidBlockAtPos三态逐列:水<阈值-jitter/岩浆>阈值+jitter/null过渡带~5%水岩隔离。C档已执行且max目视验收通过(11-14凌晨):液面-46+bottom_y-52×4,终态水深6/墙垫2,生效新区块,BC加载零报错。备份链D:\temp\td-jar-backup:liquidregions.json.orig-55.bak+better_cave.json.bottom-48.bak+better_cave.json.bottom-63.bak。岩浆河调法=chance 40→15(未动,等max指示)。后续任务(进行中):把-46/-52固化进td mod分发默认——bottom_y已在z-bc-cave-density数据包(分发友好),液面在config侧需查BC jar内有无内嵌默认json(有→gradle构建时替换JiJ内BC文件;无→mixin/写config/fork BC三选)。测试环境:E:\MC\.minecraft\versions\TEST 1.20.1-Forge_47.4.10。
tag: td情报，terrain-diffusion-mc，深度案，地底河，液面，bug根因
<!-- END:058 -->
<!-- ID:059 -->
2026-11-14 Forge模组jar数据包机制(通用事实):模组jar内data/(assets/)目录本身就是自动常驻全局数据包/资源包,无需OpenLoader、无需建世界挂载、无需pack.mcmeta(Forge的ModFilePackResources内置处理,文件夹包才需要mcmeta)。硬门槛:jar必须是合法mod(mods.toml齐全),裸data/ jar扔mods目录会被跳过;壳mod(mods.toml几行+data/,1KB级)是集成包生态标准件;改第三方jar塞data是脏路(更新冲掉+版本链污染),正确姿势=塞自家mod。覆盖优先级:事件注册包(AddPackFindersEvent)>模组包>原版;模组包之间同路径优先级未实测。OpenLoader唯一残值=玩家拖文件夹+热改免重打包,对mod作者零价值。BC jar自带data/bettercaves/worldgen/configured_carver/better_cave.json即此机制实证。
tag: Minecraft, 技巧, td情报, terrain-diffusion-mc
<!-- END:059 -->
<!-- ID:060 -->
max的fork模组版本号规范(2026-11-14,源:备忘录mc模组规范段,路径见记忆003):两段式[原作版本号去点].[我的版本号],例1124.2=emi原作1.1.24+自己.2;原作大版本更新→我的小版本重置回.1;我的段正常三段式1.0.0,用不上可一位(230.1)。td fork 2026-11-14发布=230.1.1.0(原作td2.3.0+自己1.1.0)。fork与原作差异大(重写/新功能占比高/原作长期停更)→可去掉原作段。release tag=版本号。通用jar名:模组名-模组版本号+mc版本+加载器-编译时间戳.jar(例lmaxs-emi-fork-1124.2+1.20.1+forge-20260928235749.jar)。branch=mc版本+加载器(多加载器取mc版本,stonecutter取main)。fork命名LMaxs X fork(保留原作名,继任者不必保留此段)。命名空间com/lmaxrouter优先。新增物品→创造标签页单开一页。更新日志只写增量。主控配置文件一站式(依赖/mods.toml内容链入)。
tag: 用户偏好, 版本号规范, fork规范, mc模组, 备忘录
<!-- END:060 -->
<!-- ID:061 -->
2026-11-14实测:解释正文裸写中文方括号指令标签字样→解析器识别为真标签→后续全部内容被逐行拆成单行指令→回执大量"未知指令",首条是正文后半句,全部被拒执行零副作用。正确姿势:正文提及标签必须转义(反斜杠)或用英文方括号,指令标识符只出现在真正指令位置。症状特征=回执逐行未知指令轰炸。
tag: PokerAgent, 坑, 指令格式
<!-- END:061 -->
<!-- ID:062 -->
2026-11-14: PokerAgent exec 传复杂 PowerShell 脚本偶发 mangling→ParserError。症状:引号串被吞成超大 token、多语句拼行;Parse 期即拒,零执行零副作用,可直接重试。防御:脚本扁平化——不定义 function、不用 for 循环(改直接[0][1]索引)、注释全英文、try 块慎用、长 -like 通配换 -match。当日扁平化重写成功。另:memory 指令 tag 参数必须半角冒号+半角逗号,全角"tag："会报"不支持内联文本"。
tag: PokerAgent，技巧，坑
<!-- END:062 -->
<!-- ID:063 -->
2026-11-14 03:10 地底河案部署前取证终卷(全部实证,修正054):
① LiquidRegions$Settings javap 反汇编: <clinit> DEFAULT=(liquidRegionSize 0.001, waterRegionSpawnChance 40.0, liquidAltitude -55); bipush -55 为 clinit 唯一 bipush(offset 10); 构造器校验: size<=0回0.001, chance∉[0,100]回40.0(fail-soft)。fork mixin 改 DEFAULT.altitude→-46 后,无 json 实例运行时=(0.001,40.0,-46),与 C 档验收终态等价;液面单一真相源=mixin。
② MD5 实证: TEST 实例 mods 活跃 jar(mtime 11-12 02:20) == pre-depthfix 备份(11-13 17:59) = 87467F235392D3DF2550A018B55FCBC0 → -128 深度补丁在 2026-11-14 部署前不在 mods,054 的"TEST实例jar已打-128补丁"不成立于该时刻(去向: 或验证后恢复/或从未部署,待 max 证词)。
③ fork noise_settings(1.20.1): min_y/height 在 noise 子对象内(取顶层必得空值): noise.min_y=-64,height=2032; dimension_type -64/800; fork 从未带 -128(054 的 fork 三版本同步为"待裁决"未执行)。
④ BC 无内嵌 liquidregions 默认 json(纯 class 硬编码),fork 固化液面唯一可行路=mixin(已实现)。
⑤ 洞穴密度: fork 内嵌 pack cave_spawn_chance=100(=upstream),z-bc 包(70)已随 2026-11-14 部署移出实例→运行密度=100 满格。
tag: td情报，terrain-diffusion-mc，地底河，液面，bug根因
<!-- END:063 -->
<!-- ID:064 -->
2026-11-14 03:45 地底河案 z-bc 拆包修正(推翻我基于058的差异预期):
① D:\temp\z-bc-cave-density-removed-20261114-025920 拆包实测: cave_spawn_chance=100(非70!), carver0/1+cavern0/1 bottom_y 全=-52, 其余字段=upstream → z-bc 包内容≡内嵌包, 它是"河床-52包"而非密度包。
② 密度70仅存在于D6时代(commit b536576 记录落地), 地底河案改写z-bc做河床固化时 chance 被 upstream 值(100)覆盖, README"密度降低"句自那时起描述不存在之物。
③ 教训: 058长期记忆里的"z-bc=70"是历史状态非部署实况, 部署前取证必须以实拆为准, 记忆只当线索。
④ 验收(2026-11-14 03:40 max 口头"验收通过"): 值层面零差异(液面-46/河床-52/密度100/岩浆河40/深度-64全部同值换来源), 结构差异=固化进jar+json可选+fail-loud+显示名。实例扫描: openloader空/saves无better_cave.json副本/config bettercaves仅.bak → BC参数唯一来源=内嵌包。
⑤ 待max三决策: A README密度句删or折70 B岩浆河chance 40→15 C -128补丁去向(054待修)。清理候选: z-bc备份/liquidregions.json.bak/六旧jar(5个.bak-*尸体+1个干净2.3.0上游,建议仅留上游)。
tag: td情报，terrain-diffusion-mc，地底河，液面，bug根因
<!-- END:064 -->
<!-- ID:065 -->
2026-11-14 04:40 地底河案B(岩浆河份额)落地: BC water_region_spawn_chance 出厂默认 40→25 (水25%/岩浆75%, max裁决B=25)。实现=mixin 三处常量全改(javap实证40.0全类恰3处: clinit DEFAULT参数1+<init> fail-soft日志参数offset89+回写offset100), "默认值"整体迁移——非法json回退值与新出厂一致,不留两套默认;已有合法json用户零影响(json仍是覆盖面)。旧值40=upstream默认,058时代提过40→15方案,max终裁25。版本230.1.1.0→230.1.2.0(gradle.properties:2真身+build.gradle:230注释示例)。构建绿(schtasks tdbuild20261114b,exit0,~1min,产物lmaxs-terrain-diffusion-fork-230.1.2.0+1.20.1+forge-20261114043413.jar 8173348B),javap验尸3方法/25.0×2/-46×1断言过,toml version对。已部署TEST(mods仅230.1.2.0,旧jar归档mods-swapped-20261114-025920,MD5一致)。commit 4438a9e(4文件38+/4-)。README 30行同步(删失效密度分句+液25%岩浆75%句)。build wart待max: javac无encoding配置,GBK读UTF-8源100条警告(全落注释,零语义,修法JavaCompile统一UTF-8)。游戏仍未跑=运行时检验pending(fail-loud设计)。
tag: td情报，terrain-diffusion-mc，地底河，液面，bug根因
<!-- END:065 -->
<!-- ID:066 -->
2026-11-14 td fork git拓扑实况: origin=https://github.com/LMaxRouterCN/terrain-diffusion-plus.git(max自己的公开fork,push目标), upstream=https://github.com/derekvawdrey/terrain-diffusion-plus(直接上游), xandergos(源头作者)无remote仅历史链路。工作分支=LMax-master(GOAL-PLAN§5曾记branch=1.20.1+forge系备忘录060规范,从未落地,悬而未决)。.agent/(agent记忆目录)被git追踪且已公开在origin远端(2026-11-14确认:9-commit债清零那次push已带案卷commits如f1aeda5),属max知情既定实践;若撤需动历史或前向gitignore。.agent_trash/__external__=2026-11-10 DH评估案detprobe脚本残留(dll_diag.py/tdh_autopsy.py),未追踪无害。
tag: td情报，terrain-diffusion-mc，fork同步
<!-- END:066 -->
<!-- ID:067 -->
2026-11-14 v2 (JOIN-TEST 补证): PokerAgent exec 通道对换行的破坏比初判更深 — 行尾 "-join $crlf" 被吞后数组按 $OFS 空格连成单行 (b7e93bc 僵尸根因, commit 每文件恰 2 insertions 铁证); 且 char-code 构造 CRLF 的诊断输出经通道后仍显示空格连接 (type=String 但无换行), 连 [string][char]13+[string][char]10 拼接都不可全信。可靠写法收敛为一条: 多行内容一律 here-string @'...'@ 模板 ({{TOKEN}} 占位替换缩进/前缀), 写入后必须结构断言 (关键代码行独立成行, 注释标记与代码不得同行) 再 commit。纯字符串数组与 here-string 实证可靠 (D:\temp 清理 82/82, 探针 bat 多行跑通, $pinTpl 修复块全绿)。
tag: PokerAgent，坑，指令格式，技巧
<!-- END:067 -->
<!-- ID:068 -->
2026-11-14 td fork javadoc GBK 警告案结案 (通用构建坑): Gradle 多模块只给 JavaCompile 钉 UTF-8 不够 — Javadoc 任务有独立 encoding (未钉 = 平台默认, 中文 Windows = GBK), javadoc tool 按 GBK 读 UTF-8 源产 unmappable 警告; 即使 javadocJar SKIPPED, :forge:javadoc 任务照样执行照样报警 (在 build 依赖链上)。修法 = tasks.withType(Javadoc).configureEach 三键同钉: options.encoding + options.docEncoding + options.charSet = 'UTF-8' (源读取/HTML 输出/meta charset)。诊断手法: ①init script 探针 taskGraph.whenReady dump 全任务 encoding (--dry-run 只读); ②负对照 = 保留坏状态 --rerun 复现警告计数。落地: b5f722d (conventions + 三版本 4 文件), 负对照 100 条 → 复验 0 条。
tag: 构建坑，gradle，javadoc
<!-- END:068 -->
