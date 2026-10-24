package io.izzel.arclight.common.mixin.bukkit.plugin;

import io.izzel.arclight.common.mod.util.remapper.generated.RemappingURLClassLoader;
import java.net.URL;
import java.net.URLClassLoader;
import org.bukkit.plugin.PluginDescriptionFile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "org.bukkit.plugin.java.LibraryLoader", remap = false)
public class LibraryLoaderMixin {

  @Redirect(method = "createLoader", at = @At(value = "NEW", target = "java/net/URLClassLoader"))
  private URLClassLoader arclight$useRemapped(
      URL[] urls, ClassLoader loader, PluginDescriptionFile desc) {
    return new RemappingURLClassLoader(String.format("%s/L", desc.getName()), urls, loader);
  }
}
