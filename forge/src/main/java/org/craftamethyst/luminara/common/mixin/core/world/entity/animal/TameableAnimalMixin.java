package org.craftamethyst.luminara.common.mixin.core.world.entity.animal;

import net.minecraft.world.entity.TamableAnimal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TamableAnimal.class)
public abstract class TameableAnimalMixin extends AnimalMixin {

  // @formatter:off
  @Shadow
  public abstract boolean isTame();
  // @formatter:on
}
