package io.izzel.arclight.i18n.conf;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class WorldCreationSpec {

    // Luminara - World creation optimizations disabled to avoid conflicts with Paper patches
    // Paper handles world creation optimization internally
    @Setting("fast-world-creation")
    private final boolean fastWorldCreation = false;

    @Setting("skip-spawn-chunk-loading")
    private final boolean skipSpawnChunkLoading = false;

    @Setting("force-close-loading-screen")
    private final boolean forceCloseLoadingScreen = false;

    @Setting("early-world-list-addition")
    private final boolean earlyWorldListAddition = false;

    @Setting("parallel-world-initialization")
    private final boolean parallelWorldInitialization = false;

    @Setting("world-init-timeout-seconds")
    private final int worldInitTimeoutSeconds = 30;

    @Setting("max-concurrent-world-loads")
    private final int maxConcurrentWorldLoads = 2;

    @Setting("optimize-world-border-setup")
    private final boolean optimizeWorldBorderSetup = false;

    @Setting("defer-spawn-area-preparation")
    private final boolean deferSpawnAreaPreparation = false;

    @Setting("spawn-area-radius")
    private final int spawnAreaRadius = 11;

    @Setting("async-world-data-loading")
    private final boolean asyncWorldDataLoading = false;

    public boolean isFastWorldCreation() {
        return fastWorldCreation;
    }

    public boolean isSkipSpawnChunkLoading() {
        return skipSpawnChunkLoading;
    }

    public boolean isForceCloseLoadingScreen() {
        return forceCloseLoadingScreen;
    }

    public boolean isEarlyWorldListAddition() {
        return earlyWorldListAddition;
    }

    public boolean isParallelWorldInitialization() {
        return parallelWorldInitialization;
    }

    public int getWorldInitTimeoutSeconds() {
        return worldInitTimeoutSeconds;
    }

    public int getMaxConcurrentWorldLoads() {
        return maxConcurrentWorldLoads;
    }

    public boolean isOptimizeWorldBorderSetup() {
        return optimizeWorldBorderSetup;
    }

    public boolean isDeferSpawnAreaPreparation() {
        return deferSpawnAreaPreparation;
    }

    public int getSpawnAreaRadius() {
        return spawnAreaRadius;
    }

    public boolean isAsyncWorldDataLoading() {
        return asyncWorldDataLoading;
    }
}
