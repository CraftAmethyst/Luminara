package io.papermc.paper.configuration;

import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;

/**
 * Main Paper configuration manager for Luminara.
 * This class manages both global and world-specific configurations.
 */
public class PaperConfigurations extends Configurations<GlobalConfiguration, WorldConfiguration> {

    private static final Logger LOGGER = ArclightI18nLogger.getLogger("PaperConfigurations");
    private static PaperConfigurations instance;

    public PaperConfigurations() {
        super(
                Paths.get("config"),
                GlobalConfiguration.class,
                WorldConfiguration.class,
                "paper-global.yml",
                "paper-world-defaults.yml",
                "paper-world.yml"
        );
    }

    public static PaperConfigurations getInstance() {
        if (instance == null) {
            instance = new PaperConfigurations();
        }
        return instance;
    }

    public static void initialize() {
        PaperConfigurations configs = getInstance();
        configs.initializeGlobalConfiguration();

        // Initialize default world configuration
        configs.initializeWorldConfiguration(WORLD_DEFAULTS_KEY);

        LOGGER.info("Initialized Paper configuration system for Luminara");
    }

    public static void initializeWorldConfiguration(ServerLevel level) {
        getInstance().initializeWorldConfiguration(level.dimension().location());
    }

    public static GlobalConfiguration getGlobalConfiguration() {
        return getInstance().globalConfiguration();
    }

    public static WorldConfiguration getWorldConfiguration(ResourceLocation worldKey) {
        return getInstance().worldConfiguration(worldKey);
    }

    public static WorldConfiguration getWorldConfiguration(ServerLevel level) {
        return getInstance().worldConfiguration(level);
    }

    /**
     * Reloads all configurations.
     */
    public static void reloadAll() {
        PaperConfigurations configs = getInstance();
        configs.reloadGlobalConfiguration();

        // Reload all world configurations
        for (ResourceLocation worldKey : configs.getWorldConfigurationKeys()) {
            configs.reloadWorldConfiguration(worldKey);
        }

        LOGGER.info("Reloaded all Paper configurations");
    }

    /**
     * Cleanup configuration for a world that is being unloaded.
     */
    public static void cleanupWorldConfiguration(ResourceLocation worldKey) {
        getInstance().removeWorldConfiguration(worldKey);
    }
}
