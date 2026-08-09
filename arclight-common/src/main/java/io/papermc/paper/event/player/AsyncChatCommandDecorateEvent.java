package io.papermc.paper.event.player;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public class AsyncChatCommandDecorateEvent extends AsyncChatDecorateEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    @ApiStatus.Internal
    public AsyncChatCommandDecorateEvent(
        boolean async,
        @Nullable Player player,
        @NotNull Component originalMessage,
        @NotNull Component result
    ) {
        super(async, player, originalMessage, result);
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
