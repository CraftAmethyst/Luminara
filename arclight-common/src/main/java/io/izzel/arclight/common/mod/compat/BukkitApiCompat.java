package io.izzel.arclight.common.mod.compat;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BukkitApiCompat {

    private static final Pattern MINECRAFT_VERSION = Pattern.compile(
        "(\\d+)\\.(\\d+)(?:\\.(\\d+))?"
    );

    private BukkitApiCompat() {
    }

    public static String getMinecraftVersion(Server server) {
        if (server != null) {
            try {
                Method method = server.getClass().getMethod(
                    "getMinecraftVersion"
                );
                Object result = method.invoke(server);
                if (result instanceof String version && !version.isBlank()) {
                    return version;
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }

        Matcher matcher = MINECRAFT_VERSION.matcher(Bukkit.getBukkitVersion());
        if (matcher.find()) {
            String release = matcher.group(3);
            return matcher.group(1) + "." + matcher.group(2) +
                (release == null ? "" : "." + release);
        }
        return "1.20.1";
    }

    public static CommandMap getCommandMap(Server server) {
        if (server != null) {
            try {
                Method method = server.getClass().getMethod("getCommandMap");
                Object result = method.invoke(server);
                if (result instanceof CommandMap commandMap) {
                    return commandMap;
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }

        Object pluginManager = Bukkit.getPluginManager();
        Class<?> currentClass = pluginManager.getClass();
        while (currentClass != null) {
            try {
                Field field = currentClass.getDeclaredField("commandMap");
                field.setAccessible(true);
                Object result = field.get(pluginManager);
                if (result instanceof CommandMap commandMap) {
                    return commandMap;
                }
                break;
            } catch (NoSuchFieldException exception) {
                currentClass = currentClass.getSuperclass();
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException(
                    "Failed to retrieve Bukkit command map",
                    exception
                );
            }
        }
        throw new IllegalStateException("Failed to retrieve Bukkit command map");
    }
}
