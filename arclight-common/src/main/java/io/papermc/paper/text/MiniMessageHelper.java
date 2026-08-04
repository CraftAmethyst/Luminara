package io.papermc.paper.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// Helper class for MiniMessage operations
public final class MiniMessageHelper {

    private static final MiniMessage MINI_MESSAGE =
        MiniMessage.builder().build();
    private static final MiniMessage STRICT_MINI_MESSAGE = MiniMessage.builder()
        .tags(
            TagResolver.resolver(
                StandardTags.color(),
                StandardTags.decorations(),
                StandardTags.reset()
            )
        )
        .build();

    private MiniMessageHelper() {
        throw new RuntimeException(
            "MiniMessageHelper is not to be instantiated!"
        );
    }

    // Parse MiniMessage string to Component
    public static @NotNull Component parse(@NotNull String miniMessage) {
        return MINI_MESSAGE.deserialize(miniMessage);
    }

    // Parse MiniMessage string to Component with custom tag resolvers
    public static @NotNull Component parse(
        @NotNull String miniMessage,
        @NotNull TagResolver... resolvers
    ) {
        return MINI_MESSAGE.deserialize(miniMessage, resolvers);
    }

    // Parse MiniMessage string to Component with strict tags (only basic formatting)
    public static @NotNull Component parseStrict(@NotNull String miniMessage) {
        return STRICT_MINI_MESSAGE.deserialize(miniMessage);
    }

    // Serialize Component to MiniMessage string
    public static @NotNull String serialize(@NotNull Component component) {
        return MINI_MESSAGE.serialize(component);
    }

    // Check if string contains MiniMessage tags
    public static boolean containsTags(@Nullable String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return input.contains("<") && input.contains(">");
    }

    // Strip MiniMessage tags from string (convert to plain text)
    public static @NotNull String stripTags(@NotNull String miniMessage) {
        try {
            Component component = MINI_MESSAGE.deserialize(miniMessage);
            return PaperComponents.plainTextSerializer().serialize(component);
        } catch (Exception e) {
            // If parsing fails, return original string
            return miniMessage;
        }
    }

    // Get default MiniMessage instance
    public static @NotNull MiniMessage miniMessage() {
        return MINI_MESSAGE;
    }

    // Get strict MiniMessage instance (limited tags)
    public static @NotNull MiniMessage strictMiniMessage() {
        return STRICT_MINI_MESSAGE;
    }
}
