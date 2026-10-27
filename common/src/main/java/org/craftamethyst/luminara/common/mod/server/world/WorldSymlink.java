package org.craftamethyst.luminara.common.mod.server.world;

import org.craftamethyst.luminara.common.mod.util.log.LuminaraLog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.world.level.storage.DerivedLevelData;
import org.bukkit.Bukkit;

public class WorldSymlink {

  public static void create(DerivedLevelData worldInfo, File dimensionFolder) {
    String name = worldInfo.getLevelName();
    Path source = new File(Bukkit.getWorldContainer(), name).toPath();
    Path dest = dimensionFolder.toPath();
    try {
      if (!Files.isSymbolicLink(source)) {
        if (Files.exists(source)) {
          LuminaraLog.LOGGER.warn("symlink-file-exist", source);
          return;
        }
        Files.createSymbolicLink(source, dest);
      }
    } catch (UnsupportedOperationException e) {
      LuminaraLog.LOGGER.warn("error-symlink", e);
    } catch (IOException e) {
      LuminaraLog.LOGGER.error("symlink.create-error", e);
    }
  }
}
