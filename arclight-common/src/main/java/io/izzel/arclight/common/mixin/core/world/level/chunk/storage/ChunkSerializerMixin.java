package io.izzel.arclight.common.mixin.core.world.level.chunk.storage;

import io.izzel.arclight.common.bridge.core.world.chunk.ChunkAccessBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import org.bukkit.craftbukkit.v.persistence.CraftPersistentDataContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ca.spottedleaf.dataconverter.minecraft.MCDataConverter;
import ca.spottedleaf.dataconverter.minecraft.MCVersions;
import ca.spottedleaf.dataconverter.minecraft.datatypes.MCDataType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mixin(ChunkSerializer.class)
public class ChunkSerializerMixin {

    private static final Logger LOGGER = LogManager.getLogger("Luminara");

    @Redirect(method = "read", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkAccess;setLightCorrect(Z)V"))
    private static void arclight$loadPersistent(ChunkAccess instance, boolean correct, ServerLevel level, PoiManager poiManager, ChunkPos pos, CompoundTag tag) {
        // Luminara - Apply data conversion to chunk data
        try {
            int dataVersion = tag.getInt("DataVersion");
            if (dataVersion > 0 && dataVersion < MCVersions.V1_20_1) {
                CompoundTag convertedTag = MCDataConverter.convertTag(MCDataType.CHUNK, tag, dataVersion, MCVersions.V1_20_1);
                if (convertedTag != null) {
                    tag = convertedTag;
                }
            }
        } catch (Exception e) {
            // Log error but continue with original data
            LOGGER.error("Failed to convert chunk data: {}", e.getMessage());
        }

        net.minecraft.nbt.Tag persistentBase = tag.get("ChunkBukkitValues");
        if (persistentBase instanceof CompoundTag) {
            ((CraftPersistentDataContainer) ((ChunkAccessBridge) instance).bridge$getPersistentDataContainer()).putAll((CompoundTag) persistentBase);
        }
        instance.setLightCorrect(correct);
    }


    @Inject(method = "write", at = @At("RETURN"))
    private static void arclight$savePersistent(ServerLevel level, ChunkAccess chunkAccess, CallbackInfoReturnable<CompoundTag> cir) {
        var container = (CraftPersistentDataContainer) ((ChunkAccessBridge) chunkAccess).bridge$getPersistentDataContainer();
        if (!container.isEmpty()) {
            cir.getReturnValue().put("ChunkBukkitValues", container.toTagCompound());
        }

        // Luminara - Mark chunk data with current version
        CompoundTag result = cir.getReturnValue();
        if (result != null) {
            result.putInt("DataVersion", MCVersions.V1_20_1);
        }
    }
}
