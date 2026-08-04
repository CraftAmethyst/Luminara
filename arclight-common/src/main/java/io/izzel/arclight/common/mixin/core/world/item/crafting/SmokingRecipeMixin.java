package io.izzel.arclight.common.mixin.core.world.item.crafting;

import io.izzel.arclight.common.bridge.core.item.crafting.IRecipeBridge;
import io.izzel.arclight.common.mod.util.ArclightSpecialRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v.inventory.CraftRecipe;
import org.bukkit.craftbukkit.v.inventory.CraftSmokingRecipe;
import org.bukkit.craftbukkit.v.util.CraftNamespacedKey;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SmokingRecipe.class)
public abstract class SmokingRecipeMixin
    extends AbstractCookingRecipe
    implements IRecipeBridge
{

    public SmokingRecipeMixin(
        RecipeType<?> p_250197_,
        ResourceLocation p_249379_,
        String p_249518_,
        CookingBookCategory p_250891_,
        Ingredient p_251354_,
        ItemStack p_252185_,
        float p_252165_,
        int p_250256_
    ) {
        super(
            p_250197_,
            p_249379_,
            p_249518_,
            p_250891_,
            p_251354_,
            p_252185_,
            p_252165_,
            p_250256_
        );
    }

    @Override
    public Recipe bridge$toBukkitRecipe() {
        if (this.result.isEmpty()) {
            return new ArclightSpecialRecipe(this);
        }
        CraftItemStack result = CraftItemStack.asCraftMirror(this.result);
        CraftSmokingRecipe recipe = new CraftSmokingRecipe(
            CraftNamespacedKey.fromMinecraft(this.getId()),
            result,
            CraftRecipe.toBukkit(this.ingredient),
            this.experience,
            this.cookingTime
        );
        recipe.setGroup(this.group == null ? "" : this.group);
        recipe.setCategory(CraftRecipe.getCategory(this.category()));
        return recipe;
    }
}
