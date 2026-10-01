package io.izzel.arclight.common.mixin.bukkit.enumvalues;

import org.bukkit.entity.EnderDragon.Phase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Phase.class, remap = false)
public class EnderDragonPhaseEnumMixin {

    @Mutable
    @Shadow
    private static Phase[] ENUM$VALUES;
}