package io.izzel.arclight.fabric.mod;

import io.izzel.arclight.api.ArclightPlatform;
import io.izzel.arclight.common.mod.boot.AbstractBootstrap;
import io.izzel.arclight.common.mod.ArclightCommon;
import io.izzel.arclight.common.mod.ArclightMixinPlugin;
import io.izzel.arclight.common.mod.util.log.ArclightJulBridge;
import io.izzel.arclight.i18n.ArclightConfig;
import io.izzel.arclight.i18n.ArclightLocale;
import io.izzel.arclight.mixin.MixinTools;
import org.apache.logging.log4j.LogManager;
import org.slf4j.LoggerFactory;

public class FabricMixinPlugin extends ArclightMixinPlugin implements AbstractBootstrap {

    @Override
    public void onLoad(String mixinPackage) {
        // Fabric loads mixins from fabric.mod.json and does not run the standalone
        // MixinConnector declared in the common manifest. Install the JUL bridge here,
        // before Bukkit or any plugin logger is initialized, so Spigot records use the
        // server Log4j format instead of JUL's default ConsoleHandler formatter.
        ArclightJulBridge.install();
        ArclightCommon.setInstance(new FabricCommonImpl());
        super.onLoad(mixinPackage);
        MixinTools.setup();
        LoggerFactory.getLogger("Arclight").info(
            ArclightLocale.getInstance().format("i18n.using-language", ArclightConfig.spec().getLocale().getCurrent(), ArclightConfig.spec().getLocale().getFallback())
        );
        try {
            this.setupMod(ArclightPlatform.FABRIC);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(LogManager::shutdown, "log flusher"));
    }
}
