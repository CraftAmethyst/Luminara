package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.PaperCompatSupport;
import io.papermc.paper.entity.TeleportFlag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Mixin(value = Entity.class, remap = false)
public interface Entity_PaperCompatMixin {

    @Shadow
    boolean teleport(@NotNull Location location, @NotNull PlayerTeleportEvent.TeleportCause cause);

    default boolean teleport(@NotNull Location location, @NotNull TeleportFlag @NotNull ... teleportFlags) {
        return this.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN, teleportFlags);
    }

    default boolean teleport(@NotNull Location location, @NotNull PlayerTeleportEvent.TeleportCause cause, @NotNull TeleportFlag @NotNull ... teleportFlags) {
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(cause, "cause");

        Entity entity = (Entity) this;
        World targetWorld = location.getWorld();
        boolean sameWorld = targetWorld != null && targetWorld.equals(entity.getWorld());

        Set<TeleportFlag> flags = PaperCompatSupport.toFlagSet(teleportFlags);
        boolean retainPassengers = flags.contains(TeleportFlag.EntityState.RETAIN_PASSENGERS);
        boolean retainVehicle = flags.contains(TeleportFlag.EntityState.RETAIN_VEHICLE);

        if (retainPassengers && !entity.getPassengers().isEmpty() && !sameWorld) {
            return false;
        }
        if (retainVehicle && entity.isInsideVehicle() && !sameWorld) {
            return false;
        }

        if (!retainPassengers && !entity.getPassengers().isEmpty()) {
            return false;
        }

        List<Entity> passengers = List.of();
        if (retainPassengers && sameWorld && !entity.getPassengers().isEmpty()) {
            passengers = List.copyOf(entity.getPassengers());
            for (Entity passenger : passengers) {
                passenger.leaveVehicle();
            }
        }

        Entity previousVehicle = null;
        if (retainVehicle && sameWorld && entity.isInsideVehicle()) {
            previousVehicle = entity.getVehicle();
            entity.leaveVehicle();
        }

        boolean success = this.teleport(location, cause);
        if (!success) {
            PaperCompatSupport.restoreEntityRelationships(entity, previousVehicle, passengers);
            return false;
        }

        if (sameWorld) {
            PaperCompatSupport.restoreEntityRelationships(entity, previousVehicle, passengers);
        }
        return true;
    }

    default @NotNull CompletableFuture<Boolean> teleportAsync(@NotNull Location loc) {
        return this.teleportAsync(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    default @NotNull CompletableFuture<Boolean> teleportAsync(@NotNull Location loc, @NotNull PlayerTeleportEvent.TeleportCause cause) {
        Objects.requireNonNull(loc, "loc");
        Objects.requireNonNull(cause, "cause");
        World world = Objects.requireNonNull(loc.getWorld(), "loc.world");
        Location target = loc.clone();
        int chunkX = ((int) Math.floor(target.getX())) >> 4;
        int chunkZ = ((int) Math.floor(target.getZ())) >> 4;

        CompletableFuture<Boolean> urgentFuture = PaperCompatSupport.tryUrgentChunkTeleport(world, target, () -> this.teleport(target, cause));
        if (urgentFuture != null) {
            return urgentFuture;
        }

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        if (Bukkit.isPrimaryThread()) {
            world.getChunkAt(chunkX, chunkZ, true);
            future.complete(this.teleport(target, cause));
            return future;
        }

        if (world instanceof CraftWorld craftWorld) {
            Object minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
            if (minecraftServer instanceof java.util.concurrent.Executor executor) {
                executor.execute(() -> {
                    try {
                        craftWorld.getChunkAt(chunkX, chunkZ, true);
                        future.complete(this.teleport(target, cause));
                    } catch (Throwable throwable) {
                        future.completeExceptionally(throwable);
                    }
                });
            } else {
                try {
                    craftWorld.getChunkAt(chunkX, chunkZ, true);
                    future.complete(this.teleport(target, cause));
                } catch (Throwable throwable) {
                    future.completeExceptionally(throwable);
                }
            }
        } else {
            try {
                world.getChunkAt(chunkX, chunkZ, true);
                future.complete(this.teleport(target, cause));
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        }
        return future;
    }
}
