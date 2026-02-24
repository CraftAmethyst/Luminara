package io.izzel.arclight.common.mixin.bukkit.adventure;

import net.kyori.adventure.title.Title;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = Title.class, remap = false)
public interface TitleAdventureCompatMixin {
}
