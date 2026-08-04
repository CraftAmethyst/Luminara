package io.izzel.arclight.boot.asm;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BukkitApiSupport {

    private static final Pattern MC_VERSION_PATTERN = Pattern.compile(
        "(\\d+)\\.(\\d+)(?:\\.(\\d+))?"
    );

    private BukkitApiSupport() {}

    public static String getMinecraftVersion(Object server) {
        if (server != null) {
            try {
                Method method = server
                    .getClass()
                    .getMethod("getMinecraftVersion");
                Object result = method.invoke(server);
                if (result instanceof String version && !version.isBlank()) {
                    return version;
                }
            } catch (ReflectiveOperationException ignored) {}
        }

        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Object bukkitVersion = bukkitClass
                .getMethod("getBukkitVersion")
                .invoke(null);
            if (bukkitVersion instanceof String versionString) {
                Matcher matcher = MC_VERSION_PATTERN.matcher(versionString);
                if (matcher.find()) {
                    String major = matcher.group(1);
                    String minor = matcher.group(2);
                    String release = matcher.group(3);
                    return release == null
                        ? major + "." + minor
                        : major + "." + minor + "." + release;
                }
            }
        } catch (ReflectiveOperationException ignored) {}
        return "1.20.1";
    }

    public static Object getCommandMap(Object server) {
        if (server != null) {
            try {
                Method method = server.getClass().getMethod("getCommandMap");
                Object result = method.invoke(server);
                if (result != null) {
                    return result;
                }
            } catch (ReflectiveOperationException ignored) {}
        }

        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Object pluginManager = bukkitClass
                .getMethod("getPluginManager")
                .invoke(null);
            if (pluginManager != null) {
                Class<?> currentClass = pluginManager.getClass();
                while (currentClass != null) {
                    try {
                        Field field = currentClass.getDeclaredField(
                            "commandMap"
                        );
                        field.setAccessible(true);
                        Object result = field.get(pluginManager);
                        if (result != null) {
                            return result;
                        }
                        break;
                    } catch (NoSuchFieldException e) {
                        currentClass = currentClass.getSuperclass();
                    }
                }
            }
        } catch (ReflectiveOperationException ignored) {}
        throw new IllegalStateException("Failed to retrieve command map");
    }
}
