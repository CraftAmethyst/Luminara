package io.izzel.arclight.common.mixin.bukkit.plugin;

import io.izzel.arclight.api.Unsafe;
import io.izzel.arclight.common.bridge.bukkit.JavaPluginLoaderBridge;
import io.izzel.arclight.common.bridge.bukkit.PluginClassLoaderBridge;
import io.izzel.arclight.common.mod.server.ArclightServer;
import io.izzel.arclight.common.mod.util.EventExecutorFactory;
import io.izzel.arclight.i18n.ArclightConfig;
import org.apache.commons.lang3.Validate;
import org.bukkit.Server;
import org.bukkit.Warning;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

@Mixin(value = JavaPluginLoader.class, remap = false)
public abstract class JavaPluginLoaderMixin implements JavaPluginLoaderBridge {

    // @formatter:off
    @Shadow @Final Server server;
    @Invoker("setClass") public abstract void bridge$setClass(final String name, final Class<?> clazz);
    @Invoker("getClassByName") public abstract Class<?> arclight$getClassByName(String name, boolean resolve, PluginDescriptionFile description);
    @Accessor("loaders") public abstract<T extends URLClassLoader & PluginClassLoaderBridge> List<T> arclight$getLoaders();
    // @formatter:on

    @Unique
    private MethodHandle arclight$mh_ctorPcl;
    @Unique
    private static final AtomicInteger CMI_THREAD_COUNTER = new AtomicInteger();

    @Inject(method = "enablePlugin", at = @At("HEAD"), require = 0)
    private void arclight$reviveCmiExecutor(Plugin plugin, CallbackInfo ci) {
        if (plugin == null || !"CMI".equalsIgnoreCase(plugin.getName())) {
            return;
        }
        try {
            ClassLoader loader = plugin.getClass().getClassLoader();
            Class<?> threadClass = Class.forName("com.Zrips.CMI.utils.CMIThread", false, loader);
            Field executorField = threadClass.getDeclaredField("EXECUTOR");
            executorField.setAccessible(true);
            ExecutorService executor = (ExecutorService) executorField.get(null);
            if (executor != null && !executor.isShutdown() && !executor.isTerminated()) {
                return;
            }

            int poolSize = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);
            ExecutorService replacement = Executors.newFixedThreadPool(poolSize, runnable -> {
                Thread thread = new Thread(runnable);
                thread.setName("CMI Thread-" + CMI_THREAD_COUNTER.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            });

            Object base = Unsafe.staticFieldBase(executorField);
            long offset = Unsafe.staticFieldOffset(executorField);
            Unsafe.putObjectVolatile(base, offset, replacement);
            plugin.getLogger().warning("[Arclight] Revived terminated CMI async executor to avoid startup failure.");
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable throwable) {
            plugin.getLogger().log(Level.WARNING, "[Arclight] Failed to revive CMI async executor.", throwable);
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void arclight$initMH(Server instance, CallbackInfo ci) {
        try {
            Class<?> clz = Class.forName("org.bukkit.plugin.java.PluginClassLoader", true, getClass().getClassLoader());
            arclight$mh_ctorPcl = MethodHandles.lookup().findConstructor(clz, MethodType.methodType(void.class, String.class, JavaPluginLoader.class, ClassLoader.class, PluginDescriptionFile.class, File.class, File.class, ClassLoader.class));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Redirect(method = "loadPlugin", at = @At(value = "NEW", target = "(Lorg/bukkit/plugin/java/JavaPluginLoader;Ljava/lang/ClassLoader;Lorg/bukkit/plugin/PluginDescriptionFile;Ljava/io/File;Ljava/io/File;Ljava/lang/ClassLoader;)Lorg/bukkit/plugin/java/PluginClassLoader;"))
    @Coerce
    private Object arclight$debug$redirectConstructor(JavaPluginLoader loader, ClassLoader parent, PluginDescriptionFile desc, File file, File file2, ClassLoader ex) {
        try {
            return arclight$mh_ctorPcl.invoke(desc.getName(), loader, parent, desc, file, file2, ex);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Server arclight$server() {
        return server;
    }

    /**
     * @author InitAuther97
     * @reason Support plugin class loader isolation
     */
    @Overwrite
    Class<?> getClassByName(String name, boolean resolve, PluginDescriptionFile description) {
        SimplePluginManager manager = (SimplePluginManager) this.server.getPluginManager();
        if (ArclightConfig.spec().getCompat().isIsolatedPluginClassLoaders(name)) {
            Set<String> loaders = ArclightServer.iterateDepends(description);
            for (PluginClassLoaderBridge loader : arclight$getLoaders()) {
                PluginDescriptionFile desc = loader.arclight$desc();
                if (loaders.contains(desc.getName()) || !Collections.disjoint(loaders, desc.getProvides())) {
                    try {
                        return loader.arclight$loadFromExternal(name, resolve, true);
                    } catch (ClassNotFoundException ignored) {
                    }
                }
            }
        } else {

            for (PluginClassLoaderBridge loader : arclight$getLoaders()) {
                try {
                    return loader.arclight$loadFromExternal(name, resolve, manager.isTransitiveDepend(description, loader.arclight$desc()));
                } catch (ClassNotFoundException ignored) {
                }
            }
        }

        return null;
    }

    /**
     * @author IzzelAliz
     * @reason use asm event executor
     */
    @Overwrite
    @NotNull
    public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(@NotNull Listener listener, @NotNull Plugin plugin) {
        Validate.notNull(plugin, "Plugin can not be null");
        Validate.notNull(listener, "Listener can not be null");

        boolean useTimings = server.getPluginManager().useTimings();
        Map<Class<? extends Event>, Set<RegisteredListener>> ret = new HashMap<>();
        Set<Method> methods;
        try {
            Method[] publicMethods = listener.getClass().getMethods();
            Method[] privateMethods = listener.getClass().getDeclaredMethods();
            methods = new HashSet<>(publicMethods.length + privateMethods.length, 1.0f);
            methods.addAll(Arrays.asList(publicMethods));
            methods.addAll(Arrays.asList(privateMethods));
        } catch (NoClassDefFoundError e) {
            plugin.getLogger().severe("Plugin " + plugin.getDescription().getFullName() + " has failed to register events for " + listener.getClass() + " because " + e.getMessage() + " does not exist.");
            return ret;
        }

        for (final Method method : methods) {
            final EventHandler eh = method.getAnnotation(EventHandler.class);
            if (eh == null) continue;
            // Do not register bridge or synthetic methods to avoid event duplication
            // Fixes SPIGOT-893
            if (method.isBridge() || method.isSynthetic()) {
                continue;
            }
            final Class<?> checkClass;
            if (method.getParameterTypes().length != 1 || !Event.class.isAssignableFrom(checkClass = method.getParameterTypes()[0])) {
                plugin.getLogger().severe(plugin.getDescription().getFullName() + " attempted to register an invalid EventHandler method signature \"" + method.toGenericString() + "\" in " + listener.getClass());
                continue;
            }
            final Class<? extends Event> eventClass = checkClass.asSubclass(Event.class);
            method.setAccessible(true);
            Set<RegisteredListener> eventSet = ret.get(eventClass);
            if (eventSet == null) {
                eventSet = new HashSet<>();
                ret.put(eventClass, eventSet);
            }

            for (Class<?> clazz = eventClass; Event.class.isAssignableFrom(clazz); clazz = clazz.getSuperclass()) {
                // This loop checks for extending deprecated events
                if (clazz.getAnnotation(Deprecated.class) != null) {
                    Warning warning = clazz.getAnnotation(Warning.class);
                    Warning.WarningState warningState = server.getWarningState();
                    if (!warningState.printFor(warning)) {
                        break;
                    }
                    plugin.getLogger().log(
                        Level.WARNING,
                        String.format(
                            "\"%s\" has registered a listener for %s on method \"%s\", but the event is Deprecated. \"%s\"; please notify the authors %s.",
                            plugin.getDescription().getFullName(),
                            clazz.getName(),
                            method.toGenericString(),
                            (warning != null && warning.reason().length() != 0) ? warning.reason() : "Server performance will be affected",
                            Arrays.toString(plugin.getDescription().getAuthors().toArray())),
                        warningState == Warning.WarningState.ON ? new AuthorNagException(null) : null);
                    break;
                }
            }

            // final CustomTimingsHandler timings = new CustomTimingsHandler("Plugin: " + plugin.getDescription().getFullName() + " Event: " + listener.getClass().getName() + "::" + method.getName() + "(" + eventClass.getSimpleName() + ")", pluginParentTimer); // Spigot

            try {
                Class<? extends EventExecutor> executorClass = EventExecutorFactory.createExecutor(method, eventClass);
                Constructor<? extends EventExecutor> constructor = executorClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                EventExecutor executor = constructor.newInstance();
                eventSet.add(new RegisteredListener(listener, executor, eh.priority(), plugin, eh.ignoreCancelled()));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return ret;
    }
}
