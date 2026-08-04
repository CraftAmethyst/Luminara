package io.izzel.arclight.common.mixin.core.world.entity.projectile;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.mixin.core.world.entity.EntityMixin;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.projectiles.ProjectileSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public abstract class ProjectileMixin extends EntityMixin {

    private boolean hitCancelled = false;

    // @formatter:off
    @Shadow @Nullable public abstract Entity getOwner();

    // @formatter:on

    @Shadow
    protected void onHit(HitResult result) {}

    @Inject(method = "setOwner", at = @At("RETURN"))
    private void arclight$updateSource(Entity entityIn, CallbackInfo ci) {
        if (entityIn != null) {
            CraftEntity entity = (
                (EntityBridge) entityIn
            ).bridge$getBukkitEntity();
            if (entity instanceof ProjectileSource) {
                this.projectileSource = (ProjectileSource) entity;
            }
        }
    }

    @Inject(method = "onHitBlock", cancellable = true, at = @At("HEAD"))
    private void arclight$cancelBlockHit(
        BlockHitResult result,
        CallbackInfo ci
    ) {
        if (hitCancelled) {
            ci.cancel();
        }
    }

    protected void preOnHit(HitResult hitResult) {
        org.bukkit.event.entity.ProjectileHitEvent event =
            CraftEventFactory.callProjectileHitEvent(
                (Projectile) (Object) this,
                hitResult
            );
        this.hitCancelled = event != null && event.isCancelled();
        if (hitResult.getType() == HitResult.Type.BLOCK || !this.hitCancelled) {
            this.onHit(hitResult);
        }
    }
}
