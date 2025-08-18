package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Paper API Patch 0031: Custom replacement for eaten items
 * Adds replacement item functionality to PlayerItemConsumeEvent
 */
@Mixin(value = PlayerItemConsumeEvent.class, remap = false)
public class PlayerItemConsumeEventMixin {

    @Unique
    private ItemStack arclight$replacement = null;

    /**
     * Get the replacement item that will be given to the player when the consumed item is finished.
     *
     * @return the replacement item, or null if no custom replacement is set
     */
    @Nullable
    public ItemStack getReplacement() {
        return this.arclight$replacement;
    }

    /**
     * Set a custom item to be given to the player when the consumed item is finished.
     * Replaces the empty bucket, bottle, etc. that would normally be returned.
     *
     * @param replacement the item to replace the default replacement, or null for no replacement
     */
    public void setReplacement(@Nullable ItemStack replacement) {
        this.arclight$replacement = replacement;
    }
}
