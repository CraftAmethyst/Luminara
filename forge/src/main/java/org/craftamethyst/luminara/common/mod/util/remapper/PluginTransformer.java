package org.craftamethyst.luminara.common.mod.util.remapper;

import org.objectweb.asm.tree.ClassNode;

public interface PluginTransformer {
  void handleClass(ClassNode node, ClassLoaderRemapper remapper, ArclightRemapConfig config);

  default int priority() {
    return 0;
  }
}
