package io.izzel.arclight.common.mixin.core.async;

import io.izzel.arclight.common.mod.util.async.AsyncCatcher;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkMap.TrackedEntity.class)
public abstract class ChunkMapTrackedEntityAsyncMixin {

  // @formatter:off
  @Shadow
  public abstract void removePlayer(ServerPlayer player);

  @Shadow
  public abstract void updatePlayer(ServerPlayer player);

  // @formatter:on

  @Inject(
      method = "removePlayer(Lnet/minecraft/server/level/ServerPlayer;)V",
      at = @At("HEAD"),
      cancellable = true)
  private void arclight$guardPlayerTrackerClear(ServerPlayer player, CallbackInfo ci) {
    boolean handled =
        AsyncCatcher.checkVoid(
            "player tracker clear",
            () -> {
              this.removePlayer(player);
              return Boolean.TRUE;
            });
    if (handled) ci.cancel();
  }

  @Inject(
      method = "updatePlayer(Lnet/minecraft/server/level/ServerPlayer;)V",
      at = @At("HEAD"),
      cancellable = true)
  private void arclight$guardPlayerTrackerUpdate(ServerPlayer player, CallbackInfo ci) {
    boolean handled =
        AsyncCatcher.checkVoid(
            "player tracker update",
            () -> {
              this.updatePlayer(player);
              return Boolean.TRUE;
            });
    if (handled) ci.cancel();
  }
}
