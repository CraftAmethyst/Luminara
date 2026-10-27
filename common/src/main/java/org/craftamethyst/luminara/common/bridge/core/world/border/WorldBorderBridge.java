package org.craftamethyst.luminara.common.bridge.core.world.border;

import net.minecraft.world.level.Level;

public interface WorldBorderBridge {
  Level bridge$getWorld();

  void bridge$setWorld(Level world);
}
