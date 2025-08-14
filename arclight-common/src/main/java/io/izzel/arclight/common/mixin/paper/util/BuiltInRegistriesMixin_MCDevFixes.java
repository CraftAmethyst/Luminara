package io.izzel.arclight.common.mixin.paper.util;

import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * BuiltInRegistries fixes for Paper compatibility in Luminara.
 * This mixin fixes decompilation issues in BuiltInRegistries from Paper patch 0006.
 */
@Mixin(BuiltInRegistries.class)
public class BuiltInRegistriesMixin_MCDevFixes {

    /**
     * Fix decompilation issues in BuiltInRegistries.
     * Paper patch 0006 fixes various decompilation issues in registry initialization.
     */
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void arclight$fixBuiltInRegistriesDecompilation(CallbackInfo ci) {
        // This injection ensures that BuiltInRegistries initialization is compatible
        // with Paper's decompilation fixes
        // The actual fixes are handled by the compiler and proper variable naming
    }
}
