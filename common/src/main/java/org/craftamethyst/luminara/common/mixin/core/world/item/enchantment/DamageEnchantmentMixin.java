package org.craftamethyst.luminara.common.mixin.core.world.item.enchantment;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.craftamethyst.luminara.common.bridge.core.entity.LivingEntityBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DamageEnchantment.class)
public class DamageEnchantmentMixin {

  @Inject(
      method = "doPostAttack",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
  public void arclight$entityDamage(LivingEntity user, Entity target, int level, CallbackInfo ci) {
    ((LivingEntityBridge) target).bridge$pushEffectCause(EntityPotionEffectEvent.Cause.ATTACK);
  }
}
