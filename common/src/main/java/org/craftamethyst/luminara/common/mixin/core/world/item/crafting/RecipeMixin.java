package org.craftamethyst.luminara.common.mixin.core.world.item.crafting;

import org.bukkit.inventory.Recipe;
import org.craftamethyst.luminara.common.bridge.core.item.crafting.IRecipeBridge;
import org.craftamethyst.luminara.common.mod.util.ArclightSpecialRecipe;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(net.minecraft.world.item.crafting.Recipe.class)
public interface RecipeMixin extends IRecipeBridge {
  default Recipe toBukkitRecipe() {
    return bridge$toBukkitRecipe();
  }

  @Override
  default Recipe bridge$toBukkitRecipe() {
    return new ArclightSpecialRecipe((net.minecraft.world.item.crafting.Recipe<?>) this);
  }
}
