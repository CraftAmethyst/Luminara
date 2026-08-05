package org.bukkit.event.player;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when a player attempts to pick an item up from the ground.
 */
public class PlayerAttemptPickupItemEvent
    extends PlayerEvent
    implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Item item;
    private final int remaining;
    private boolean flyAtPlayer = true;
    private boolean cancelled;

    /**
     * @deprecated use {@link #PlayerAttemptPickupItemEvent(Player, Item, int)}
     */
    @Deprecated
    public PlayerAttemptPickupItemEvent(
        @NotNull Player player,
        @NotNull Item item
    ) {
        this(player, item, 0);
    }

    public PlayerAttemptPickupItemEvent(
        @NotNull Player player,
        @NotNull Item item,
        int remaining
    ) {
        super(player);
        this.item = item;
        this.remaining = remaining;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Gets the item the player attempted to pick up.
     *
     * @return attempted item
     */
    @NotNull
    public Item getItem() {
        return this.item;
    }

    /**
     * Gets the amount that will remain on the ground if pickup succeeds.
     *
     * @return remaining item count
     */
    public int getRemaining() {
        return this.remaining;
    }

    /**
     * Gets whether the item pickup animation is sent to the player when this
     * event prevents pickup.
     *
     * @return whether the item should fly at the player
     */
    public boolean getFlyAtPlayer() {
        return this.flyAtPlayer;
    }

    /**
     * Sets whether the item pickup animation is sent to the player when this
     * event prevents pickup.
     *
     * @param flyAtPlayer whether the item should fly at the player
     */
    public void setFlyAtPlayer(boolean flyAtPlayer) {
        this.flyAtPlayer = flyAtPlayer;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        this.flyAtPlayer = !cancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
