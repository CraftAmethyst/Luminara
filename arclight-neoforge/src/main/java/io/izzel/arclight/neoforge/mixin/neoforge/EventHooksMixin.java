package io.izzel.arclight.neoforge.mixin.neoforge;

import io.izzel.arclight.common.mod.util.ArclightCaptures;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EventHooks.class)
public abstract class EventHooksMixin {
  @Inject(method = "onBlockPlace", remap = false, at = @At("HEAD"))
  private static void arclight$captureDirection(
      Entity entity,
      BlockSnapshot blockSnapshot,
      Direction direction,
      CallbackInfoReturnable<Boolean> cir) {
    ArclightCaptures.capturePlaceEventDirection(direction);
  }

  @Inject(method = "onMultiBlockPlace", remap = false, at = @At("HEAD"))
  private static void arclight$captureDirection(
      Entity entity,
      List<BlockSnapshot> blockSnapshots,
      Direction direction,
      CallbackInfoReturnable<Boolean> cir) {
    ArclightCaptures.capturePlaceEventDirection(direction);
  }
}
