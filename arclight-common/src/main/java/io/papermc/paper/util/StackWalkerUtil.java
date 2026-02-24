package io.papermc.paper.util;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Optional;

public class StackWalkerUtil {

    @Nullable
    public static JavaPlugin getFirstPluginCaller() {
        Optional<JavaPlugin> foundFrame = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(stream -> stream
                        .map(frame -> resolvePlugin(frame.getDeclaringClass().getClassLoader()))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst());

        return foundFrame.orElse(null);
    }

    private static Optional<JavaPlugin> resolvePlugin(ClassLoader classLoader) {
        if (classLoader == null) {
            return Optional.empty();
        }
        if (!"org.bukkit.plugin.java.PluginClassLoader".equals(classLoader.getClass().getName())) {
            return Optional.empty();
        }
        try {
            Method method = classLoader.getClass().getDeclaredMethod("getPlugin");
            method.setAccessible(true);
            Object plugin = method.invoke(classLoader);
            if (plugin instanceof JavaPlugin javaPlugin) {
                return Optional.of(javaPlugin);
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return Optional.empty();
    }
}
