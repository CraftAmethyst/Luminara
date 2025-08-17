package io.izzel.arclight.common.mixin.paper.player;

import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Paper API patch 0012: Player affects spawning API
 * Adds getAffectsSpawning() and setAffectsSpawning() methods to CraftPlayer
 */
@Mixin(value = CraftPlayer.class, remap = false)
public abstract class CraftPlayerMixin_AffectsSpawning {

    @Shadow
    public abstract ServerPlayer getHandle();

    /**
     * Get whether the player can affect mob spawning
     *
     * @return if the player can affect mob spawning
     */
    public boolean getAffectsSpawning() {
        return ((io.izzel.arclight.common.bridge.core.entity.player.ServerPlayerEntityBridge) getHandle()).bridge$getAffectsSpawning();
    }

    /**
     * Set whether the player can affect mob spawning
     *
     * @param affects Whether the player can affect mob spawning
     */
    public void setAffectsSpawning(boolean affects) {
        ((io.izzel.arclight.common.bridge.core.entity.player.ServerPlayerEntityBridge) getHandle()).bridge$setAffectsSpawning(affects);
    }
}
