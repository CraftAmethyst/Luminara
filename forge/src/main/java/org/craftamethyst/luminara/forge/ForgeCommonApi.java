package org.craftamethyst.luminara.forge;

import com.google.common.collect.ImmutableSet;
import io.izzel.arclight.api.ArclightServer;
import java.util.Set;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.CrashReportCallables;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.bukkit.craftbukkit.v.CraftCrashReport;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.ServerOperator;
import org.craftamethyst.luminara.common.mod.ArclightCommon;
import org.craftamethyst.luminara.common.mod.server.ArclightForgePermissible;
import org.craftamethyst.luminara.common.mod.server.api.DefaultArclightServer;

/** Forge implementation of the loader-neutral common services. */
public final class ForgeCommonApi implements ArclightCommon.Api {

  private static Set<IForgeRegistry<?>> registries() {
    return ImmutableSet.of(
        ForgeRegistries.BLOCKS,
        ForgeRegistries.ITEMS,
        ForgeRegistries.MOB_EFFECTS,
        ForgeRegistries.POTIONS,
        ForgeRegistries.ENTITY_TYPES,
        ForgeRegistries.BLOCK_ENTITY_TYPES,
        ForgeRegistries.BIOMES);
  }

  @Override
  public boolean isModLoaded(String modid) {
    return ModList.get() != null && ModList.get().isLoaded(modid);
  }

  @Override
  public MinecraftServer getCurrentServer() {
    return ServerLifecycleHooks.getCurrentServer();
  }

  @Override
  public ArclightServer createArclightServer() {
    return new DefaultArclightServer();
  }

  @Override
  public PermissibleBase createPermissible(ServerOperator operator) {
    return new ArclightForgePermissible(operator);
  }

  @Override
  public void registerCrashCallables() {
    CrashReportCallables.registerCrashCallable("Arclight", new CraftCrashReport());
  }

  @Override
  public void unlockRegistries() {
    for (IForgeRegistry<?> registry : registries()) {
      if (registry instanceof ForgeRegistry<?> forgeRegistry) {
        forgeRegistry.unfreeze();
      }
    }
  }

  @Override
  public void lockRegistries() {
    for (IForgeRegistry<?> registry : registries()) {
      if (registry instanceof ForgeRegistry<?> forgeRegistry) {
        forgeRegistry.freeze();
      }
    }
  }
}
