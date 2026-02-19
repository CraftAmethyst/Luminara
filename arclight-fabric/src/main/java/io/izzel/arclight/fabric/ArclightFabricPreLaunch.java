package io.izzel.arclight.fabric;

import io.izzel.arclight.common.mod.ArclightCommon;
import io.izzel.arclight.common.mod.ArclightConnector;
import io.izzel.arclight.fabric.mod.FabricCommonImpl;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public final class ArclightFabricPreLaunch implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {
        ArclightCommon.setInstance(new FabricCommonImpl());
        new ArclightConnector().connect();
    }
}
