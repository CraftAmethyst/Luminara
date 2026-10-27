package org.craftamethyst.luminara.common.mixin.core.world.level.block.state;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.craftamethyst.luminara.common.mod.util.ArclightCaptures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class BlockBehaviour_BlockStateBaseMixin {

  @Inject(method = "entityInside", at = @At("HEAD"))
  private void arclight$captureBlockCollide(
      Level worldIn, BlockPos pos, Entity entityIn, CallbackInfo ci) {
    ArclightCaptures.captureDamageEventBlock(pos);
  }

  @Inject(method = "entityInside", at = @At("RETURN"))
  private void arclight$resetBlockCollide(
      Level worldIn, BlockPos pos, Entity entityIn, CallbackInfo ci) {
    ArclightCaptures.captureDamageEventBlock(null);
  }
}
