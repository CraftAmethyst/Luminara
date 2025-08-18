package io.izzel.arclight.common.mixin.bukkit.adventure;

import io.izzel.arclight.common.bridge.core.entity.player.PlayerEntityBridge;
import io.izzel.arclight.common.bridge.core.entity.player.ServerPlayerEntityBridge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

// Implements Paper's Player interface methods

@Mixin(value = CraftPlayer.class, remap = false)
public abstract class CraftPlayerPaperPlayerMixin {

    // Paper Player interface methods that HuskHomes might expect

    public void sendActionBar(@NotNull Component message) {
        CraftPlayer player = (CraftPlayer) (Object) this;
        if (player.getHandle() instanceof ServerPlayerEntityBridge bridge) {
            bridge.bridge$sendActionBar(message);
        }
    }

    public void showTitle(@NotNull Title title) {
        CraftPlayer player = (CraftPlayer) (Object) this;
        if (player.getHandle() instanceof ServerPlayerEntityBridge bridge) {
            bridge.bridge$sendTitle(title);
        }
    }

    public int getPing() {
        CraftPlayer player = (CraftPlayer) (Object) this;
        if (player.getHandle() instanceof ServerPlayerEntityBridge bridge) {
            return bridge.bridge$getPing();
        }
        return 0;
    }

    public float getAttackCooldown() {
        CraftPlayer player = (CraftPlayer) (Object) this;
        if (player.getHandle() instanceof PlayerEntityBridge bridge) {
            return bridge.bridge$getAttackCooldown();
        }
        return 1.0f;
    }

    public void resetCooldown() {
        CraftPlayer player = (CraftPlayer) (Object) this;
        if (player.getHandle() instanceof PlayerEntityBridge bridge) {
            bridge.bridge$resetAttackCooldown();
        }
    }

    @Nullable
    public Location getCompassTarget() {
        CraftPlayer player = (CraftPlayer) (Object) this;
        if (player.getHandle() instanceof PlayerEntityBridge bridge) {
            return bridge.bridge$getCompassTarget();
        }
        return null;
    }

    public void updateCommands() {
        CraftPlayer player = (CraftPlayer) (Object) this;
        if (player.getHandle() instanceof ServerPlayerEntityBridge bridge) {
            bridge.bridge$updateCommands();
        }
    }

    public boolean isFakePlayer() {
        CraftPlayer player = (CraftPlayer) (Object) this;
        return player.getHandle() instanceof net.minecraftforge.common.util.FakePlayer;
    }


    // Paper's playerListName methods
    @Nullable
    public Component playerListName() {
        CraftPlayer player = (CraftPlayer) (Object) this;
        String listName = player.getPlayerListName();
        return listName != null ?
                net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(listName) :
                null;
    }

    public void playerListName(@Nullable Component name) {
        CraftPlayer player = (CraftPlayer) (Object) this;
        if (name == null) {
            player.setPlayerListName(null);
        } else {
            String legacy = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(name);
            player.setPlayerListName(legacy);
        }
    }

    // Additional Paper Player methods that plugins might expect
    public void kick(@NotNull Component message) {
        CraftPlayer player = (CraftPlayer) (Object) this;
        String legacy = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(message);
        player.kickPlayer(legacy);
    }

    public void sendRichMessage(@NotNull String message) {
        CraftPlayer player = (CraftPlayer) (Object) this;
        Component component = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(message);
        if (player instanceof net.kyori.adventure.audience.Audience audience) {
            audience.sendMessage(component);
        }
    }

    // Paper API Patch 0021: Graduate bungeecord chat API from spigot subclasses

    /**
     * Sends the component to the player
     *
     * @param component the components to send
     * @deprecated use {@code sendMessage} methods that accept {@link net.kyori.adventure.text.Component}
     */
    @Deprecated
    public void sendMessage(@NotNull BaseComponent component) {
        CraftPlayer player = (CraftPlayer) (Object) this;
        player.spigot().sendMessage(component);
    }

    /**
     * Sends an array of components as a single message to the player
     *
     * @param components the components to send
     * @deprecated use {@code sendMessage} methods that accept {@link net.kyori.adventure.text.Component}
     */
    @Deprecated
    public void sendMessage(@NotNull BaseComponent... components) {
        CraftPlayer player = (CraftPlayer) (Object) this;
        player.spigot().sendMessage(components);
    }

    /**
     * Sends an array of components as a single message to the specified screen position of this player
     *
     * @param position   the screen position
     * @param components the components to send
     */
    public void sendMessage(ChatMessageType position, BaseComponent... components) {
        CraftPlayer player = (CraftPlayer) (Object) this;
        player.spigot().sendMessage(position, components);
    }

    // Paper API Patch 0025: Player Tab List and Title APIs

    /**
     * Sends a title to this player
     *
     * @param title the title to send
     * @throws NullPointerException if the title is null
     * @deprecated use {@link #showTitle(net.kyori.adventure.title.Title)} or {@link #sendTitlePart(net.kyori.adventure.title.TitlePart, Object)}
     */
    @Deprecated
    public void sendTitle(@NotNull org.bukkit.Title title) {
        CraftPlayer player = (CraftPlayer) (Object) this;

        // Send title and subtitle using spigot methods
        // Use ACTION_BAR as fallback since TITLE/SUBTITLE may not be available
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, title.title());
        if (title.subtitle() != null) {
            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, title.subtitle());
        }

        // Create Adventure title for timing
        net.kyori.adventure.title.Title adventureTitle = net.kyori.adventure.title.Title.title(
                net.kyori.adventure.text.Component.empty(),
                net.kyori.adventure.text.Component.empty(),
                net.kyori.adventure.title.Title.Times.times(
                        java.time.Duration.ofMillis(title.fadeIn() * 50L),
                        java.time.Duration.ofMillis(title.stay() * 50L),
                        java.time.Duration.ofMillis(title.fadeOut() * 50L)
                )
        );

        // Send timing using existing bridge method
        if (player.getHandle() instanceof io.izzel.arclight.common.bridge.core.entity.player.ServerPlayerEntityBridge bridge) {
            bridge.bridge$sendTitle(adventureTitle);
        }
    }

    /**
     * Hides any title that is currently visible to the player
     *
     * @deprecated use {@link #clearTitle()}
     */
    @Deprecated
    public void hideTitle() {
        CraftPlayer player = (CraftPlayer) (Object) this;
        // Clear title by sending empty title
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR);
    }

    /**
     * Sets the header and footer of the player's player list
     *
     * @param header the header text, null to clear
     * @param footer the footer text, null to clear
     * @deprecated in favour of {@link #sendPlayerListHeaderAndFooter(Component, Component)}
     */
    @Deprecated
    public void setPlayerListHeaderFooter(@Nullable BaseComponent[] header, @Nullable BaseComponent[] footer) {
        CraftPlayer player = (CraftPlayer) (Object) this;
        // Use existing player list header/footer methods
        if (header != null) {
            player.setPlayerListHeader(net.md_5.bungee.chat.ComponentSerializer.toString(header));
        }
        if (footer != null) {
            player.setPlayerListFooter(net.md_5.bungee.chat.ComponentSerializer.toString(footer));
        }
    }

    /**
     * Sets the header and footer of the player's player list
     *
     * @param header the header text, null to clear
     * @param footer the footer text, null to clear
     * @deprecated in favour of {@link #sendPlayerListHeaderAndFooter(Component, Component)}
     */
    @Deprecated
    public void setPlayerListHeaderFooter(@Nullable BaseComponent header, @Nullable BaseComponent footer) {
        CraftPlayer player = (CraftPlayer) (Object) this;
        // Use existing player list header/footer methods
        if (header != null) {
            player.setPlayerListHeader(header.toLegacyText());
        }
        if (footer != null) {
            player.setPlayerListFooter(footer.toLegacyText());
        }
    }

    // Paper API Patch 0027: Complete resource pack API

    /**
     * Request that the player's client download and switch resource packs.
     * <p>
     * The player's client will download the new resource pack asynchronously
     * in the background, and will automatically switch to it once the
     * download is complete. If the client has downloaded and cached the same
     * resource pack in the past, it will perform a file size/hash check
     * against the cached pack to determine if a download is necessary. When
     * this request is sent for the very first time from a given server, the
     * client will first display a confirmation GUI to the player before
     * proceeding with the download.
     * <p>
     * Notes:
     * <ul>
     * <li>Players can disable server resources on their client, in which
     * case this method will have no affect on them.
     * <li>There is no concept of resetting resource packs back to default
     * within Minecraft, so players will have to relog to do so.
     * </ul>
     *
     * @param url      The URL from which the client will download the resource pack. The string must contain only US-ASCII characters and should be encoded as per RFC 3986.
     * @param hash     A 40 character hexadecimal and lowercase SHA-1 digest of the resource pack file.
     * @param required Marks if the resource pack should be required by the client
     * @throws IllegalArgumentException Thrown if the URL is null.
     * @throws IllegalArgumentException Thrown if the URL is too long. The
     *                                  length restriction is an implementation specific arbitrary value.
     */
    public void setResourcePack(@NotNull String url, @NotNull String hash, boolean required) {
        setResourcePack(url, hash, required, null);
    }

    /**
     * Request that the player's client download and switch resource packs.
     * <p>
     * The player's client will download the new resource pack asynchronously
     * in the background, and will automatically switch to it once the
     * download is complete. If the client has downloaded and cached the same
     * resource pack in the past, it will perform a file size/hash check
     * against the cached pack to determine if a download is necessary. When
     * this request is sent for the very first time from a given server, the
     * client will first display a confirmation GUI to the player before
     * proceeding with the download.
     * <p>
     * Notes:
     * <ul>
     * <li>Players can disable server resources on their client, in which
     * case this method will have no affect on them.
     * <li>There is no concept of resetting resource packs back to default
     * within Minecraft, so players will have to relog to do so.
     * </ul>
     *
     * @param url                The URL from which the client will download the resource pack. The string must contain only US-ASCII characters and should be encoded as per RFC 3986.
     * @param hash               A 40 character hexadecimal and lowercase SHA-1 digest of the resource pack file.
     * @param required           Marks if the resource pack should be required by the client
     * @param resourcePackPrompt A Prompt to be displayed in the client request
     * @throws IllegalArgumentException Thrown if the URL is null.
     * @throws IllegalArgumentException Thrown if the URL is too long. The
     *                                  length restriction is an implementation specific arbitrary value.
     */
    public void setResourcePack(@NotNull String url, @NotNull String hash, boolean required, @Nullable net.kyori.adventure.text.Component resourcePackPrompt) {
        CraftPlayer player = (CraftPlayer) (Object) this;
        if (url == null) {
            throw new IllegalArgumentException("Resource pack URL cannot be null");
        }

        // Convert Adventure component to legacy text for compatibility
        String promptText = null;
        if (resourcePackPrompt != null) {
            promptText = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(resourcePackPrompt);
        }

        // Use the existing setResourcePack method with hash converted to byte array
        byte[] hashBytes = hash != null ? hash.getBytes(java.nio.charset.StandardCharsets.UTF_8) : null;
        player.setResourcePack(url, hashBytes);

        // Note: The 'required' and 'resourcePackPrompt' parameters would need additional
        // implementation in the underlying CraftPlayer to be fully functional.
        // This provides the API surface for future implementation.
    }
}
