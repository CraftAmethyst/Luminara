package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Paper API Patch 0028: Add a call helper to Event
 * Adds callEvent() method to Event class for convenient event calling and cancellation checking
 */
@Mixin(value = Event.class, remap = false)
public class EventMixin {

    /**
     * Calls the event and tests if cancelled.
     *
     * @return false if event was cancelled, if cancellable. otherwise true.
     */
    public boolean callEvent() {
        Event event = (Event) (Object) this;
        Bukkit.getPluginManager().callEvent(event);
        if (event instanceof Cancellable) {
            return !((Cancellable) event).isCancelled();
        } else {
            return true;
        }
    }
}
