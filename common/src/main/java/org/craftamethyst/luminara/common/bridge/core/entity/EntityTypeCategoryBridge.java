package org.craftamethyst.luminara.common.bridge.core.entity;

import net.minecraft.world.entity.MobCategory;

public interface EntityTypeCategoryBridge {

  default MobCategory getCategory() {
    return ((EntityTypeBridge<?>) (Object) this).bridge$getMobCategory();
  }
}
