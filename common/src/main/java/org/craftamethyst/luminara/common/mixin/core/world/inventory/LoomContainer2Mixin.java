package org.craftamethyst.luminara.common.mixin.core.world.inventory;

import net.minecraft.world.inventory.LoomMenu;
import org.bukkit.Location;
import org.craftamethyst.luminara.common.bridge.core.inventory.container.PosContainerBridge;
import org.craftamethyst.luminara.common.mixin.core.world.SimpleContainerMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net/minecraft/world/inventory/LoomMenu$2")
public abstract class LoomContainer2Mixin extends SimpleContainerMixin {

  @Shadow(
      aliases = {"this$0", "f_39905_"},
      remap = false)
  private LoomMenu outerThis;

  @Override
  public Location getLocation() {
    return ((PosContainerBridge) outerThis).bridge$getWorldLocation();
  }
}
