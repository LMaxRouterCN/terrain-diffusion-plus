package com.github.xandergos.terraindiffusionmc.pack;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModList;

/**
 * [地底河案] jar 内嵌数据包注册: Better Caves 洞穴参数 (bottom_y -52)。
 * 优先级: Forge APFE 事件包 > 模组自带包 > 原版 (OpenLoader 同机制, 字节码实证)。
 * 工厂 readMetaAndCreate = SRG m_245429_ (OpenLoader 同款 7 参工厂, tsrg L48440 实证):
 * 自己从 supplier 读 pack.mcmeta 生成 Info; mojmap 下 create 仅 9 参重载 (2026-11-14 编译实测)。
 */
public final class BetterCavesEmbeddedPack {
    public static final String FML_MOD_ID = "terrain_diffusion_mc";
    public static final String PACK_ID = "td_bettercaves";

    private BetterCavesEmbeddedPack() {}

    public static void register(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) {
            return;
        }
        // jar 内 packs/td-bettercaves (pack.mcmeta + data/bettercaves/worldgen/...)
        var packDir = ModList.get().getModFileById(FML_MOD_ID).getFile().findResource("packs/td-bettercaves");
        var pack = Pack.readMetaAndCreate(
                PACK_ID,
                Component.literal("TD: Better Caves cave params (embedded)"),
                true, // 强制启用 (OpenLoader 同款)
                id -> new PathPackResources(id, packDir, false),
                PackType.SERVER_DATA,
                Pack.Position.TOP, // TOP = 压过 BC 模组自带包
                PackSource.DEFAULT);
        event.addRepositorySource(consumer -> consumer.accept(pack));
    }
}