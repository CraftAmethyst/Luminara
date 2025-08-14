package io.izzel.arclight.common.mixin.paper.server;

import io.izzel.arclight.common.mod.server.PaperIntegration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to integrate Paper features into Arclight's server lifecycle.
 * This mixin ensures that Paper integration is properly initialized and cleaned up.
 */
@Mixin(MinecraftServer.class)
public class ArclightPaperIntegrationMixin {

    /**
     * Initialize Paper integration when the server starts.
     */
    @Inject(method = "runServer", at = @At("HEAD"))
    private void arclight$initializePaperIntegration(CallbackInfo ci) {
        try {
            PaperIntegration.initialize();
        } catch (Exception e) {
            System.err.println("Failed to initialize Paper integration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Complete Paper integration setup after server has started.
     */
    @Inject(method = "runServer", at = @At("RETURN"))
    private void arclight$completePaperIntegrationSetup(CallbackInfo ci) {
        try {
            MinecraftServer server = (MinecraftServer) (Object) this;
            PaperIntegration.onServerStarted(server);
        } catch (Exception e) {
            System.err.println("Failed to complete Paper integration setup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle Paper integration cleanup when server stops.
     */
    @Inject(method = "stopServer", at = @At("HEAD"))
    private void arclight$shutdownPaperIntegration(CallbackInfo ci) {
        try {
            PaperIntegration.onServerShutdown();
        } catch (Exception e) {
            System.err.println("Failed to shutdown Paper integration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle world creation for Paper integration.
     */
    @Inject(method = "createLevels", at = @At("RETURN"))
    private void arclight$onWorldsCreated(CallbackInfo ci) {
        try {
            MinecraftServer server = (MinecraftServer) (Object) this;
            for (ServerLevel level : server.getAllLevels()) {
                PaperIntegration.onWorldCreated(level);
            }
        } catch (Exception e) {
            System.err.println("Failed to handle world creation in Paper integration: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
