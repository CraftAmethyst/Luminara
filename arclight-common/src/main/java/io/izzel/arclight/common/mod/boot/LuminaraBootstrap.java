package io.izzel.arclight.common.mod.boot;

import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.reflect.TypeToken;
import io.izzel.arclight.api.ArclightVersion;
import io.izzel.arclight.api.EnumHelper;
import io.izzel.arclight.api.Unsafe;
import io.izzel.arclight.i18n.ArclightLocale;
import io.izzel.arclight.i18n.LuminaraVersion;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.Field;

public final class LuminaraBootstrap {

    private static boolean platformApplied;
    private static boolean gsonEnumFactoryApplied;

    private LuminaraBootstrap() {
    }

    public static synchronized void applyPlatform() {
        if (platformApplied) {
            return;
        }
        try {
            ArclightVersion.current();
        } catch (IllegalStateException notSet) {
            ArclightVersion.setVersion(ArclightVersion.TRIALS);
        }
        if (System.getProperty("arclight.version") == null) {
            System.setProperty("arclight.version", LuminaraVersion.version());
        }
        platformApplied = true;
    }

    // Gson's stock enum factory rejects the constants Luminara adds to the Bukkit enums.
    public static synchronized void installGsonEnumFactory() {
        if (gsonEnumFactoryApplied) {
            return;
        }
        TypeAdapters.ENUM_FACTORY.create(null, TypeToken.get(Object.class));
        Field field;
        try {
            field = TypeAdapters.class.getDeclaredField("ENUM_FACTORY");
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(
                "Unsupported Gson TypeAdapters for Minecraft 1.20.1 / Forge 47",
                e
            );
        }
        Unsafe.putObjectVolatile(
            Unsafe.staticFieldBase(field),
            Unsafe.staticFieldOffset(field),
            new EnumTypeFactory()
        );
        gsonEnumFactoryApplied = true;
    }

    public static void logBanner() {
        var logger = LogManager.getLogger("Luminara");
        logger.info(
            ArclightLocale.getInstance().get("logo"),
            ArclightLocale.getInstance().get(
                "release-name." + ArclightVersion.current().getReleaseName()
            ),
            LuminaraVersion.version(),
            LuminaraVersion.gitCommit()
        );
        logger.info(LuminaraVersion.compatibilityLine());
    }


    public static void warmUpEnumHelper() {
        Unsafe.ensureClassInitialized(EnumHelper.class);
    }

    // Reflective on purpose: Forge owns org.apache.logging.log4j, so linking against the JUL
    // bridge directly throws NoClassDefFoundError when a plugin loads.
    public static boolean isJulBridgeActive() {
        return java.util.logging.LogManager.getLogManager()
            .getClass()
            .getName()
            .equals("org.apache.logging.log4j.jul.LogManager");
    }
}
