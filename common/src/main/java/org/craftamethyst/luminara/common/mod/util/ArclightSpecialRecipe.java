package org.craftamethyst.luminara.common.mod.util;

import org.craftamethyst.luminara.common.bridge.core.item.crafting.RecipeManagerBridge;
import org.craftamethyst.luminara.common.mod.server.ArclightServer;
import net.minecraft.world.item.crafting.Recipe;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v.inventory.CraftComplexRecipe;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v.util.CraftNamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ArclightSpecialRecipe extends CraftComplexRecipe {

  private final Recipe<?> recipe;

  public ArclightSpecialRecipe(Recipe<?> recipe) {
    super(null);
    this.recipe = recipe;
  }

  @Override
  public @NotNull ItemStack getResult() {
    return CraftItemStack.asCraftMirror(
        this.recipe.getResultItem(ArclightServer.getMinecraftServer().registryAccess()));
  }

  @Override
  public @NotNull NamespacedKey getKey() {
    return CraftNamespacedKey.fromMinecraft(this.recipe.getId());
  }

  @Override
  public void addToCraftingManager() {
    ((RecipeManagerBridge) ArclightServer.getMinecraftServer().getRecipeManager())
        .bridge$addRecipe(this.recipe);
  }
}
