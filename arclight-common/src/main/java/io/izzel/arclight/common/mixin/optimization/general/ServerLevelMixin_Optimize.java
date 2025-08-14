package io.izzel.arclight.common.mixin.optimization.general;

import io.izzel.arclight.common.optimization.mpem.EntityOptimizer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin_Optimize {


    // Luminara - Chunk optimization methods removed to avoid conflicts with Paper patches
    // Paper handles chunk loading optimization and chunk access tracking internally

    @Inject(method = "tickNonPassenger", at = @At("HEAD"), cancellable = true)
    private void luminara$optimizeEntityTick(Entity entity, CallbackInfo ci) {
        // Additional entity tick optimization at the level processing stage
        if (EntityOptimizer.shouldOptimizeEntityTick(entity)) {
            // Skip processing for very distant entities
            if (entity.tickCount % 4 != 0) {
                ci.cancel();
            }
        }
    }
}
