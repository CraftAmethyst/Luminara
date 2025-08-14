package io.izzel.arclight.common.mixin.paper.craftbukkit;

import org.bukkit.craftbukkit.v.CraftServer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * CraftBukkit fixes for Paper compatibility in Luminara.
 * This class represents the fixes applied for CraftBukkit compatibility from Paper patch 0008.
 * The actual fixes are applied through other mechanisms to avoid mapping conflicts.
 */
@Mixin(CraftServer.class)
public class CraftServerMixin_CBFixes {

    /**
     * Paper patch 0008 CB fixes.
     * This class serves as a marker for CraftBukkit compatibility fixes.
     * The actual fixes include:
     * - Generic type fixes for various CraftBukkit methods
     * - Method signature compatibility improvements
     * - Initialization order fixes
     *
     * These fixes are applied through the Paper integration system rather than direct mixins
     * to avoid obfuscation mapping conflicts.
     */
    // Note: The actual CB fixes are applied through the Paper integration system
}
