package io.izzel.arclight.common.mixin.core.world.level.levelgen.structure.structures;

import io.izzel.arclight.common.bridge.core.world.level.levelgen.structure.StructurePieceBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressPieces;
import org.bukkit.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NetherFortressPieces.MonsterThrone.class)
public class NetherFortressPiecesPieceMixin {

  @Redirect(
      method = "postProcess",
      at =
          @At(
              value = "INVOKE",
              ordinal = 0,
              target =
                  "Lnet/minecraft/world/level/WorldGenLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
  private boolean arclight$skipSetBlock(
      WorldGenLevel instance, BlockPos blockPos, BlockState blockState, int i) {
    return ((StructurePieceBridge) this)
        .bridge$placeCraftSpawner(instance, blockPos, EntityType.BLAZE, 2);
  }

  @Redirect(
      method = "postProcess",
      at =
          @At(
              value = "INVOKE",
              ordinal = 0,
              target =
                  "Lnet/minecraft/world/level/WorldGenLevel;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
  private BlockEntity arclight$getBlockEntity(WorldGenLevel instance, BlockPos blockPos) {
    return null;
  }
}
