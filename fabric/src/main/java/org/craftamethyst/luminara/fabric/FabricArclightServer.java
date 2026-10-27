package org.craftamethyst.luminara.fabric;

import io.izzel.arclight.api.ArclightServer;
import io.izzel.arclight.api.TickingTracker;
import net.minecraftforge.eventbus.api.IEventBus;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public final class FabricArclightServer implements ArclightServer {

  private final TickingTracker tickingTracker = new FabricTickingTracker();

  @Override
  public void registerForgeEvent(Plugin plugin, IEventBus eventBus, Object target) {
    // Fabric has no Forge event bus; plugin listeners are Bukkit listeners.
  }

  @Override
  public TickingTracker getTickingTracker() {
    return this.tickingTracker;
  }

  private static final class FabricTickingTracker implements TickingTracker {
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
}
