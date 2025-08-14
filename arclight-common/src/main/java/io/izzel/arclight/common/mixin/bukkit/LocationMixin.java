package io.izzel.arclight.common.mixin.bukkit;

import io.papermc.paper.math.FinePosition;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Location.class, remap = false)
public abstract class LocationMixin implements FinePosition {

    @Shadow public abstract double getX();
    @Shadow public abstract double getY();
    @Shadow public abstract double getZ();
    @Shadow public abstract float getYaw();
    @Shadow public abstract float getPitch();

    // Paper - add Position
    @Override
    public double x() {
        return this.getX();
    }

    @Override
    public double y() {
        return this.getY();
    }

    @Override
    public double z() {
        return this.getZ();
    }

    @Override
    public @NotNull Location toLocation(@NotNull World world) {
        return new Location(world, this.x(), this.y(), this.z(), this.getYaw(), this.getPitch());
    }
    // Paper end
}
