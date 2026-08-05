package io.izzel.arclight.common.bridge.core.world.storage;

import net.minecraft.nbt.CompoundTag;

import java.io.File;

public interface PlayerDataBridge {
    File bridge$getPlayerDir();

    CompoundTag bridge$getPlayerData(String uuid);
}
