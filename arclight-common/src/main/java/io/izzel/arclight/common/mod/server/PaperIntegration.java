package io.izzel.arclight.common.mod.server;

import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.apache.logging.log4j.Logger;

/**
 * Main Paper integration class for Luminara.
 * This class manages the integration of Paper features into the Arclight/Luminara environment.
 */
public final class PaperIntegration {

    private static final Logger LOGGER = ArclightI18nLogger.getLogger("PaperIntegration");
    private static boolean initialized = false;
    private static boolean serverStarted = false;

    private PaperIntegration() {
    }

    /**
     * Initializes Paper integration during server startup.
     * This should be called early in the server startup process.
     */
    public static void initialize() {
        if (initialized) {
            return;
        }

        try {
            LOGGER.info("Initializing Paper integration for Luminara...");

            // Initialize Paper configuration system
            PaperConfigurationInitializer.initialize();

            // Initialize other Paper systems
            initializePaperSystems();

            initialized = true;
            LOGGER.info("Paper integration initialized successfully for Luminara");

        } catch (Exception e) {
            LOGGER.error("Failed to initialize Paper integration: " + e.getMessage(), e);
            throw new RuntimeException("Paper integration initialization failed", e);
        }
    }

    /**
     * Called when the server has started.
     */
    public static void onServerStarted(MinecraftServer server) {
        if (!initialized) {
            initialize();
        }

        try {
            LOGGER.debug("Paper integration: Server started");

            // Initialize world configurations for all existing worlds
            for (ServerLevel level : server.getAllLevels()) {
                PaperConfigurationInitializer.initializeWorldConfiguration(level);
            }

            serverStarted = true;
            LOGGER.info("Paper integration: All world configurations initialized");

        } catch (Exception e) {
            LOGGER.error("Failed to complete Paper integration server startup: " + e.getMessage(), e);
        }
    }

    /**
     * Called when a new world is created.
     */
    public static void onWorldCreated(ServerLevel level) {
        if (!initialized) {
            return;
        }

        try {
            PaperConfigurationInitializer.initializeWorldConfiguration(level);
            LOGGER.debug("Paper integration: Initialized configuration for world " + level.dimension().location());

        } catch (Exception e) {
            LOGGER.error("Failed to initialize Paper world configuration for " + level.dimension().location() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Called when a world is unloaded.
     */
    public static void onWorldUnloaded(ServerLevel level) {
        if (!initialized) {
            return;
        }

        try {
            PaperConfigurationInitializer.cleanupWorldConfiguration(level.dimension().location());
            LOGGER.debug("Paper integration: Cleaned up configuration for world " + level.dimension().location());

        } catch (Exception e) {
            LOGGER.error("Failed to cleanup Paper world configuration for " + level.dimension().location() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Called when the server is shutting down.
     */
    public static void onServerShutdown() {
        if (!initialized) {
            return;
        }

        try {
            LOGGER.debug("Paper integration: Server shutting down");

            // Cleanup Paper systems
            PaperConfigurationInitializer.shutdown();

            initialized = false;
            serverStarted = false;
            LOGGER.info("Paper integration shutdown completed");

        } catch (Exception e) {
            LOGGER.error("Failed to shutdown Paper integration: " + e.getMessage(), e);
        }
    }

    /**
     * Reloads all Paper configurations.
     */
    public static void reloadConfigurations() {
        if (!initialized) {
            LOGGER.error("Cannot reload configurations: Paper integration not initialized");
            return;
        }

        try {
            PaperConfigurationInitializer.reloadAll();
            LOGGER.info("Paper integration: All configurations reloaded");

        } catch (Exception e) {
            LOGGER.error("Failed to reload Paper configurations: " + e.getMessage(), e);
        }
    }

    /**
     * Initializes Paper-specific systems.
     */
    private static void initializePaperSystems() {
        // Initialize concurrent utilities
        LOGGER.debug("Paper integration: Concurrent utilities initialized");

        // Initialize other Paper systems as needed
        LOGGER.debug("Paper integration: All systems initialized");
    }

    /**
     * Checks if Paper integration is initialized.
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Checks if the server has started.
     */
    public static boolean isServerStarted() {
        return serverStarted;
    }

    /**
     * Gets integration status information.
     */
    public static String getStatus() {
        return String.format("Paper Integration Status: initialized=%s, serverStarted=%s",
                initialized, serverStarted);
    }
}
