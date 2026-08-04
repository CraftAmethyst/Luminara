package io.papermc.paper.event.player;

import io.papermc.paper.chat.ChatRenderer;
import java.util.Set;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import org.bukkit.Warning;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

// Sync chat event - use AsyncChatEvent instead
@Deprecated
@Warning(
    reason = "Listening to this event forces chat to wait for the main thread, delaying chat messages."
)
public final class ChatEvent extends AbstractChatEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    @ApiStatus.Internal
    public ChatEvent(
        final @NotNull Player player,
        final @NotNull Set<Audience> viewers,
        final @NotNull ChatRenderer renderer,
        final @NotNull Component message,
        final @NotNull Component originalMessage,
        final @NotNull SignedMessage signedMessage
    ) {
        super(
            false,
            player,
            viewers,
            renderer,
            message,
            originalMessage,
            signedMessage
        );
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
