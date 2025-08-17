package com.destroystokyo.paper.event.block;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Paper API patch 0018: Add BeaconEffectEvent
 * Called when a beacon applies an effect to a player.
 * <p>
 * Cancelling this event will prevent the beacon effect from being applied to the player.
 */
public class BeaconEffectEvent extends BlockEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Collection<Player> players;
    private PotionEffect effect;
    private boolean primary;
    private boolean cancelled;

    public BeaconEffectEvent(@NotNull Block block, @NotNull PotionEffect effect, @NotNull Collection<Player> players, boolean primary) {
        super(block);
        this.effect = effect;
        this.players = players;
        this.primary = primary;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Get the potion effect being applied by the beacon.
     *
     * @return Potion effect
     */
    @NotNull
    public PotionEffect getEffect() {
        return effect;
    }

    /**
     * Sets the potion effect that will be applied by the beacon.
     *
     * @param effect Potion effect
     */
    public void setEffect(@NotNull PotionEffect effect) {
        this.effect = effect;
    }

    /**
     * Gets the list of players being affected by the beacon.
     *
     * @return List of affected players
     */
    @NotNull
    public Collection<Player> getPlayers() {
        return players;
    }

    /**
     * @return {@code true} if this is a primary effect of the beacon, {@code false} otherwise.
     */
    public boolean isPrimary() {
        return primary;
    }

    /**
     * Set whether this is a primary effect of the beacon.
     *
     * @param primary {@code true} if this is a primary effect of the beacon, {@code false} otherwise.
     */
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
