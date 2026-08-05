package io.izzel.arclight.common.mod.compat.mixin;

import io.izzel.arclight.api.Unsafe;
import io.izzel.arclight.common.mod.compat.ModIds;
import io.izzel.arclight.common.mod.mixins.annotation.LoadIfMod;
import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentLinkedDeque;
import net.minecraft.server.Bootstrap;
import net.minecraftforge.fml.ModWorkManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Restore the vanilla mod-loading task queue when ModernFix is present.
 *
 * <p>ModernFix replaces {@code ModWorkManager.syncExecutor()}'s {@code tasks} field with its
 * {@code ModWorkManagerQueue}, whose {@code pollFirst()} parks the driving thread for 25ms
 * whenever the queue is empty (instead of returning {@code null} immediately like a plain
 * {@code ConcurrentLinkedDeque}). On a hybrid server the main thread acts as the executor
 * driver during mod loading ({@code ModLoader.waitForTransition} -> {@code drive} ->
 * {@code driveOne} -> {@code pollFirst}) and executes a large number of main-thread tasks
 * (mod constructors/events of every mod). Every gap between tasks then costs 25ms of parking,
 * which adds up to minutes of seemingly frozen startup. The 25ms pause only benefits the
 * client loading screen, so on a server the vanilla busy-loop semantics are preferable.
 *
 * <p>This runs at {@code Bootstrap.bootStrap()} RETURN, after ModernFix's bootstrap mixin has
 * installed its queue, and swaps the queue back to a plain {@code ConcurrentLinkedDeque}.
 */
@Mixin(Bootstrap.class)
@LoadIfMod(modid = ModIds.MODERNFIX, condition = LoadIfMod.ModCondition.PRESENT)
public class BootstrapModWorkManagerCompatMixin {

    private static final Logger ARCLIGHT_LOGGER = ArclightI18nLogger.getLogger(
        "ModWorkManagerCompat"
    );

    @Inject(method = "bootStrap", at = @At("RETURN"))
    private static void arclight$restoreModWorkManagerQueue(CallbackInfo ci) {
        try {
            Class<?> syncExecutorClass = Class.forName(
                "net.minecraftforge.fml.ModWorkManager$SyncExecutor"
            );
            Field tasksField = syncExecutorClass.getDeclaredField("tasks");
            long offset = Unsafe.objectFieldOffset(tasksField);
            Unsafe.putObjectVolatile(
                ModWorkManager.syncExecutor(),
                offset,
                new ConcurrentLinkedDeque<Runnable>()
            );
        } catch (Throwable t) {
            ARCLIGHT_LOGGER.warn(
                "Failed to restore vanilla ModWorkManager task queue, "
                    + "keeping ModernFix's queue: {}",
                t.toString(),
                t
            );
        }
    }
}
