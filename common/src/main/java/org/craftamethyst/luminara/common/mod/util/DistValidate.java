package org.craftamethyst.luminara.common.mod.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.craftamethyst.luminara.common.mod.util.log.LuminaraLog;
import org.craftamethyst.luminara.i18n.ArclightConfig;

public class DistValidate {

  private static final Marker MARKER = MarkerManager.getMarker("EXT_LOGIC");
  private static final Map<Class<?>, Boolean> SEEN_CLASSES = new ConcurrentHashMap<>();

  public static boolean isValid(UseOnContext context) {
    return context != null && isValid(context.getLevel());
  }

  public static boolean isValid(LevelAccessor level) {
    return level != null && !level.isClientSide() && isLogicWorld(level);
  }

  public static boolean isValid(BlockGetter getter) {
    return getter instanceof LevelAccessor level && isValid(level);
  }

  private static boolean isLogicWorld(LevelAccessor level) {
    var cl = level.getClass();
    return (cl == ServerLevel.class || cl == WorldGenRegion.class || isLogicWorld(cl));
  }

  private static boolean isLogicWorld(Class<?> cl) {
    return SEEN_CLASSES.computeIfAbsent(
        cl,
        c -> {
          var name = c.getName();
          var result =
              ArclightConfig.spec().getCompat().getExtraLogicWorlds().contains(cl.getName());
          LuminaraLog.LOGGER.warn(MARKER, "dist.logic-world-check", name, result);
          return result;
        });
  }
}
