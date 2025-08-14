package io.izzel.arclight.common.adventure;

import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import org.apache.logging.log4j.Logger;

/**
 * Initializer for Adventure integration in Luminara.
 * This class manages the lifecycle of Adventure components.
 */
public final class AdventureInitializer {

    private static final Logger LOGGER = ArclightI18nLogger.getLogger("AdventureInitializer");
    private static boolean initialized = false;

    private AdventureInitializer() {
    }

    /**
     * Initialize Adventure integration.
     */
    public static void initialize() {
        if (initialized) {
            return;
        }

        try {
            LOGGER.debug("Initializing Adventure integration for Luminara");

            // Initialize Adventure configuration
            AdventureConfig.initialize();

            // Initialize Adventure components
            initializeAdventureComponents();

            initialized = true;
            LOGGER.debug("Adventure integration initialized successfully");

        } catch (Exception e) {
            LOGGER.error("Failed to initialize Adventure integration: " + e.getMessage(), e);
            throw new RuntimeException("Adventure integration initialization failed", e);
        }
    }

    /**
     * Initialize Adventure components.
     */
    private static void initializeAdventureComponents() {
        // Verify Adventure serializers are working
        try {
            PaperAdventure.gsonSerializer();
            PaperAdventure.legacySerializer();
            PaperAdventure.plainSerializer();
            LOGGER.debug("Adventure serializers initialized successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Adventure serializers: " + e.getMessage(), e);
            throw new RuntimeException("Adventure serializers initialization failed", e);
        }

        // Verify MiniMessage is working
        try {
            PaperAdventure.miniMessage();
            LOGGER.debug("MiniMessage initialized successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize MiniMessage: " + e.getMessage(), e);
            throw new RuntimeException("MiniMessage initialization failed", e);
        }

        // Verify component flattener is working
        try {
            PaperAdventure.componentFlattener();
            LOGGER.debug("Component flattener initialized successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize component flattener: " + e.getMessage(), e);
            throw new RuntimeException("Component flattener initialization failed", e);
        }
    }

    /**
     * Shutdown Adventure integration.
     */
    public static void shutdown() {
        if (!initialized) {
            return;
        }

        try {
            LOGGER.debug("Shutting down Adventure integration");

            // Cleanup Adventure resources if needed

            initialized = false;
            LOGGER.info("Adventure integration shutdown completed");

        } catch (Exception e) {
            LOGGER.error("Failed to shutdown Adventure integration: " + e.getMessage(), e);
        }
    }

    /**
     * Check if Adventure integration is initialized.
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Reload Adventure configuration.
     */
    public static void reload() {
        if (!initialized) {
            LOGGER.warn("Cannot reload Adventure configuration: not initialized");
            return;
        }

        try {
            LOGGER.info("Reloading Adventure configuration");

            // Reload Adventure configuration
            AdventureConfig.initialize();

            LOGGER.info("Adventure configuration reloaded successfully");

        } catch (Exception e) {
            LOGGER.error("Failed to reload Adventure configuration: " + e.getMessage(), e);
        }
    }

    /**
     * Get Adventure integration status.
     */
    public static String getStatus() {
        if (!initialized) {
            return "Adventure integration: Not initialized";
        }

        return String.format("Adventure integration: Initialized - %s",
                AdventureConfig.getConfigurationSummary());
    }

    /**
     * Validate Adventure integration.
     */
    public static boolean validate() {
        if (!initialized) {
            return false;
        }

        try {
            // Test basic Adventure functionality
            net.kyori.adventure.text.Component testComponent = net.kyori.adventure.text.Component.text("Test");
            PaperAdventure.asVanilla(testComponent);
            PaperAdventure.asAdventure(net.minecraft.network.chat.Component.literal("Test"));

            return true;
        } catch (Exception e) {
            LOGGER.error("Adventure integration validation failed: " + e.getMessage(), e);
            return false;
        }
    }
}
