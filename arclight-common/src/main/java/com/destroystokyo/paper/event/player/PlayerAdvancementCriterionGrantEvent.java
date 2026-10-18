package com.destroystokyo.paper.event.player;

import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player is granted a criterion in an advancement.
 */
public class PlayerAdvancementCriterionGrantEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    @NotNull
    private final Advancement advancement;
    @NotNull
    private final String criterion;
    private boolean cancelled;

    public PlayerAdvancementCriterionGrantEvent(
        @NotNull Player player,
        @NotNull Advancement advancement,
        @NotNull String criterion
    ) {
        super(player);
        this.advancement = advancement;
        this.criterion = criterion;
    }

    @NotNull
    public Advancement getAdvancement() {
        return this.advancement;
    }

    @NotNull
    public String getCriterion() {
        return this.criterion;
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
