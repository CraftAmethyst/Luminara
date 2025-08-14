package io.izzel.arclight.common.mod;

import io.izzel.arclight.common.mod.server.event.ArclightEventDispatcherRegistry;
import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import io.izzel.arclight.common.mod.velocity.VelocityManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkConstants;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.io.PrintStream;

@Mod("arclight")
public class ArclightMod {

    public static final Logger LOGGER = ArclightI18nLogger.getLogger("Luminara");

    public ArclightMod(FMLJavaModLoadingContext context) {
        LOGGER.info("mod-load");
        System.setOut(new LoggingPrintStream("STDOUT", System.out, Level.INFO));
        System.setErr(new LoggingPrintStream("STDERR", System.err, Level.ERROR));
        ArclightEventDispatcherRegistry.registerAllEventDispatchers();
        context.registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));

        context.getModEventBus().addListener(this::onCommonSetup);

        MinecraftForge.EVENT_BUS.addListener(this::onServerAboutToStart);
    }

    public static boolean isModLoaded(String modid) {
        return ModList.get() != null ? ModList.get().isLoaded(modid) : FMLLoader.getLoadingModList().getModFileById(modid) != null;
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        // MPEM optimization system removed to avoid conflicts with Paper
        LOGGER.info("Common setup completed - MPEM optimizations disabled for Paper compatibility");
    }

    private void onServerAboutToStart(ServerAboutToStartEvent event) {
        // Initialize Velocity forwarding
        VelocityManager.getInstance().initialize();
        LOGGER.info("server-initialization-completed");
    }

    private static class LoggingPrintStream extends PrintStream {

        private final Logger logger;
        private final Level level;

        public LoggingPrintStream(String name, @NotNull OutputStream out, Level level) {
            super(out);
            this.logger = LogManager.getLogger(name);
            this.level = level;
        }

        @Override
        public void println(@Nullable String x) {
            logger.log(level, x);
        }

        @Override
        public void println(@Nullable Object x) {
            logger.log(level, String.valueOf(x));
        }
    }
}
