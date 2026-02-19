package io.izzel.arclight.fabric.mod;

import io.izzel.arclight.common.mod.ArclightCommon;
import net.fabricmc.loader.api.FabricLoader;

public class FabricCommonImpl implements ArclightCommon.Api {

    @Override
    public boolean isModLoaded(String modid) {
        return FabricLoader.getInstance().isModLoaded(modid);
    }
}
