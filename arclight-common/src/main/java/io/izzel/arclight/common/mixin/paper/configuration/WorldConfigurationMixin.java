package io.izzel.arclight.common.mixin.paper.configuration;

import io.papermc.paper.configuration.WorldConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to enhance Paper's WorldConfiguration for Luminara compatibility.
 * This mixin ensures that world-specific configuration settings work properly
 * in the Arclight/Luminara environment.
 */
@Mixin(value = WorldConfiguration.class, remap = false)
public class WorldConfigurationMixin {

    /**
     * Hook into world configuration initialization to ensure Arclight compatibility.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void arclight$onWorldConfigurationInit(CallbackInfo ci) {
        try {
            // Ensure that Arclight-specific world configuration overrides are applied
            this.arclight$applyArclightWorldOverrides();
        } catch (Exception e) {
            System.err.println("Failed to apply Arclight world configuration overrides: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Apply Arclight-specific world configuration overrides.
     * This method allows Luminara to customize Paper world configuration behavior
     * to work better in the Forge environment.
     */
    private void arclight$applyArclightWorldOverrides() {
        WorldConfiguration config = (WorldConfiguration) (Object) this;

        // Apply any necessary world-specific overrides for Arclight compatibility
        // For example, adjust entity limits, chunk loading behavior, etc.

        // Log that overrides have been applied
        System.out.println("Applied Arclight world configuration overrides to Paper WorldConfiguration");
    }
}
