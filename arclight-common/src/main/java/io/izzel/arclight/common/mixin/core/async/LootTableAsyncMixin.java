package io.izzel.arclight.common.mixin.core.async;

import io.izzel.arclight.common.mod.util.async.AsyncCatcher;
import net.minecraft.world.Container;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootTable.class)
public abstract class LootTableAsyncMixin {

  @Shadow
  public abstract void fill(Container container, LootParams params, long seed);

  @Inject(
      method =
          "fill(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/storage/loot/LootParams;J)V",
      at = @At("HEAD"),
      cancellable = true)
  private void arclight$guardLootGenerate(
      Container container, LootParams params, long seed, CallbackInfo ci) {
    boolean handled =
        AsyncCatcher.checkVoid(
            "loot generate",
            () -> {
              this.fill(container, params, seed);
              return Boolean.TRUE;
            });
    if (handled) ci.cancel();
  }
}
