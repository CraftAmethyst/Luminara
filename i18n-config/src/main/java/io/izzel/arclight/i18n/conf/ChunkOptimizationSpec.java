package io.izzel.arclight.i18n.conf;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ChunkOptimizationSpec {

    // Luminara - Chunk optimizations disabled to avoid conflicts with Paper patches
    // Paper handles chunk optimization internally
    @Setting("aggressive-chunk-unloading")
    private boolean aggressiveChunkUnloading = false;

    @Setting("chunk-unload-delay")
    private int chunkUnloadDelay = 300;

    @Setting("optimize-chunk-loading")
    private boolean optimizeChunkLoading = false;

    @Setting("chunk-load-rate-limit")
    private int chunkLoadRateLimit = 10;

    public boolean isAggressiveChunkUnloading() {
        return aggressiveChunkUnloading;
    }

    public int getChunkUnloadDelay() {
        return chunkUnloadDelay;
    }

    public boolean isOptimizeChunkLoading() {
        return optimizeChunkLoading;
    }

    public int getChunkLoadRateLimit() {
        return chunkLoadRateLimit;
    }
}
