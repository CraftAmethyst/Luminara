package io.izzel.arclight.common.mixin.core.async;

import io.izzel.arclight.common.mod.util.async.AsyncCatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.level.ServerLevel$EntityCallbacks")
public abstract class ServerLevelEntityCallbacksAsyncMixin {

    // @formatter:off
    @Shadow public abstract void onTrackingStart(Entity entity);
    @Shadow public abstract void onTrackingEnd(Entity entity);
    // @formatter:on

    @Inject(method = "onTrackingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void arclight$guardEntityRegister(
        Entity entity,
        CallbackInfo ci
    ) {
        boolean handled = AsyncCatcher.checkVoid("entity register", () -> {
            this.onTrackingStart(entity);
            return Boolean.TRUE;
        });
        if (handled) ci.cancel();
    }

    @Inject(method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void arclight$guardEntityUnregister(
        Entity entity,
        CallbackInfo ci
    ) {
        boolean handled = AsyncCatcher.checkVoid("entity unregister", () -> {
            this.onTrackingEnd(entity);
            return Boolean.TRUE;
        });
        if (handled) ci.cancel();
    }
}