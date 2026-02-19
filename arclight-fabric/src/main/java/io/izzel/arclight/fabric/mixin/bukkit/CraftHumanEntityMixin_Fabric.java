package io.izzel.arclight.fabric.mixin.bukkit;

import org.bukkit.craftbukkit.v.entity.CraftHumanEntity;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.ServerOperator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CraftHumanEntity.class, remap = false)
public abstract class CraftHumanEntityMixin_Fabric {

    @Redirect(
            method = "<init>",
            at = @At(value = "NEW", target = "(Lorg/bukkit/permissions/ServerOperator;)Lorg/bukkit/permissions/PermissibleBase;"),
            require = 0
    )
    private PermissibleBase luminara$fabricPermissible(ServerOperator opable) {
        return new PermissibleBase(opable);
    }
}
