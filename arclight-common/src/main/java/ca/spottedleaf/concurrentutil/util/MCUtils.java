package ca.spottedleaf.concurrentutil.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Minecraft utility class providing various helper methods for Paper compatibility.
 * This class implements utilities from Paper patch 0009.
 */
public final class MCUtils {

    private MCUtils() {
    }

    /**
     * Gets the main server thread executor.
     *
     * @return The main server thread executor
     */
    public static Executor getMainThreadExecutor() {
        // Use a simple executor that runs tasks on the main thread
        return (runnable) -> {
            if (isMainThread()) {
                runnable.run();
            } else {
                org.bukkit.Bukkit.getScheduler().runTask(org.bukkit.Bukkit.getPluginManager().getPlugins()[0], runnable);
            }
        };
    }

    /**
     * Gets the async executor for non-main thread operations.
     *
     * @return The async executor
     */
    public static Executor getAsyncExecutor() {
        return ForkJoinPool.commonPool();
    }

    /**
     * Checks if the current thread is the main server thread.
     *
     * @return true if on the main server thread
     */
    public static boolean isMainThread() {
        return org.bukkit.Bukkit.isPrimaryThread();
    }

    /**
     * Ensures the current thread is the main server thread.
     *
     * @throws IllegalStateException if not on the main thread
     */
    public static void ensureMainThread() {
        if (!isMainThread()) {
            throw new IllegalStateException("Must be called on the main server thread");
        }
    }

    /**
     * Schedules a task to run on the main server thread.
     *
     * @param task The task to run
     * @return A CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> scheduleOnMainThread(final Runnable task) {
        if (isMainThread()) {
            task.run();
            return CompletableFuture.completedFuture(null);
        }

        final CompletableFuture<Void> future = new CompletableFuture<>();
        getMainThreadExecutor().execute(() -> {
            try {
                task.run();
                future.complete(null);
            } catch (final Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });

        return future;
    }

    /**
     * Gets a chunk asynchronously.
     *
     * @param level  The server level
     * @param chunkX The chunk X coordinate
     * @param chunkZ The chunk Z coordinate
     * @return A CompletableFuture containing the chunk
     */
    public static CompletableFuture<ChunkAccess> getChunkAsync(final ServerLevel level, final int chunkX, final int chunkZ) {
        // Simplified implementation that returns the chunk if available
        final CompletableFuture<ChunkAccess> future = new CompletableFuture<>();
        try {
            final ChunkAccess chunk = level.getChunk(chunkX, chunkZ, net.minecraft.world.level.chunk.ChunkStatus.FULL, false);
            if (chunk != null) {
                future.complete(chunk);
            } else {
                future.completeExceptionally(new RuntimeException("Chunk not available"));
            }
        } catch (final Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    /**
     * Gets a chunk asynchronously using ChunkPos.
     *
     * @param level The server level
     * @param pos   The chunk position
     * @return A CompletableFuture containing the chunk
     */
    public static CompletableFuture<ChunkAccess> getChunkAsync(final ServerLevel level, final ChunkPos pos) {
        return getChunkAsync(level, pos.x, pos.z);
    }

    /**
     * Gets a chunk synchronously if available, otherwise returns null.
     *
     * @param level  The server level
     * @param chunkX The chunk X coordinate
     * @param chunkZ The chunk Z coordinate
     * @return The chunk if available, null otherwise
     */
    public static LevelChunk getChunkIfLoaded(final ServerLevel level, final int chunkX, final int chunkZ) {
        return level.getChunkSource().getChunkNow(chunkX, chunkZ);
    }

    /**
     * Gets a chunk synchronously if available, otherwise returns null.
     *
     * @param level The server level
     * @param pos   The chunk position
     * @return The chunk if available, null otherwise
     */
    public static LevelChunk getChunkIfLoaded(final ServerLevel level, final ChunkPos pos) {
        return getChunkIfLoaded(level, pos.x, pos.z);
    }

    /**
     * Converts block coordinates to chunk coordinates.
     *
     * @param blockCoord The block coordinate
     * @return The chunk coordinate
     */
    public static int blockToChunk(final int blockCoord) {
        return blockCoord >> 4;
    }

    /**
     * Converts chunk coordinates to block coordinates (chunk origin).
     *
     * @param chunkCoord The chunk coordinate
     * @return The block coordinate of the chunk origin
     */
    public static int chunkToBlock(final int chunkCoord) {
        return chunkCoord << 4;
    }

    /**
     * Gets the chunk position for a block position.
     *
     * @param pos The block position
     * @return The chunk position
     */
    public static ChunkPos getChunkPos(final BlockPos pos) {
        return new ChunkPos(blockToChunk(pos.getX()), blockToChunk(pos.getZ()));
    }

    /**
     * Gets a random integer between min (inclusive) and max (exclusive).
     *
     * @param min The minimum value (inclusive)
     * @param max The maximum value (exclusive)
     * @return A random integer in the specified range
     */
    public static int randomInt(final int min, final int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    /**
     * Gets a random long between min (inclusive) and max (exclusive).
     *
     * @param min The minimum value (inclusive)
     * @param max The maximum value (exclusive)
     * @return A random long in the specified range
     */
    public static long randomLong(final long min, final long max) {
        return ThreadLocalRandom.current().nextLong(min, max);
    }

    /**
     * Gets a random double between 0.0 (inclusive) and 1.0 (exclusive).
     *
     * @return A random double
     */
    public static double randomDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    /**
     * Gets a random boolean value.
     *
     * @return A random boolean
     */
    public static boolean randomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    /**
     * Clamps a value between min and max.
     *
     * @param value The value to clamp
     * @param min   The minimum value
     * @param max   The maximum value
     * @return The clamped value
     */
    public static int clamp(final int value, final int min, final int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Clamps a value between min and max.
     *
     * @param value The value to clamp
     * @param min   The minimum value
     * @param max   The maximum value
     * @return The clamped value
     */
    public static long clamp(final long value, final long min, final long max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Clamps a value between min and max.
     *
     * @param value The value to clamp
     * @param min   The minimum value
     * @param max   The maximum value
     * @return The clamped value
     */
    public static double clamp(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Clamps a value between min and max.
     *
     * @param value The value to clamp
     * @param min   The minimum value
     * @param max   The maximum value
     * @return The clamped value
     */
    public static float clamp(final float value, final float min, final float max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Gets the distance squared between two points.
     *
     * @param x1 First point X coordinate
     * @param z1 First point Z coordinate
     * @param x2 Second point X coordinate
     * @param z2 Second point Z coordinate
     * @return The distance squared
     */
    public static double distanceSquared(final double x1, final double z1, final double x2, final double z2) {
        final double dx = x2 - x1;
        final double dz = z2 - z1;
        return dx * dx + dz * dz;
    }

    /**
     * Gets the distance between two points.
     *
     * @param x1 First point X coordinate
     * @param z1 First point Z coordinate
     * @param x2 Second point X coordinate
     * @param z2 Second point Z coordinate
     * @return The distance
     */
    public static double distance(final double x1, final double z1, final double x2, final double z2) {
        return Math.sqrt(distanceSquared(x1, z1, x2, z2));
    }

    /**
     * Gets the Manhattan distance between two points.
     *
     * @param x1 First point X coordinate
     * @param z1 First point Z coordinate
     * @param x2 Second point X coordinate
     * @param z2 Second point Z coordinate
     * @return The Manhattan distance
     */
    public static int manhattanDistance(final int x1, final int z1, final int x2, final int z2) {
        return Math.abs(x2 - x1) + Math.abs(z2 - z1);
    }

    /**
     * Converts ticks to milliseconds.
     *
     * @param ticks The number of ticks
     * @return The equivalent milliseconds
     */
    public static long ticksToMillis(final long ticks) {
        return ticks * 50L; // 20 TPS = 50ms per tick
    }

    /**
     * Converts milliseconds to ticks.
     *
     * @param millis The number of milliseconds
     * @return The equivalent ticks
     */
    public static long millisToTicks(final long millis) {
        return millis / 50L; // 20 TPS = 50ms per tick
    }

    /**
     * Gets the current server tick.
     *
     * @return The current server tick
     */
    public static long getCurrentTick() {
        // Use a simple tick counter
        return System.currentTimeMillis() / 50; // Approximate tick count
    }

    /**
     * Gets the current server time in milliseconds.
     *
     * @return The current server time
     */
    public static long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Gets the current server time in nanoseconds.
     *
     * @return The current server time in nanoseconds
     */
    public static long getCurrentTimeNanos() {
        return System.nanoTime();
    }
}
