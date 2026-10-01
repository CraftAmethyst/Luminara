package io.izzel.arclight.common.mixin.bukkit.enumvalues;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = org.bukkit.Fluid.class, remap = false)
public class FluidEnumMixin {

    @Mutable
    @Shadow
    private static org.bukkit.Fluid[] ENUM$VALUES;
}