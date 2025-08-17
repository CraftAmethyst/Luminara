package io.izzel.arclight.common.mixin.paper.server;

import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.v.CraftServer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Paper API patch 0020: Expose server CommandMap
 * Implements getCommandMap() method for CraftServer to expose the active command map
 */
@Mixin(value = CraftServer.class, remap = false)
public class CraftServerMixin_CommandMap {

    @Shadow
    @Final
    private org.bukkit.craftbukkit.v.command.CraftCommandMap commandMap;

    /**
     * Gets the active command map
     *
     * @return the active command map
     */
    @NotNull
    public CommandMap getCommandMap() {
        return commandMap;
    }
}
