package io.izzel.arclight.common.mixin.core.world.entity;

import io.izzel.arclight.common.bridge.core.entity.MobEntityBridge;
import io.izzel.arclight.common.mod.mixins.annotation.LoadIfMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import org.bukkit.craftbukkit.v.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Mob.class)
@LoadIfMod(modid = {"forge"}, condition = LoadIfMod.ModCondition.ABSENT)
public abstract class MobMixin_FabricBridge extends LivingEntityMixin_FabricBridge implements MobEntityBridge {

    @Unique
    private transient EntityTargetEvent.TargetReason arclight$targetReason;
    @Unique
    private transient boolean arclight$fireTargetEvent;
    @Unique
    private transient boolean arclight$lastTargetResult;
    @Unique
    private transient EntityTransformEvent.TransformReason arclight$transformReason;
    @Unique
    private boolean arclight$aware = true;

    @Override
    public void bridge$pushGoalTargetReason(EntityTargetEvent.TargetReason reason, boolean fireEvent) {
        this.arclight$targetReason = reason;
        this.arclight$fireTargetEvent = fireEvent;
    }

    @Override
    public void bridge$pushTransformReason(EntityTransformEvent.TransformReason transformReason) {
        this.arclight$transformReason = transformReason;
    }

    @Override
    public boolean bridge$setGoalTarget(LivingEntity livingEntity, EntityTargetEvent.TargetReason reason, boolean fireEvent) {
        var self = (Mob) (Object) this;
        bridge$pushGoalTargetReason(reason, fireEvent);
        if (self.getTarget() == livingEntity) {
            this.arclight$lastTargetResult = false;
            return false;
        }
        if (fireEvent) {
            var event = CraftEventFactory.callEntityTargetLivingEvent(self, livingEntity, reason);
            if (event.isCancelled()) {
                this.arclight$lastTargetResult = false;
                return false;
            }
            var target = event.getTarget();
            livingEntity = target == null ? null : ((CraftLivingEntity) target).getHandle();
        }
        self.setTarget(livingEntity);
        this.arclight$lastTargetResult = true;
        return true;
    }

    @Override
    public boolean bridge$lastGoalTargetResult() {
        return this.arclight$lastTargetResult;
    }

    @Override
    public ResourceLocation bridge$getLootTable() {
        return ((Mob) (Object) this).getLootTable();
    }

    @Override
    public boolean bridge$isPersistenceRequired() {
        return ((Mob) (Object) this).isPersistenceRequired();
    }

    @Override
    public void bridge$setPersistenceRequired(boolean value) {
        if (value) {
            ((Mob) (Object) this).setPersistenceRequired();
        }
    }

    @Override
    public void bridge$setAware(boolean aware) {
        this.arclight$aware = aware;
    }

    @Override
    public void bridge$captureItemDrop(ItemEntity itemEntity) {
    }
}
