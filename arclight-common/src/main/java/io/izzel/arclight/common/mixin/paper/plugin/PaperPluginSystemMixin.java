package io.izzel.arclight.common.mixin.paper.plugin;

import io.izzel.arclight.common.mod.server.PaperPluginIntegration;
import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.apache.logging.log4j.Logger;

/**
 * Optimized Mixin to integrate Paper plugin system into the server lifecycle.
 * This mixin provides more precise injection points for better integration.
 */
@Mixin(MinecraftServer.class)
public class PaperPluginSystemMixin {

    private static final Logger LOGGER = ArclightI18nLogger.getLogger("PaperPluginSystemMixin");

    /**
     * Initialize Paper plugin system at the optimal point during server startup.
     * This is called during level loading, which is the ideal time for plugin system initialization.
     */
    @Inject(method = "loadLevel", at = @At("HEAD"))
    private void arclight$initializePaperPluginSystem(CallbackInfo ci) {
        if (!PaperPluginIntegration.isInitialized()) {
            try {
                LOGGER.info("Initializing Paper plugin system during level loading...");
                PaperPluginIntegration.initialize();
            } catch (Exception e) {
                LOGGER.error("Failed to initialize Paper plugin system: " + e.getMessage());
                e.printStackTrace();
                // Don't fail server startup, but log the error
            }
        }
    }

    /**
     * Shutdown Paper plugin system when the server stops.
     * This ensures proper cleanup of all Paper plugin resources.
     */
    @Inject(method = "stopServer", at = @At("HEAD"))
    private void arclight$shutdownPaperPluginSystem(CallbackInfo ci) {
        try {
            LOGGER.info("Shutting down Paper plugin system during server shutdown...");
            PaperPluginIntegration.shutdown();
        } catch (Exception e) {
            LOGGER.error("Error during Paper plugin system shutdown: " + e.getMessage());
            e.printStackTrace();
            // Continue with shutdown even if there's an error
        }
    }
}
