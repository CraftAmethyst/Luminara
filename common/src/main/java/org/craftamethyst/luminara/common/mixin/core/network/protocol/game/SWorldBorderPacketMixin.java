package org.craftamethyst.luminara.common.mixin.core.network.protocol.game;

import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.world.level.border.WorldBorder;
import org.craftamethyst.luminara.common.bridge.core.world.border.WorldBorderBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundSetBorderCenterPacket.class)
public class SWorldBorderPacketMixin {

  // @formatter:off
  @Shadow @Final @Mutable private double newCenterX;

  @Shadow @Final @Mutable private double newCenterZ;

  // @formatter:on

  @Inject(method = "<init>(Lnet/minecraft/world/level/border/WorldBorder;)V", at = @At("RETURN"))
  private void arclight$nether(WorldBorder border, CallbackInfo ci) {
    var level = ((WorldBorderBridge) border).bridge$getWorld();
    this.newCenterX =
        border.getCenterX() * (level != null ? level.dimensionType().coordinateScale() : 1.0);
    this.newCenterZ =
        border.getCenterZ() * (level != null ? level.dimensionType().coordinateScale() : 1.0);
  }
}
