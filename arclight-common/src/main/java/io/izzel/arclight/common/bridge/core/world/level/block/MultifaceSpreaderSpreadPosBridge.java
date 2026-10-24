package io.izzel.arclight.common.bridge.core.world.level.block;

import net.minecraft.core.BlockPos;

public interface MultifaceSpreaderSpreadPosBridge {
  BlockPos source();

  void arclight$setSource(BlockPos source);
}
