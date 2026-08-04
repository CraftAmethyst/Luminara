package io.izzel.arclight.common.mixin.bukkit.adventure;

import io.izzel.arclight.common.bridge.core.entity.player.PlayerEntityBridge;
import io.izzel.arclight.common.bridge.core.entity.player.ServerPlayerEntityBridge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
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
        return player.getHandle() instanceof
            net.minecraftforge.common.util.FakePlayer;
    }

    // Paper's playerListName methods
    @Nullable
    public Component playerListName() {
        CraftPlayer player = (CraftPlayer) (Object) this;
        String listName = player.getPlayerListName();
        return listName != null
            ? net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(
                  listName
              )
            : null;
    }

    public void playerListName(@Nullable Component name) {
        CraftPlayer player = (CraftPlayer) (Object) this;
        if (name == null) {
            player.setPlayerListName(null);
        } else {
            String legacy =
                net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(
                    name
                );
            player.setPlayerListName(legacy);
        }
    }

    // Additional Paper Player methods that plugins might expect
    public void kick(@NotNull Component message) {
        CraftPlayer player = (CraftPlayer) (Object) this;
        String legacy =
            net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(
                message
            );
        player.kickPlayer(legacy);
    }

    public void sendRichMessage(@NotNull String message) {
        CraftPlayer player = (CraftPlayer) (Object) this;
        Component component =
            net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(
                message
            );
        if (player instanceof net.kyori.adventure.audience.Audience audience) {
            audience.sendMessage(component);
        }
    }
}
