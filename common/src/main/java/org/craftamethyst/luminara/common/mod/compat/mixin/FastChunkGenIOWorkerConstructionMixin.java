package org.craftamethyst.luminara.common.mod.compat.mixin;

import java.nio.file.Path;
import java.util.function.Consumer;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import org.craftamethyst.luminara.common.mod.compat.ModIds;
import org.craftamethyst.luminara.common.mod.mixins.annotation.LoadIfMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * FastChunkGen constructs its custom IOWorker with a null path because it replaces the
 * RegionFileStorage backend. Hyperbox's constructor hook assumes the path is always present and
 * dereferences it before deciding whether the storage belongs to Hyperbox.
 */
@Pseudo
@Mixin(targets = "commoble.hyperbox.MixinCallbacks", remap = false)
@LoadIfMod(modid = ModIds.FASTCHUNKGEN, condition = LoadIfMod.ModCondition.PRESENT)
public class FastChunkGenIOWorkerConstructionMixin {

  @Inject(method = "onIOWorkerConstruction", at = @At("HEAD"), cancellable = true, remap = false)
  private static void arclight$skipNullPath(
      Path path, boolean sync, Consumer<RegionFileStorage> storageConsumer, CallbackInfo ci) {
    if (path == null) {
      ci.cancel();
    }
  }
}
