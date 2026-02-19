package io.izzel.arclight.common.mixin.core.world.entity;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.entity.InternalEntityBridge;
import io.izzel.arclight.common.mod.mixins.annotation.LoadIfMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.PositionImpl;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.projectiles.ProjectileSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collection;
import java.util.List;

@Mixin(Entity.class)
@LoadIfMod(modid = {"forge"}, condition = LoadIfMod.ModCondition.ABSENT)
public abstract class EntityMixin_FabricBridge implements EntityBridge, InternalEntityBridge {

    @Unique
    private CraftEntity arclight$bukkitEntity;
    @Unique
    private boolean arclight$persist = true;
    @Unique
    private boolean arclight$valid;
    @Unique
    private ProjectileSource arclight$projectileSource;
    @Unique
    private boolean arclight$lastDamageCancelled;
    @Unique
    private int arclight$rideCooldown;
    @Unique
    private Collection<ItemEntity> arclight$capturedDrops;

    @Shadow
    protected abstract void unsetRemoved();

    public CraftEntity getBukkitEntity() {
        return internal$getBukkitEntity();
    }

    @Override
    public CommandSender bridge$getBukkitSender(CommandSourceStack wrapper) {
        return internal$getBukkitEntity();
    }

    @Override
    public Entity bridge$teleportTo(ServerLevel world, PositionImpl blockPos) {
        Entity self = (Entity) (Object) this;
        if (self.level() == world) {
            self.teleportTo(blockPos.x(), blockPos.y(), blockPos.z());
            return self;
        }
        return self.changeDimension(world);
    }

    @Override
    public void bridge$setOnFire(int tick, boolean callEvent) {
        ((Entity) (Object) this).setSecondsOnFire(tick);
    }

    @Override
    public CraftEntity bridge$getBukkitEntity() {
        return internal$getBukkitEntity();
    }

    @Override
    public void bridge$setBukkitEntity(CraftEntity craftEntity) {
        this.arclight$bukkitEntity = craftEntity;
    }

    @Override
    public boolean bridge$isPersist() {
        return this.arclight$persist;
    }

    @Override
    public void bridge$setPersist(boolean persist) {
        this.arclight$persist = persist;
    }

    @Override
    public boolean bridge$isValid() {
        return this.arclight$valid;
    }

    @Override
    public void bridge$setValid(boolean valid) {
        this.arclight$valid = valid;
    }

    @Override
    public ProjectileSource bridge$getProjectileSource() {
        return this.arclight$projectileSource;
    }

    @Override
    public void bridge$setProjectileSource(ProjectileSource projectileSource) {
        this.arclight$projectileSource = projectileSource;
    }

    @Override
    public float bridge$getBukkitYaw() {
        return ((Entity) (Object) this).getYRot();
    }

    @Override
    public boolean bridge$isChunkLoaded() {
        Entity self = (Entity) (Object) this;
        return self.level().hasChunk((int) Math.floor(self.getX()) >> 4, (int) Math.floor(self.getZ()) >> 4);
    }

    @Override
    public boolean bridge$isLastDamageCancelled() {
        return this.arclight$lastDamageCancelled;
    }

    @Override
    public void bridge$setLastDamageCancelled(boolean cancelled) {
        this.arclight$lastDamageCancelled = cancelled;
    }

    @Override
    public Collection<ItemEntity> bridge$captureDrops() {
        return this.arclight$capturedDrops;
    }

    @Override
    public Collection<ItemEntity> bridge$captureDrops(Collection<ItemEntity> value) {
        var old = this.arclight$capturedDrops;
        this.arclight$capturedDrops = value;
        return old;
    }

    @Override
    public void bridge$revive() {
        this.unsetRemoved();
    }

    @Override
    public void bridge$postTick() {
    }

    @Override
    public boolean bridge$removePassenger(Entity passenger) {
        Entity self = (Entity) (Object) this;
        if (passenger.getVehicle() == self) {
            passenger.stopRiding();
            return true;
        }
        return false;
    }

    @Override
    public boolean bridge$addPassenger(Entity entity) {
        return entity.startRiding((Entity) (Object) this, true);
    }

    @Override
    public List<Entity> bridge$getPassengers() {
        return ((Entity) (Object) this).getPassengers();
    }

    @Override
    public void bridge$setRideCooldown(int rideCooldown) {
        this.arclight$rideCooldown = rideCooldown;
    }

    @Override
    public int bridge$getRideCooldown() {
        return this.arclight$rideCooldown;
    }

    @Override
    public boolean bridge$canCollideWith(Entity entity) {
        Entity self = (Entity) (Object) this;
        return self.isPushable() && entity.isPushable() && !self.isPassengerOfSameVehicle(entity);
    }

    @Override
    public CraftEntity internal$getBukkitEntity() {
        if (this.arclight$bukkitEntity == null) {
            this.arclight$bukkitEntity = CraftEntity.getEntity((CraftServer) Bukkit.getServer(), (Entity) (Object) this);
        }
        return this.arclight$bukkitEntity;
    }
}
