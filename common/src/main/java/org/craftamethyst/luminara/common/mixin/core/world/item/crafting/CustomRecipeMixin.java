package org.craftamethyst.luminara.common.mixin.core.world.item.crafting;

import net.minecraft.world.item.crafting.CustomRecipe;
import org.bukkit.inventory.Recipe;
import org.craftamethyst.luminara.common.bridge.core.item.crafting.IRecipeBridge;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CustomRecipe.class)
public class CustomRecipeMixin implements IRecipeBridge {

  @Override
  public Recipe bridge$toBukkitRecipe() {
    return new org.bukkit.craftbukkit.v.inventory.CraftComplexRecipe((CustomRecipe) (Object) this);
  }
}
