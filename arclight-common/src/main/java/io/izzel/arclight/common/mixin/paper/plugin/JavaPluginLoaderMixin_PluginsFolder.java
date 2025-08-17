package io.izzel.arclight.common.mixin.paper.plugin;

import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;

/**
 * Paper API patch 0011: Add command line option to load extra plugin jars
 * Modifies JavaPluginLoader to use server.getPluginsFolder() for data directories
 */
@Mixin(value = JavaPluginLoader.class, remap = false)
public class JavaPluginLoaderMixin_PluginsFolder {

    @Shadow
    @Final
    Server server;

    /**
     * Redirect the parent file access to use the server's plugins folder instead.
     * This ensures that plugin data folders are always created in the plugins directory,
     * even when the plugin jar is loaded from a different location (e.g., command line).
     */
    @Redirect(method = "loadPlugin(Ljava/io/File;)Lorg/bukkit/plugin/Plugin;",
            at = @At(value = "INVOKE", target = "Ljava/io/File;getParentFile()Ljava/io/File;"))
    private File arclight$usePluginsFolder(File file) {
        // Use the server's plugins folder instead of the jar file's parent directory
        return new File("plugins"); // Use default plugins folder
    }
}
