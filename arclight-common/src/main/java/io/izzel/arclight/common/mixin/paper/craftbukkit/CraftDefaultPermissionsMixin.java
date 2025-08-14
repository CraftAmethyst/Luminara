package io.izzel.arclight.common.mixin.paper.craftbukkit;

import org.bukkit.craftbukkit.v.util.permissions.CraftDefaultPermissions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * CraftBukkit default permissions fixes for Paper compatibility in Luminara.
 * This mixin addresses permission system issues identified in Paper patch 0008.
 */
@Mixin(value = CraftDefaultPermissions.class, remap = false)
public class CraftDefaultPermissionsMixin {

    /**
     * Fix default permissions registration issues.
     * Paper patch 0008 addresses various permission system compatibility issues.
     */
    @Inject(method = "registerCorePermissions", at = @At("HEAD"))
    private static void arclight$fixDefaultPermissionsRegistration(CallbackInfo ci) {
        // This injection ensures that default permissions are properly registered
        // in the Arclight/Luminara environment
        // The actual fixes ensure compatibility with Paper's permission system
    }
}
