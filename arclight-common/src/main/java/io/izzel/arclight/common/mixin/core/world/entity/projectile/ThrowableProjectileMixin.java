package io.izzel.arclight.common.mixin.core.world.entity.projectile;

import io.izzel.arclight.common.bridge.core.entity.LivingEntityBridge;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@Mixin(ThrowableProjectile.class)
public abstract class ThrowableProjectileMixin extends ProjectileMixin {

    private static final MethodHandle ARCLIGHT$PROJECTILE_ON_HIT = arclight$findProjectileOnHit();

    private static MethodHandle arclight$findProjectileOnHit() {
        try {
            return MethodHandles.lookup().findSpecial(
                    Projectile.class,
                    "onHit",
                    MethodType.methodType(void.class, HitResult.class),
                    ThrowableProjectile.class
            );
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    protected void onHit(HitResult result) {
        try {
            ARCLIGHT$PROJECTILE_ON_HIT.invokeExact((ThrowableProjectile) (Object) this, result);
        } catch (Throwable throwable) {
            throw new RuntimeException("Failed to invoke Projectile#onHit super implementation", throwable);
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;)V", at = @At("RETURN"))
    private void arclight$init(EntityType<? extends ThrowableProjectile> type, LivingEntity livingEntityIn, Level worldIn, CallbackInfo ci) {
        this.projectileSource = ((LivingEntityBridge) livingEntityIn).bridge$getBukkitEntity();
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ThrowableProjectile;onHit(Lnet/minecraft/world/phys/HitResult;)V"))
    private void arclight$projectileHit(ThrowableProjectile entity, HitResult result) {
        this.preOnHit(result);
    }
}
