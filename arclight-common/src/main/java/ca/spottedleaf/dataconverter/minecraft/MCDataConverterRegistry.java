package ca.spottedleaf.dataconverter.minecraft;

import ca.spottedleaf.dataconverter.converters.datatypes.MCTypeRegistry;
import ca.spottedleaf.dataconverter.minecraft.datatypes.MCDataType;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public final class MCDataConverterRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean initialized = false;

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        LOGGER.info("Initializing Minecraft Data Converter Registry...");

        try {
            // Register all converters
            MCTypeRegistry.registerConverters();

            LOGGER.info("Successfully initialized {} data types with converters", MCDataType.values().length);
        } catch (final Exception ex) {
            LOGGER.error("Failed to initialize data converter registry", ex);
            throw new RuntimeException("Failed to initialize data converter registry", ex);
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }

    private MCDataConverterRegistry() {}

}
