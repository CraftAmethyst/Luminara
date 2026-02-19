package io.izzel.arclight.common.mixin.core.world.entity.vehicle;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.world.WorldBridge;
import io.izzel.arclight.common.mixin.core.world.entity.EntityMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.extensions.IForgeAbstractMinecart;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.*;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin extends EntityMixin implements IForgeAbstractMinecart {

    public boolean slowWhenEmpty = true;
    public double maxSpeed = 0.4D;
    @Shadow
    private int lSteps;
    @Shadow
    private double lx;
    @Shadow
    private double ly;
    @Shadow
    private double lz;
    @Shadow
    private double lyr;
    @Shadow
    private double lxr;
    @Shadow
    private boolean flipped;
    @Shadow
    private boolean onRails;
    private double derailedX = 0.5;
    private double derailedY = 0.5;
    private double derailedZ = 0.5;
    private double flyingX = 0.95;
    private double flyingY = 0.95;
    private double flyingZ = 0.95;
    private transient Location arclight$prevLocation;

    @Shadow
    public abstract int getHurtDir();
    // @formatter:on

    // @formatter:off
    @Shadow public abstract void setHurtDir(int rollingDirection);

    @Shadow public abstract float getDamage();

    @Shadow public abstract void setDamage(float damage);

    @Shadow public abstract void destroy(DamageSource source);

    @Shadow public abstract int getHurtTime();

    @Shadow public abstract void setHurtTime(int rollingAmplitude);

    @Shadow protected abstract void moveAlongTrack(BlockPos pos, BlockState state);

    @Shadow public abstract void activateMinecart(int x, int y, int z, boolean receivingPower);

    @Shadow public abstract AbstractMinecart.Type getMinecartType();

    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At("RETURN"))
    private void arclight$init(EntityType<?> type, Level worldIn, CallbackInfo ci) {
        slowWhenEmpty = true;
        derailedX = 0.5;
        derailedY = 0.5;
        derailedZ = 0.5;
        flyingX = 0.95;
        flyingY = 0.95;
        flyingZ = 0.95;
        maxSpeed = 0.4D;
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide || this.isRemoved()) {
            return true;
        }
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        Vehicle vehicle = (Vehicle) this.getBukkitEntity();
        org.bukkit.entity.Entity passenger = (source.getEntity() == null) ? null : ((EntityBridge) source.getEntity()).bridge$getBukkitEntity();
        VehicleDamageEvent event = new VehicleDamageEvent(vehicle, passenger, amount);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        amount = (float) event.getDamage();
        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        ((Entity) (Object) this).hurtMarked = true;
        this.setDamage(this.getDamage() + amount * 10.0f);
        this.gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());
        boolean flag = source.getEntity() instanceof Player && ((Player) source.getEntity()).getAbilities().instabuild;
        if (flag || this.getDamage() > 40.0f) {
            VehicleDestroyEvent destroyEvent = new VehicleDestroyEvent(vehicle, passenger);
            Bukkit.getPluginManager().callEvent(destroyEvent);
            if (destroyEvent.isCancelled()) {
                this.setDamage(40.0f);
                return true;
            }
            this.ejectPassengers();
            if (flag && !this.hasCustomName()) {
                this.discard();
            } else {
                this.destroy(source);
            }
        }
        return true;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void arclight$storePrevLocation(CallbackInfo ci) {
        arclight$prevLocation = new Location(null, getX(), getY(), getZ(), getYRot(), getXRot());
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;handleNetherPortal()V"))
    private void arclight$skipHandleNetherPortal(AbstractMinecart instance) {
        // CraftBukkit - handled in postTick
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;setRot(FF)V"))
    private void arclight$fireVehicleEvents(CallbackInfo ci) {
        org.bukkit.World bworld = ((WorldBridge) this.level()).bridge$getWorld();
        Location from = this.arclight$prevLocation;
        this.arclight$prevLocation = null;
        from.setWorld(bworld);
        Location to = new Location(bworld, this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        Vehicle vehicle = (Vehicle) this.getBukkitEntity();
        Bukkit.getPluginManager().callEvent(new VehicleUpdateEvent(vehicle));
        if (!from.equals(to)) {
            Bukkit.getPluginManager().callEvent(new VehicleMoveEvent(vehicle, from, to));
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;startRiding(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean arclight$fireCollisionEventsRiding(Entity that, Entity self) {
        VehicleEntityCollisionEvent collisionEvent = new VehicleEntityCollisionEvent((Vehicle) this.getBukkitEntity(), ((EntityBridge) that).bridge$getBukkitEntity());
        Bukkit.getPluginManager().callEvent(collisionEvent);

        if (collisionEvent.isCancelled()) {
            return false;
        }
        return that.startRiding((Entity) (Object) this);
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;push(Lnet/minecraft/world/entity/Entity;)V"))
    private void arclight$fireCollisionEventsPush(Entity that, Entity self) {
        VehicleEntityCollisionEvent collisionEvent = new VehicleEntityCollisionEvent((Vehicle) this.getBukkitEntity(), ((EntityBridge) that).bridge$getBukkitEntity());
        Bukkit.getPluginManager().callEvent(collisionEvent);

        if (collisionEvent.isCancelled()) {
            return;
        }
        that.push((Entity) (Object) this);
    }

    /*
     * Don't use Overwrite to preserve LVT for injectors.
     * See #1677 and Spelunkery AbstractMinecartMixin#rattleMinecart
     */
    //@Overwrite
    //public void tick()

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    protected double getMaxSpeed() {
        return maxSpeed;
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    protected void comeOffTrack() {
        final double d0 = this.getMaxSpeed();
        final Vec3 vec3d = this.getDeltaMovement();
        this.setDeltaMovement(Mth.clamp(vec3d.x, -d0, d0), vec3d.y, Mth.clamp(vec3d.z, -d0, d0));
        if (this.onGround) {
            this.setDeltaMovement(new Vec3(this.getDeltaMovement().x * this.derailedX, this.getDeltaMovement().y * this.derailedY, this.getDeltaMovement().z * this.derailedZ));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        if (!this.onGround) {
            this.setDeltaMovement(new Vec3(this.getDeltaMovement().x * this.flyingX, this.getDeltaMovement().y * this.flyingY, this.getDeltaMovement().z * this.flyingZ));
        }
    }

    @Redirect(method = "applyNaturalSlowdown", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;isVehicle()Z"))
    private boolean arclight$slowWhenEmpty(AbstractMinecart abstractMinecartEntity) {
        return this.isVehicle() || !this.slowWhenEmpty;
    }

    @Inject(method = "push", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;hasPassenger(Lnet/minecraft/world/entity/Entity;)Z"))
    private void arclight$vehicleCollide(Entity entityIn, CallbackInfo ci) {
        if (!this.hasPassenger(entityIn)) {
            VehicleEntityCollisionEvent collisionEvent = new VehicleEntityCollisionEvent((Vehicle) this.getBukkitEntity(), ((EntityBridge) entityIn).bridge$getBukkitEntity());
            Bukkit.getPluginManager().callEvent(collisionEvent);
            if (collisionEvent.isCancelled()) {
                ci.cancel();
            }
        }
    }

    public Vector getFlyingVelocityMod() {
        return new Vector(flyingX, flyingY, flyingZ);
    }

    public void setFlyingVelocityMod(Vector flying) {
        flyingX = flying.getX();
        flyingY = flying.getY();
        flyingZ = flying.getZ();
    }

    public Vector getDerailedVelocityMod() {
        return new Vector(derailedX, derailedY, derailedZ);
    }

    public void setDerailedVelocityMod(Vector derailed) {
        derailedX = derailed.getX();
        derailedY = derailed.getY();
        derailedZ = derailed.getZ();
    }
}
