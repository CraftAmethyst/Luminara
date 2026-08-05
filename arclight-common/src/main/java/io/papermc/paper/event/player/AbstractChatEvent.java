package io.papermc.paper.event.player;

import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static java.util.Objects.requireNonNull;

@ApiStatus.NonExtendable
public abstract class AbstractChatEvent
    extends PlayerEvent
    implements Cancellable {

    private final Set<Audience> viewers;
    private final Component originalMessage;
    private final SignedMessage signedMessage;
    private ChatRenderer renderer;
    private Component message;
    private boolean cancelled = false;

    AbstractChatEvent(
        final boolean async,
        final @NotNull Player player,
        final @NotNull Set<Audience> viewers,
        final @NotNull ChatRenderer renderer,
        final @NotNull Component message,
        final @NotNull Component originalMessage,
        final @NotNull SignedMessage signedMessage
    ) {
        super(player);
        this.viewers = viewers;
        this.renderer = renderer;
        this.message = message;
        this.originalMessage = originalMessage;
        this.signedMessage = signedMessage;
    }

    @NotNull
    public final Set<Audience> viewers() {
        return this.viewers;
    }

    public final void renderer(final @NotNull ChatRenderer renderer) {
        this.renderer = requireNonNull(renderer, "renderer");
    }

    @NotNull
    public final ChatRenderer renderer() {
        return this.renderer;
    }

    @NotNull
    public final Component message() {
        return this.message;
    }

    public final void message(final @NotNull Component message) {
        this.message = requireNonNull(message, "message");
    }

    @NotNull
    public final Component originalMessage() {
        return this.originalMessage;
    }

    @NotNull
    public final SignedMessage signedMessage() {
        return this.signedMessage;
    }

    @Override
    public final boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public final void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
}
