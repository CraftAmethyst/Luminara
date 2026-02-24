package io.izzel.arclight.common.bridge.core.network.play;

import net.minecraft.world.entity.RelativeMovement;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Set;

public interface ServerPlayNetHandlerBridge {

    void bridge$pushTeleportCause(PlayerTeleportEvent.TeleportCause cause);

    void bridge$disconnect(String str);

    void bridge$teleport(Location dest);

    void bridge$teleport(double x, double y, double z, float yaw, float pitch, Set<RelativeMovement> relativeSet, PlayerTeleportEvent.TeleportCause cause);

    boolean bridge$processedDisconnect();

    boolean bridge$isDisconnected();

    int bridge$getLatency();
}
