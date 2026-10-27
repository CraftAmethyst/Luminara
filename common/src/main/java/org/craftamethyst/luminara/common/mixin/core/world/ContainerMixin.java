package org.craftamethyst.luminara.common.mixin.core.world;

import org.craftamethyst.luminara.common.bridge.core.inventory.IInventoryBridge;
import org.craftamethyst.luminara.common.mod.inventory.SideViewingTracker;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Container.class)
public interface ContainerMixin extends IInventoryBridge {
  @Override
  default void onOpen(CraftHumanEntity who) {
    SideViewingTracker.onOpen((Container) this, who);
  }

  @Override
  default void onClose(CraftHumanEntity who) {
    SideViewingTracker.onClose((Container) this, who);
  }

  @Override
  default List<HumanEntity> getViewers() {
    return SideViewingTracker.getViewers((Container) this);
  }

  @Override
  default InventoryHolder getOwner() {
    return null;
  }

  @Override
  default void setMaxStackSize(int size) {}

  @Override
  default Location getLocation() {
    return null;
  }

  @Override
  default Recipe<?> getCurrentRecipe() {
    return null;
  }

  @Override
  default void setCurrentRecipe(Recipe<?> recipe) {}
}
