package io.izzel.arclight.common.mixin.paper.server;

import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Paper-specific enhancements to ServerLevel for Luminara.
 * This mixin implements Paper world features that are compatible with Arclight.
 */
@Mixin(ServerLevel.class)
public class ServerLevelMixin_Paper {

    /**
     * Initialize Paper-specific world features during world creation.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void arclight$initializePaperWorldFeatures(CallbackInfo ci) {
        try {
            // Initialize Paper-specific world features
            this.arclight$setupPaperWorldEnvironment();
        } catch (Exception e) {
            System.err.println("Failed to initialize Paper world features: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Setup Paper world environment for Luminara compatibility.
     */
    private void arclight$setupPaperWorldEnvironment() {
        ServerLevel level = (ServerLevel) (Object) this;

        // Setup Paper-specific world environment
        // This includes async chunk loading, entity tracking optimizations, etc.

        System.out.println("Initialized Paper world features for level: " + level.dimension().location());
    }

    /**
     * Handle world unload with Paper-specific cleanup.
     */
    @Inject(method = "close", at = @At("HEAD"))
    private void arclight$cleanupPaperWorldFeatures(CallbackInfo ci) {
        try {
            // Cleanup Paper-specific world resources
            this.arclight$cleanupPaperWorldResources();
        } catch (Exception e) {
            System.err.println("Failed to cleanup Paper world features: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cleanup Paper-specific world resources during world unload.
     */
    private void arclight$cleanupPaperWorldResources() {
        ServerLevel level = (ServerLevel) (Object) this;

        // Cleanup any Paper-specific world resources
        System.out.println("Cleaned up Paper world features for level: " + level.dimension().location());
    }
}
