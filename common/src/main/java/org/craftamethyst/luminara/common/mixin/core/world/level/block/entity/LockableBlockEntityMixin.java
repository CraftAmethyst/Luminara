package org.craftamethyst.luminara.common.mixin.core.world.level.block.entity;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.craftamethyst.luminara.common.bridge.core.inventory.IInventoryBridge;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BaseContainerBlockEntity.class)
public abstract class LockableBlockEntityMixin extends BlockEntityMixin
    implements IInventoryBridge, Container {

  @Override
  public Location getLocation() {
    return CraftBlock.at(this.level, this.worldPosition).getLocation();
  }

  @Override
  public Recipe<?> getCurrentRecipe() {
    return null;
  }

  @Override
  public void setCurrentRecipe(Recipe<?> recipe) {}
}
