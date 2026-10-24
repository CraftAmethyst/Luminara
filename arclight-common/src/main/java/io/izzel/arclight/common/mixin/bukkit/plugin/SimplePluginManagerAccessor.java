package io.izzel.arclight.common.mixin.bukkit.plugin;

import com.google.common.graph.MutableGraph;
import org.bukkit.plugin.SimplePluginManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = SimplePluginManager.class, remap = false)
public interface SimplePluginManagerAccessor {

  @Accessor("dependencyGraph")
  MutableGraph<String> arclight$dependencyGraph();
}
