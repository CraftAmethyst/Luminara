package org.craftamethyst.luminara.common.bridge.core.world.server;

import net.minecraft.world.level.chunk.LevelChunk;

public interface ChunkHolderBridge {
  int bridge$getOldTicketLevel();

  LevelChunk bridge$getFullChunk();

  LevelChunk bridge$getFullChunkUnchecked();
}
