package com.github.xandergos.terraindiffusionmc.mixin;

import com.yungnickyoung.minecraft.bettercaves.worldgen.liquidregion.LiquidRegions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * [地底河案] Better Caves 出厂液面 -55 -> -46 (河床-52, 水深6, 墙垫2)。
 * BC 为本 mod 编译期 implementation 依赖 (jarJar 钉 2.0.6), 直接类引用;
 * mixin AP 规则: classpath 可见的 public 目标必须 value 引用, targets 字符串会被拒 (2026-11-14 编译实测)。
 * remap=false: BC 非 MC 混淆类, dev=prod 类名恒等, 不写 refmap。
 * clinit 内 -55 为全类唯一 bipush (javap 实证), require=1: BC 升级形状漂移时 fail-loud。
 * 已有 liquidregions.json 的用户不走 DEFAULT, 零影响; 用户配置面 = BC 自带 json (单一真相源)。
 */
@Mixin(value = LiquidRegions.Settings.class, remap = false)
public class BetterCavesLiquidRegionsSettingsMixin {

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = -55), require = 1)
    private static int terrainDiffusion$defaultLiquidAltitude(int original) {
        return -46; // [地底河案] C档验收值
    }
}