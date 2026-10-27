package org.craftamethyst.luminara.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.craftamethyst.luminara.common.mod.ArclightCommon;

/** Installs platform services before mixins are applied. */
public final class LuminaraFabricPreLaunch implements PreLaunchEntrypoint {

  @Override
  public void onPreLaunch() {
    if (ArclightCommon.instance() == null) {
      ArclightCommon.install(new FabricCommonApi());
    }
    if (FabricLoader.getInstance().getEnvironmentType()
        != net.fabricmc.api.EnvType.SERVER) {
      throw new IllegalStateException("Luminara only runs on a dedicated Fabric server");
    }
  }
}
