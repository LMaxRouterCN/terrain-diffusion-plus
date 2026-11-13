package com.github.xandergos.terraindiffusionmc.mixin;

import com.yungnickyoung.minecraft.bettercaves.worldgen.liquidregion.LiquidRegions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * [地底河案] Better Caves 出厂液面 -55 -> -46 (河床-52, 水深6, 墙垫2)。
 * [岩浆河份额] water_region_spawn_chance 40.0 -> 25.0 (水25%/岩浆75%, 2026-11-14 max 裁决B)。
 * BC 为本 mod 编译期 implementation 依赖 (jarJar 钉 2.0.6), 直接类引用;
 * mixin AP 规则: classpath 可见的 public 目标必须 value 引用, targets 字符串会被拒 (2026-11-14 编译实测)。
 * remap=false: BC 非 MC 混淆类, dev=prod 类名恒等, 不写 refmap。
 * 常量命中均为 javap 实证 (2026-11-14): -55 全类唯一 bipush (clinit);
 * 40.0 全类恰好 3 处 = clinit DEFAULT 参数 1 处 + <init> fail-soft 2 处 (offset89 日志参数 + offset100 回写),
 * 三处全改 = "默认值"整体迁移, 非法 json 回退值与新出厂默认一致, 不留两套默认。
 * 已有合法 liquidregions.json 的用户不经过这些常量, 零影响; 用户配置面 = BC 自带 json (单一真相源)。
 */
@Mixin(value = LiquidRegions.Settings.class, remap = false)
public class BetterCavesLiquidRegionsSettingsMixin {

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = -55), require = 1)
    private static int terrainDiffusion$defaultLiquidAltitude(int original) {
        return -46; // [地底河案] C档验收值
    }

    // [岩浆河份额] DEFAULT = (0.001, 40.0, -55) 的 chance 参数, clinit 内唯一 40.0
    @ModifyConstant(method = "<clinit>", constant = @Constant(doubleValue = 40.0), require = 1)
    private static double terrainDiffusion$defaultWaterRegionSpawnChance(double original) {
        return 25.0;
    }

    // [岩浆河份额] <init> fail-soft: json 非法 chance (<=0 或 >100) 回退默认值, 日志参数 + 回写共 2 处
    @ModifyConstant(method = "<init>", constant = @Constant(doubleValue = 40.0), require = 2)
    private static double terrainDiffusion$fallbackWaterRegionSpawnChance(double original) {
        return 25.0;
    }
}