package io.izzel.arclight.common.mixin.core.world.entity.projectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.HitResult;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.event.entity.ExpBottleEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@Mixin(ThrownExperienceBottle.class)
public abstract class ThrownExperienceBottleMixin extends ThrowableItemProjectileMixin {

    private static final MethodHandle ARCLIGHT$PROJECTILE_ON_HIT = arclight$findProjectileOnHit();

    private static MethodHandle arclight$findProjectileOnHit() {
        try {
            return MethodHandles.lookup().findSpecial(
                    Projectile.class,
                    "onHit",
                    MethodType.methodType(void.class, HitResult.class),
                    ThrownExperienceBottle.class
            );
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private void arclight$invokeProjectileOnHit(HitResult result) {
        try {
            ARCLIGHT$PROJECTILE_ON_HIT.invokeExact((ThrownExperienceBottle) (Object) this, result);
        } catch (Throwable throwable) {
            throw new RuntimeException("Failed to invoke Projectile#onHit super implementation", throwable);
        }
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    protected void onHit(HitResult result) {
        this.arclight$invokeProjectileOnHit(result);
        if (!this.level().isClientSide) {
            int i = 3 + this.level().random.nextInt(5) + this.level().random.nextInt(5);
            ExpBottleEvent event = CraftEventFactory.callExpBottleEvent((ThrownExperienceBottle) (Object) this, i);
            i = event.getExperience();
            if (event.getShowEffect()) {
                this.level().levelEvent(2002, this.blockPosition(), PotionUtils.getColor(Potions.WATER));
            }
            ExperienceOrb.award((ServerLevel) this.level(), this.position(), i);
            this.discard();
        }
    }
}
