package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.event.player.PlayerTeleportEvent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = PlayerTeleportEvent.class, remap = false)
public abstract class PlayerTeleportEvent_PaperCompatMixin {
}
