package org.craftamethyst.luminara.common.bridge.bukkit;

import org.craftamethyst.luminara.i18n.conf.EntityPropertySpec;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Location;

public interface EntityTypeBridge {
  void bridge$setup(ResourceLocation location, EntityType<?> entityType, EntityPropertySpec spec);

  EntityType<?> bridge$getHandle();

  void bridge$setHandle(EntityType<?> entityType);

  EntityPropertySpec bridge$getSpec();

  Function<Location, ? extends Entity> bridge$entityFactory();

  void bridge$setEntityFactory(Function<Location, ? extends Entity> function);
}
