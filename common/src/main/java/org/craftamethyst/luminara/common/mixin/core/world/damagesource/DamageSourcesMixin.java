package org.craftamethyst.luminara.common.mixin.core.world.damagesource;

import org.craftamethyst.luminara.common.bridge.core.util.DamageSourceBridge;
import org.craftamethyst.luminara.common.bridge.core.util.DamageSourcesBridge;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DamageSources.class)
public abstract class DamageSourcesMixin implements DamageSourcesBridge {

  public DamageSource melting;
  // @formatter:on
  public DamageSource poison;

  // @formatter:off
  @Shadow
  protected abstract DamageSource source(ResourceKey<DamageType> p_270957_);

  @Inject(method = "<init>", at = @At("RETURN"))
  private void arclight$init(RegistryAccess p_270740_, CallbackInfo ci) {
    this.melting = ((DamageSourceBridge) this.source(DamageTypes.ON_FIRE)).bridge$melting();
    this.poison = ((DamageSourceBridge) this.source(DamageTypes.MAGIC)).bridge$poison();
  }

  @Override
  public DamageSource bridge$poison() {
    return poison;
  }

  @Override
  public DamageSource bridge$melting() {
    return melting;
  }
}
