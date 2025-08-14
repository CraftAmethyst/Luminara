package io.izzel.arclight.common.mod.server;

import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import io.papermc.paper.configuration.GlobalConfiguration;
import io.papermc.paper.configuration.PaperConfigurations;
import io.papermc.paper.configuration.WorldConfiguration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.apache.logging.log4j.Logger;

/**
 * Initializer for Paper configuration system in Luminara.
 * This class manages the lifecycle of Paper configurations.
 */
public final class PaperConfigurationInitializer {

    private static final Logger LOGGER = ArclightI18nLogger.getLogger("PaperConfiguration");
    private static boolean initialized = false;

    private PaperConfigurationInitializer() {
    }

    /**
     * Initializes the Paper configuration system.
     * This should be called during server startup.
     */
    public static void initialize() {
        if (initialized) {
            return;
        }

        try {
            // Initialize the Paper configuration system
            PaperConfigurations.initialize();

            // Set up global configuration
            GlobalConfiguration.initialize(java.nio.file.Paths.get("config"));

            initialized = true;
            LOGGER.info("Paper configuration system initialized for Luminara");

        } catch (Exception e) {
            LOGGER.error("Failed to initialize Paper configuration system: " + e.getMessage(), e);
        }
    }

    /**
     * Initializes world configuration for a specific world.
     */
    public static void initializeWorldConfiguration(ServerLevel level) {
        if (!initialized) {
            initialize();
        }

        try {
            ResourceLocation worldKey = level.dimension().location();
            PaperConfigurations.initializeWorldConfiguration(level);
            LOGGER.debug("Initialized Paper world configuration for: " + worldKey);

        } catch (Exception e) {
            LOGGER.error("Failed to initialize world configuration for " + level.dimension().location() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Gets the global configuration.
     */
    public static GlobalConfiguration getGlobalConfiguration() {
        if (!initialized) {
            initialize();
        }
        return PaperConfigurations.getGlobalConfiguration();
    }

    /**
     * Gets the world configuration for a specific world.
     */
    public static WorldConfiguration getWorldConfiguration(ServerLevel level) {
        if (!initialized) {
            initialize();
        }
        return PaperConfigurations.getWorldConfiguration(level);
    }

    /**
     * Gets the world configuration for a specific world key.
     */
    public static WorldConfiguration getWorldConfiguration(ResourceLocation worldKey) {
        if (!initialized) {
            initialize();
        }
        return PaperConfigurations.getWorldConfiguration(worldKey);
    }

    /**
     * Reloads all configurations.
     */
    public static void reloadAll() {
        if (!initialized) {
            initialize();
            return;
        }

        try {
            PaperConfigurations.reloadAll();
            LOGGER.info("Reloaded all Paper configurations");

        } catch (Exception e) {
            LOGGER.error("Failed to reload Paper configurations: " + e.getMessage(), e);
        }
    }

    /**
     * Cleans up world configuration when a world is unloaded.
     */
    public static void cleanupWorldConfiguration(ResourceLocation worldKey) {
        if (!initialized) {
            return;
        }

        try {
            PaperConfigurations.cleanupWorldConfiguration(worldKey);
            LOGGER.debug("Cleaned up Paper world configuration for: " + worldKey);

        } catch (Exception e) {
            LOGGER.error("Failed to cleanup world configuration for " + worldKey + ": " + e.getMessage(), e);
        }
    }

    /**
     * Checks if the configuration system is initialized.
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Shuts down the configuration system.
     */
    public static void shutdown() {
        if (!initialized) {
            return;
        }

        try {
            // Cleanup any resources
            initialized = false;
            LOGGER.info("Paper configuration system shut down");

        } catch (Exception e) {
            LOGGER.error("Failed to shutdown Paper configuration system: " + e.getMessage(), e);
        }
    }
}
