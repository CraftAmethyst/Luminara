package org.craftamethyst.luminara.common.mixin.core.world.entity.projectile;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SpectralArrow;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.craftamethyst.luminara.common.bridge.core.entity.LivingEntityBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpectralArrow.class)
public abstract class SpectralArrowMixin extends AbstractArrowMixin {

  @Inject(
      method = "doPostHurtEffects",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"))
  private void arclight$hit(LivingEntity living, CallbackInfo ci) {
    ((LivingEntityBridge) living).bridge$pushEffectCause(EntityPotionEffectEvent.Cause.ARROW);
  }
}
