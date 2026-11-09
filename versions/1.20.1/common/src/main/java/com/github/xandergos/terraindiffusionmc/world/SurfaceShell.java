package com.github.xandergos.terraindiffusionmc.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Keeps carvers from breaking the surface: while a carve is running, air and fluid writes
 * within {@code caves.surface_shell.thickness} blocks of the terrain surface are refused.
 *
 * <h2>How it runs</h2>
 * <p>{@code NoiseBasedChunkGeneratorMixin} wraps each {@code ConfiguredWorldCarver.carve}
 * call in a session ({@link #begin} / {@link #end}), and {@code ProtoChunkMixin} consults
 * {@link #shouldBlock} on every {@code setBlockState} made while a session is open on that
 * thread. The session is thread-local, so worker threads carving different chunks never see
 * each other's; the chunk reference is matched as well, so any write that is not part of the
 * carve being wrapped -- features, structures, anything -- passes through untouched.</p>
 *
 * <h2>Why the OCEAN_FLOOR heightmap is the ruler</h2>
 * <p>During the carving step the chunk maintains its heightmaps incrementally on every write,
 * and OCEAN_FLOOR's predicate counts solid blocks only -- water is not solid -- so its value
 * is the ground surface on land and the sea floor under water: the one surface the shell must
 * protect, measured by one ruler in both places. Reading it through
 * {@code ChunkAccess.getHeight} also primes the map itself if it was somehow missing, so no
 * column ever reports a value the map never held.</p>
 *
 * <h2>What is refused and what is not</h2>
 * <ul>
 *   <li><b>Air and fluids are refused</b> within {@code thickness} blocks below the surface. The
 *       surface therefore never gets carved away, which means the heightmap cannot drift downward
 *       while the carve runs -- the ruler holds itself steady for the whole carve.</li>
 *   <li><b>Fluids go with the air</b>. A cave filled to its ceiling with water writes no air at
 *       all, so guarding air alone would let it punch straight through the shell; aquifer water
 *       and lava are refused by the same rule, and within the shell band the terrain stays as
 *       authored. The price is a pocket of fluid sealed under the shell's stone lid where a
 *       high cave met the water table, accepted.</li>
 *   <li><b>A column with no surface</b> -- all air, or its heightmap never primed -- reads at
 *       the world floor and is let through: when the ruler is missing, carve. A stray opening
 *       beats a wrongly sealed world.</li>
 * </ul>
 */
public final class SurfaceShell {

    private SurfaceShell() {}

    /** The carve being wrapped, and the shell thickness to enforce through it. */
    private record Session(ChunkAccess chunk, int thickness) {}

    private static final ThreadLocal<Session> SESSION = new ThreadLocal<>();

    /**
     * Opens the shell around one {@code carve} call. Must be paired with {@link #end} in a
     * {@code finally}: a leaked session would keep refusing writes on this thread long after
     * the carve is gone.
     */
    public static void begin(ChunkAccess chunk, int thickness) {
        SESSION.set(new Session(chunk, thickness));
    }

    /** Closes the session opened by {@link #begin}. */
    public static void end() {
        SESSION.remove();
    }

    /**
     * Whether writing {@code state} into {@code chunk} at {@code pos} must be refused to keep
     * the surface sealed. True only for air or fluid writes, inside a session on this thread and
     * this chunk, at or above {@code surface - thickness}.
     */
    public static boolean shouldBlock(ChunkAccess chunk, BlockPos pos, BlockState state) {
        Session session = SESSION.get();
        if (session == null || session.chunk() != chunk) return false;
        // 空气与流体一并拒: 满水洞穴全程零空气写入, 只拦空气等于旁观它穿面。
        // Air and fluid writes are both refused: a water-filled cave writes no air at all,
        // so guarding air alone would let it punch through the shell. Anything else passes.
        if (!state.isAir() && state.getFluidState().isEmpty()) return false;
        // Local column, masked so the value is right whichever coordinate convention the
        // caller's heightmap expects. getHeight primes the map itself if it was absent.
        int surface = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR, pos.getX() & 15, pos.getZ() & 15);
        // No surface in this column (all air, or never primed): let it through.
        if (surface <= chunk.getMinBuildHeight()) return false;
        return pos.getY() >= surface - session.thickness();
    }
}
