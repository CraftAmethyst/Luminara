package io.izzel.arclight.common.mod.compat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public final class CMICompat {

    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger();
    private static final Map<ClassLoader, ExecutorService> FALLBACK_EXECUTORS =
        new ConcurrentHashMap<>();

    private CMICompat() {}

    public static ExecutorService getExecutor(
        Class<?> ownerClass,
        ExecutorService original
    ) {
        if (
            original != null &&
            !original.isShutdown() &&
            !original.isTerminated()
        ) {
            return original;
        }

        ClassLoader loader =
            ownerClass == null
                ? CMICompat.class.getClassLoader()
                : ownerClass.getClassLoader();
        return FALLBACK_EXECUTORS.compute(loader, (key, existing) -> {
            if (
                existing != null &&
                !existing.isShutdown() &&
                !existing.isTerminated()
            ) {
                return existing;
            }
            int poolSize = Math.max(
                2,
                Runtime.getRuntime().availableProcessors() - 1
            );
            return Executors.newFixedThreadPool(poolSize, runnable -> {
                Thread thread = new Thread(runnable);
                thread.setName(
                    "CMI Async Compat-" + THREAD_COUNTER.incrementAndGet()
                );
                thread.setDaemon(true);
                return thread;
            });
        });
    }
}
