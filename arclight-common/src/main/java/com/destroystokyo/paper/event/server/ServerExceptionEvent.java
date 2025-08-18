package com.destroystokyo.paper.event.server;

import com.destroystokyo.paper.exception.ServerException;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Called whenever an exception is thrown in a recoverable section of the server.
 */
public class ServerExceptionEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @NotNull
    private final ServerException exception;

    public ServerExceptionEvent(@NotNull ServerException exception) {
        super(!Bukkit.isPrimaryThread());
        this.exception = checkNotNull(exception, "exception");
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Calls the event and reports the exception to the logger if no handlers are registered.
     *
     * @param exception the exception to report
     */
    public static void reportException(@NotNull ServerException exception) {
        try {
            ServerExceptionEvent exceptionEvent = new ServerExceptionEvent(exception);
            Bukkit.getPluginManager().callEvent(exceptionEvent);
            if (exceptionEvent.getHandlers().getRegisteredListeners().length == 0) {
                Bukkit.getLogger().log(Level.SEVERE, exception.getMessage(), exception);
            }
        } catch (Throwable t) {
            Bukkit.getLogger().log(Level.WARNING, "Exception posting ServerExceptionEvent", t); // Don't want to rethrow!
        }
    }

    /**
     * Gets the wrapped exception that was thrown.
     *
     * @return Exception thrown
     */
    @NotNull
    public ServerException getException() {
        return exception;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
