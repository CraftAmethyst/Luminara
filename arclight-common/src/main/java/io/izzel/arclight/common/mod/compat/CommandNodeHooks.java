package io.izzel.arclight.common.mod.compat;

import com.mojang.brigadier.tree.CommandNode;
import io.izzel.arclight.api.Unsafe;
import io.izzel.arclight.common.bridge.core.commands.CommandSourceStackBridge;
import java.lang.reflect.Field;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandNodeHooks {

  private static final Logger LOGGER = LogManager.getLogger("Arclight");

  private static final long CHILDREN, LITERALS, ARGUMENTS, CURRENT;
  private static final Object CURRENT_BASE;

  static {
    try {
      CHILDREN = Unsafe.objectFieldOffset(CommandNode.class.getDeclaredField("children"));
      LITERALS = Unsafe.objectFieldOffset(CommandNode.class.getDeclaredField("literals"));
      ARGUMENTS = Unsafe.objectFieldOffset(CommandNode.class.getDeclaredField("arguments"));
    } catch (Throwable t) {
      throw new IllegalStateException("Unable to resolve CommandNode child maps", t);
    }
    // CURRENT_COMMAND is only available where the class could be patched: the legacy
    // launcher redefines CommandNode, and the mod loaders add the field through Mixin.
    // NeoForge keeps Brigadier in the MC-BOOTSTRAP module layer, which Mixin does not
    // transform, so the field is absent there and the cache is simply unused.
    Object currentBase = null;
    long currentOffset = -1L;
    try {
      Field field = CommandNode.class.getDeclaredField("CURRENT_COMMAND");
      currentBase = Unsafe.staticFieldBase(field);
      currentOffset = Unsafe.staticFieldOffset(field);
    } catch (Throwable t) {
      LOGGER.debug(
          "CommandNode.CURRENT_COMMAND is not available; unwrapped requirement tests fall back"
              + " to the vanilla permission level");
    }
    CURRENT_BASE = currentBase;
    CURRENT = currentOffset;
  }

  @SuppressWarnings("unchecked")
  public static void removeCommand(CommandNode<?> node, String command) {
    ((Map<String, ?>) Unsafe.getObject(node, CHILDREN)).remove(command);
    ((Map<String, ?>) Unsafe.getObject(node, LITERALS)).remove(command);
    ((Map<String, ?>) Unsafe.getObject(node, ARGUMENTS)).remove(command);
  }

  public static CommandNode<?> getCurrent() {
    if (CURRENT_BASE == null) {
      return null;
    }
    return (CommandNode<?>) Unsafe.getObjectVolatile(CURRENT_BASE, CURRENT);
  }

  public static <S> boolean canUse(CommandNode<S> node, S source) {
    if (source instanceof CommandSourceStackBridge s) {
      try {
        s.bridge$setCurrentCommand(node);
        return node.canUse(source);
      } finally {
        s.bridge$setCurrentCommand(null);
      }
    } else {
      return node.canUse(source);
    }
  }
}
