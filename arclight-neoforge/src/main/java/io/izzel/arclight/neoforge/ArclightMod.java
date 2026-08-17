package io.izzel.arclight.neoforge;

import io.izzel.arclight.api.Arclight;
import io.izzel.arclight.api.ArclightPlatform;
import io.izzel.arclight.common.mod.boot.AbstractBootstrap;
import io.izzel.arclight.common.mod.server.ArclightServer;
import io.izzel.arclight.neoforge.mod.NeoForgeArclightServer;
import io.izzel.arclight.neoforge.mod.event.ArclightEventDispatcherRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.io.PrintStream;

@Mod("arclight")
public class ArclightMod implements AbstractBootstrap {

    public ArclightMod() {
        if (FMLLoader.getDist() == Dist.CLIENT) {
            throw new IllegalStateException("Arclight only initializes Bukkit on a dedicated NeoForge server; "
                + "do not run it in a client or integrated-server environment.");
        }
        try {
            // On the standalone mod path the CommandNode hack must not redefine the class:
            // the mod class loader differs from the one that loads brigadier, which would
            // create a duplicate CommandNode and break type identity. The CURRENT_COMMAND
            // field is provided by a Mixin (CommandNodeMixin_NeoForge) instead.
            this.installGsonEnumFactory();
        } catch (Throwable t) {
            ArclightServer.LOGGER.warn("Failed to apply the Gson enum bootstrap", t);
        }
        try {
            this.setupMod(ArclightPlatform.NEOFORGE);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up Arclight mod runtime", e);
        }
        ArclightServer.LOGGER.info("mod-load");
        Arclight.setServer(new NeoForgeArclightServer());
        System.setOut(new LoggingPrintStream("STDOUT", System.out, Level.INFO));
        System.setErr(new LoggingPrintStream("STDERR", System.err, Level.ERROR));
        ArclightEventDispatcherRegistry.registerAllEventDispatchers();
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