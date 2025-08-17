package io.izzel.arclight.common.mixin.paper.server;

import org.bukkit.craftbukkit.v.CraftServer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.io.File;

/**
 * Paper API patch 0011: Add command line option to load extra plugin jars
 * Implements getPluginsFolder() method for CraftServer
 */
@Mixin(value = CraftServer.class, remap = false)
public class CraftServerMixin_PluginsFolder {

    /**
     * Returns the de facto plugins directory, generally used for storing plugin jars to be loaded,
     * as well as their {@link org.bukkit.plugin.Plugin#getDataFolder() data folders}.
     *
     * <p>Plugins should use {@link org.bukkit.plugin.Plugin#getDataFolder()} rather than traversing this
     * directory manually when determining the location in which to store their data and configuration files.</p>
     *
     * @return plugins directory
     */
    @NotNull
    public File getPluginsFolder() {
        // Return the standard plugins folder
        return new File("plugins");
    }
}
