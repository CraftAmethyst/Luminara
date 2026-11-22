package org.craftamethyst.luminara.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.craftamethyst.luminara.common.mod.ArclightCommon;
import org.craftamethyst.luminara.common.mod.boot.LuminaraBootstrap;
import org.craftamethyst.luminara.i18n.ArclightConfig;

/** Fabric entry point. Installs platform services and runs the shared bootstrap. */
public final class LuminaraFabric implements ModInitializer {

  @Override
  public void onInitialize() {
    if (ArclightCommon.instance() == null) {
      ArclightCommon.install(new FabricCommonApi());
    }
    LuminaraBootstrap.applyPlatform();
    LuminaraBootstrap.registerLoggingPlugins();
    ArclightConfig.applyLogging();
    LuminaraBootstrap.installGsonEnumFactory();
    LuminaraBootstrap.warmUpEnumHelper();
    var loader = FabricLoader.getInstance().getModContainer("fabricloader").orElseThrow();
    LuminaraBootstrap.logBanner("Fabric", loader.getMetadata().getVersion().getFriendlyString());
  }
}
