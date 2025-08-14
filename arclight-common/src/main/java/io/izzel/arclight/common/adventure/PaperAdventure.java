package io.izzel.arclight.common.adventure;

import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Adventure integration for Luminara.
 * This class provides conversion utilities between Adventure components and Minecraft components.
 */
public final class PaperAdventure {

    private static final Logger LOGGER = ArclightI18nLogger.getLogger("Adventure");
    private static final GsonComponentSerializer GSON_SERIALIZER = GsonComponentSerializer.gson();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();
    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();
    private static final ComponentFlattener COMPONENT_FLATTENER = ComponentFlattener.basic();
    private static final MiniMessage MINI_MESSAGE = createMiniMessage();

    private PaperAdventure() {
        throw new RuntimeException("PaperAdventure is not to be instantiated!");
    }

    private static MiniMessage createMiniMessage() {
        return AdventureConfig.createMiniMessage();
    }

    /**
     * Convert Adventure Component to Minecraft Component.
     *
     * @param component The Adventure component to convert
     * @return The equivalent Minecraft component
     */
    public static net.minecraft.network.chat.Component asVanilla(@NotNull Component component) {
        try {
            String json = GSON_SERIALIZER.serialize(component);
            return net.minecraft.network.chat.Component.Serializer.fromJson(json);
        } catch (Exception e) {
            LOGGER.debug("Failed to convert Adventure component to vanilla, falling back to plain text: " + e.getMessage());
            return net.minecraft.network.chat.Component.literal(PLAIN_SERIALIZER.serialize(component));
        }
    }

    /**
     * Convert Minecraft Component to Adventure Component.
     *
     * @param component The Minecraft component to convert
     * @return The equivalent Adventure component
     */
    public static @NotNull Component asAdventure(@NotNull net.minecraft.network.chat.Component component) {
        try {
            String json = net.minecraft.network.chat.Component.Serializer.toJson(component);
            return GSON_SERIALIZER.deserialize(json);
        } catch (Exception e) {
            LOGGER.debug("Failed to convert vanilla component to Adventure, falling back to plain text: " + e.getMessage());
            return Component.text(component.getString());
        }
    }

    // Convert legacy string to Adventure Component
    public static @NotNull Component legacyToAdventure(@NotNull String legacy) {
        return LEGACY_SERIALIZER.deserialize(legacy);
    }

    // Convert Adventure Component to legacy string
    public static @NotNull String adventureToLegacy(@NotNull Component component) {
        return LEGACY_SERIALIZER.serialize(component);
    }

    // Convert Adventure Component to plain text
    public static @NotNull String asPlain(@NotNull Component component) {
        return PLAIN_SERIALIZER.serialize(component);
    }

    // Convert MiniMessage string to Adventure Component
    public static @NotNull Component miniMessageToAdventure(@NotNull String miniMessage) {
        return MINI_MESSAGE.deserialize(miniMessage);
    }

    // Convert Adventure Component to MiniMessage string
    public static @NotNull String adventureToMiniMessage(@NotNull Component component) {
        return MINI_MESSAGE.serialize(component);
    }

    // Get MiniMessage instance
    public static @NotNull MiniMessage miniMessage() {
        return MINI_MESSAGE;
    }


    // Enhanced message parsing that supports multiple formats
    public static @NotNull Component parseMessage(@NotNull String message) {
        // Try to detect the message format and parse accordingly
        if (message.contains("<") && message.contains(">")) {
            // Likely MiniMessage format
            return miniMessageToAdventure(message);
        } else if (message.contains("§") || message.contains("&")) {
            // Legacy format
            return legacyToAdventure(message);
        } else {
            // Plain text
            return Component.text(message);
        }
    }

    /**
     * Resolve a component with context (placeholder for future implementation).
     *
     * @param input             The input component
     * @param context           The command sender context
     * @param scoreboardSubject The scoreboard subject entity
     * @param bypassPermissions Whether to bypass permissions
     * @return The resolved component
     * @throws IOException If resolution fails
     */
    public static @NotNull Component resolveWithContext(@NotNull Component input, @Nullable CommandSender context, @Nullable Entity scoreboardSubject, boolean bypassPermissions) throws IOException {
        if (context == null) {
            return input;
        }
        // TODO: Implement context resolution (placeholders, permissions, etc.)
        return input;
    }

    /**
     * Get the component flattener instance.
     *
     * @return The component flattener
     */
    public static @NotNull ComponentFlattener componentFlattener() {
        return COMPONENT_FLATTENER;
    }

    /**
     * Flatten a component to plain text using the component flattener.
     *
     * @param component The component to flatten
     * @return The flattened text
     */
    public static @NotNull String flatten(@NotNull Component component) {
        StringBuilder builder = new StringBuilder();
        COMPONENT_FLATTENER.flatten(component, builder::append);
        return builder.toString();
    }

    /**
     * Convert a ComponentLike to a Component.
     *
     * @param componentLike The ComponentLike to convert
     * @return The Component
     */
    public static @NotNull Component asComponent(@NotNull ComponentLike componentLike) {
        return componentLike.asComponent();
    }

    public static @NotNull List<Audience> audiences(@NotNull List<? extends CommandSender> senders) {
        return senders.stream()
                .filter(sender -> sender instanceof Audience)
                .map(sender -> (Audience) sender)
                .toList();
    }

    public static @NotNull SignedMessage createUnsignedMessage(@NotNull String content) {
        return SimpleSignedMessage.unsigned(content);
    }

    /**
     * Create a signed SignedMessage.
     *
     * @param content The message content
     * @param sender  The sender UUID
     * @return The signed message
     */
    public static @NotNull SignedMessage createSignedMessage(@NotNull String content, @NotNull UUID sender) {
        return SimpleSignedMessage.signed(content, sender);
    }

    /**
     * Check if a string is likely a MiniMessage format.
     *
     * @param message The message to check
     * @return true if it looks like MiniMessage format
     */
    public static boolean isMiniMessage(@NotNull String message) {
        return message.contains("<") && message.contains(">");
    }

    /**
     * Check if a string is likely a legacy format.
     *
     * @param message The message to check
     * @return true if it looks like legacy format
     */
    public static boolean isLegacyFormat(@NotNull String message) {
        return message.contains("§") || message.contains("&");
    }

    /**
     * Get the GSON component serializer.
     *
     * @return The GSON serializer
     */
    public static @NotNull GsonComponentSerializer gsonSerializer() {
        return GSON_SERIALIZER;
    }

    /**
     * Get the legacy component serializer.
     *
     * @return The legacy serializer
     */
    public static @NotNull LegacyComponentSerializer legacySerializer() {
        return LEGACY_SERIALIZER;
    }

    /**
     * Get the plain text component serializer.
     *
     * @return The plain text serializer
     */
    public static @NotNull PlainTextComponentSerializer plainSerializer() {
        return PLAIN_SERIALIZER;
    }

    /**
     * Convert a string to a Component using the most appropriate format.
     * This method automatically detects the format and uses the best converter.
     *
     * @param input The input string
     * @return The converted Component
     */
    public static @NotNull Component autoConvert(@NotNull String input) {
        if (input.isEmpty()) {
            return Component.empty();
        }
        return parseMessage(input);
    }

    /**
     * Convert multiple ComponentLike objects to a single Component.
     *
     * @param components The components to join
     * @return The joined component
     */
    public static @NotNull Component join(@NotNull ComponentLike... components) {
        if (components.length == 0) {
            return Component.empty();
        }
        if (components.length == 1) {
            return components[0].asComponent();
        }

        Component result = components[0].asComponent();
        for (int i = 1; i < components.length; i++) {
            result = result.append(components[i]);
        }
        return result;
    }
}
