package org.craftamethyst.luminara.common.mixin.core.world;

import org.craftamethyst.luminara.common.bridge.core.world.IWorldWriterBridge;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelWriter;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelWriter.class)
public interface IWorldWriterMixin extends IWorldWriterBridge {
  default boolean addFreshEntity(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
    return bridge$addEntity(entity, reason);
  }
}
