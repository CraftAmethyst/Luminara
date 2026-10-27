package org.craftamethyst.luminara.common.mod;

import io.izzel.arclight.mixin.injector.EjectorInfo;
import org.apache.logging.log4j.Logger;
import org.craftamethyst.luminara.common.mod.util.log.ArclightI18nLogger;
import org.craftamethyst.luminara.forge.ForgeCommonApi;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;

public class ArclightConnector implements IMixinConnector {

  public static final Logger LOGGER = ArclightI18nLogger.getLogger("Arclight");

  @Override
  public void connect() {
    if (ArclightCommon.instance() == null) {
      ArclightCommon.install(new ForgeCommonApi());
    }
    InjectionInfo.register(EjectorInfo.class);
    Mixins.addConfiguration("mixins.arclight.core.json");
    Mixins.addConfiguration("mixins.arclight.bukkit.json");
    Mixins.addConfiguration("mixins.arclight.forge.json");
    Mixins.addConfiguration("mixins.arclight.compat.json");
    LOGGER.info("mixin-load.core");
    Mixins.addConfiguration("mixins.arclight.impl.forge.optimization.json");
    Mixins.addConfiguration("mixins.arclight.forge.compat.json");
    LOGGER.info("mixin-load.optimization");
  }
}
