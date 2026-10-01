package io.izzel.arclight.common.mixin.bukkit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.izzel.arclight.api.Unsafe;
import io.izzel.arclight.common.bridge.bukkit.JavaPluginLoaderBridge;
import io.izzel.arclight.common.mod.util.EventExecutorFactory;
import org.apache.commons.lang3.Validate;
import org.bukkit.Server;
import org.bukkit.Warning;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.AuthorNagException;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

@Mixin(value = JavaPluginLoader.class, remap = false)
public abstract class JavaPluginLoaderMixin implements JavaPluginLoaderBridge {

    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static final Cache<
        Method,
        Class<? extends EventExecutor>
        > EXECUTOR_CACHE = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.HOURS)
        .build();
    private static final AtomicInteger CMI_THREAD_COUNTER = new AtomicInteger();
    private static final String HIDDEN_FORM =
        Float.parseFloat(System.getProperty("java.class.version")) < 57
            ? "Ljava/lang/invoke/LambdaForm$Hidden;"
            : "Ljdk/internal/vm/annotation/Hidden;";

    // @formatter:on
    // @formatter:off
    @Shadow @Final Server server;

    @Invoker("setClass") public abstract void bridge$setClass(final String name, final Class<?> clazz);

    @Accessor("loaders") public abstract List<URLClassLoader> bridge$getLoaders();

    @Inject(method = "enablePlugin", at = @At("HEAD"), require = 1)
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
                Class<? extends EventExecutor> executorClass = createExecutor(method, eventClass);
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

    private Class<? extends EventExecutor> createExecutor(Method method, Class<? extends Event> eventClass) throws ExecutionException {
        return EXECUTOR_CACHE.get(method, () -> EventExecutorFactory.createExecutor(method, eventClass));
    }
}
