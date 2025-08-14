package io.izzel.arclight.common.mixin.paper.server;

import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import io.papermc.paper.command.PaperCommand;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.v.CraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to automatically register Paper commands in CraftServer.
 * This provides seamless Paper command integration without manual registration.
 */
@Mixin(value = CraftServer.class, remap = false)
public class CraftServerMixin_PaperCommands {

    private static final Logger LOGGER = ArclightI18nLogger.getLogger("PaperCommands");

    /**
     * Automatically register Paper commands during CraftServer initialization.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void arclight$registerPaperCommands(CallbackInfo ci) {
        try {
            CraftServer server = (CraftServer) (Object) this;

            // Register Paper commands
            server.getCommandMap().register("paper", new PaperCommand("paper"));

            // Skip CallbackCommand registration due to protected constructor
            // The callback command functionality can be handled elsewhere if needed

            LOGGER.info("Registered Paper commands: /paper");
        } catch (Exception e) {
            LOGGER.error("Failed to register Paper commands: " + e.getMessage(), e);
        }
    }
}
