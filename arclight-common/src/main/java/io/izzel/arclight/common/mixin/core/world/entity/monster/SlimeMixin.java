package io.izzel.arclight.common.mixin.core.world.entity.monster;

import io.izzel.arclight.common.bridge.core.world.WorldBridge;
import io.izzel.arclight.common.mixin.core.world.entity.MobMixin;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

@Mixin(net.minecraft.world.entity.monster.Slime.class)
public abstract class SlimeMixin extends MobMixin {

    private transient List<LivingEntity> arclight$slimes;
    private static final MethodHandle ARCLIGHT$ENTITY_REMOVE = arclight$findEntityRemove();

    // @formatter:off
    @Shadow public abstract int getSize();
    // @formatter:on

    @Shadow
    public abstract EntityType<? extends net.minecraft.world.entity.monster.Slime> getType();

    private static MethodHandle arclight$findEntityRemove() {
        var lookup = MethodHandles.lookup();
        var type = MethodType.methodType(void.class, Entity.RemovalReason.class);
        ReflectiveOperationException error = null;
        for (String name : new String[]{"remove", "method_5650"}) {
            try {
                return lookup.findSpecial(Entity.class, name, type, net.minecraft.world.entity.monster.Slime.class);
            } catch (NoSuchMethodException | IllegalAccessException ex) {
                if (error == null) {
                    error = ex;
                } else {
                    error.addSuppressed(ex);
                }
            }
        }
        throw new ExceptionInInitializerError(error);
    }

    private void arclight$invokeEntityRemove(Entity.RemovalReason reason) {
        try {
            ARCLIGHT$ENTITY_REMOVE.invokeExact((net.minecraft.world.entity.monster.Slime) (Object) this, reason);
        } catch (Throwable throwable) {
            throw new RuntimeException("Failed to invoke Entity#remove super implementation", throwable);
        }
    }

    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void arclight$removeWithSplitEvent(Entity.RemovalReason p_149847_, CallbackInfo ci) {
        int i = this.getSize();
        if (!this.level().isClientSide && i > 1 && this.isDeadOrDying()) {
            Component itextcomponent = this.getCustomName();
            boolean flag = this.isNoAi();
            float f = (float) i / 4.0F;
            int j = i / 2;
            int k = 2 + this.random.nextInt(3);

            {
                SlimeSplitEvent event = new SlimeSplitEvent((Slime) this.getBukkitEntity(), k);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled() || event.getCount() <= 0) {
                    this.arclight$invokeEntityRemove(p_149847_);
                    ci.cancel();
                    return;
                }
                k = event.getCount();
            }
            arclight$slimes = new ArrayList<>(k);

            for (int l = 0; l < k; ++l) {
                float f1 = ((float) (l % 2) - 0.5F) * f;
                float f2 = ((float) (l / 2) - 0.5F) * f;
                net.minecraft.world.entity.monster.Slime slimeentity = this.getType().create(this.level());
                if (slimeentity == null) continue;
                if (this.isPersistenceRequired()) {
                    slimeentity.setPersistenceRequired();
                }

                slimeentity.setCustomName(itextcomponent);
                slimeentity.setNoAi(flag);
                slimeentity.setInvulnerable(this.isInvulnerable());
                slimeentity.setSize(j, true);
                slimeentity.moveTo(this.getX() + (double) f1, this.getY() + 0.5D, this.getZ() + (double) f2, this.random.nextFloat() * 360.0F, 0.0F);
                arclight$slimes.add(slimeentity);
            }
            if (CraftEventFactory.callEntityTransformEvent((net.minecraft.world.entity.monster.Slime) (Object) this, arclight$slimes, EntityTransformEvent.TransformReason.SPLIT).isCancelled()) {
                this.arclight$invokeEntityRemove(p_149847_);
                arclight$slimes = null;
                ci.cancel();
                return;
            }
            for (int l = 0; l < arclight$slimes.size(); l++) {
                // Apotheosis compat, see https://github.com/IzzelAliz/Arclight/issues/1078
                float f1 = ((float) (l % 2) - 0.5F) * f;
                float f2 = ((float) (l / 2) - 0.5F) * f;
                net.minecraft.world.entity.monster.Slime living = (net.minecraft.world.entity.monster.Slime) arclight$slimes.get(l);
                ((WorldBridge) this.level()).bridge$pushAddEntityReason(CreatureSpawnEvent.SpawnReason.SLIME_SPLIT);
                this.level().addFreshEntity(living);
            }
            arclight$slimes = null;
        }
        this.arclight$invokeEntityRemove(p_149847_);
        ci.cancel();
    }
}
