package org.bukkit;

import org.jetbrains.annotations.NotNull;

@Deprecated
public interface World$ChunkLoadCallback extends java.util.function.Consumer<Chunk> {

    void onLoad(@NotNull Chunk chunk);

    @Override
    default void accept(@NotNull Chunk chunk) {
        onLoad(chunk);
    }
}
