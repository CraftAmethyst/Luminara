package io.izzel.arclight.common.mixin.paper.plugin;

import io.papermc.paper.plugin.configuration.PaperPluginDescriptionFile;
import io.papermc.paper.plugin.loader.PaperPluginClassLoader;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.logging.Logger;

/**
 * Mixin to enhance JavaPlugin with Paper plugin system features.
 * This allows all JavaPlugin instances to automatically gain Paper plugin capabilities
 * without requiring inheritance from a specific base class.
 */
@Mixin(JavaPlugin.class)
public abstract class JavaPluginPaperMixin implements PaperPluginAPI {

    @Unique
    private PaperPluginDescriptionFile paperDescription;
    @Unique
    private boolean paperPluginInitialized = false;

    // Shadow methods from JavaPlugin
    @Shadow
    protected abstract ClassLoader getClassLoader();

    @Shadow
    public abstract PluginDescriptionFile getDescription();

    @Shadow
    public abstract Logger getLogger();

    /**
     * Initialize Paper plugin features during JavaPlugin construction.
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    private void arclight$initPaperFeatures(CallbackInfo ci) {
        initializePaperFeatures();
    }

    /**
     * Initialize Paper plugin features if this plugin is loaded by Paper plugin system.
     */
    @Unique
    private void initializePaperFeatures() {
        if (!paperPluginInitialized && getClassLoader() instanceof PaperPluginClassLoader loader) {
            try {
                this.paperDescription = loader.getDescription();
                this.paperPluginInitialized = true;

                // Log Paper plugin initialization
                getLogger().info("Paper plugin features initialized for: " + getDescription().getName());

            } catch (Exception e) {
                getLogger().warning("Failed to initialize Paper plugin features: " + e.getMessage());
            }
        }
    }

    // Implementation of PaperPluginAPI interface

    /**
     * Gets the Paper plugin description file for this plugin.
     *
     * @return the Paper plugin description, or null if this is not a Paper plugin
     */
    public PaperPluginDescriptionFile getPaperDescription() {
        if (!paperPluginInitialized) {
            initializePaperFeatures();
        }
        return paperDescription;
    }

    /**
     * Checks if this plugin is loaded by the Paper plugin system.
     *
     * @return true if this is a Paper plugin, false otherwise
     */
    public boolean isPaperPlugin() {
        return getClassLoader() instanceof PaperPluginClassLoader;
    }

    /**
     * Gets the Paper plugin class loader if this plugin is loaded by the Paper system.
     *
     * @return the Paper plugin class loader, or null if not a Paper plugin
     */
    public PaperPluginClassLoader getPaperClassLoader() {
        if (getClassLoader() instanceof PaperPluginClassLoader) {
            return (PaperPluginClassLoader) getClassLoader();
        }
        return null;
    }

    /**
     * Checks if this plugin has an open classloader that allows other plugins to access its classes.
     *
     * @return true if the classloader is open, false otherwise
     */
    public boolean hasOpenClassloader() {
        PaperPluginDescriptionFile desc = getPaperDescription();
        return desc != null && desc.hasOpenClassloader();
    }

    /**
     * Gets information about the plugin's classloader configuration.
     *
     * @return classloader information string
     */
    public String getClassLoaderInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Plugin: ").append(getDescription().getName()).append("\n");
        info.append("Type: ").append(isPaperPlugin() ? "Paper Plugin" : "Bukkit Plugin").append("\n");
        info.append("ClassLoader: ").append(getClassLoader().getClass().getSimpleName()).append("\n");

        if (isPaperPlugin()) {
            info.append("Open ClassLoader: ").append(hasOpenClassloader()).append("\n");
            PaperPluginDescriptionFile desc = getPaperDescription();
            if (desc != null) {
                info.append("Load Before: ").append(desc.getLoadBefore()).append("\n");
                info.append("Load After: ").append(desc.getLoadAfter()).append("\n");
            }
        }

        return info.toString();
    }

    /**
     * Gets the Paper plugin loader name.
     *
     * @return the loader name
     */
    public String getPaperLoaderName() {
        return "PaperPlugin";
    }

    /**
     * Gets the Paper plugin loader version.
     *
     * @return the loader version
     */
    public String getPaperLoaderVersion() {
        return "1.0.0";
    }

    /**
     * Checks if this loader supports the given plugin type.
     *
     * @param pluginType the plugin type to check
     * @return true if supported, false otherwise
     */
    public boolean supportsPaperPluginType(String pluginType) {
        return "paper".equalsIgnoreCase(pluginType);
    }

    /**
     * Gets additional information about this loader.
     *
     * @return additional loader information
     */
    public String getPaperAdditionalInfo() {
        return "Paper plugin with enhanced classloader isolation and dependency management";
    }

    /**
     * Checks if this loader provides classloader isolation.
     *
     * @return true if classloader isolation is provided
     */
    public boolean providesPaperClassloaderIsolation() {
        return isPaperPlugin();
    }

    /**
     * Gets the priority of this loader.
     *
     * @return the loader priority
     */
    public int getPaperLoaderPriority() {
        return isPaperPlugin() ? 100 : 50; // Higher priority for Paper plugins
    }
}
