package io.izzel.arclight.common.mod.util;

import io.papermc.paper.entity.TeleportFlag;
import net.minecraft.world.entity.RelativeMovement;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public final class PaperCompatSupport {

    private PaperCompatSupport() {
    }

    public static int chunkCoord(double coord) {
        return ((int) Math.floor(coord)) >> 4;
    }

    public static Void logChunkCallbackException(Throwable ex) {
        Bukkit.getLogger().log(Level.WARNING, "Exception in chunk load callback", ex);
        return null;
    }

    public static Set<TeleportFlag> toFlagSet(TeleportFlag[] teleportFlags) {
        if (teleportFlags == null || teleportFlags.length == 0) {
            return Collections.emptySet();
        }
        return new HashSet<>(Arrays.asList(teleportFlags));
    }

    public static RelativeMovement toNmsRelative(TeleportFlag.Relative relative) {
        return switch (relative) {
            case X -> RelativeMovement.X;
            case Y -> RelativeMovement.Y;
            case Z -> RelativeMovement.Z;
            case YAW -> RelativeMovement.Y_ROT;
            case PITCH -> RelativeMovement.X_ROT;
        };
    }

    public static void restoreEntityRelationships(Entity entity, Entity previousVehicle, List<Entity> passengers) {
        if (previousVehicle != null && entity.getWorld().equals(previousVehicle.getWorld())) {
            previousVehicle.addPassenger(entity);
        }
        if (!passengers.isEmpty()) {
            for (Entity passenger : passengers) {
                if (passenger.getWorld().equals(entity.getWorld())) {
                    entity.addPassenger(passenger);
                }
            }
        }
    }

    public static void restorePlayerRelationships(Player player, Entity previousVehicle, List<Entity> passengers) {
        if (previousVehicle != null && player.getWorld().equals(previousVehicle.getWorld())) {
            previousVehicle.addPassenger(player);
        }
        if (!passengers.isEmpty()) {
            for (Entity passenger : passengers) {
                if (passenger.getWorld().equals(player.getWorld())) {
                    player.addPassenger(passenger);
                }
            }
        }
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    public static CompletableFuture<Boolean> tryUrgentChunkTeleport(World world, Location target, Callable<Boolean> teleportCall) {
        try {
            var method = world.getClass().getMethod("getChunkAtAsyncUrgently", Location.class);
            Object result = method.invoke(world, target);
            if (!(result instanceof CompletableFuture<?> chunkFuture)) {
                return null;
            }

            CompletableFuture<Boolean> resultFuture = new CompletableFuture<>();
            chunkFuture.whenComplete((ignored, throwable) -> {
                if (throwable != null) {
                    resultFuture.completeExceptionally(throwable);
                    return;
                }
                try {
                    resultFuture.complete(teleportCall.call());
                } catch (Throwable t) {
                    resultFuture.completeExceptionally(t);
                }
            });
            return resultFuture;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
