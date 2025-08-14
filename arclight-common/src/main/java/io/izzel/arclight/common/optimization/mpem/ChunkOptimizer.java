package io.izzel.arclight.common.optimization.mpem;

import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import io.izzel.arclight.i18n.ArclightConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkOptimizer {
    private static final Logger LOGGER = ArclightI18nLogger.getLogger("ChunkOptimizer");
    private static final Map<ChunkPos, Long> chunkAccessTimes = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        // Luminara - Chunk optimization disabled to avoid conflicts with Paper patches
        // Paper handles chunk loading/unloading optimization internally
        // This method is kept for compatibility but does nothing
        return;
    }

    public static void markChunkAccessed(ChunkPos pos) {
        // Luminara - Chunk access tracking disabled (Paper handles this internally)
        // Method kept for compatibility
    }

    public static boolean isChunkActive(ChunkPos pos) {
        // Luminara - Chunk activity tracking disabled (Paper handles this internally)
        // Method kept for compatibility, always returns true to avoid breaking existing code
        return true;
    }


}
