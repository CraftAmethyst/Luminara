package io.izzel.arclight.common.mixin.core.world.storage;

import io.izzel.arclight.common.bridge.core.entity.player.ServerPlayerEntityBridge;
import io.izzel.arclight.common.bridge.core.world.storage.PlayerDataBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import ca.spottedleaf.dataconverter.minecraft.MCDataConverter;
import ca.spottedleaf.dataconverter.minecraft.MCVersions;
import ca.spottedleaf.dataconverter.minecraft.datatypes.MCDataType;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.FileInputStream;

@Mixin(PlayerDataStorage.class)
public class PlayerDataMixin implements PlayerDataBridge {
    private static final org.apache.logging.log4j.Logger ARCLIGHT_LOGGER = io.izzel.arclight.common.mod.util.log.ArclightI18nLogger.getLogger("PlayerData");


    @Shadow
    @Final
    private static Logger LOGGER;
    // @formatter:off
    @Shadow @Final private File playerDir;
    // @formatter:on

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtUtils;getDataVersion(Lnet/minecraft/nbt/CompoundTag;I)I"))
    private void arclight$lastSeenTime(Player player, CallbackInfoReturnable<CompoundTag> cir) {
        if (player instanceof ServerPlayer) {
            CraftPlayer craftPlayer = ((ServerPlayerEntityBridge) player).bridge$getBukkitEntity();
            // Only update first played if it is older than the one we have
            long modified = new File(this.playerDir, player.getUUID() + ".dat").lastModified();
            if (modified < craftPlayer.getFirstPlayed()) {
                craftPlayer.setFirstPlayed(modified);
            }
        }
    }

    public File getPlayerDir() {
        return this.playerDir;
    }

    public CompoundTag getPlayerData(String uuid) {
        try {
            final File file1 = new File(this.playerDir, uuid + ".dat");
            if (file1.exists()) {
                CompoundTag playerData = NbtIo.readCompressed(new FileInputStream(file1));

                // Luminara - Apply data conversion to player data
                if (playerData != null) {
                    try {
                        int dataVersion = playerData.getInt("DataVersion");
                        if (dataVersion > 0 && dataVersion < MCVersions.V1_20_1) {
                            CompoundTag convertedData = MCDataConverter.convertTag(MCDataType.PLAYER, playerData, dataVersion, MCVersions.V1_20_1);
                            if (convertedData != null) {
                                playerData = convertedData;
                            }
                        }
                    } catch (Exception e) {
                        // Log error but continue with original data
                        LOGGER.warn("Failed to convert player data for {}: {}", uuid, e.getMessage());
                    }
                }

                return playerData;
            }
        } catch (Exception exception) {
            ARCLIGHT_LOGGER.warn("player.data.load-failed", uuid);
        }
        return null;
    }

    @Override
    public File bridge$getPlayerDir() {
        return getPlayerDir();
    }

    @Override
    public CompoundTag bridge$getPlayerData(String uuid) {
        return getPlayerData(uuid);
    }
}
