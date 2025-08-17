package com.destroystokyo.paper.event.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Paper API patch 0019: Add PlayerInitialSpawnEvent
 * Called when a player is about to spawn in a world for the first time.
 * <p>
 * <b>This event is deprecated and will be removed in a future version.</b>
 * Use PlayerJoinEvent or similar events instead.
 */
@Deprecated
public class PlayerInitialSpawnEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private Location spawnLocation;

    public PlayerInitialSpawnEvent(@NotNull Player who, @NotNull Location spawnLocation) {
        super(who);
        this.spawnLocation = spawnLocation;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Gets the location where the player will spawn
     *
     * @return the spawn location
     */
    @NotNull
    public Location getSpawnLocation() {
        return spawnLocation;
    }

    /**
     * Sets the location where the player will spawn
     *
     * @param spawnLocation the new spawn location
     */
    public void setSpawnLocation(@NotNull Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
