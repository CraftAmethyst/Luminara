package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.core.network.play.ServerPlayNetHandlerBridge;
import io.izzel.arclight.common.mod.util.PaperCompatSupport;
import io.papermc.paper.entity.LookAnchor;
import io.papermc.paper.entity.TeleportFlag;
import io.papermc.paper.math.Position;
import net.minecraft.world.entity.RelativeMovement;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Mixin(value = Player.class, remap = false)
public interface Player_PaperCompatMixin {

    default boolean teleport(@NotNull Location location, @NotNull TeleportFlag @NotNull ... teleportFlags) {
        return this.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN, teleportFlags);
    }

    default boolean teleport(@NotNull Location location, @NotNull PlayerTeleportEvent.TeleportCause cause, @NotNull TeleportFlag @NotNull ... teleportFlags) {
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(cause, "cause");
        Player player = (Player) this;
        Location target = location.clone();
        Set<TeleportFlag> allFlags = PaperCompatSupport.toFlagSet(teleportFlags);
        Set<RelativeMovement> relative = EnumSet.noneOf(RelativeMovement.class);
        for (TeleportFlag flag : allFlags) {
            if (flag instanceof TeleportFlag.Relative relativeFlag) {
                relative.add(PaperCompatSupport.toNmsRelative(relativeFlag));
            }
        }

        boolean retainPassengers = allFlags.contains(TeleportFlag.EntityState.RETAIN_PASSENGERS);
        boolean retainVehicle = allFlags.contains(TeleportFlag.EntityState.RETAIN_VEHICLE);
        boolean retainOpenInventory = allFlags.contains(TeleportFlag.EntityState.RETAIN_OPEN_INVENTORY);
        boolean sameWorld = target.getWorld() != null && target.getWorld().equals(player.getWorld());

        if (retainPassengers && !player.getPassengers().isEmpty() && !sameWorld) {
            return false;
        }
        if (retainVehicle && player.isInsideVehicle() && !sameWorld) {
            return false;
        }

        if (!retainPassengers && !player.getPassengers().isEmpty()) {
            return false;
        }

        List<Entity> passengers = List.of();
        if (retainPassengers && sameWorld && !player.getPassengers().isEmpty()) {
            passengers = List.copyOf(player.getPassengers());
            for (Entity passenger : passengers) {
                passenger.leaveVehicle();
            }
        }

        Entity previousVehicle = null;
        if (retainVehicle && sameWorld && player.isInsideVehicle()) {
            previousVehicle = player.getVehicle();
            player.leaveVehicle();
        }

        boolean success;
        if (player instanceof CraftPlayer craftPlayer
                && sameWorld
                && (retainOpenInventory || !relative.isEmpty())) {
            ((ServerPlayNetHandlerBridge) craftPlayer.getHandle().connection).bridge$teleport(
                    target.getX(), target.getY(), target.getZ(), target.getYaw(), target.getPitch(), relative, cause
            );
            success = true;
        } else {
            success = player.teleport(target, cause);
        }

        if (!success) {
            PaperCompatSupport.restorePlayerRelationships(player, previousVehicle, passengers);
            return false;
        }

        if (sameWorld) {
            PaperCompatSupport.restorePlayerRelationships(player, previousVehicle, passengers);
        }
        return true;
    }

    default void setRotation(float yaw, float pitch) {
        Player player = (Player) this;
        Location targetLocation = player.getEyeLocation();
        targetLocation.setYaw(yaw);
        targetLocation.setPitch(pitch);
        org.bukkit.util.Vector direction = targetLocation.getDirection();
        direction.multiply(9999999);
        targetLocation.add(direction);
        this.lookAt(targetLocation.getX(), targetLocation.getY(), targetLocation.getZ(), LookAnchor.EYES);
    }

    default void lookAt(double x, double y, double z, @NotNull LookAnchor playerAnchor) {
        Objects.requireNonNull(playerAnchor, "playerAnchor");
        Player player = (Player) this;
        Location source = playerAnchor == LookAnchor.EYES ? player.getEyeLocation() : player.getLocation();
        double dx = x - source.getX();
        double dy = y - source.getY();
        double dz = z - source.getZ();
        double xz = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) Math.toDegrees(-Math.atan2(dy, xz));
        this.setRotation(yaw, pitch);
    }

    default void lookAt(@NotNull Position position, @NotNull LookAnchor playerAnchor) {
        Objects.requireNonNull(position, "position");
        this.lookAt(position.x(), position.y(), position.z(), playerAnchor);
    }

    default void lookAt(@NotNull org.bukkit.entity.Entity entity, @NotNull LookAnchor playerAnchor, @NotNull LookAnchor entityAnchor) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(entityAnchor, "entityAnchor");
        Location target = (entityAnchor == LookAnchor.EYES && entity instanceof LivingEntity livingEntity)
                ? livingEntity.getEyeLocation()
                : entity.getLocation();
        this.lookAt(target.getX(), target.getY(), target.getZ(), playerAnchor);
    }
}
