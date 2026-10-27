package org.craftamethyst.luminara.common.mixin.core.async;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.craftamethyst.luminara.common.mod.util.async.AsyncCatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourAsyncMixin {

  // @formatter:off
  @Shadow
  protected abstract void onPlace(
      BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston);

  @Shadow
  protected abstract void onRemove(
      BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston);

  // @formatter:on

  @Inject(
      method =
          "onPlace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V",
      at = @At("HEAD"),
      cancellable = true)
  private void arclight$guardBlockPlace(
      BlockState state,
      Level level,
      BlockPos pos,
      BlockState oldState,
      boolean movedByPiston,
      CallbackInfo ci) {
    boolean handled =
        AsyncCatcher.checkVoid(
            "block place",
            () -> {
              this.onPlace(state, level, pos, oldState, movedByPiston);
              return Boolean.TRUE;
            });
    if (handled) ci.cancel();
  }

  @Inject(
      method =
          "onRemove(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V",
      at = @At("HEAD"),
      cancellable = true)
  private void arclight$guardBlockRemove(
      BlockState state,
      Level level,
      BlockPos pos,
      BlockState newState,
      boolean movedByPiston,
      CallbackInfo ci) {
    boolean handled =
        AsyncCatcher.checkVoid(
            "block remove",
            () -> {
              this.onRemove(state, level, pos, newState, movedByPiston);
              return Boolean.TRUE;
            });
    if (handled) ci.cancel();
  }
}
