package io.papermc.paper.util;

import ca.spottedleaf.concurrentutil.util.MCUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.concurrent.CompletableFuture;

/**
 * Paper MCUtil compatibility class for Luminara.
 * This class provides Paper API compatibility by delegating to our MCUtils implementation.
 */
public final class MCUtil {

    private MCUtil() {
    }

    /**
     * Gets a chunk asynchronously.
     */
    public static CompletableFuture<ChunkAccess> getChunkAsync(final ServerLevel level, final int chunkX, final int chunkZ) {
        return MCUtils.getChunkAsync(level, chunkX, chunkZ);
    }

    /**
     * Gets a chunk asynchronously using ChunkPos.
     */
    public static CompletableFuture<ChunkAccess> getChunkAsync(final ServerLevel level, final ChunkPos pos) {
        return MCUtils.getChunkAsync(level, pos);
    }

    /**
     * Gets a chunk if loaded.
     */
    public static net.minecraft.world.level.chunk.LevelChunk getChunkIfLoaded(final ServerLevel level, final int chunkX, final int chunkZ) {
        return MCUtils.getChunkIfLoaded(level, chunkX, chunkZ);
    }

    /**
     * Gets a chunk if loaded using ChunkPos.
     */
    public static net.minecraft.world.level.chunk.LevelChunk getChunkIfLoaded(final ServerLevel level, final ChunkPos pos) {
        return MCUtils.getChunkIfLoaded(level, pos);
    }

    /**
     * Converts block coordinates to chunk coordinates.
     */
    public static int blockToChunk(final int blockCoord) {
        return MCUtils.blockToChunk(blockCoord);
    }

    /**
     * Converts chunk coordinates to block coordinates.
     */
    public static int chunkToBlock(final int chunkCoord) {
        return MCUtils.chunkToBlock(chunkCoord);
    }

    /**
     * Gets the chunk position for a block position.
     */
    public static ChunkPos getChunkPos(final BlockPos pos) {
        return MCUtils.getChunkPos(pos);
    }

    /**
     * Checks if the current thread is the main server thread.
     */
    public static boolean isMainThread() {
        return MCUtils.isMainThread();
    }

    /**
     * Ensures the current thread is the main server thread.
     */
    public static void ensureMainThread() {
        MCUtils.ensureMainThread();
    }

    /**
     * Gets the distance squared between two points.
     */
    public static double distanceSquared(final double x1, final double z1, final double x2, final double z2) {
        return MCUtils.distanceSquared(x1, z1, x2, z2);
    }

    /**
     * Gets the distance between two points.
     */
    public static double distance(final double x1, final double z1, final double x2, final double z2) {
        return MCUtils.distance(x1, z1, x2, z2);
    }

    /**
     * Gets the Manhattan distance between two points.
     */
    public static int manhattanDistance(final int x1, final int z1, final int x2, final int z2) {
        return MCUtils.manhattanDistance(x1, z1, x2, z2);
    }

    /**
     * Converts ticks to milliseconds.
     */
    public static long ticksToMillis(final long ticks) {
        return MCUtils.ticksToMillis(ticks);
    }

    /**
     * Converts milliseconds to ticks.
     */
    public static long millisToTicks(final long millis) {
        return MCUtils.millisToTicks(millis);
    }

    /**
     * Gets the current server tick.
     */
    public static long getCurrentTick() {
        return MCUtils.getCurrentTick();
    }

    /**
     * Clamps a value between min and max.
     */
    public static int clamp(final int value, final int min, final int max) {
        return MCUtils.clamp(value, min, max);
    }

    /**
     * Clamps a value between min and max.
     */
    public static long clamp(final long value, final long min, final long max) {
        return MCUtils.clamp(value, min, max);
    }

    /**
     * Clamps a value between min and max.
     */
    public static double clamp(final double value, final double min, final double max) {
        return MCUtils.clamp(value, min, max);
    }

    /**
     * Clamps a value between min and max.
     */
    public static float clamp(final float value, final float min, final float max) {
        return MCUtils.clamp(value, min, max);
    }

    /**
     * Gets a random integer between min (inclusive) and max (exclusive).
     */
    public static int randomInt(final int min, final int max) {
        return MCUtils.randomInt(min, max);
    }

    /**
     * Gets a random long between min (inclusive) and max (exclusive).
     */
    public static long randomLong(final long min, final long max) {
        return MCUtils.randomLong(min, max);
    }

    /**
     * Gets a random double between 0.0 (inclusive) and 1.0 (exclusive).
     */
    public static double randomDouble() {
        return MCUtils.randomDouble();
    }

    /**
     * Gets a random boolean value.
     */
    public static boolean randomBoolean() {
        return MCUtils.randomBoolean();
    }
}
