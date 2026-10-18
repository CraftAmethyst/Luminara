package com.destroystokyo.paper.event.entity;

import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called before a creature is created for selected spawn paths.
 */
public class PreCreatureSpawnEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    @NotNull
    private final Location location;
    @NotNull
    private final EntityType type;
    @NotNull
    private final CreatureSpawnEvent.SpawnReason reason;
    private boolean shouldAbortSpawn;
    private boolean cancelled;

    public PreCreatureSpawnEvent(
        @NotNull Location location,
        @NotNull EntityType type,
        @NotNull CreatureSpawnEvent.SpawnReason reason
    ) {
        this.location = Preconditions.checkNotNull(location, "Location may not be null");
        this.type = Preconditions.checkNotNull(type, "Type may not be null");
        this.reason = Preconditions.checkNotNull(reason, "Reason may not be null");
    }

    @NotNull
    public Location getSpawnLocation() {
        return this.location;
    }

    @NotNull
    public EntityType getType() {
        return this.type;
    }

    @NotNull
    public CreatureSpawnEvent.SpawnReason getReason() {
        return this.reason;
    }

    public boolean shouldAbortSpawn() {
        return this.shouldAbortSpawn;
    }

    public void setShouldAbortSpawn(boolean shouldAbortSpawn) {
        this.shouldAbortSpawn = shouldAbortSpawn;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
