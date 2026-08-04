package io.izzel.arclight.common.mixin.core.server.network;

import io.izzel.arclight.common.adventure.PaperAdventure;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.Set;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin_Paper {

    @Shadow
    public ServerPlayer player;

    @Inject(
        method = "broadcastChatMessage",
        at = @At("HEAD"),
        cancellable = true
    )
    private void arclight$handlePaperChat(
        PlayerChatMessage message,
        CallbackInfo ci
    ) {
        if (!message.hasSignature()) {
            return;
        }

        CraftPlayer craftPlayer =
            ((io.izzel.arclight.common.bridge.core.entity.player.ServerPlayerEntityBridge) this.player).bridge$getBukkitEntity();
        String content = message.signedContent();

        Component originalMessage = Component.text(content);
        Component processedMessage = originalMessage;

        SignedMessage signedMessage = PaperAdventure.createUnsignedMessage(
            content
        );

        Set<Audience> audiences = Set.copyOf(
            PaperAdventure.audiences(
                Bukkit.getOnlinePlayers()
                    .stream()
                    .map(p -> (org.bukkit.command.CommandSender) p)
                    .toList()
            )
        );

        AsyncChatEvent asyncChatEvent = new AsyncChatEvent(
            true,
            craftPlayer,
            audiences,
            ChatRenderer.defaultRenderer(),
            processedMessage,
            originalMessage,
            signedMessage
        );

        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getPluginManager().callEvent(asyncChatEvent);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(null, () -> {
                Bukkit.getPluginManager().callEvent(asyncChatEvent);
            });
        }

        if (asyncChatEvent.isCancelled()) {
            ci.cancel();
            return;
        }

        Component finalMessage = asyncChatEvent.message();
        if (!finalMessage.equals(originalMessage)) {
            // Message modified - would need packet update
        }
    }
}
