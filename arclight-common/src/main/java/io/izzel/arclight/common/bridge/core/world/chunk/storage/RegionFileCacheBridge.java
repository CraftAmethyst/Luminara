package io.izzel.arclight.common.bridge.core.world.chunk.storage;

import net.minecraft.world.level.ChunkPos;

import java.io.IOException;

public interface RegionFileCacheBridge {
    boolean bridge$chunkExists(ChunkPos pos) throws IOException;
}
