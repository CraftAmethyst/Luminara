package io.izzel.arclight.common.mod.compat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.junit.jupiter.api.Test;

class CommandNodeHooksTest {

  @Test
  void toleratesMissingCurrentCommandField() {
    // Unit tests run against plain Brigadier, which is also the layout NeoForge
    // presents when Brigadier stays in the MC-BOOTSTRAP module layer: the mixin that
    // injects CURRENT_COMMAND never applies there. Initializing the hooks must not
    // fail and the cache must simply stay empty.
    assertFalse(hasCurrentCommandField());
    assertNull(CommandNodeHooks.getCurrent());
  }

  @Test
  void removesCommandsFromAllChildMaps() {
    LiteralCommandNode<Object> root = literal("root");
    LiteralCommandNode<Object> child = literal("child");
    root.addChild(child);
    assertSame(child, root.getChild("child"));

    CommandNodeHooks.removeCommand(root, "child");

    assertNull(root.getChild("child"));
  }

  @Test
  void canUseKeepsVanillaRequirementResult() {
    LiteralCommandNode<Object> node = literal("node");
    assertTrue(CommandNodeHooks.canUse(node, new Object()));
  }

  private static boolean hasCurrentCommandField() {
    try {
      CommandNode.class.getDeclaredField("CURRENT_COMMAND");
      return true;
    } catch (NoSuchFieldException e) {
      return false;
    }
  }

  private static LiteralCommandNode<Object> literal(String name) {
    return new LiteralCommandNode<>(name, context -> 0, source -> true, null, null, false);
  }
}
