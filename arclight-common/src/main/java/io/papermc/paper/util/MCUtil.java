package io.papermc.paper.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

import java.lang.ref.Cleaner;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class MCUtil {

    private MCUtil() {
    }

    public static Runnable once(Runnable run) {
        AtomicBoolean ran = new AtomicBoolean(false);
        return () -> {
            if (ran.compareAndSet(false, true)) {
                run.run();
            }
        };
    }

    public static <T> Runnable once(List<T> list, Consumer<T> cb) {
        return once(() -> list.forEach(cb));
    }

    public static Runnable registerCleaner(Object obj, Runnable run) {
        Runnable cleaner = once(run);
        CleanerHolder.CLEANER.register(obj, cleaner);
        return cleaner;
    }

    public static <T> Runnable registerListCleaner(Object obj, List<T> list, Consumer<T> cleaner) {
        return registerCleaner(obj, () -> {
            list.forEach(cleaner);
            list.clear();
        });
    }

    public static <T> Runnable registerCleaner(Object obj, T resource, Consumer<T> cleaner) {
        return registerCleaner(obj, () -> cleaner.accept(resource));
    }

    public static int fastFloor(double x) {
        int truncated = (int) x;
        return x < (double) truncated ? truncated - 1 : truncated;
    }

    public static int fastFloor(float x) {
        int truncated = (int) x;
        return x < (double) truncated ? truncated - 1 : truncated;
    }

    public static long getCoordinateKey(final BlockPos blockPos) {
        return ((long) (blockPos.getZ() >> 4) << 32) | ((blockPos.getX() >> 4) & 0xFFFFFFFFL);
    }

    public static long getCoordinateKey(final Entity entity) {
        return ((long) (fastFloor(entity.getZ()) >> 4) << 32) | ((fastFloor(entity.getX()) >> 4) & 0xFFFFFFFFL);
    }

    public static long getCoordinateKey(final ChunkPos pair) {
        return ((long) pair.z << 32) | (pair.x & 0xFFFFFFFFL);
    }

    public static long getCoordinateKey(final int x, final int z) {
        return ((long) z << 32) | (x & 0xFFFFFFFFL);
    }

    public static int getCoordinateX(final long key) {
        return (int) key;
    }

    public static int getCoordinateZ(final long key) {
        return (int) (key >>> 32);
    }

    private static final class CleanerHolder {
        private static final Cleaner CLEANER = Cleaner.create();
    }
}
