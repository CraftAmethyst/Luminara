package io.izzel.arclight.common.mixin.bukkit.enumvalues;

import org.bukkit.entity.Villager.Profession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Profession.class, remap = false)
public class VillagerProfessionEnumMixin {

    @Mutable
    @Shadow
    private static Profession[] ENUM$VALUES;
}