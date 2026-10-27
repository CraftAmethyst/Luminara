package org.craftamethyst.luminara.common.bridge.core.entity.passive;

import net.minecraft.world.item.ItemStack;
import org.craftamethyst.luminara.common.bridge.core.entity.MobEntityBridge;

public interface AnimalEntityBridge extends MobEntityBridge {
  ItemStack bridge$getBreedItem();
}
