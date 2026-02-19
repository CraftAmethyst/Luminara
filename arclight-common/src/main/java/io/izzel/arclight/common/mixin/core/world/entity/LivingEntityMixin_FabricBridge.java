package io.izzel.arclight.common.mixin.core.world.entity;

import io.izzel.arclight.common.bridge.core.entity.LivingEntityBridge;
import io.izzel.arclight.common.mod.mixins.annotation.LoadIfMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v.entity.CraftLivingEntity;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(LivingEntity.class)
@LoadIfMod(modid = {"forge"}, condition = LoadIfMod.ModCondition.ABSENT)
public abstract class LivingEntityMixin_FabricBridge extends EntityMixin_FabricBridge implements LivingEntityBridge {

    @Unique
    private int arclight$expToDrop;
    @Unique
    private boolean arclight$forceDrops;
    @Unique
    private transient EntityRegainHealthEvent.RegainReason arclight$regainReason;
    @Unique
    private transient EntityPotionEffectEvent.Cause arclight$effectCause;
    @Unique
    private transient EntityPotionEffectEvent.Action arclight$action;

    // @formatter:off
    @Shadow public abstract int getExperienceReward();
    @Shadow public abstract void heal(float healAmount);
    @Shadow public abstract boolean addEffect(MobEffectInstance effectInstance);
    @Shadow public abstract boolean removeEffect(MobEffect effect);
    @Shadow public abstract boolean removeAllEffects();
    // @formatter:on

    public CraftLivingEntity getBukkitEntity() {
        return (CraftLivingEntity) internal$getBukkitEntity();
    }

    @Override
    public CraftLivingEntity bridge$getBukkitEntity() {
        return (CraftLivingEntity) internal$getBukkitEntity();
    }

    @Override
    public void bridge$setSlot(EquipmentSlot slotIn, ItemStack stack, boolean silent) {
        if ((Object) this instanceof Mob mob) {
            mob.setItemSlot(slotIn, stack);
        }
    }

    @Override
    public void bridge$playEquipSound(EquipmentSlot slot, ItemStack oldItem, ItemStack newItem, boolean silent) {
    }

    @Override
    public boolean bridge$canPickUpLoot() {
        return (Object) this instanceof Mob mob && mob.canPickUpLoot();
    }

    @Override
    public boolean bridge$isForceDrops() {
        return this.arclight$forceDrops;
    }

    @Override
    public int bridge$getExpReward() {
        return this.getExperienceReward();
    }

    @Override
    public void bridge$setExpToDrop(int amount) {
        this.arclight$expToDrop = amount;
    }

    @Override
    public int bridge$getExpToDrop() {
        return this.arclight$expToDrop;
    }

    @Override
    public void bridge$pushHealReason(EntityRegainHealthEvent.RegainReason regainReason) {
        this.arclight$regainReason = regainReason;
    }

    @Override
    public void bridge$heal(float healAmount, EntityRegainHealthEvent.RegainReason regainReason) {
        this.arclight$regainReason = regainReason;
        this.heal(healAmount);
        this.arclight$regainReason = null;
    }

    @Override
    public void bridge$pushEffectCause(EntityPotionEffectEvent.Cause cause) {
        this.arclight$effectCause = cause;
    }

    @Override
    public boolean bridge$addEffect(MobEffectInstance effect, EntityPotionEffectEvent.Cause cause) {
        this.arclight$effectCause = cause;
        var result = this.addEffect(effect);
        if (result) {
            this.arclight$action = EntityPotionEffectEvent.Action.ADDED;
        }
        return result;
    }

    @Override
    public boolean bridge$removeEffect(MobEffect effect, EntityPotionEffectEvent.Cause cause) {
        this.arclight$effectCause = cause;
        var result = this.removeEffect(effect);
        if (result) {
            this.arclight$action = EntityPotionEffectEvent.Action.REMOVED;
        }
        return result;
    }

    @Override
    public boolean bridge$removeAllEffects(EntityPotionEffectEvent.Cause cause) {
        this.arclight$effectCause = cause;
        var result = this.removeAllEffects();
        if (result) {
            this.arclight$action = EntityPotionEffectEvent.Action.CLEARED;
        }
        return result;
    }

    @Override
    public Optional<EntityPotionEffectEvent.Cause> bridge$getEffectCause() {
        try {
            return Optional.ofNullable(this.arclight$effectCause);
        } finally {
            this.arclight$effectCause = null;
        }
    }

    @Override
    public EntityPotionEffectEvent.Action bridge$getAndResetAction() {
        try {
            return this.arclight$action;
        } finally {
            this.arclight$action = null;
        }
    }
}
