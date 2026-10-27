package org.craftamethyst.luminara.common.mod;

import io.izzel.arclight.api.ArclightServer;
import net.minecraft.server.MinecraftServer;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.ServerOperator;

/**
 * Loader-neutral access point for services that differ between Forge and Fabric.
 *
 * <p>The loader entry point installs the platform implementation before any common mixin or
 * service is used. Common code must call this class instead of touching loader APIs directly.
 */
public final class ArclightCommon {

  private static volatile Api instance;

  private ArclightCommon() {}

  public static Api api() {
    Api current = instance;
    if (current == null) {
      throw new IllegalStateException("Luminara platform services have not been installed");
    }
    return current;
  }

  public static Api instance() {
    return instance;
  }

  public static void install(Api implementation) {
    instance = implementation;
  }

  public static boolean isModLoaded(String modid) {
    return api().isModLoaded(modid);
  }

  public interface Api {

    boolean isModLoaded(String modid);

    MinecraftServer getCurrentServer();

    ArclightServer createArclightServer();

    PermissibleBase createPermissible(ServerOperator operator);

    default void registerCrashCallables() {}

    default void unlockRegistries() {}

    default void lockRegistries() {}

    default byte[] remapPlatformClass(byte[] bytes) {
      return bytes;
    }
  }
}
