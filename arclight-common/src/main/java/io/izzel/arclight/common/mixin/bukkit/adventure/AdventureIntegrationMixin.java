package io.izzel.arclight.common.mixin.bukkit.adventure;

import io.izzel.arclight.common.adventure.AdventureInitializer;
import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to integrate Adventure initialization into the server lifecycle.
 */
@Mixin(MinecraftServer.class)
public class AdventureIntegrationMixin {

    private static final Logger LOGGER = ArclightI18nLogger.getLogger("AdventureIntegration");

    /**
     * Initialize Adventure integration when the server starts.
     */
    @Inject(method = "runServer", at = @At("HEAD"))
    private void arclight$initializeAdventure(CallbackInfo ci) {
        try {
            AdventureInitializer.initialize();
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Adventure integration: " + e.getMessage(), e);
        }
    }

    /**
     * Shutdown Adventure integration when the server stops.
     */
    @Inject(method = "stopServer", at = @At("HEAD"))
    private void arclight$shutdownAdventure(CallbackInfo ci) {
        try {
            AdventureInitializer.shutdown();
        } catch (Exception e) {
            LOGGER.error("Failed to shutdown Adventure integration: " + e.getMessage(), e);
        }
    }
}
