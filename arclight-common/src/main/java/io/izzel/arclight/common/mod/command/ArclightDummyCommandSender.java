package io.izzel.arclight.common.mod.command;

import io.izzel.arclight.common.adventure.PaperAdventure;
import io.izzel.arclight.common.mod.permission.ArclightDummyPermissible;
import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v.util.CraftChatMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArclightDummyCommandSender
    extends ArclightDummyPermissible
    implements CommandSender, Audience
{

    public CommandSourceStack stack;
    public Spigot spigot;

    public ArclightDummyCommandSender(CommandSourceStack stack) {
        this.stack = stack;
    }

    @Override
    public void sendMessage(@NotNull String s) {
        // Enhanced message parsing to support Adventure formats
        try {
            Component adventureComponent = PaperAdventure.parseMessage(s);
            sendMessage(adventureComponent);
        } catch (Exception e) {
            // Fallback to original implementation
            for (var msg : CraftChatMessage.fromString(s)) {
                sendToAppropriateTarget(msg);
            }
        }
    }

    // Adventure Audience implementation
    @Override
    public void sendMessage(@NotNull Component message) {
        net.minecraft.network.chat.Component vanillaComponent =
            PaperAdventure.asVanilla(message);
        sendToAppropriateTarget(vanillaComponent);
    }

    private void sendToAppropriateTarget(
        net.minecraft.network.chat.Component message
    ) {
        try {
            // Try to get the player from the CommandSourceStack
            ServerPlayer player = stack.getPlayer();
            if (player != null) {
                player.sendSystemMessage(message);
            } else {
                stack.sendSystemMessage(message);
            }
        } catch (Exception e) {
            // If getPlayer() fails, fallback to system message
            stack.sendSystemMessage(message);
        }
    }

    @Override
    public void sendMessage(
        @NotNull Component message,
        net.kyori.adventure.audience.MessageType type
    ) {
        sendMessage(message);
    }

    @Override
    public void sendMessage(
        @NotNull Identity source,
        @NotNull Component message
    ) {
        sendMessage(message);
    }

    @Override
    public void sendMessage(
        @NotNull Identity source,
        @NotNull Component message,
        net.kyori.adventure.audience.MessageType type
    ) {
        sendMessage(message);
    }

    public @NotNull Identity identity() {
        return Identity.identity(UUID.nameUUIDFromBytes(getName().getBytes()));
    }

    @Override
    public void sendMessage(@NotNull String... strings) {
        for (var raw : strings) {
            sendMessage(raw);
        }
    }

    @Override
    public void sendMessage(@Nullable UUID uuid, @NotNull String s) {
        sendMessage(s);
    }

    @Override
    public void sendMessage(@Nullable UUID uuid, @NotNull String... strings) {
        sendMessage(strings);
    }

    @Override
    public @NotNull Server getServer() {
        // TODO: Use stack.getServer().bridge$getServer() after PR #1724 is merged
        return Bukkit.getServer();
    }

    @Override
    public @NotNull String getName() {
        return stack.getTextName();
    }

    @Override
    public @NotNull Spigot spigot() {
        if (spigot != null) {
            return spigot;
        }
        return (spigot = new Spigot());
    }

    public class Spigot extends CommandSender.Spigot {

        @Override
        public void sendMessage(@NotNull BaseComponent... components) {
            for (var raw : components) {
                sendMessage(raw);
            }
        }

        @Override
        public void sendMessage(
            @Nullable UUID sender,
            @NotNull BaseComponent component
        ) {
            sendMessage(component);
        }

        @Override
        public void sendMessage(
            @Nullable UUID sender,
            @NotNull BaseComponent... components
        ) {
            sendMessage(components);
        }

        @Override
        public void sendMessage(@NotNull BaseComponent component) {
            try {
                // Enhanced BaseComponent handling for better MineDown support
                var json = ComponentSerializer.toString(component);
                var result =
                    net.minecraft.network.chat.Component.Serializer.fromJson(
                        json
                    );
                if (result != null) {
                    sendToAppropriateTarget(result);
                } else {
                    // Fallback to plain text if JSON parsing fails
                    String plainText = component.toPlainText();
                    Component adventureComponent = PaperAdventure.parseMessage(
                        plainText
                    );
                    ArclightDummyCommandSender.this.sendMessage(
                        adventureComponent
                    );
                }
            } catch (Exception e) {
                // Final fallback to plain text
                String plainText = component.toPlainText();
                sendToAppropriateTarget(
                    net.minecraft.network.chat.Component.literal(plainText)
                );
            }
        }
    }
}
