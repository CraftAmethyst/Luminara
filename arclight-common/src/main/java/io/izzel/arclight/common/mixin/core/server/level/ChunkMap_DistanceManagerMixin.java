package io.izzel.arclight.common.mixin.core.server.level;

import io.izzel.arclight.common.bridge.core.server.level.DistanceManagerBridge;
import net.minecraft.server.level.ChunkHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.server.level.ChunkMap$DistanceManager")
public class ChunkMap_DistanceManagerMixin {

    @Inject(method = "updateChunkScheduling", at = @At("RETURN"))
    private void arclight$queueChunkUpdate(long chunkPos, int newLevel, ChunkHolder holder, int oldLevel,
                                           CallbackInfoReturnable<ChunkHolder> cir) {
        ChunkHolder updatedHolder = cir.getReturnValue();
        if (updatedHolder != null) {
            ((DistanceManagerBridge) this).arclight$offerUpdate(updatedHolder);
        }
    }
}
