package io.izzel.arclight.common.mixin.bukkit.enumvalues;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = org.bukkit.inventory.recipe.CookingBookCategory.class, remap = false)
public class CookingBookCategoryEnumMixin {

    @Mutable
    @Shadow
    private static org.bukkit.inventory.recipe.CookingBookCategory[] ENUM$VALUES;
}