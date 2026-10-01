package io.izzel.arclight.common.mixin.bukkit.enumvalues;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = org.bukkit.Material.class, remap = false)
public class MaterialEnumMixin {

    @Mutable
    @Shadow
    private static org.bukkit.Material[] ENUM$VALUES;
}