package io.papermc.paper.configuration;

import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simplified Paper Configurations system for Luminara compatibility.
 * This provides the basic structure needed for Paper configuration management.
 */
public abstract class Configurations<G, W> {

    public static final String WORLD_DEFAULTS = "__world_defaults__";
    public static final ResourceLocation WORLD_DEFAULTS_KEY = new ResourceLocation("configurations", WORLD_DEFAULTS);

    protected final Path globalFolder;
    protected final Class<G> globalConfigClass;
    protected final Class<W> worldConfigClass;
    protected final String globalConfigFileName;
    protected final String defaultWorldConfigFileName;
    protected final String worldConfigFileName;
    protected final Map<ResourceLocation, W> worldConfigurations = new ConcurrentHashMap<>();
    protected final Logger logger;
    protected G globalConfiguration;

    public Configurations(
            final Path globalFolder,
            final Class<G> globalConfigType,
            final Class<W> worldConfigClass,
            final String globalConfigFileName,
            final String defaultWorldConfigFileName,
            final String worldConfigFileName
    ) {
        this.globalFolder = globalFolder;
        this.globalConfigClass = globalConfigType;
        this.worldConfigClass = worldConfigClass;
        this.globalConfigFileName = globalConfigFileName;
        this.defaultWorldConfigFileName = defaultWorldConfigFileName;
        this.worldConfigFileName = worldConfigFileName;
        this.logger = ArclightI18nLogger.getLogger("Configurations");
    }

    /**
     * Gets the global configuration.
     */
    public G globalConfiguration() {
        return this.globalConfiguration;
    }

    /**
     * Gets the world configuration for the specified world.
     */
    public W worldConfiguration(final ResourceLocation worldKey) {
        return this.worldConfigurations.get(worldKey);
    }

    /**
     * Gets the world configuration for the specified server level.
     */
    public W worldConfiguration(final ServerLevel level) {
        return this.worldConfiguration(level.dimension().location());
    }

    /**
     * Initializes the configuration system.
     */
    public void initializeGlobalConfiguration() {
        try {
            this.globalConfiguration = this.globalConfigClass.getDeclaredConstructor().newInstance();
            this.logger.info("Initialized global configuration: " + this.globalConfigClass.getSimpleName());
        } catch (Exception e) {
            this.logger.error("Failed to initialize global configuration: " + e.getMessage(), e);
        }
    }

    /**
     * Initializes world configuration for the specified world.
     */
    public void initializeWorldConfiguration(final ResourceLocation worldKey) {
        try {
            W worldConfig = this.worldConfigClass.getDeclaredConstructor().newInstance();
            this.worldConfigurations.put(worldKey, worldConfig);
            this.logger.debug("Initialized world configuration for: " + worldKey);
        } catch (Exception e) {
            this.logger.error("Failed to initialize world configuration for " + worldKey + ": " + e.getMessage(), e);
        }
    }

    /**
     * Reloads the global configuration.
     */
    public void reloadGlobalConfiguration() {
        this.initializeGlobalConfiguration();
        this.logger.info("Reloaded global configuration");
    }

    /**
     * Reloads world configuration for the specified world.
     */
    public void reloadWorldConfiguration(final ResourceLocation worldKey) {
        this.initializeWorldConfiguration(worldKey);
        this.logger.debug("Reloaded world configuration for: " + worldKey);
    }

    /**
     * Gets all world configuration keys.
     */
    public java.util.Set<ResourceLocation> getWorldConfigurationKeys() {
        return this.worldConfigurations.keySet();
    }

    /**
     * Removes world configuration for the specified world.
     */
    public void removeWorldConfiguration(final ResourceLocation worldKey) {
        this.worldConfigurations.remove(worldKey);
        this.logger.debug("Removed world configuration for: " + worldKey);
    }
}
