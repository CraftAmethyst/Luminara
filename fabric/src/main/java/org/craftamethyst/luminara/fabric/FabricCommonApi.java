package org.craftamethyst.luminara.fabric;

import io.izzel.arclight.api.ArclightServer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.ServerOperator;
import org.craftamethyst.luminara.common.mod.ArclightCommon;

/** Fabric implementation of the loader-neutral common services. */
public final class FabricCommonApi implements ArclightCommon.Api {

  @Override
  public boolean isModLoaded(String modid) {
    return FabricLoader.getInstance().isModLoaded(modid);
  }

  @Override
  public MinecraftServer getCurrentServer() {
    return org.craftamethyst.luminara.common.mod.server.ArclightServer.getMinecraftServer();
  }

  @Override
  public ArclightServer createArclightServer() {
    return new FabricArclightServer();
  }

  @Override
  public PermissibleBase createPermissible(ServerOperator operator) {
    return new PermissibleBase(operator);
  }
}
