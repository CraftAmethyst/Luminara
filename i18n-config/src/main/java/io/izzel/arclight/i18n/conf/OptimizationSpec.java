package io.izzel.arclight.i18n.conf;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class OptimizationSpec {

    @Setting("cache-plugin-class")
    private boolean cachePluginClass;

    @Setting("goal-selector-update-interval")
    private int goalSelectorInterval;

    @Setting("use-activation-and-tracking-range")
    private boolean useActivationAndTrackingRange;

    @Setting("entity-optimization")
    private EntityOptimizationSpec entityOptimization;
    @Setting("chunk-optimization")
    private ChunkOptimizationSpec chunkOptimization;
    @Setting("memory-optimization")
    private MemoryOptimizationSpec memoryOptimization;
    @Setting("async-system")
    private AsyncSystemSpec asyncSystem;
    @Setting("world-creation")
    private WorldCreationSpec worldCreation;

    public boolean isCachePluginClass() {
        return cachePluginClass;
    }

    public int getGoalSelectorInterval() {
        return goalSelectorInterval;
    }

    public boolean useActivationAndTrackingRange() {
        return useActivationAndTrackingRange;
    }

    public EntityOptimizationSpec getEntityOptimization() {
        return entityOptimization != null ? entityOptimization : new EntityOptimizationSpec();
    }

    public ChunkOptimizationSpec getChunkOptimization() {
        return chunkOptimization != null ? chunkOptimization : new ChunkOptimizationSpec();
    }

    public MemoryOptimizationSpec getMemoryOptimization() {
        return memoryOptimization != null ? memoryOptimization : new MemoryOptimizationSpec();
    }

    public AsyncSystemSpec getAsyncSystem() {
        return asyncSystem != null ? asyncSystem : new AsyncSystemSpec();
    }

    public WorldCreationSpec getWorldCreation() {
        return worldCreation != null ? worldCreation : new WorldCreationSpec();
    }
}
