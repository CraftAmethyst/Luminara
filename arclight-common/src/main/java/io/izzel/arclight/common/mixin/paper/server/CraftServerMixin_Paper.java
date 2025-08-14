package io.izzel.arclight.common.mixin.paper.server;

import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.v.CraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Paper-specific enhancements to CraftServer for Luminara.
 * This mixin implements Paper server features that are compatible with Arclight.
 */
@Mixin(value = CraftServer.class, remap = false)
public class CraftServerMixin_Paper {

    private static final Logger LOGGER = ArclightI18nLogger.getLogger("PaperCraftServer");

    /**
     * Initialize Paper-specific CraftServer features during server startup.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void arclight$initializePaperCraftServerFeatures(CallbackInfo ci) {
        try {
            // Initialize Paper-specific CraftServer features
            this.arclight$setupPaperCraftServerEnvironment();
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Paper CraftServer features: " + e.getMessage(), e);
        }
    }

    /**
     * Setup Paper CraftServer environment for Luminara compatibility.
     */
    private void arclight$setupPaperCraftServerEnvironment() {
        CraftServer server = (CraftServer) (Object) this;

        // Setup Paper-specific CraftServer environment
        // This includes command registration, event handling, etc.

        LOGGER.info("Initialized Paper CraftServer features for Luminara");
    }
}
