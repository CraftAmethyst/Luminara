package io.izzel.arclight.common.bridge.core.world;

import java.util.Set;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;

public interface ServerEntityBridge {
    void bridge$setTrackedPlayers(Set<ServerPlayerConnection> trackedPlayers);

    Entity bridge$getTrackingEntity();

    boolean bridge$syncPosition();

    boolean bridge$instantSyncPosition();

    boolean bridge$instantSyncMotion();
}
