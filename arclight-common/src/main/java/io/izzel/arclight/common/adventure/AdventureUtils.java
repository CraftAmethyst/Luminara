package io.izzel.arclight.common.adventure;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for Adventure operations in Luminara.
 * Provides convenient methods for common Adventure tasks.
 */
public final class AdventureUtils {

    private AdventureUtils() {
    }

    /**
     * Create a simple text component with color.
     */
    public static @NotNull Component text(@NotNull String text, @Nullable TextColor color) {
        Component component = Component.text(text);
        return color != null ? component.color(color) : component;
    }

    /**
     * Create a simple text component with named color.
     */
    public static @NotNull Component text(@NotNull String text, @Nullable NamedTextColor color) {
        return text(text, (TextColor) color);
    }

    /**
     * Create a styled text component.
     */
    public static @NotNull Component styledText(@NotNull String text, @NotNull Style style) {
        return Component.text(text).style(style);
    }

    /**
     * Create a bold text component.
     */
    public static @NotNull Component bold(@NotNull String text) {
        return Component.text(text).decorate(TextDecoration.BOLD);
    }

    /**
     * Create an italic text component.
     */
    public static @NotNull Component italic(@NotNull String text) {
        return Component.text(text).decorate(TextDecoration.ITALIC);
    }

    /**
     * Create an underlined text component.
     */
    public static @NotNull Component underlined(@NotNull String text) {
        return Component.text(text).decorate(TextDecoration.UNDERLINED);
    }

    /**
     * Create a strikethrough text component.
     */
    public static @NotNull Component strikethrough(@NotNull String text) {
        return Component.text(text).decorate(TextDecoration.STRIKETHROUGH);
    }

    /**
     * Create an obfuscated text component.
     */
    public static @NotNull Component obfuscated(@NotNull String text) {
        return Component.text(text).decorate(TextDecoration.OBFUSCATED);
    }

    /**
     * Join multiple components with a separator.
     */
    public static @NotNull Component join(@NotNull ComponentLike separator, @NotNull ComponentLike... components) {
        return Component.join(separator, components);
    }

    /**
     * Join multiple components with a separator.
     */
    public static @NotNull Component join(@NotNull ComponentLike separator, @NotNull Iterable<? extends ComponentLike> components) {
        return Component.join(separator, components);
    }

    /**
     * Create a newline component.
     */
    public static @NotNull Component newline() {
        return Component.newline();
    }

    /**
     * Create a space component.
     */
    public static @NotNull Component space() {
        return Component.space();
    }

    /**
     * Create an empty component.
     */
    public static @NotNull Component empty() {
        return Component.empty();
    }

    /**
     * Convert a collection of CommandSenders to Audiences.
     */
    public static @NotNull List<Audience> toAudiences(@NotNull Collection<? extends CommandSender> senders) {
        return senders.stream()
                .filter(sender -> sender instanceof Audience)
                .map(sender -> (Audience) sender)
                .collect(Collectors.toList());
    }

    /**
     * Send a message to multiple audiences.
     */
    public static void sendMessage(@NotNull Collection<? extends Audience> audiences, @NotNull ComponentLike message) {
        Component component = message.asComponent();
        for (Audience audience : audiences) {
            audience.sendMessage(component);
        }
    }

    /**
     * Send a message to multiple CommandSenders.
     */
    public static void sendMessageToSenders(@NotNull Collection<? extends CommandSender> senders, @NotNull ComponentLike message) {
        sendMessage(toAudiences(senders), message);
    }

    /**
     * Broadcast a message to all online players.
     */
    public static void broadcast(@NotNull ComponentLike message) {
        Component component = message.asComponent();
        org.bukkit.Bukkit.getOnlinePlayers().forEach(player -> {
            if (player instanceof Audience) {
                ((Audience) player).sendMessage(component);
            }
        });
    }

    /**
     * Send an action bar message to a player.
     */
    public static void sendActionBar(@NotNull Player player, @NotNull ComponentLike message) {
        if (player instanceof Audience) {
            ((Audience) player).sendActionBar(message);
        }
    }

    /**
     * Send a title to a player.
     */
    public static void sendTitle(@NotNull Player player, @NotNull ComponentLike title, @NotNull ComponentLike subtitle) {
        if (player instanceof Audience) {
            ((Audience) player).showTitle(net.kyori.adventure.title.Title.title(title.asComponent(), subtitle.asComponent()));
        }
    }

    /**
     * Check if a string contains Adventure formatting.
     */
    public static boolean hasAdventureFormatting(@NotNull String text) {
        return PaperAdventure.isMiniMessage(text) || PaperAdventure.isLegacyFormat(text);
    }

    /**
     * Parse a string to a Component using the best available method.
     */
    public static @NotNull Component parseString(@NotNull String text) {
        return PaperAdventure.autoConvert(text);
    }

    /**
     * Convert a Component to a legacy string.
     */
    public static @NotNull String toLegacy(@NotNull ComponentLike component) {
        return PaperAdventure.adventureToLegacy(component.asComponent());
    }

    /**
     * Convert a Component to plain text.
     */
    public static @NotNull String toPlain(@NotNull ComponentLike component) {
        return PaperAdventure.asPlain(component.asComponent());
    }

    /**
     * Convert a Component to MiniMessage format.
     */
    public static @NotNull String toMiniMessage(@NotNull ComponentLike component) {
        return PaperAdventure.adventureToMiniMessage(component.asComponent());
    }

    /**
     * Create a clickable component that runs a command.
     */
    public static @NotNull Component clickableCommand(@NotNull String text, @NotNull String command) {
        return Component.text(text)
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand(command));
    }

    /**
     * Create a clickable component that suggests a command.
     */
    public static @NotNull Component clickableSuggest(@NotNull String text, @NotNull String command) {
        return Component.text(text)
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.suggestCommand(command));
    }

    /**
     * Create a hoverable component.
     */
    public static @NotNull Component hoverable(@NotNull String text, @NotNull ComponentLike hoverText) {
        return Component.text(text)
                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(hoverText));
    }

    /**
     * Create a component with both click and hover events.
     */
    public static @NotNull Component interactive(@NotNull String text,
                                                 @NotNull String command,
                                                 @NotNull ComponentLike hoverText) {
        return Component.text(text)
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand(command))
                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(hoverText));
    }

    /**
     * Create a gradient text component (requires MiniMessage).
     */
    public static @NotNull Component gradient(@NotNull String text, @NotNull TextColor startColor, @NotNull TextColor endColor) {
        if (!AdventureConfig.isMiniMessageEnabled()) {
            return Component.text(text).color(startColor);
        }

        String miniMessage = String.format("<gradient:%s:%s>%s</gradient>",
                startColor.asHexString(),
                endColor.asHexString(),
                text);
        return PaperAdventure.miniMessageToAdventure(miniMessage);
    }

    /**
     * Create a rainbow text component (requires MiniMessage).
     */
    public static @NotNull Component rainbow(@NotNull String text) {
        if (!AdventureConfig.isMiniMessageEnabled()) {
            return Component.text(text).color(NamedTextColor.WHITE);
        }

        String miniMessage = String.format("<rainbow>%s</rainbow>", text);
        return PaperAdventure.miniMessageToAdventure(miniMessage);
    }

    /**
     * Validate that Adventure integration is working.
     */
    public static boolean validateAdventure() {
        return AdventureInitializer.validate();
    }

    /**
     * Get Adventure integration status.
     */
    public static @NotNull String getAdventureStatus() {
        return AdventureInitializer.getStatus();
    }
}
