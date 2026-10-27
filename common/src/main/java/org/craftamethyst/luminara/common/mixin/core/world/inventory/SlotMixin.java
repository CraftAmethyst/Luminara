package org.craftamethyst.luminara.common.mixin.core.world.inventory;

import net.minecraft.world.inventory.Slot;
import org.craftamethyst.luminara.common.bridge.core.inventory.container.SlotBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Slot.class)
public abstract class SlotMixin implements SlotBridge {

  // @formatter:off
  @Shadow
  protected abstract void onSwapCraft(int numItemsCrafted);

  // @formatter:on

  @Override
  public void bridge$onSwapCraft(int numItemsCrafted) {
    onSwapCraft(numItemsCrafted);
  }
}
