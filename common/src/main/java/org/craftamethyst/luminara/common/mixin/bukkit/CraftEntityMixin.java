package org.craftamethyst.luminara.common.mixin.bukkit;

import org.craftamethyst.luminara.common.mod.server.entity.ArclightFakePlayer;
import org.craftamethyst.luminara.common.mod.server.entity.EntityClassLookup;
import org.craftamethyst.luminara.common.mod.util.PlatformHooks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.entity.CraftComplexPart;
import org.bukkit.craftbukkit.v.entity.CraftEnderDragonPart;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CraftEntity.class, remap = false)
public abstract class CraftEntityMixin implements org.bukkit.entity.Entity {

  @Shadow protected Entity entity;

  @Shadow @Final protected CraftServer server;

  @Inject(method = "getEntity", cancellable = true, at = @At("HEAD"))
  private static void arclight$fakePlayer(
      CraftServer server, Entity entity, CallbackInfoReturnable<CraftEntity> cir) {
    if (PlatformHooks.isFakePlayer(entity)) {
      cir.setReturnValue(new ArclightFakePlayer(server, (net.minecraft.server.level.ServerPlayer) entity));
      return;
    }
    if (entity instanceof EnderDragonPart part) {
      if (part.parentMob instanceof EnderDragon) {
        cir.setReturnValue(new CraftEnderDragonPart(server, (EnderDragonPart) entity));
        return;
      }

      cir.setReturnValue(new CraftComplexPart(server, (EnderDragonPart) entity));
      return;
    }
    var convert = EntityClassLookup.getConvert(entity);
    cir.setReturnValue((CraftEntity) convert.apply(server, entity));
  }
}
