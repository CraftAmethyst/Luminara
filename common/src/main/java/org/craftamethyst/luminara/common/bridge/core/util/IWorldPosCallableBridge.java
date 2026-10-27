package org.craftamethyst.luminara.common.bridge.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.CraftWorld;
import org.craftamethyst.luminara.common.bridge.core.world.WorldBridge;

public interface IWorldPosCallableBridge {
  default Level bridge$getWorld() {
    return ((ContainerLevelAccess) this).evaluate((a, b) -> a).orElse(null);
  }

  default BlockPos bridge$getPosition() {
    return ((ContainerLevelAccess) this).evaluate((a, b) -> b).orElse(null);
  }

  default Location bridge$getLocation() {
    BlockPos blockPos = bridge$getPosition();
    if (blockPos == null) {
      return null;
    } else {
      Level level = bridge$getWorld();
      CraftWorld world = level == null ? null : ((WorldBridge) level).bridge$getWorld();
      return new Location(world, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }
  }
}
