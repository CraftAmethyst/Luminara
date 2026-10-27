package org.craftamethyst.luminara.common.mixin.bukkit;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;
import org.craftamethyst.luminara.common.mod.util.log.ArclightPluginLogger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(JavaPlugin.class)
public class JavaPluginMixin {

  @Redirect(
      method = "init",
      remap = false,
      at = @At(value = "NEW", target = "org/bukkit/plugin/PluginLogger"))
  private PluginLogger arclight$createLogger(Plugin plugin) {
    return new ArclightPluginLogger(plugin);
  }
}
