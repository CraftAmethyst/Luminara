package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.Bukkit;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = Bukkit.class, remap = false)
public abstract class Bukkit_PaperCompatMixin {
}
