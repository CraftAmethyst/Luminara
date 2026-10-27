package org.craftamethyst.luminara.common.mod.server.event;

import net.minecraftforge.common.MinecraftForge;
import org.craftamethyst.luminara.common.mod.util.log.LuminaraLog;

public abstract class ArclightEventDispatcherRegistry {

  public static void registerAllEventDispatchers() {
    MinecraftForge.EVENT_BUS.register(new BlockBreakEventDispatcher());
    MinecraftForge.EVENT_BUS.register(new BlockPlaceEventDispatcher());
    MinecraftForge.EVENT_BUS.register(new EntityPotionEffectEventDispatcher());
    MinecraftForge.EVENT_BUS.register(new EntityEventDispatcher());
    MinecraftForge.EVENT_BUS.register(new EntityTeleportEventDispatcher());
    MinecraftForge.EVENT_BUS.register(new ItemEntityEventDispatcher());
    MinecraftForge.EVENT_BUS.register(new WorldEventDispatcher());
    LuminaraLog.LOGGER.info("registry.forge-event");
  }
}
