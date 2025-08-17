package io.izzel.arclight.common.mixin.paper.server;

import io.papermc.paper.plugin.manager.PaperPluginManagerImpl;
import joptsimple.OptionSet;
import net.minecraft.server.MinecraftServer;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.plugin.PluginManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Paper API patch 0011: Add command line option to load extra plugin jars
 * Modifies CraftServer to handle extra plugin jars from command line arguments
 */
@Mixin(value = CraftServer.class, remap = false)
public class CraftServerMixin_ExtraPluginJars {

    @Shadow
    @Final
    protected net.minecraft.server.dedicated.DedicatedServer console;
    @Shadow
    @Final
    private org.bukkit.plugin.SimplePluginManager pluginManager;
    @Shadow
    @Final
    private java.util.logging.Logger logger;

    /**
     * Redirect the plugin loading to include extra plugin jars from command line arguments.
     */
    @Redirect(method = "loadPlugins", at = @At(value = "INVOKE",
            target = "Lorg/bukkit/plugin/PluginManager;loadPlugins(Ljava/io/File;)[Lorg/bukkit/plugin/Plugin;"))
    private org.bukkit.plugin.Plugin[] arclight$loadPluginsWithExtraJars(PluginManager pluginManager, File pluginsFolder) {
        List<File> extraPluginJars = getExtraPluginJars();

        if (extraPluginJars.isEmpty()) {
            // No extra plugin jars, use normal loading
            return pluginManager.loadPlugins(pluginsFolder);
        }

        // Use the enhanced loading method if available (PaperPluginManagerImpl)
        if (pluginManager instanceof PaperPluginManagerImpl) {
            try {
                Method loadPluginsMethod = PaperPluginManagerImpl.class.getMethod("loadPlugins", File.class, List.class);
                return (org.bukkit.plugin.Plugin[]) loadPluginsMethod.invoke(pluginManager, pluginsFolder, extraPluginJars);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to load extra plugin jars using PaperPluginManagerImpl", e);
                // Fall back to normal loading
                return pluginManager.loadPlugins(pluginsFolder);
            }
        } else {
            // For other plugin managers, try to use reflection to call the enhanced method
            try {
                Method loadPluginsMethod = pluginManager.getClass().getMethod("loadPlugins", File.class, List.class);
                return (org.bukkit.plugin.Plugin[]) loadPluginsMethod.invoke(pluginManager, pluginsFolder, extraPluginJars);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Plugin manager does not support extra plugin jars, loading normally", e);
                return pluginManager.loadPlugins(pluginsFolder);
            }
        }
    }

    /**
     * Get extra plugin jars from command line arguments.
     */
    private List<File> getExtraPluginJars() {
        List<File> extraJars = new ArrayList<>();

        try {
            MinecraftServer server = this.console;
            if (server != null) {
                // Get the options from the server
                java.lang.reflect.Field optionsField = server.getClass().getDeclaredField("options");
                optionsField.setAccessible(true);
                OptionSet options = (OptionSet) optionsField.get(server);

                if (options != null && options.has("add-plugin")) {
                    @SuppressWarnings("unchecked")
                    List<File> pluginFiles = (List<File>) options.valuesOf("add-plugin");
                    for (File file : pluginFiles) {
                        if (file.exists() && file.isFile() && file.getName().endsWith(".jar")) {
                            extraJars.add(file);
                            logger.info("Found extra plugin jar from command line: " + file.getAbsolutePath());
                        } else {
                            logger.warning("Extra plugin jar not found or invalid: " + file.getAbsolutePath());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to get extra plugin jars from command line", e);
        }

        return extraJars;
    }
}
