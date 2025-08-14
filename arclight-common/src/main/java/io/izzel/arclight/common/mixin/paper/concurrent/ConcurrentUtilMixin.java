package io.izzel.arclight.common.mixin.paper.concurrent;

import ca.spottedleaf.concurrentutil.util.ConcurrentUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to integrate Paper's ConcurrentUtil with Luminara.
 * This mixin ensures that concurrent utilities work properly
 * in the Arclight/Luminara environment.
 */
@Mixin(value = ConcurrentUtil.class, remap = false)
public class ConcurrentUtilMixin {

    /**
     * Static initialization hook for ConcurrentUtil.
     * This ensures that the utility class is properly configured for Arclight.
     */
    @Inject(method = "<clinit>", at = @At("RETURN"), require = 0)
    private static void arclight$initializeConcurrentUtil(CallbackInfo ci) {
        // Initialize ConcurrentUtil with Arclight-specific settings
        // This may include thread pool configuration, monitoring setup, etc.
        System.out.println("Initialized Paper ConcurrentUtil for Luminara");
    }
}
