package org.craftamethyst.luminara.common.mixin.core.world.inventory;

import org.craftamethyst.luminara.common.bridge.core.inventory.IInventoryBridge;
import org.craftamethyst.luminara.common.bridge.core.inventory.container.PosContainerBridge;
import net.minecraft.world.inventory.StonecutterMenu;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net/minecraft/world/inventory/StonecutterMenu$1")
public abstract class StonecutterContainer1Mixin implements IInventoryBridge {

  @Shadow(
      aliases = {"this$0", "f_40344_"},
      remap = false)
  private StonecutterMenu outerThis;

  @Override
  public Location getLocation() {
    return ((PosContainerBridge) outerThis).bridge$getWorldLocation();
  }
}
