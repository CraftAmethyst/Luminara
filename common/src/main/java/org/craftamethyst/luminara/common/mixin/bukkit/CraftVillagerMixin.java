package org.craftamethyst.luminara.common.mixin.bukkit;

import org.craftamethyst.luminara.common.mod.util.ResourceLocationUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.bukkit.craftbukkit.v.entity.CraftVillager;
import org.bukkit.entity.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = CraftVillager.class, remap = false)
public class CraftVillagerMixin {

  /**
   * @author IzzelAliz
   * @reason
   */
  @Overwrite
  public static Villager.Profession nmsToBukkitProfession(VillagerProfession nms) {
    return Villager.Profession.valueOf(
        ResourceLocationUtil.standardize(BuiltInRegistries.VILLAGER_PROFESSION.getKey(nms)));
  }
}
