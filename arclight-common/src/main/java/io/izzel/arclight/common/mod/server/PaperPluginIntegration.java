package io.izzel.arclight.common.mod.server;

import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import io.papermc.paper.plugin.manager.PaperPluginManagerImpl;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

/**
 * Integration class for Paper plugin system in Luminara.
 * This class handles the initialization and integration of the Paper plugin system
 * with the existing Arclight/Bukkit plugin infrastructure.
 */
public class PaperPluginIntegration {

    private static final Logger LOGGER = ArclightI18nLogger.getLogger("PaperPluginIntegration");
    private static boolean initialized = false;
    private static PaperPluginManagerImpl paperPluginManager;

    private PaperPluginIntegration() {
    }

    /**
     * Initializes the Paper plugin system integration.
     * This should be called during server startup after the basic Bukkit server is initialized.
     */
    public static void initialize() {
        if (initialized) {
            return;
        }

        try {
            LOGGER.info("Initializing Paper plugin system integration for Luminara...");

            Server server = Bukkit.getServer();
            if (!(server instanceof CraftServer craftServer)) {
                LOGGER.warn("Server is not a CraftServer instance, Paper plugin integration may not work correctly");
                return;
            }

            // Validate that the Paper plugin manager is properly initialized via Mixin
            if (!validatePaperPluginManager(craftServer)) {
                LOGGER.error("Failed to initialize Paper plugin manager, Paper plugin integration cannot continue");
                return;
            }

            // Get the Paper plugin manager instance
            paperPluginManager = (PaperPluginManagerImpl) craftServer.getPluginManager();

            initialized = true;
            LOGGER.info("Paper plugin system integration initialized successfully for Luminara");

        } catch (Exception e) {
            LOGGER.error("Failed to initialize Paper plugin system integration: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Paper plugin system integration initialization failed", e);
        }
    }

    /**
     * Validates that the Paper plugin manager is properly initialized.
     * With Mixin integration, the plugin manager should be automatically replaced.
     */
    private static boolean validatePaperPluginManager(@NotNull CraftServer server) {
        PluginManager manager = server.getPluginManager();
        if (manager instanceof PaperPluginManagerImpl) {
            LOGGER.info("Paper plugin manager is properly initialized via Mixin");
            return true;
        } else {
            LOGGER.warn("Paper plugin manager was not initialized via Mixin, falling back to reflection");
            return fallbackPluginManagerReplacement(server);
        }
    }

    /**
     * Fallback method to replace plugin manager using reflection if Mixin failed.
     */
    private static boolean fallbackPluginManagerReplacement(@NotNull CraftServer server) {
        try {
            Field pluginManagerField = CraftServer.class.getDeclaredField("pluginManager");
            pluginManagerField.setAccessible(true);

            Field commandMapField = CraftServer.class.getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            SimpleCommandMap commandMap = (SimpleCommandMap) commandMapField.get(server);

            PaperPluginManagerImpl paperManager = new PaperPluginManagerImpl(server, commandMap);
            pluginManagerField.set(server, paperManager);

            LOGGER.info("Successfully replaced plugin manager using reflection fallback");
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to replace plugin manager using reflection fallback: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if the Paper plugin system integration is initialized.
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Gets the Paper plugin manager instance.
     */
    @NotNull
    public static PaperPluginManagerImpl getPaperPluginManager() {
        if (!initialized || paperPluginManager == null) {
            throw new IllegalStateException("Paper plugin system integration not initialized");
        }
        return paperPluginManager;
    }

    /**
     * Shuts down the Paper plugin system integration.
     * This should be called during server shutdown.
     */
    public static void shutdown() {
        if (!initialized) {
            return;
        }

        try {
            LOGGER.info("Shutting down Paper plugin system integration...");

            if (paperPluginManager != null) {
                paperPluginManager.disablePlugins();
                paperPluginManager.clearPlugins();
            }

            initialized = false;
            paperPluginManager = null;

            LOGGER.info("Paper plugin system integration shut down successfully");

        } catch (Exception e) {
            LOGGER.error("Error during Paper plugin system integration shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Reloads the Paper plugin system.
     * This will disable all plugins, clear the plugin manager, and reinitialize.
     */
    public static void reload() {
        if (!initialized) {
            LOGGER.warn("Cannot reload Paper plugin system: not initialized");
            return;
        }

        try {
            LOGGER.info("Reloading Paper plugin system...");

            shutdown();
            initialize();

            LOGGER.info("Paper plugin system reloaded successfully");

        } catch (Exception e) {
            LOGGER.error("Error during Paper plugin system reload: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets information about the current Paper plugin system state.
     */
    @NotNull
    public static String getSystemInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Paper Plugin System Integration Status:\n");
        info.append("Initialized: ").append(initialized).append("\n");

        if (initialized && paperPluginManager != null) {
            info.append("Loaded Plugins: ").append(paperPluginManager.getPlugins().length).append("\n");
            info.append("Plugin Manager: ").append(paperPluginManager.getClass().getSimpleName()).append("\n");
            info.append("Load Order Tree: ").append(paperPluginManager.getLoadOrderTree().getClass().getSimpleName()).append("\n");
        }

        return info.toString();
    }
}
