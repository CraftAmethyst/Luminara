package org.craftamethyst.luminara.common.bridge.core.inventory.container;

import net.minecraft.world.inventory.ContainerLevelAccess;
import org.bukkit.Location;
import org.craftamethyst.luminara.common.bridge.core.util.IWorldPosCallableBridge;

public interface PosContainerBridge extends ContainerBridge {
  ContainerLevelAccess bridge$getWorldPos();

  default Location bridge$getWorldLocation() {
    return ((IWorldPosCallableBridge) bridge$getWorldPos()).bridge$getLocation();
  }
}
