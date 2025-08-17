package io.izzel.arclight.common.mixin.paper.entity;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.entity.CraftTNTPrimed;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Paper API patch 0015: Entity Origin API
 * Adds deprecated getSourceLoc() method to CraftTNTPrimed for backward compatibility
 */
@Mixin(value = CraftTNTPrimed.class, remap = false)
public class CraftTNTPrimedMixin_SourceLoc {

    /**
     * @deprecated Use {@link org.bukkit.entity.Entity#getOrigin()}
     */
    @Deprecated
    @Nullable
    public Location getSourceLoc() {
        // Call the getOrigin method that will be provided by CraftEntityMixin_Origin
        // Since CraftTNTPrimed extends CraftEntity, it will have the getOrigin method
        CraftTNTPrimed self = (CraftTNTPrimed) (Object) this;
        return ((EntityBridge) self.getHandle()).bridge$getOrigin();
    }
}
