package io.izzel.arclight.common.mixin.bukkit.enumvalues;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = org.bukkit.potion.PotionType.class, remap = false)
public class PotionTypeEnumMixin {

    @Mutable
    @Shadow
    private static org.bukkit.potion.PotionType[] ENUM$VALUES;
}