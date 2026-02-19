package io.izzel.arclight.fabric.mod;

import io.izzel.arclight.api.TickingTracker;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

public final class FabricTickingTracker implements TickingTracker {

    @Nullable
    @Override
    public Object getTickingSource() {
        return null;
    }

    @Nullable
    @Override
    public Entity getTickingEntity() {
        return null;
    }

    @Nullable
    @Override
    public Block getTickingBlock() {
        return null;
    }

    @Nullable
    @Override
    public TileState getTickingBlockEntity() {
        return null;
    }
}
