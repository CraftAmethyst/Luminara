package io.izzel.arclight.common.mod.compat.mixin;

import io.izzel.arclight.common.mod.compat.ModIds;
import io.izzel.arclight.common.mod.mixins.annotation.LoadIfMod;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@LoadIfMod(
    modid = {ModIds.BLUEPRINT},
    condition = LoadIfMod.ModCondition.PRESENT
)
@Pseudo
@Mixin(
    targets = "com.teamabnormals.blueprint.core.events.FallingBlockEvent",
    remap = false
)
public class BlueprintFallingBlockEventMixin {

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
        method = "onBlockFall",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z"
        )
    )
    private static boolean arclight$guardOnBlockFall(
        IEventBus bus,
        Event event
    ) {
        return arclight$safePost(bus, event);
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
        method = "onFallingBlockTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z"
        )
    )
    private static boolean arclight$guardOnFallingBlockTick(
        IEventBus bus,
        Event event
    ) {
        return arclight$safePost(bus, event);
    }

    private static boolean arclight$safePost(IEventBus bus, Event event) {
        try {
            return bus.post(event);
        } catch (RuntimeException e) {
            if (arclight$isMissingNoArgCtor(e)) {
                return false;
            }
            throw e;
        }
    }

    private static boolean arclight$isMissingNoArgCtor(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof NoSuchMethodException) {
                return true;
            }
        }
        String message = error.getMessage();
        return (
            message != null && message.contains("Error computing listener list")
        );
    }
}
