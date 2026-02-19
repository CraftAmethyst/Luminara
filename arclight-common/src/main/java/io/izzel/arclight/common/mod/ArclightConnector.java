package io.izzel.arclight.common.mod;

import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;

public class ArclightConnector implements IMixinConnector {

    public static final Logger LOGGER = ArclightI18nLogger.getLogger("Arclight");
    private static volatile boolean connected;

    @SuppressWarnings("unchecked")
    private static void registerEjectorInfo() {
        try {
            Class<?> clazz = Class.forName("io.izzel.arclight.mixin.injector.EjectorInfo", false, ArclightConnector.class.getClassLoader());
            if (InjectionInfo.class.isAssignableFrom(clazz)) {
                InjectionInfo.register((Class<? extends InjectionInfo>) clazz);
            }
        } catch (Throwable t) {
            LOGGER.debug("Skip EjectorInfo registration: {}", t.toString());
        }
    }

    private static boolean isClassPresent(String name) {
        try {
            Class.forName(name, false, ArclightConnector.class.getClassLoader());
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public void connect() {
        if (connected) {
            return;
        }
        connected = true;
        registerEjectorInfo();
        Mixins.addConfiguration("mixins.arclight.core.json");
        Mixins.addConfiguration("mixins.arclight.bukkit.json");
        Mixins.addConfiguration("mixins.arclight.compat.json");
        if (isClassPresent("net.fabricmc.loader.api.FabricLoader")) {
            LOGGER.info("mixin-load.core.fabric");
            return;
        }
        Mixins.addConfiguration("mixins.arclight.forge.json");
        LOGGER.info("mixin-load.core");
        Mixins.addConfiguration("mixins.arclight.impl.forge.optimization.json");
        LOGGER.info("mixin-load.optimization");
        Mixins.addConfiguration("mixins.arclight.impl.forge.paper.json");
    }
}
