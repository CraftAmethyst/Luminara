package org.bukkit.event.player;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerAttemptPickupItemEventTest {

    @Test
    void cancellationControlsPickupAnimationByDefault() {
        var event = new PlayerAttemptPickupItemEvent((Player) null, (Item) null, 3);

        assertEquals(3, event.getRemaining());
        assertTrue(event.getFlyAtPlayer());
        event.setCancelled(true);
        assertTrue(event.isCancelled());
        assertFalse(event.getFlyAtPlayer());
        event.setFlyAtPlayer(true);
        assertTrue(event.getFlyAtPlayer());
    }
}
