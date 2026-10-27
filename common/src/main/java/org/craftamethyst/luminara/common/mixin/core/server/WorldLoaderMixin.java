package org.craftamethyst.luminara.common.mixin.core.server;

import net.minecraft.server.WorldLoader;
import org.craftamethyst.luminara.common.mod.util.ArclightCaptures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(WorldLoader.class)
public class WorldLoaderMixin {

  @ModifyArg(
      method = "load",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/server/WorldLoader$WorldDataSupplier;get(Lnet/minecraft/server/WorldLoader$DataLoadContext;)Lnet/minecraft/server/WorldLoader$DataLoadOutput;"))
  private static WorldLoader.DataLoadContext arclight$captureContext(
      WorldLoader.DataLoadContext context) {
    ArclightCaptures.captureDataLoadContext(context);
    return context;
  }
}
