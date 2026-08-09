package io.izzel.arclight.common.mixin.bukkit.adventure;

import org.bukkit.ChatColor;
import org.spongepowered.asm.mixin.Mixin;

// Mark ChatColor as deprecated in favor of Adventure API
@Deprecated
@Mixin(value = ChatColor.class, remap = false)
public class ChatColorAdventureMixin {
}
