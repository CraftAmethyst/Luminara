package org.craftamethyst.luminara.common.bridge.core.world.storage;

import java.io.File;
import net.minecraft.nbt.CompoundTag;

public interface PlayerDataBridge {
  File bridge$getPlayerDir();

  CompoundTag bridge$getPlayerData(String uuid);
}
