package io.izzel.arclight.i18n.conf;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class OptimizationSpec {

    @Setting("cache-plugin-class")
    private boolean cachePluginClass;

    @Setting("goal-selector-update-interval")
    private int goalSelectorInterval;

    // Entity optimization settings
    @Setting("entity-optimization")
    private EntityOptimizationSpec entityOptimization;

    // Chunk optimization settings
    @Setting("chunk-optimization")
    private ChunkOptimizationSpec chunkOptimization;

    // MPEM optimization settings removed to avoid conflicts with Paper
    // @Setting("memory-optimization")
    // private MemoryOptimizationSpec memoryOptimization;

    // @Setting("async-system")
    // private AsyncSystemSpec asyncSystem;

    // World creation optimization settings
    @Setting("world-creation")
    private WorldCreationSpec worldCreation;

    public boolean isCachePluginClass() {
        return cachePluginClass;
    }

    public int getGoalSelectorInterval() {
        return goalSelectorInterval;
    }

    public EntityOptimizationSpec getEntityOptimization() {
        return entityOptimization != null ? entityOptimization : new EntityOptimizationSpec();
    }

    public ChunkOptimizationSpec getChunkOptimization() {
        return chunkOptimization != null ? chunkOptimization : new ChunkOptimizationSpec();
    }

    // MPEM optimization getters removed - functionality disabled
    // public MemoryOptimizationSpec getMemoryOptimization() {
    //     return null; // MPEM memory optimization disabled
    // }

    // public AsyncSystemSpec getAsyncSystem() {
    //     return null; // MPEM async system disabled
    // }

    public WorldCreationSpec getWorldCreation() {
        return worldCreation != null ? worldCreation : new WorldCreationSpec();
    }
}
