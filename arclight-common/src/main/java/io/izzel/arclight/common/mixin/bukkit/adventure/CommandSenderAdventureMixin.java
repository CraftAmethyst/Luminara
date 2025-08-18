package io.izzel.arclight.common.mixin.bukkit.adventure;

import io.izzel.arclight.common.adventure.PaperAdventure;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = CommandSender.class, remap = false)
public interface CommandSenderAdventureMixin extends Audience {

    @Override
    default void sendMessage(@NotNull Component message) {
        CommandSender sender = (CommandSender) this;
        // Enhanced message handling for better format preservation
        try {
            // If this is an ArclightDummyCommandSender, it has its own Adventure implementation
            if (sender instanceof io.izzel.arclight.common.mod.command.ArclightDummyCommandSender) {
                ((io.izzel.arclight.common.mod.command.ArclightDummyCommandSender) sender).sendMessage(message);
                return;
            }
            // Convert Adventure Component to legacy string and send
            String legacyMessage = PaperAdventure.adventureToLegacy(message);
            sender.sendMessage(legacyMessage);
        } catch (Exception e) {
            // Fallback to plain text
            String plainText = PaperAdventure.asPlain(message);
            sender.sendMessage(plainText);
        }
    }

    @Override
    default void sendMessage(@NotNull Component message, net.kyori.adventure.audience.MessageType type) {
        sendMessage(message);
    }

    @Override
    default void sendMessage(@NotNull Identity source, @NotNull Component message) {
        sendMessage(message);
    }

    @Override
    default void sendMessage(@NotNull Identity source, @NotNull Component message, net.kyori.adventure.audience.MessageType type) {
        sendMessage(message);
    }

    // Paper API Patch 0023: Add BaseComponent sendMessage methods to CommandSender

    /**
     * Sends the component to the sender
     *
     * <p>If this sender does not support sending full components then
     * the component will be sent as legacy text.</p>
     *
     * @param component the component to send
     * @deprecated use {@link #sendMessage(Identity, Component, net.kyori.adventure.audience.MessageType)} instead
     */
    @Deprecated
    default void sendMessage(@NotNull BaseComponent component) {
        CommandSender sender = (CommandSender) this;
        sender.sendMessage(component.toLegacyText());
    }

    /**
     * Sends an array of components as a single message to the sender
     *
     * <p>If this sender does not support sending full components then
     * the components will be sent as legacy text.</p>
     *
     * @param components the components to send
     * @deprecated use {@link #sendMessage(Identity, Component, net.kyori.adventure.audience.MessageType)} instead
     */
    @Deprecated
    default void sendMessage(@NotNull BaseComponent... components) {
        CommandSender sender = (CommandSender) this;
        sender.sendMessage(new TextComponent(components).toLegacyText());
    }
}
