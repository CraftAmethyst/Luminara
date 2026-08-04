package io.papermc.paper.event.player;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.server.ServerEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// Event for decorating chat components before chat events
@ApiStatus.Experimental
public class AsyncChatDecorateEvent extends ServerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final Component originalMessage;
    private Component result;
    private boolean cancelled;

    @ApiStatus.Internal
    public AsyncChatDecorateEvent(
        final boolean async,
        final @Nullable Player player,
        final @NotNull Component originalMessage,
        final @NotNull Component result
    ) {
        super(async);
        this.player = player;
        this.originalMessage = originalMessage;
        this.result = result;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    // Get player (may be null for commands)
    public @Nullable Player player() {
        return this.player;
    }

    // Get original message
    public @NotNull Component originalMessage() {
        return this.originalMessage;
    }

    // Get decoration result
    public @NotNull Component result() {
        return this.result;
    }

    // Set decoration result
    public void result(@NotNull Component result) {
        this.result = result;
    }

    // Check if part of preview (deprecated)
    @Deprecated(forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "1.21")
    @Contract(value = "-> false", pure = true)
    public boolean isPreview() {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    // Cancel decoration (result equals original)
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
