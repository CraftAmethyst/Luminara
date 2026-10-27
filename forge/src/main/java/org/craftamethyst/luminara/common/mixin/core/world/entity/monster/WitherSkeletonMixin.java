package org.craftamethyst.luminara.common.mixin.core.world.entity.monster;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.WitherSkeleton;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.craftamethyst.luminara.common.bridge.core.entity.LivingEntityBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WitherSkeleton.class)
public abstract class WitherSkeletonMixin extends AbstractSkeletonMixin {

  @Inject(
      method = "doHurtTarget",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"))
  private void arclight$reason(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
    ((LivingEntityBridge) entityIn).bridge$pushEffectCause(EntityPotionEffectEvent.Cause.ATTACK);
  }
}
