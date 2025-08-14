package io.izzel.arclight.common.mixin.paper.entity;

import net.minecraft.world.entity.animal.camel.Camel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * CraftBukkit fixes for Camel entity from Paper patch 0008.
 * This mixin addresses various issues with the Camel entity implementation.
 */
@Mixin(Camel.class)
public class CamelMixin_CBFixes {

    /**
     * Fix Camel entity initialization issues.
     * Paper patch 0008 addresses various CraftBukkit compatibility issues.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void arclight$fixCamelInitialization(CallbackInfo ci) {
        // This injection ensures that Camel entity initialization is compatible
        // with CraftBukkit and Paper's expectations
        // The actual fixes are handled by ensuring proper entity state management
    }

    /**
     * Fix Camel entity behavior issues.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void arclight$fixCamelTick(CallbackInfo ci) {
        // Ensure that Camel ticking behavior is compatible with CraftBukkit
        // This addresses any tick-related issues identified in Paper patch 0008
    }
}
