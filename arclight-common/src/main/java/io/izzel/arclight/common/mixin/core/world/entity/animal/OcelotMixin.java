package io.izzel.arclight.common.mixin.core.world.entity.animal;

import net.minecraft.world.entity.animal.Ocelot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Ocelot.class)
public abstract class OcelotMixin extends AnimalMixin {

    public boolean spawnBonus = true;

    // @formatter:on

    // @formatter:off
    @Shadow abstract boolean isTrusting();
}
