package io.izzel.arclight.common.mixin.paper.player;

import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Paper API patch 0017: Add view distance API
 * Adds view distance, simulation distance, no-tick view distance, and send view distance methods to CraftPlayer
 */
@Mixin(value = CraftPlayer.class, remap = false)
public abstract class CraftPlayerMixin_ViewDistance {

    @Shadow
    public abstract ServerPlayer getHandle();

    /**
     * Gets the view distance for this player
     *
     * @return the player's view distance
     */
    public int getViewDistance() {
        return getHandle().server.getPlayerList().getViewDistance();
    }

    /**
     * Sets the view distance for this player
     *
     * @param viewDistance the view distance to set
     */
    public void setViewDistance(int viewDistance) {
        // Set the player's view distance by updating the connection's view distance
        if (viewDistance < 2) viewDistance = 2;
        if (viewDistance > 32) viewDistance = 32;

        ServerPlayer handle = getHandle();
        if (handle.connection != null) {
            // Update the player's view distance in the chunk sender
            handle.connection.send(new net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket(viewDistance));
        }
    }

    /**
     * Gets the simulation distance for this player
     *
     * @return the player's simulation distance
     */
    public int getSimulationDistance() {
        return getHandle().server.getPlayerList().getSimulationDistance();
    }

    /**
     * Sets the simulation distance for this player
     *
     * @param simulationDistance the simulation distance to set
     */
    public void setSimulationDistance(int simulationDistance) {
        // Set the player's simulation distance by updating the connection's simulation distance
        if (simulationDistance < 2) simulationDistance = 2;
        if (simulationDistance > 32) simulationDistance = 32;

        ServerPlayer handle = getHandle();
        if (handle.connection != null) {
            // Update the player's simulation distance in the chunk sender
            handle.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket(simulationDistance));
        }
    }

    /**
     * Gets the no-tick view distance for this player.
     * This is the distance at which chunks are sent to the player but not ticked.
     *
     * @return the player's no-tick view distance
     */
    public int getNoTickViewDistance() {
        // For now, return the same as view distance since Minecraft doesn't have separate no-tick distance
        return getViewDistance();
    }

    /**
     * Sets the no-tick view distance for this player
     *
     * @param noTickViewDistance the no-tick view distance to set
     */
    public void setNoTickViewDistance(int noTickViewDistance) {
        // For now, this is a no-op since Minecraft doesn't have separate no-tick distance
        // In a full implementation, this would require custom chunk loading logic
    }

    /**
     * Gets the send view distance for this player.
     * This is the distance at which chunks are sent to the player.
     *
     * @return the player's send view distance
     */
    public int getSendViewDistance() {
        return getViewDistance();
    }

    /**
     * Sets the send view distance for this player
     *
     * @param sendViewDistance the send view distance to set
     */
    public void setSendViewDistance(int sendViewDistance) {
        setViewDistance(sendViewDistance);
    }
}
