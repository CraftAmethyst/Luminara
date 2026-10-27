package org.craftamethyst.luminara.common.mod.util.remapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.izzel.arclight.api.PluginPatcher;
import java.util.List;
import org.craftamethyst.luminara.common.mod.util.remapper.patcher.ArclightPluginPatcher;
import org.craftamethyst.luminara.common.mod.util.remapper.patcher.PluginLoggerTransformer;
import org.junit.jupiter.api.Test;

class RemapperPipelineTest {

  @Test
  void fixesCoreTransformerOrderAndMakesItImmutable() {
    PluginPatcher patcher =
        new PluginPatcher() {
          @Override
          public void handleClass(
              org.objectweb.asm.tree.ClassNode classNode, PluginPatcher.ClassRepo classRepo) {}
        };

    List<PluginTransformer> pipeline = RemapperPipeline.create(true, List.of(patcher));

    assertEquals(
        List.of(
            CraftBukkitVersionRemapper.class,
            ArclightInterfaceInvokerGen.class,
            ArclightRedirectAdapter.class,
            ClassLoaderAdapter.class,
            ArclightPluginPatcher.class),
        pipeline.stream().map(Object::getClass).toList());
    assertThrows(UnsupportedOperationException.class, () -> pipeline.clear());
  }

  @Test
  void insertsLoggerTransformerOnlyWithoutJulBridge() {
    List<PluginTransformer> pipeline = RemapperPipeline.create(false, List.of());

    assertEquals(
        List.of(
            CraftBukkitVersionRemapper.class,
            ArclightInterfaceInvokerGen.class,
            ArclightRedirectAdapter.class,
            ClassLoaderAdapter.class,
            PluginLoggerTransformer.class,
            ArclightPluginPatcher.class),
        pipeline.stream().map(Object::getClass).toList());
  }
}
