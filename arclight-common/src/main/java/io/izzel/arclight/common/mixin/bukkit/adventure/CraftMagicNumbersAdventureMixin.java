package io.izzel.arclight.common.mixin.bukkit.adventure;

import io.izzel.arclight.common.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.io.IOException;

@Mixin(value = CraftMagicNumbers.class, remap = false)
public class CraftMagicNumbersAdventureMixin {

    // Adventure component resolution
    public @NotNull Component resolveWithContext(@NotNull Component input, @Nullable CommandSender context, @Nullable Entity scoreboardSubject, boolean bypassPermissions) throws IOException {
        return PaperAdventure.resolveWithContext(input, context, scoreboardSubject, bypassPermissions);
    }

    // Adventure component flattener
    public @NotNull ComponentFlattener componentFlattener() {
        return PaperAdventure.componentFlattener();
    }

    // Adventure serializers (deprecated methods for compatibility)
    @Deprecated(forRemoval = true)
    public @NotNull PlainComponentSerializer plainComponentSerializer() {
        return PlainComponentSerializer.plain();
    }

    @Deprecated(forRemoval = true)
    public @NotNull PlainTextComponentSerializer plainTextSerializer() {
        return PlainTextComponentSerializer.plainText();
    }

    @Deprecated(forRemoval = true)
    public @NotNull GsonComponentSerializer gsonComponentSerializer() {
        return PaperAdventure.gsonSerializer();
    }

    @Deprecated(forRemoval = true)
    public @NotNull GsonComponentSerializer colorDownsamplingGsonComponentSerializer() {
        return GsonComponentSerializer.colorDownsamplingGson();
    }

    @Deprecated(forRemoval = true)
    public @NotNull LegacyComponentSerializer legacyComponentSerializer() {
        return PaperAdventure.legacySerializer();
    }

    // MiniMessage serializer
    public @NotNull MiniMessage miniMessage() {
        return PaperAdventure.miniMessage();
    }
}
