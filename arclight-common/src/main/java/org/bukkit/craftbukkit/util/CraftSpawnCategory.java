package org.bukkit.craftbukkit.util;

import net.minecraft.world.entity.MobCategory;
import org.bukkit.entity.SpawnCategory;

/**
 * Unversioned CraftBukkit package alias used by reflection-based Paper plugins.
 */
public final class CraftSpawnCategory {

    private CraftSpawnCategory() {
    }

    public static SpawnCategory toBukkit(MobCategory category) {
        return org.bukkit.craftbukkit.v.util.CraftSpawnCategory.toBukkit(category);
    }
}
