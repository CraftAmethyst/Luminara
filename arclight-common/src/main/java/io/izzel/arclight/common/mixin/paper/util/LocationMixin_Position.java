package io.izzel.arclight.common.mixin.paper.util;

import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.FinePosition;
import io.papermc.paper.math.Position;
import org.bukkit.Location;
import org.bukkit.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Mixin to provide additional Paper Position functionality to Location.
 * This works alongside LocationMixin (bukkit package) to provide complete
 * Position API support.
 * LocationMixin handles FinePosition implementation, this provides
 * BlockPosition utilities.
 */
@Mixin(value = Location.class, remap = false)
public abstract class LocationMixin_Position {

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract double getY();

    @Shadow
    public abstract double getZ();

    @Shadow
    public abstract World getWorld();

    @Shadow
    public abstract int getBlockX();

    @Shadow
    public abstract int getBlockY();

    @Shadow
    public abstract int getBlockZ();

    /**
     * Gets a BlockPosition view of this Location.
     * This provides BlockPosition functionality without implementing the interface
     * directly.
     *
     * @return a BlockPosition representing the block coordinates of this location
     */
    @Unique
    public BlockPosition asBlockPosition() {
        return Position.block(this.getBlockX(), this.getBlockY(), this.getBlockZ());
    }

    /**
     * Gets a FinePosition view of this Location.
     * This delegates to the existing FinePosition implementation from
     * LocationMixin.
     *
     * @return this location as a FinePosition
     */
    @Unique
    public FinePosition asFinePosition() {
        return (FinePosition) (Object) this;
    }

    /**
     * Creates a new Location offset by block coordinates.
     * This provides BlockPosition-style offset functionality.
     *
     * @param x block offset in x direction
     * @param y block offset in y direction
     * @param z block offset in z direction
     * @return new Location offset by the specified block amounts
     */
    @Unique
    public Location offsetByBlocks(int x, int y, int z) {
        if (x == 0 && y == 0 && z == 0) {
            return (Location) (Object) this;
        }
        Location location = (Location) (Object) this;
        return new Location(location.getWorld(),
                location.getX() + x,
                location.getY() + y,
                location.getZ() + z,
                location.getYaw(),
                location.getPitch());
    }

    /**
     * Checks if this location represents exact block coordinates.
     *
     * @return true if x, y, z are all integers (block coordinates)
     */
    @Unique
    public boolean isBlockCoordinates() {
        return this.getX() == this.getBlockX() &&
                this.getY() == this.getBlockY() &&
                this.getZ() == this.getBlockZ();
    }
}
