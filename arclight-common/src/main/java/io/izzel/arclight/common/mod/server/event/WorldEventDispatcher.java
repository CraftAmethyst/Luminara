package io.izzel.arclight.common.mod.server.event;

import io.izzel.arclight.common.bridge.bukkit.CraftServerBridge;
import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;

public class WorldEventDispatcher {

    private static final Logger LOGGER = ArclightI18nLogger.getLogger(
        "WorldEventDispatcher"
    );

    @SubscribeEvent
    public void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            LOGGER.info("world.unloading", level.dimension().location());
            ((CraftServerBridge) Bukkit.getServer()).bridge$removeWorld(level);
            LOGGER.info("world.unloaded", level.dimension().location());
        }
    }
}
