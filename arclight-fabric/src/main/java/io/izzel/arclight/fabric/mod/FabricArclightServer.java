package io.izzel.arclight.fabric.mod;

import io.izzel.arclight.api.ArclightServer;
import io.izzel.arclight.api.TickingTracker;
import net.minecraftforge.eventbus.api.IEventBus;
import org.bukkit.plugin.Plugin;

public class FabricArclightServer implements ArclightServer {

    private final TickingTracker tickingTracker = new FabricTickingTracker();

    @Override
    public void registerForgeEvent(Plugin plugin, IEventBus eventBus, Object target) {
        // Fabric has no Forge event bus; keep this as a no-op for plugin compatibility.
    }

    @Override
    public TickingTracker getTickingTracker() {
        return this.tickingTracker;
    }
}
