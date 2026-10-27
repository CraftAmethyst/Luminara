package org.craftamethyst.luminara.common.mixin.core.world.spawner;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.craftamethyst.luminara.common.bridge.core.world.IWorldWriterBridge;
import org.craftamethyst.luminara.common.mod.server.event.PreCreatureSpawnEventDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SpawnUtil.class)
public class SpawnUtilMixin {

  @Redirect(
      method = "trySpawnMob",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/nbt/CompoundTag;Ljava/util/function/Consumer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;ZZ)Lnet/minecraft/world/entity/Entity;"))
  private static <T extends Mob> T arclight$preCreatureSpawn(
      EntityType<T> entityType,
      ServerLevel level,
      CompoundTag tag,
      Consumer<T> consumer,
      BlockPos pos,
      MobSpawnType spawnType,
      boolean shouldOffsetY,
      boolean shouldOffsetYMore) {
    CreatureSpawnEvent.SpawnReason reason =
        ((IWorldWriterBridge) level).bridge$getAddEntityReason();
    if (reason == null) {
      reason = CreatureSpawnEvent.SpawnReason.DEFAULT;
    }
    var event = PreCreatureSpawnEventDispatcher.call(level, entityType, pos, reason);
    if (event != null && event.isCancelled()) {
      return null;
    }
    return entityType.create(
        level, tag, consumer, pos, spawnType, shouldOffsetY, shouldOffsetYMore);
  }
}
