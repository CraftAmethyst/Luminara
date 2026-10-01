package io.izzel.arclight.common.mixin.bukkit.enumvalues;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = org.bukkit.entity.Pose.class, remap = false)
public class PoseEnumMixin {

    @Mutable
    @Shadow
    private static org.bukkit.entity.Pose[] ENUM$VALUES;
}