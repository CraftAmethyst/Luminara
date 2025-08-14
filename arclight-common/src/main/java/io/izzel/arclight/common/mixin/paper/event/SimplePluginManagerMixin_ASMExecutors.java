package io.izzel.arclight.common.mixin.paper.event;

import com.destroystokyo.paper.event.executor.asm.ASMEventExecutorGenerator;
import com.destroystokyo.paper.event.executor.asm.ClassDefiner;
import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.SimplePluginManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;

/**
 * Mixin to integrate Paper's ASM event executors into the event system.
 * This provides performance improvements for event handling.
 */
@Mixin(value = SimplePluginManager.class, remap = false)
public class SimplePluginManagerMixin_ASMExecutors {

    private static final Logger LOGGER = ArclightI18nLogger.getLogger("ASMEventExecutors");

    /**
     * Use ASM-generated event executors for better performance.
     */
    @Inject(method = "getEventExecutor", at = @At("HEAD"), cancellable = true)
    private void arclight$useASMEventExecutor(EventHandler eh, Method method, CallbackInfoReturnable<EventExecutor> cir) {
        try {
            // Try to generate ASM event executor for better performance
            ClassDefiner definer = ClassDefiner.getInstance();
            if (definer != null) {
                String name = ASMEventExecutorGenerator.generateName();
                byte[] classData = ASMEventExecutorGenerator.generateEventExecutor(method, name);
                Class<?> executorClass = definer.defineClass(method.getDeclaringClass().getClassLoader(), name, classData);
                EventExecutor executor = (EventExecutor) executorClass.getDeclaredConstructor().newInstance();
                cir.setReturnValue(executor);
                return;
            }
        } catch (Exception e) {
            // Fall back to default implementation if ASM generation fails
            LOGGER.warn("Failed to generate ASM event executor for {}: {}", method.getName(), e.getMessage());
        }

        // Let the original method handle it if ASM generation fails
    }
}
