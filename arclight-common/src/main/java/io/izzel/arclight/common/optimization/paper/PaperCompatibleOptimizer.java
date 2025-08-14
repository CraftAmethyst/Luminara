package io.izzel.arclight.common.optimization.paper;

import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import io.izzel.arclight.i18n.ArclightConfig;
import net.minecraft.server.level.ServerLevel;
import org.apache.logging.log4j.Logger;

/**
 * Paper-compatible optimization system for Luminara
 * 
 * This class provides optimizations that work alongside Paper patches
 * without conflicting with Paper's internal optimization systems.
 * 
 * Key principles:
 * - Never override Paper's chunk management
 * - Never interfere with Paper's world creation process
 * - Only provide complementary optimizations
 * - Always check if Paper is handling the optimization first
 */
public class PaperCompatibleOptimizer {
    private static final Logger LOGGER = ArclightI18nLogger.getLogger("PaperCompatibleOptimizer");
    
    /**
     * Performs Paper-compatible world initialization optimizations
     * This method only applies optimizations that don't conflict with Paper
     */
    public static void optimizeWorldCompatibly(ServerLevel world) {
        var config = ArclightConfig.spec().getOptimization();
        
        // Only perform non-conflicting optimizations
        if (config.getMemoryOptimization().isCacheCleanupEnabled()) {
            performMemoryOptimization(world);
        }
        
        LOGGER.debug("Applied Paper-compatible optimizations for world: {}", 
                    world.dimension().location());
    }
    
    /**
     * Performs memory optimization that doesn't conflict with Paper
     */
    private static void performMemoryOptimization(ServerLevel world) {
        // Only perform memory cleanup, not chunk management
        System.gc(); // Suggest garbage collection
        
        LOGGER.debug("Performed memory optimization for world: {}", 
                    world.dimension().location());
    }
    
    /**
     * Checks if a specific optimization is safe to use with Paper
     */
    public static boolean isOptimizationSafeWithPaper(String optimizationType) {
        switch (optimizationType.toLowerCase()) {
            case "memory":
            case "entity":
                return true;
            case "chunk":
            case "world":
            case "spawn":
                return false; // Paper handles these
            default:
                return false; // Conservative approach
        }
    }
    
    /**
     * Validates that current configuration is Paper-compatible
     */
    public static boolean validatePaperCompatibility() {
        var config = ArclightConfig.spec().getOptimization();
        
        // Check that conflicting optimizations are disabled
        boolean worldOptDisabled = !config.getWorldCreation().isFastWorldCreation();
        boolean chunkOptDisabled = !config.getChunkOptimization().isOptimizeChunkLoading();
        boolean aggressiveUnloadDisabled = !config.getChunkOptimization().isAggressiveChunkUnloading();
        
        boolean compatible = worldOptDisabled && chunkOptDisabled && aggressiveUnloadDisabled;
        
        if (!compatible) {
            LOGGER.warn("Configuration contains settings that may conflict with Paper patches!");
            LOGGER.warn("Please ensure world-creation and chunk-optimization are disabled");
        }
        
        return compatible;
    }
    
    /**
     * Logs the current Paper compatibility status
     */
    public static void logCompatibilityStatus() {
        boolean compatible = validatePaperCompatibility();
        
        if (compatible) {
            LOGGER.info("Luminara optimizations are configured for Paper compatibility");
        } else {
            LOGGER.warn("Luminara optimizations may conflict with Paper patches - check configuration");
        }
    }
}
