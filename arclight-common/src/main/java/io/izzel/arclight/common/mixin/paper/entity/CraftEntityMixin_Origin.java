package io.izzel.arclight.common.mixin.paper.entity;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import net.minecraft.world.entity.Entity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Paper API patch 0015: Entity Origin API
 * Adds getOrigin() method to CraftEntity to track where entities originated from
 */
@Mixin(value = CraftEntity.class, remap = false)
public class CraftEntityMixin_Origin {

    @Shadow
    protected Entity entity;

    /**
     * Gets the location where this entity originates from.
     * <p>
     * This value can be null if the entity hasn't yet been added to the world.
     *
     * @return Location where entity originates or null if not yet added
     */
    @Nullable
    public Location getOrigin() {
        // Use the bridge method to get the entity origin
        return ((EntityBridge) entity).bridge$getOrigin();
    }
}
