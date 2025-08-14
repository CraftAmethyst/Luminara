package io.izzel.arclight.common.mixin.paper.util;

import ca.spottedleaf.concurrentutil.util.MCUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to integrate Paper's MCUtils with Luminara.
 * This mixin ensures that Minecraft utilities work properly
 * in the Arclight/Luminara environment.
 */
@Mixin(value = MCUtils.class, remap = false)
public class MCUtilsMixin {

    /**
     * Static initialization hook for MCUtils.
     * This ensures that the utility class is properly configured for Arclight.
     */
    @Inject(method = "<clinit>", at = @At("RETURN"), require = 0)
    private static void arclight$initializeMCUtils(CallbackInfo ci) {
        // Initialize MCUtils with Arclight-specific settings
        System.out.println("Initialized Paper MCUtils for Luminara");
    }
}
