package org.craftamethyst.luminara.common.mixin.core.async;

import org.craftamethyst.luminara.common.mod.util.async.AsyncCatcher;
import java.util.Optional;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityAsyncMixin {

  @Shadow
  public abstract boolean addEffect(MobEffectInstance effect, Entity source);

  @Inject(
      method =
          "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
      at = @At("HEAD"),
      cancellable = true)
  private void arclight$guardEffectAdd(
      MobEffectInstance effect, Entity source, CallbackInfoReturnable<Boolean> cir) {
    Optional<Boolean> guarded =
        AsyncCatcher.check("effect add", () -> this.addEffect(effect, source));
    guarded.ifPresent(cir::setReturnValue);
  }
}
