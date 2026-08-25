package io.izzel.arclight.common.mod;

import io.izzel.arclight.api.ArclightPlatform;
import io.izzel.arclight.api.ArclightVersion;
import io.izzel.arclight.common.mod.boot.AbstractBootstrap;
import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import io.izzel.arclight.common.mod.util.log.ArclightJulBridge;
import io.izzel.arclight.mixin.MixinTools;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

public class ArclightConnector implements IMixinConnector {

    public static final Logger LOGGER = ArclightI18nLogger.getLogger("Arclight");

    @Override
    public void connect() {
        // Bukkit logs through java.util.logging and nothing routes it into Log4j on the
        // standalone mod path, so the bridge is installed before any Bukkit class loads.
        ArclightJulBridge.install();
        // The legacy launcher set the version/platform in its bootstrap phase before any
        // mixin was applied. As a standalone mod the connector is the earliest hook on
        // NeoForge, so both must be established here or mixins fail on version lookup.
        AbstractBootstrap.setVersionIfAbsent(ArclightVersion.FEUDAL_KINGS);
        AbstractBootstrap.setPlatformIfAbsent(AbstractBootstrap.detectPlatform());
        MixinTools.setup();
        Mixins.addConfiguration("mixins.arclight.core.json");
        Mixins.addConfiguration("mixins.arclight.bukkit.json");
        switch (currentPlatform()) {
            case VANILLA -> Mixins.addConfiguration("mixins.arclight.vanilla.json");
            case FORGE -> Mixins.addConfiguration("mixins.arclight.forge.json");
            case NEOFORGE -> Mixins.addConfiguration("mixins.arclight.neoforge.json");
        }
        LOGGER.info("mixin-load.core");
        Mixins.addConfiguration("mixins.arclight.impl.optimization.json");
        LOGGER.info("mixin-load.optimization");
    }

    private static ArclightPlatform currentPlatform() {
        AbstractBootstrap.setPlatformIfAbsent(AbstractBootstrap.detectPlatform());
        return ArclightPlatform.current();
    }
}
