package io.izzel.arclight.common.mixin.bukkit.enumvalues;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = org.bukkit.Art.class, remap = false)
public class ArtEnumMixin {

    @Mutable
    @Shadow
    private static org.bukkit.Art[] ENUM$VALUES;
}