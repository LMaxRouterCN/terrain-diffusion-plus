package com.github.xandergos.terraindiffusionmc.mixin;

import com.github.xandergos.terraindiffusionmc.world.SurfaceShell;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Refuses the writes {@link SurfaceShell} marks as surface-breaking, by cancelling them at
 * the head of {@code ProtoChunk.setBlockState}.
 *
 * <p>Every carver writes its blocks through this one method -- the game's own cave, tunnel
 * and canyon carvers, Better Caves' carve loop, and any carver a datapack lists -- and every
 * one of them throws the returned state away, so returning null for a refused write is
 * invisible to them: the carve moves on and that block stays terrain. Writes made outside a
 * carve session never see a session on their thread and always pass through.</p>
 */
@Mixin(ProtoChunk.class)
public abstract class ProtoChunkMixin {

    @Inject(method = "setBlockState", at = @At("HEAD"), cancellable = true)
    private void terrainDiffusion$refuseSurfaceBreakingCarve(BlockPos pos, BlockState state,
            boolean moving, CallbackInfoReturnable<BlockState> cir) {
        if (SurfaceShell.shouldBlock((ChunkAccess) (Object) this, pos, state)) {
            cir.setReturnValue(null);
        }
    }
}
