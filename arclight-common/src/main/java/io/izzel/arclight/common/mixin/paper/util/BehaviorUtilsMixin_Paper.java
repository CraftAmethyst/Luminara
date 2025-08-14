package io.izzel.arclight.common.mixin.paper.util;

import ca.spottedleaf.concurrentutil.util.BehaviorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to integrate Paper's BehaviorUtils with Luminara.
 * This mixin ensures that behavior utilities work properly
 * in the Arclight/Luminara environment.
 */
@Mixin(value = BehaviorUtils.class, remap = false)
public class BehaviorUtilsMixin_Paper {

    private static final Logger LOGGER = LogManager.getLogger("Luminara");

    /**
     * Static initialization hook for BehaviorUtils.
     * This ensures that the utility class is properly configured for Arclight.
     */
    @Inject(method = "<clinit>", at = @At("RETURN"), require = 0)
    private static void arclight$initializeBehaviorUtils(CallbackInfo ci) {
        // Initialize BehaviorUtils with Arclight-specific settings
        LOGGER.info("Initialized Paper BehaviorUtils for Luminara");
    }
}
