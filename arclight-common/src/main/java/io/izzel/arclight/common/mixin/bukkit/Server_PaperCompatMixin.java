package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.Server;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = Server.class, remap = false)
public interface Server_PaperCompatMixin {
    String getMinecraftVersion();
}
