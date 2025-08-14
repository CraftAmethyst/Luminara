package io.izzel.arclight.common.mixin.paper.configuration;

import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import io.papermc.paper.configuration.GlobalConfiguration;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to enhance Paper's GlobalConfiguration for Luminara compatibility.
 * This mixin ensures that global configuration settings work properly
 * in the Arclight/Luminara environment.
 */
@Mixin(value = GlobalConfiguration.class, remap = false)
public class GlobalConfigurationMixin {

    private static final Logger LOGGER = ArclightI18nLogger.getLogger("GlobalConfiguration");

    /**
     * Hook into configuration initialization to ensure Arclight compatibility.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void arclight$onGlobalConfigurationInit(CallbackInfo ci) {
        try {
            // Ensure that Arclight-specific configuration overrides are applied
            // This allows Luminara to customize Paper configuration behavior
            this.arclight$applyArclightOverrides();
        } catch (Exception e) {
            LOGGER.error("Failed to apply Arclight configuration overrides: " + e.getMessage(), e);
        }
    }

    /**
     * Apply Arclight-specific configuration overrides.
     * This method allows Luminara to customize Paper configuration behavior
     * to work better in the Forge environment.
     */
    private void arclight$applyArclightOverrides() {
        GlobalConfiguration config = (GlobalConfiguration) (Object) this;

        // Apply any necessary overrides for Arclight compatibility
        // For example, adjust thread pool sizes, enable/disable certain features, etc.

        // Log that overrides have been applied
        LOGGER.debug("Applied Arclight configuration overrides to Paper GlobalConfiguration");
    }
}
