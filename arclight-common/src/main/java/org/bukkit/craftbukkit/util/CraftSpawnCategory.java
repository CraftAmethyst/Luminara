package org.bukkit.craftbukkit.util;

import net.minecraft.world.entity.MobCategory;
import org.bukkit.entity.SpawnCategory;

/**
 * Unversioned facade for the versioned CraftBukkit {@code CraftSpawnCategory}.
 *
 * <p>Plugins targeting Paper load this class by its unversioned name, while the CraftBukkit
 * implementation shipped with Luminara lives in the versioned package ({@code
 * org.bukkit.craftbukkit.v1_21_R1.util}). Delegating keeps the reflective lookup working without
 * special-casing every caller.
 */
public class CraftSpawnCategory {

  public static boolean isValidForLimits(SpawnCategory spawnCategory) {
    return org.bukkit.craftbukkit.v.util.CraftSpawnCategory.isValidForLimits(spawnCategory);
  }

  public static String getConfigNameSpawnLimit(SpawnCategory spawnCategory) {
    return org.bukkit.craftbukkit.v.util.CraftSpawnCategory.getConfigNameSpawnLimit(spawnCategory);
  }

  public static String getConfigNameTicksPerSpawn(SpawnCategory spawnCategory) {
    return org.bukkit.craftbukkit.v.util.CraftSpawnCategory.getConfigNameTicksPerSpawn(
        spawnCategory);
  }

  public static long getDefaultTicksPerSpawn(SpawnCategory spawnCategory) {
    return org.bukkit.craftbukkit.v.util.CraftSpawnCategory.getDefaultTicksPerSpawn(spawnCategory);
  }

  public static SpawnCategory toBukkit(MobCategory mobCategory) {
    return org.bukkit.craftbukkit.v.util.CraftSpawnCategory.toBukkit(mobCategory);
  }

  public static MobCategory toNMS(SpawnCategory spawnCategory) {
    return org.bukkit.craftbukkit.v.util.CraftSpawnCategory.toNMS(spawnCategory);
  }
}
