package org.craftamethyst.luminara.common.mixin.optimization.general.activationrange;

import net.minecraft.world.entity.Entity;
import org.craftamethyst.luminara.common.bridge.optimization.EntityBridge_ActivationRange;
import org.spigotmc.ActivationRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = ActivationRange.class, remap = false)
public class ActivationRangeMixin {

  /**
   * @author IzzelAliz
   * @reason entityLists
   */
  @Overwrite
  private static void activateEntity(Entity entity) {
    ((EntityBridge_ActivationRange) entity).bridge$updateActivation();
  }
}
