package org.craftamethyst.luminara.common.mod.server.api;

import io.izzel.arclight.api.ArclightServer;
import io.izzel.arclight.api.TickingTracker;
import io.izzel.arclight.api.Unsafe;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.IEventBus;
import org.bukkit.plugin.Plugin;
import org.craftamethyst.luminara.common.mod.util.PluginEventHandler;

public class DefaultArclightServer implements ArclightServer {

  private final TickingTracker tickingTracker = new DefaultTickingTracker();

  @Override
  public void registerForgeEvent(Plugin plugin, IEventBus bus, Object target) {
    try {
      if (bus instanceof EventBus) {
        PluginEventHandler.register(plugin, (EventBus) bus, target);
      } else {
        bus.register(target);
      }
    } catch (Throwable t) {
      Unsafe.throwException(t);
    }
  }

  @Override
  public TickingTracker getTickingTracker() {
    return this.tickingTracker;
  }
}
