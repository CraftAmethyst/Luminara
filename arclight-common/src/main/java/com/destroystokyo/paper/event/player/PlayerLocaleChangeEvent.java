package com.destroystokyo.paper.event.player;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Paper API patch 0016: Add PlayerLocaleChangeEvent
 * Called when a player changes their locale in the client settings.
 * <p>
 * <b>This event is deprecated and will be removed in a future version.</b>
 * Use {@link org.bukkit.event.player.PlayerLocaleChangeEvent} instead.
 */
@Deprecated
public class PlayerLocaleChangeEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final String oldLocale;
    private final String newLocale;

    public PlayerLocaleChangeEvent(@NotNull Player who, @NotNull String oldLocale, @NotNull String newLocale) {
        super(who);
        this.oldLocale = oldLocale;
        this.newLocale = newLocale;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Gets the locale the player switched from.
     *
     * @return the previous locale
     */
    @NotNull
    public String getOldLocale() {
        return oldLocale;
    }

    /**
     * Gets the locale the player is changed to.
     *
     * @return the new locale
     */
    @NotNull
    public String getNewLocale() {
        return newLocale;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
