package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.PaperCompatSupport;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.CraftWorld;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Mixin(value = World.class, remap = false)
public interface World_PaperCompatMixin {

    // Paper API compatibility: provide World#getCoordinateScale() at runtime
    default double getCoordinateScale() {
        if (this instanceof CraftWorld craft) {
            ServerLevel level = craft.getHandle();
            return level.dimensionType().coordinateScale();
        }
        return 1.0D;
    }

    @Deprecated
    default void getChunkAtAsync(int x, int z, @NotNull org.bukkit.World$ChunkLoadCallback cb) {
        this.getChunkAtAsync(x, z, true).thenAccept(cb::onLoad).exceptionally(PaperCompatSupport::logChunkCallbackException);
    }

    @Deprecated
    default void getChunkAtAsync(@NotNull Location loc, @NotNull org.bukkit.World$ChunkLoadCallback cb) {
        this.getChunkAtAsync(loc, true).thenAccept(cb::onLoad).exceptionally(PaperCompatSupport::logChunkCallbackException);
    }

    @Deprecated
    default void getChunkAtAsync(@NotNull Block block, @NotNull org.bukkit.World$ChunkLoadCallback cb) {
        this.getChunkAtAsync(block, true).thenAccept(cb::onLoad).exceptionally(PaperCompatSupport::logChunkCallbackException);
    }

    default void getChunkAtAsync(int x, int z, @NotNull Consumer<Chunk> cb) {
        this.getChunkAtAsync(x, z, true).thenAccept(cb).exceptionally(PaperCompatSupport::logChunkCallbackException);
    }

    default void getChunkAtAsync(int x, int z, boolean gen, @NotNull Consumer<Chunk> cb) {
        this.getChunkAtAsync(x, z, gen).thenAccept(cb).exceptionally(PaperCompatSupport::logChunkCallbackException);
    }

    default void getChunkAtAsync(@NotNull Location loc, @NotNull Consumer<Chunk> cb) {
        this.getChunkAtAsync(PaperCompatSupport.chunkCoord(loc.getX()), PaperCompatSupport.chunkCoord(loc.getZ()), true, cb);
    }

    default void getChunkAtAsync(@NotNull Location loc, boolean gen, @NotNull Consumer<Chunk> cb) {
        this.getChunkAtAsync(PaperCompatSupport.chunkCoord(loc.getX()), PaperCompatSupport.chunkCoord(loc.getZ()), gen, cb);
    }

    default void getChunkAtAsync(@NotNull Block block, @NotNull Consumer<Chunk> cb) {
        this.getChunkAtAsync(block.getX() >> 4, block.getZ() >> 4, true, cb);
    }

    default void getChunkAtAsync(@NotNull Block block, boolean gen, @NotNull Consumer<Chunk> cb) {
        this.getChunkAtAsync(block.getX() >> 4, block.getZ() >> 4, gen, cb);
    }

    default @NotNull CompletableFuture<Chunk> getChunkAtAsync(@NotNull Location loc) {
        return this.getChunkAtAsync(PaperCompatSupport.chunkCoord(loc.getX()), PaperCompatSupport.chunkCoord(loc.getZ()), true);
    }

    default @NotNull CompletableFuture<Chunk> getChunkAtAsync(@NotNull Location loc, boolean gen) {
        return this.getChunkAtAsync(PaperCompatSupport.chunkCoord(loc.getX()), PaperCompatSupport.chunkCoord(loc.getZ()), gen);
    }

    default @NotNull CompletableFuture<Chunk> getChunkAtAsync(@NotNull Block block) {
        return this.getChunkAtAsync(block.getX() >> 4, block.getZ() >> 4, true);
    }

    default @NotNull CompletableFuture<Chunk> getChunkAtAsync(@NotNull Block block, boolean gen) {
        return this.getChunkAtAsync(block.getX() >> 4, block.getZ() >> 4, gen);
    }

    default @NotNull CompletableFuture<Chunk> getChunkAtAsync(int x, int z) {
        return this.getChunkAtAsync(x, z, true);
    }

    // Paper API compatibility: World#getChunkAtAsync(int, int, boolean)
    default @NotNull CompletableFuture<Chunk> getChunkAtAsync(int x, int z, boolean gen) {
        return this.getChunkAtAsync(x, z, gen, false);
    }

    default @NotNull CompletableFuture<Chunk> getChunkAtAsyncUrgently(@NotNull Location loc) {
        return this.getChunkAtAsync(PaperCompatSupport.chunkCoord(loc.getX()), PaperCompatSupport.chunkCoord(loc.getZ()), true, true);
    }

    default @NotNull CompletableFuture<Chunk> getChunkAtAsyncUrgently(@NotNull Location loc, boolean gen) {
        return this.getChunkAtAsync(PaperCompatSupport.chunkCoord(loc.getX()), PaperCompatSupport.chunkCoord(loc.getZ()), gen, true);
    }

    default @NotNull CompletableFuture<Chunk> getChunkAtAsyncUrgently(@NotNull Block block) {
        return this.getChunkAtAsync(block.getX() >> 4, block.getZ() >> 4, true, true);
    }

    default @NotNull CompletableFuture<Chunk> getChunkAtAsyncUrgently(@NotNull Block block, boolean gen) {
        return this.getChunkAtAsync(block.getX() >> 4, block.getZ() >> 4, gen, true);
    }

    default @NotNull CompletableFuture<Chunk> getChunkAtAsyncUrgently(int x, int z) {
        return this.getChunkAtAsync(x, z, true, true);
    }

    default @NotNull CompletableFuture<Chunk> getChunkAtAsync(int x, int z, boolean gen, boolean urgent) {
        if (this instanceof CraftWorld craftWorld) {
            if (Bukkit.isPrimaryThread()) {
                return CompletableFuture.completedFuture(craftWorld.getChunkAt(x, z, gen));
            }

            CompletableFuture<Chunk> future = new CompletableFuture<>();
            CraftServer craftServer = (CraftServer) Bukkit.getServer();
            Object minecraftServer = craftServer.getServer();
            if (minecraftServer instanceof java.util.concurrent.Executor executor) {
                executor.execute(() -> {
                    try {
                        future.complete(craftWorld.getChunkAt(x, z, gen));
                    } catch (Throwable throwable) {
                        future.completeExceptionally(throwable);
                    }
                });
            } else {
                future.complete(craftWorld.getChunkAt(x, z, gen));
            }
            return future;
        }

        return CompletableFuture.completedFuture(((World) this).getChunkAt(x, z, gen));
    }
}
