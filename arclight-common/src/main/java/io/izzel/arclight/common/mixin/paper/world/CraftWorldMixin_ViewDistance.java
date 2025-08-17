package io.izzel.arclight.common.mixin.paper.world;

import net.minecraft.server.level.ServerLevel;
import org.bukkit.craftbukkit.v.CraftWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Paper API patch 0017: Add view distance API
 * Adds view distance, simulation distance, no-tick view distance, and send view distance methods to CraftWorld
 */
@Mixin(value = CraftWorld.class, remap = false)
public abstract class CraftWorldMixin_ViewDistance {

    @Shadow
    public abstract ServerLevel getHandle();

    /**
     * Gets the view distance for this world
     *
     * @return the world's view distance
     */
    public int getViewDistance() {
        return getHandle().getServer().getPlayerList().getViewDistance();
    }

    /**
     * Sets the view distance for this world
     *
     * @param viewDistance the view distance to set
     */
    public void setViewDistance(int viewDistance) {
        // Set the world's view distance by updating the server's player list
        if (viewDistance < 2) viewDistance = 2;
        if (viewDistance > 32) viewDistance = 32;

        ServerLevel handle = getHandle();
        if (handle.getServer() != null) {
            // Update view distance for all players in this world
            handle.getServer().getPlayerList().setViewDistance(viewDistance);
        }
    }

    /**
     * Gets the simulation distance for this world
     *
     * @return the world's simulation distance
     */
    public int getSimulationDistance() {
        return getHandle().getServer().getPlayerList().getSimulationDistance();
    }

    /**
     * Sets the simulation distance for this world
     *
     * @param simulationDistance the simulation distance to set
     */
    public void setSimulationDistance(int simulationDistance) {
        // Set the world's simulation distance by updating the server's player list
        if (simulationDistance < 2) simulationDistance = 2;
        if (simulationDistance > 32) simulationDistance = 32;

        ServerLevel handle = getHandle();
        if (handle.getServer() != null) {
            // Update simulation distance for all players in this world
            handle.getServer().getPlayerList().setSimulationDistance(simulationDistance);
        }
    }

    /**
     * Gets the no-tick view distance for this world.
     * This is the distance at which chunks are sent to players but not ticked.
     *
     * @return the world's no-tick view distance
     */
    public int getNoTickViewDistance() {
        // For now, return the same as view distance since Minecraft doesn't have separate no-tick distance
        return getViewDistance();
    }

    /**
     * Sets the no-tick view distance for this world
     *
     * @param noTickViewDistance the no-tick view distance to set
     */
    public void setNoTickViewDistance(int noTickViewDistance) {
        // For now, this is a no-op since Minecraft doesn't have separate no-tick distance
        // In a full implementation, this would require custom chunk loading logic
    }

    /**
     * Gets the send view distance for this world.
     * This is the distance at which chunks are sent to players.
     *
     * @return the world's send view distance
     */
    public int getSendViewDistance() {
        return getViewDistance();
    }

    /**
     * Sets the send view distance for this world
     *
     * @param sendViewDistance the send view distance to set
     */
    public void setSendViewDistance(int sendViewDistance) {
        setViewDistance(sendViewDistance);
    }
}
