package io.izzel.arclight.common.mixin.core.async;

import io.izzel.arclight.common.mod.util.async.AsyncCatcher;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkMap.class)
public abstract class ChunkMapAsyncMixin {

    // @formatter:off
    @Shadow protected abstract void addEntity(Entity entity);
    @Shadow protected abstract void removeEntity(Entity entity);
    // @formatter:on

    @Inject(method = "addEntity(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void arclight$guardEntityTrack(Entity entity, CallbackInfo ci) {
        boolean handled = AsyncCatcher.checkVoid("entity track", () -> {
            this.addEntity(entity);
            return Boolean.TRUE;
        });
        if (handled) ci.cancel();
    }

    @Inject(method = "removeEntity(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void arclight$guardEntityUntrack(Entity entity, CallbackInfo ci) {
        boolean handled = AsyncCatcher.checkVoid("entity untrack", () -> {
            this.removeEntity(entity);
            return Boolean.TRUE;
        });
        if (handled) ci.cancel();
    }
}