package io.papermc.paper.chat;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

// Chat renderer for player messages
@FunctionalInterface
public interface ChatRenderer {
    // Create default chat renderer
    @NotNull
    static ChatRenderer defaultRenderer() {
        return new ViewerUnawareImpl.Default(
            (source, sourceDisplayName, message) ->
                Component.translatable(
                    "chat.type.text",
                    sourceDisplayName,
                    message
                )
        );
    }

    // Create viewer-unaware renderer
    @NotNull
    static ChatRenderer viewerUnaware(final @NotNull ViewerUnaware renderer) {
        return new ViewerUnawareImpl(renderer);
    }

    // Render chat message for each audience
    @ApiStatus.OverrideOnly
    @NotNull
    Component render(
        @NotNull Player source,
        @NotNull Component sourceDisplayName,
        @NotNull Component message,
        @NotNull Audience viewer
    );

    @ApiStatus.Internal
    sealed interface Default
        extends ChatRenderer, ViewerUnaware
        permits ViewerUnawareImpl.Default {}

    // Chat renderer without viewer context
    interface ViewerUnaware {
        // Render chat message
        @ApiStatus.OverrideOnly
        @NotNull
        Component render(
            @NotNull Player source,
            @NotNull Component sourceDisplayName,
            @NotNull Component message
        );
    }
}
