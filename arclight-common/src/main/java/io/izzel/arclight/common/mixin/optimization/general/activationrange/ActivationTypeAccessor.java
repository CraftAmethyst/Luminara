package io.izzel.arclight.common.mixin.optimization.general.activationrange;

import net.minecraft.world.phys.AABB;
import org.spigotmc.ActivationRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ActivationRange.ActivationType.class, remap = false)
public interface ActivationTypeAccessor {
    @Accessor("boundingBox")
    AABB arclight$getBoundingBox();
}
