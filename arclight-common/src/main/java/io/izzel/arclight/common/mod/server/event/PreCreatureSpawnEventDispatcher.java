package io.izzel.arclight.common.mod.server.event;

import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import io.izzel.arclight.common.bridge.core.world.WorldBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.Nullable;

public final class PreCreatureSpawnEventDispatcher {

    private static final ThreadLocal<Boolean> NATURAL_SPAWN_ABORT =
        ThreadLocal.withInitial(() -> false);

    private PreCreatureSpawnEventDispatcher() {
    }

    @Nullable
    public static PreCreatureSpawnEvent call(
        ServerLevel level,
        EntityType<?> entityType,
        BlockPos pos,
        CreatureSpawnEvent.SpawnReason reason
    ) {
        return call(level, entityType, pos.getX(), pos.getY(), pos.getZ(), reason);
    }

    @Nullable
    public static PreCreatureSpawnEvent call(
        ServerLevel level,
        EntityType<?> entityType,
        double x,
        double y,
        double z,
        CreatureSpawnEvent.SpawnReason reason
    ) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        if (key == null) {
            return null;
        }
        org.bukkit.entity.EntityType bukkitType =
            org.bukkit.entity.EntityType.fromName(key.getPath());
        if (bukkitType == null) {
            return null;
        }

        Location location = new Location(
            ((WorldBridge) level).bridge$getWorld(),
            x,
            y,
            z
        );
        PreCreatureSpawnEvent event = new PreCreatureSpawnEvent(
            location,
            bukkitType,
            reason
        );
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static boolean callNatural(
        ServerLevel level,
        EntityType<?> entityType,
        BlockPos pos
    ) {
        PreCreatureSpawnEvent event = call(
            level,
            entityType,
            pos,
            CreatureSpawnEvent.SpawnReason.NATURAL
        );
        if (event == null || !event.isCancelled()) {
            return true;
        }
        if (event.shouldAbortSpawn()) {
            NATURAL_SPAWN_ABORT.set(true);
        }
        return false;
    }

    public static boolean shouldAbortNaturalSpawn() {
        return NATURAL_SPAWN_ABORT.get();
    }

    public static void resetNaturalSpawnAbort() {
        NATURAL_SPAWN_ABORT.remove();
    }
}
