package io.izzel.arclight.common.mixin.paper.plugin;

import io.papermc.paper.plugin.manager.PaperPluginManagerImpl;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Paper API patch 0011: Add command line option to load extra plugin jars
 * Adds support for loading extra plugin jars from command line arguments
 */
@Mixin(value = PaperPluginManagerImpl.class, remap = false)
public abstract class PaperPluginManagerMixin_ExtraJars {

    @Shadow
    @Final
    private Logger logger;

    @Shadow
    public abstract Plugin loadPlugin(@NotNull File file) throws InvalidPluginException;

    @Shadow
    @NotNull
    public abstract Plugin[] loadPlugins(@NotNull File directory);

    /**
     * Loads plugins from a directory and additional plugin jar files.
     * This is the Paper equivalent of SimplePluginManager.loadPlugins(File, List<File>)
     *
     * @param directory       the plugins directory
     * @param extraPluginJars additional plugin jar files to load
     * @return array of loaded plugins
     */
    @NotNull
    public Plugin[] loadPlugins(@NotNull File directory, @NotNull List<File> extraPluginJars) {
        // First load plugins from the directory using the existing method
        Plugin[] directoryPlugins = loadPlugins(directory);

        if (extraPluginJars.isEmpty()) {
            return directoryPlugins;
        }

        List<Plugin> allPlugins = new ArrayList<>();
        Collections.addAll(allPlugins, directoryPlugins);

        // Load extra plugin jars
        for (File file : extraPluginJars) {
            try {
                Plugin plugin = loadPlugin(file);
                if (plugin != null) {
                    allPlugins.add(plugin);
                    logger.info("Loaded extra plugin jar: " + file.getName());
                }
            } catch (InvalidPluginException e) {
                logger.log(Level.SEVERE, "Plugin loading error for extra jar: " + file.getName(), e);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Unexpected error loading extra plugin jar: " + file.getName(), e);
            }
        }

        return allPlugins.toArray(new Plugin[0]);
    }
}
