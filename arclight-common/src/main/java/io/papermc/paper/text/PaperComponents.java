package io.papermc.paper.text;

import java.io.IOException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// Paper API methods for working with Components
public final class PaperComponents {

    private PaperComponents() {
        throw new RuntimeException(
            "PaperComponents is not to be instantiated!"
        );
    }

    // Resolve component with context
    public static @NotNull Component resolveWithContext(
        @NotNull Component input,
        @Nullable CommandSender context,
        @Nullable Entity scoreboardSubject
    ) throws IOException {
        return resolveWithContext(input, context, scoreboardSubject, true);
    }

    // Resolve component with context and permissions
    public static @NotNull Component resolveWithContext(
        @NotNull Component input,
        @Nullable CommandSender context,
        @Nullable Entity scoreboardSubject,
        boolean bypassPermissions
    ) throws IOException {
        // TODO: Implement proper context resolution
        return input;
    }

    // Get component flattener
    public static @NotNull ComponentFlattener flattener() {
        return ComponentFlattener.basic();
    }

    // Get plain text serializer
    @Deprecated(forRemoval = true)
    public static @NotNull PlainComponentSerializer plainSerializer() {
        return PlainComponentSerializer.plain();
    }

    // Get plain text serializer
    @Deprecated(forRemoval = true)
    public static @NotNull PlainTextComponentSerializer plainTextSerializer() {
        return PlainTextComponentSerializer.plainText();
    }

    // Get GSON serializer
    @Deprecated(forRemoval = true)
    public static @NotNull GsonComponentSerializer gsonSerializer() {
        return GsonComponentSerializer.gson();
    }

    // Get color downsampling GSON serializer
    @Deprecated(forRemoval = true)
    public static @NotNull GsonComponentSerializer colorDownsamplingGsonSerializer() {
        return GsonComponentSerializer.colorDownsamplingGson();
    }

    // Get legacy section serializer
    @Deprecated(forRemoval = true)
    public static @NotNull LegacyComponentSerializer legacySectionSerializer() {
        return LegacyComponentSerializer.legacySection();
    }

    // Get MiniMessage serializer
    public static @NotNull MiniMessage miniMessage() {
        return MiniMessage.builder().build();
    }
}
