package org.craftamethyst.luminara.common.mixin.core.world.entity.animal;

import org.craftamethyst.luminara.common.bridge.core.inventory.container.ContainerBridge;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net/minecraft/world/entity/animal/Sheep$1")
public abstract class Sheep1Mixin implements ContainerBridge {

  @Override
  public InventoryView bridge$getBukkitView() {
    return null;
  }
}
