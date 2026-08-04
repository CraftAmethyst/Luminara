package io.izzel.arclight.common.bridge.core.entity.player;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.bukkit.event.player.PlayerSpawnChangeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public interface ServerPlayerEntityBridge extends PlayerEntityBridge {
    @Override
    CraftPlayer bridge$getBukkitEntity();

    void bridge$pushChangeDimensionCause(
        PlayerTeleportEvent.TeleportCause cause
    );

    void bridge$pushChangeSpawnCause(PlayerSpawnChangeEvent.Cause cause);

    Optional<PlayerTeleportEvent.TeleportCause> bridge$getTeleportCause();

    BlockPos bridge$getSpawnPoint(ServerLevel world);

    boolean bridge$isMovementBlocked();

    void bridge$setCompassTarget(Location location);

    boolean bridge$isJoining();

    void bridge$reset();

    Entity bridge$changeDimension(
        ServerLevel world,
        PlayerTeleportEvent.TeleportCause cause
    );

    boolean bridge$initialized();

    boolean bridge$isTrackerDirty();

    void bridge$setTrackerDirty(boolean flag);

    void bridge$sendActionBar(net.kyori.adventure.text.Component message);

    void bridge$sendTitle(net.kyori.adventure.title.Title title);

    int bridge$getPing();

    void bridge$updateCommands();
}
