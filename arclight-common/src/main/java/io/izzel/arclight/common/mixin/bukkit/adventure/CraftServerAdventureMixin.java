package io.izzel.arclight.common.mixin.bukkit.adventure;

import io.izzel.arclight.common.adventure.PaperAdventure;
import java.io.IOException;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.entity.Entity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.Merchant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = CraftServer.class, remap = false)
public abstract class CraftServerAdventureMixin
    implements net.kyori.adventure.audience.ForwardingAudience {

    private static final String BROADCAST_CHANNEL_USERS =
        "bukkit.broadcast.user";

    @Override
    public @NotNull Iterable<? extends Audience> audiences() {
        CraftServer server = (CraftServer) (Object) this;
        return PaperAdventure.audiences(
            server
                .getOnlinePlayers()
                .stream()
                .map(p -> (CommandSender) p)
                .toList()
        );
    }

    public int broadcast(net.kyori.adventure.text.@NotNull Component message) {
        return broadcast(message, BROADCAST_CHANNEL_USERS);
    }

    public int broadcast(
        net.kyori.adventure.text.@NotNull Component message,
        @NotNull String permission
    ) {
        CraftServer server = (CraftServer) (Object) this;
        return server.broadcast(
            PaperAdventure.adventureToLegacy(message),
            permission
        );
    }

    public @NotNull Inventory createInventory(
        @Nullable InventoryHolder owner,
        @NotNull InventoryType type,
        net.kyori.adventure.text.@NotNull Component title
    ) {
        CraftServer server = (CraftServer) (Object) this;
        return server.createInventory(
            owner,
            type,
            PaperAdventure.adventureToLegacy(title)
        );
    }

    public @NotNull Inventory createInventory(
        @Nullable InventoryHolder owner,
        int size,
        net.kyori.adventure.text.@NotNull Component title
    ) {
        CraftServer server = (CraftServer) (Object) this;
        return server.createInventory(
            owner,
            size,
            PaperAdventure.adventureToLegacy(title)
        );
    }

    public @NotNull Merchant createMerchant(
        net.kyori.adventure.text.@Nullable Component title
    ) {
        CraftServer server = (CraftServer) (Object) this;
        return server.createMerchant(
            title == null ? null : PaperAdventure.adventureToLegacy(title)
        );
    }

    public net.kyori.adventure.text.@NotNull Component motd() {
        CraftServer server = (CraftServer) (Object) this;
        return PaperAdventure.legacyToAdventure(server.getMotd());
    }

    public void motd(final net.kyori.adventure.text.@NotNull Component motd) {
        CraftServer server = (CraftServer) (Object) this;
        server.setMotd(PaperAdventure.adventureToLegacy(motd));
    }

    public net.kyori.adventure.text.@Nullable Component shutdownMessage() {
        CraftServer server = (CraftServer) (Object) this;
        String message = server.getShutdownMessage();
        return message == null
            ? null
            : PaperAdventure.legacyToAdventure(message);
    }

    public @NotNull Component resolveWithContext(
        @NotNull Component input,
        @Nullable CommandSender context,
        @Nullable Entity scoreboardSubject,
        boolean bypassPermissions
    ) throws IOException {
        return PaperAdventure.resolveWithContext(
            input,
            context,
            scoreboardSubject,
            bypassPermissions
        );
    }

    public @NotNull ComponentFlattener componentFlattener() {
        return ComponentFlattener.basic();
    }

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
        return GsonComponentSerializer.gson();
    }

    @Deprecated(forRemoval = true)
    public @NotNull GsonComponentSerializer colorDownsamplingGsonComponentSerializer() {
        return GsonComponentSerializer.colorDownsamplingGson();
    }

    @Deprecated(forRemoval = true)
    public @NotNull LegacyComponentSerializer legacyComponentSerializer() {
        return LegacyComponentSerializer.legacySection();
    }

    // MiniMessage serializer
    public @NotNull MiniMessage miniMessage() {
        return PaperAdventure.miniMessage();
    }
}
