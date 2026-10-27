package org.craftamethyst.luminara.common.bridge.core.entity.passive;

import org.craftamethyst.luminara.common.bridge.core.entity.MobEntityBridge;
import net.minecraft.world.item.ItemStack;

public interface AnimalEntityBridge extends MobEntityBridge {
  ItemStack bridge$getBreedItem();
}
