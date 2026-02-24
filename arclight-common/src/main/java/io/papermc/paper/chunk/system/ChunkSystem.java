package io.papermc.paper.chunk.system;

import ca.spottedleaf.concurrentutil.executor.standard.PrioritisedExecutor;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Bukkit;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public final class ChunkSystem {

    private ChunkSystem() {
        throw new RuntimeException();
    }

    public static void scheduleChunkTask(final ServerLevel level, final int chunkX, final int chunkZ, final Runnable run) {
        scheduleChunkTask(level, chunkX, chunkZ, run, PrioritisedExecutor.Priority.NORMAL);
    }

    public static void scheduleChunkTask(final ServerLevel level, final int chunkX, final int chunkZ, final Runnable run, final PrioritisedExecutor.Priority priority) {
        if (Bukkit.isPrimaryThread()) {
            run.run();
            return;
        }
        level.getServer().execute(run);
    }

    public static void scheduleChunkLoad(final ServerLevel level, final int chunkX, final int chunkZ, final boolean gen,
                                         final ChunkStatus toStatus, final boolean addTicket, final PrioritisedExecutor.Priority priority,
                                         final Consumer<ChunkAccess> onComplete) {
        scheduleChunkTask(level, chunkX, chunkZ, () -> {
            ChunkAccess chunk = null;
            try {
                chunk = level.getChunk(chunkX, chunkZ);
            } catch (Throwable ignored) {
            }
            onComplete.accept(chunk);
        }, priority);
    }

    public static void scheduleChunkLoad(final ServerLevel level, final int chunkX, final int chunkZ, final ChunkStatus toStatus,
                                         final boolean addTicket, final PrioritisedExecutor.Priority priority, final Consumer<ChunkAccess> onComplete) {
        scheduleChunkLoad(level, chunkX, chunkZ, true, toStatus, addTicket, priority, onComplete);
    }

    public static void scheduleTickingState(final ServerLevel level, final int chunkX, final int chunkZ,
                                            final FullChunkStatus toStatus, final boolean addTicket,
                                            final PrioritisedExecutor.Priority priority, final Consumer<LevelChunk> onComplete) {
        scheduleChunkTask(level, chunkX, chunkZ, () -> {
            LevelChunk chunk = null;
            try {
                chunk = level.getChunk(chunkX, chunkZ);
            } catch (Throwable ignored) {
            }
            onComplete.accept(chunk);
        }, priority);
    }

    public static List<ChunkHolder> getVisibleChunkHolders(final ServerLevel level) {
        return Collections.emptyList();
    }

    public static List<ChunkHolder> getUpdatingChunkHolders(final ServerLevel level) {
        return Collections.emptyList();
    }

    public static int getVisibleChunkHolderCount(final ServerLevel level) {
        return 0;
    }

    public static int getUpdatingChunkHolderCount(final ServerLevel level) {
        return 0;
    }

    public static boolean hasAnyChunkHolders(final ServerLevel level) {
        return false;
    }

    public static void onEntityPreAdd(final ServerLevel level, final Entity entity) {
    }

    public static void onChunkHolderCreate(final ServerLevel level, final ChunkHolder holder) {
    }

    public static void onChunkHolderDelete(final ServerLevel level, final ChunkHolder holder) {
    }

    public static void onChunkBorder(final LevelChunk chunk, final ChunkHolder holder) {
    }

    public static void onChunkNotBorder(final LevelChunk chunk, final ChunkHolder holder) {
    }

    public static void onChunkTicking(final LevelChunk chunk, final ChunkHolder holder) {
    }

    public static void onChunkNotTicking(final LevelChunk chunk, final ChunkHolder holder) {
    }

    public static void onChunkEntityTicking(final LevelChunk chunk, final ChunkHolder holder) {
    }

    public static void onChunkNotEntityTicking(final LevelChunk chunk, final ChunkHolder holder) {
    }

    public static ChunkHolder getUnloadingChunkHolder(final ServerLevel level, final int chunkX, final int chunkZ) {
        return null;
    }

    public static int getSendViewDistance(final ServerPlayer player) {
        return getLoadViewDistance(player);
    }

    public static int getLoadViewDistance(final ServerPlayer player) {
        return Bukkit.getViewDistance() + 1;
    }

    public static int getTickViewDistance(final ServerPlayer player) {
        return Bukkit.getSimulationDistance();
    }
}
