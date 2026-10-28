package org.craftamethyst.luminara.fabric.mixin.core.world.level.block;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.TNTPrimeEvent;
import org.craftamethyst.luminara.common.bridge.core.block.FireBlockBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.world.level.block.FireBlock.class)
public abstract class FireBlockMixin_Fabric implements FireBlockBridge {

  @Shadow @Final private Object2IntMap<net.minecraft.world.level.block.Block> burnOdds;

  @Inject(
      method = "checkBurnOut",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
              ordinal = 1))
  private void arclight$blockBurn(
      Level level,
      BlockPos pos,
      int chance,
      RandomSource random,
      int age,
      CallbackInfo ci) {
    BlockPos sourcePos = null;
    for (Direction direction : Direction.values()) {
      BlockPos candidate = pos.relative(direction);
      if (level.getBlockState(candidate).is(Blocks.FIRE)) {
        sourcePos = candidate;
        break;
      }
    }
    if (sourcePos == null) {
      return;
    }

    Block targetBlock = CraftBlock.at(level, pos);
    Block sourceBlock = CraftBlock.at(level, sourcePos);
    BlockBurnEvent event = new BlockBurnEvent(targetBlock, sourceBlock);
    Bukkit.getPluginManager().callEvent(event);
    if (event.isCancelled()) {
      ci.cancel();
      return;
    }
    if (level.getBlockState(pos).getBlock() instanceof net.minecraft.world.level.block.TntBlock
        && !CraftEventFactory.callTNTPrimeEvent(
            level, pos, TNTPrimeEvent.PrimeCause.FIRE, null, sourcePos)) {
      ci.cancel();
    }
  }

  @Override
  public boolean bridge$canBurn(net.minecraft.world.level.block.Block block) {
    return this.burnOdds.containsKey(block);
  }
}
