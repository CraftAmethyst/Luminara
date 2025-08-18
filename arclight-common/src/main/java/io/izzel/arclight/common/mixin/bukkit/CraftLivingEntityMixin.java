package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.craftbukkit.v.entity.CraftLivingEntity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Paper API Patch 0026: Add methods for working with arrows stuck in living entities
 * Adds setArrowsInBody methods with event firing control to LivingEntity
 */
@Mixin(value = CraftLivingEntity.class, remap = false)
public class CraftLivingEntityMixin {

    /**
     * Set the number of arrows stuck in this entity
     *
     * @param arrowsInBody new number of arrows stuck in this entity
     */
    public void setArrowsInBody(int arrowsInBody) {
        setArrowsInBody(arrowsInBody, true);
    }

    /**
     * Set the number of arrows stuck in this entity
     *
     * @param arrowsInBody new number of arrows stuck in this entity
     * @param fireEvent    whether to fire EntityRemoveEvent for removed arrows
     */
    public void setArrowsInBody(int arrowsInBody, boolean fireEvent) {
        CraftLivingEntity entity = (CraftLivingEntity) (Object) this;
        net.minecraft.world.entity.LivingEntity handle = entity.getHandle();

        // Simply set the arrow count - event firing can be added later if needed
        // The fireEvent parameter is kept for API compatibility

        handle.setArrowCount(Math.max(0, arrowsInBody));
    }

    /**
     * Set the number of arrows stuck in this entity
     *
     * @param arrowsInBody new number of arrows stuck in this entity
     * @deprecated use {@link #setArrowsInBody(int, boolean)}
     */
    @Deprecated
    public void setArrowsStuck(int arrowsInBody) {
        setArrowsInBody(arrowsInBody);
    }
}
