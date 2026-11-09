package com.github.xandergos.terraindiffusionmc.mixin;

import com.github.xandergos.terraindiffusionmc.config.TerrainDiffusionConfig;
import com.github.xandergos.terraindiffusionmc.world.CarverAltitudeRules;
import com.github.xandergos.terraindiffusionmc.world.ScaledCarvers;
import com.github.xandergos.terraindiffusionmc.world.SurfaceShell;
import com.github.xandergos.terraindiffusionmc.world.TerrainDiffusionBiomeSource;
import com.github.xandergos.terraindiffusionmc.world.WorldScaleManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Function;

/**
 * Runs whatever carvers the pack's biomes list, moved up to where our terrain is -- and
 * wraps each carve in a surface shell session so the carvers cannot open the terrain either.
 *
 * <p>The biome's carver list is intercepted rather than the carving step itself: a mod that
 * replaces the step outright never calls this, which is the correct outcome -- it has taken
 * over cave generation and we should stay out of its way. See
 * {@link ScaledCarvers} for what is and is not rewritten.</p>
 */
@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin {

    @Redirect(method = "applyCarvers", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/biome/BiomeGenerationSettings;getCarvers"
                    + "(Lnet/minecraft/world/level/levelgen/GenerationStep$Carving;)Ljava/lang/Iterable;"))
    private Iterable<Holder<ConfiguredWorldCarver<?>>> terrainDiffusion$liftCarversToTerrain(
            BiomeGenerationSettings settings, GenerationStep.Carving step) {
        Iterable<Holder<ConfiguredWorldCarver<?>>> carvers = settings.getCarvers(step);
        ChunkGenerator self = (ChunkGenerator) (Object) this;
        if (!(self.getBiomeSource() instanceof TerrainDiffusionBiomeSource)) return carvers;
        return ScaledCarvers.lift(carvers, WorldScaleManager.getCurrentScale());
    }

    /**
     * Wraps each carve in a {@link SurfaceShell} session, so the carve cannot write air near
     * the surface and no cave mouth opens on flat ground, a summit, or the sea floor. Only
     * active in our own worlds above scale 1 -- a scale-1 world has vanilla proportions, and
     * vanilla cave mouths belong in it -- and skipped for carvers the pack exempted under
     * {@code shellExempt} in {@code carver_altitudes.json}, which keeps near-surface
     * landforms that were never a problem, vanilla's canyon among them, entirely intact.
     */
    @Redirect(method = "applyCarvers", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/carver/ConfiguredWorldCarver;carve"
                    + "(Lnet/minecraft/world/level/levelgen/carver/CarvingContext;"
                    + "Lnet/minecraft/world/level/chunk/ChunkAccess;Ljava/util/function/Function;"
                    + "Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/levelgen/Aquifer;"
                    + "Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/CarvingMask;)Z"))
    private boolean terrainDiffusion$enforceSurfaceShell(ConfiguredWorldCarver<?> carver,
            CarvingContext context, ChunkAccess chunk, Function<BlockPos, Holder<Biome>> posToBiome,
            RandomSource random, Aquifer aquifer, ChunkPos pos, CarvingMask mask) {
        ChunkGenerator self = (ChunkGenerator) (Object) this;
        boolean shell = self.getBiomeSource() instanceof TerrainDiffusionBiomeSource
                && WorldScaleManager.getCurrentScale() > 1
                && TerrainDiffusionConfig.surfaceShellEnabled()
                && !CarverAltitudeRules.isShellExempt(ScaledCarvers.configuredCarverId(carver));
        if (!shell) return carver.carve(context, chunk, posToBiome, random, aquifer, pos, mask);
        SurfaceShell.begin(chunk, TerrainDiffusionConfig.surfaceShellThickness());
        try {
            return carver.carve(context, chunk, posToBiome, random, aquifer, pos, mask);
        } finally {
            SurfaceShell.end();
        }
    }
}
