package org.craftamethyst.luminara.common.mixin.core.world.entity.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.craftamethyst.luminara.common.bridge.core.entity.projectile.TridentEntityBridge;
import org.craftamethyst.luminara.common.bridge.core.world.server.ServerWorldBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin extends AbstractArrowMixin implements TridentEntityBridge {

  @Shadow public ItemStack tridentItem;

  @Redirect(
      method = "onHitEntity",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
  private boolean arclight$lightning(Level world, Entity entityIn) {
    ((ServerWorldBridge) this.level())
        .bridge$strikeLightning((LightningBolt) entityIn, LightningStrikeEvent.Cause.TRIDENT);
    return true;
  }

  @Override
  public void bridge$setThrownStack(ItemStack itemStack) {
    this.tridentItem = itemStack;
  }
}
