package io.izzel.arclight.common.mod.compat.mixin;

import io.izzel.arclight.common.mod.compat.ModIds;
import io.izzel.arclight.common.mod.mixins.annotation.LoadIfMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
    targets = "org.embeddedt.modernfix.forge.load.ModWorkManagerQueue",
    remap = false
)
@LoadIfMod(modid = ModIds.MODERNFIX, condition = LoadIfMod.ModCondition.PRESENT)
public abstract class ModWorkManagerQueueMixin {

    @Inject(method = "replace", at = @At("HEAD"), cancellable = true)
    private static void arclight$disableQueueReplacement(CallbackInfo ci) {
        ci.cancel();
    }
}
