package io.papermc.paper.configuration;

import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Simplified Paper GlobalConfiguration for Luminara compatibility.
 * This provides the basic structure needed for Paper API compatibility.
 */
public class GlobalConfiguration extends ConfigurationPart {

    private static final Logger LOGGER = ArclightI18nLogger.getLogger("GlobalConfiguration");
    private static final AtomicReference<GlobalConfiguration> INSTANCE = new AtomicReference<>();

    // Configuration sections
    public Logging logging = new Logging();
    public Console console = new Console();
    public ChunkLoading chunkLoading = new ChunkLoading();
    public UnsupportedSettings unsupportedSettings = new UnsupportedSettings();

    public static GlobalConfiguration get() {
        return INSTANCE.get();
    }

    public static void set(GlobalConfiguration config) {
        INSTANCE.set(config);
    }

    public static void initialize(Path configPath) {
        GlobalConfiguration config = new GlobalConfiguration();
        set(config);
        LOGGER.info("Initialized Paper GlobalConfiguration for Luminara");
    }

    public static class Logging extends ConfigurationPart {
        public boolean deobfuscateStacktraces = true;
        public boolean useRgbForNamedTextColors = true;
    }


    public static class Console extends ConfigurationPart {
        public boolean enableBrigadierHighlighting = true;
        public boolean enableBrigadierCompletions = true;
        public boolean hasAllPermissions = false;
    }

    public static class ChunkLoading extends ConfigurationPart {
        public int minLoadRadius = 2;
        public int maxConcurrentSends = 2;
        public boolean autoconfigSendDistance = true;
        public double targetPlayerChunkSendRate = 100.0;
        public double globalMaxChunkSendRate = -1.0;
        public boolean enableFrustumPriority = false;
        public double globalMaxChunkLoadRate = -1.0;
        public double playerMaxConcurrentLoads = 20.0;
        public double globalMaxConcurrentLoads = 500.0;
    }

    public static class UnsupportedSettings extends ConfigurationPart {
        public boolean allowHeadlessPistons = false;
        public boolean allowGrindstoneOverstacking = false;
        public boolean allowHeadlessPistonsReadonly = false;
        public boolean allowPermanentBlockBreakExploits = false;
        public boolean allowPistonDuplication = false;
        public boolean performUsernameValidation = true;
    }
}
