package io.izzel.arclight.common.mixin.forge;

import java.lang.reflect.Modifier;
import net.minecraftforge.eventbus.ListenerList;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventListenerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EventListenerHelper.class)
public class EventListenerHelperMixin {

    @Inject(
        method = "computeListenerList",
        remap = false,
        at = @At("HEAD"),
        cancellable = true
    )
    private static void arclight$handleMissingNoArgCtor(
        Class<?> eventClass,
        boolean useSuper,
        CallbackInfoReturnable<ListenerList> cir
    ) {
        if (
            useSuper ||
            eventClass == Event.class ||
            Modifier.isAbstract(eventClass.getModifiers())
        ) {
            return;
        }
        try {
            eventClass.getConstructor();
        } catch (NoSuchMethodException | SecurityException e) {
            Class<?> superClass = eventClass.getSuperclass();
            ListenerList superList = superClass == null
                ? new ListenerList()
                : EventListenerHelper.getListenerList(superClass);
            cir.setReturnValue(new ListenerList(superList));
        }
    }
}
