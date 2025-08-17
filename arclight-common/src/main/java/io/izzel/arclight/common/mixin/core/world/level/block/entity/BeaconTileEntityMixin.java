package io.izzel.arclight.common.mixin.core.world.level.block.entity;

import com.destroystokyo.paper.event.block.BeaconEffectEvent;
import io.izzel.arclight.common.bridge.core.tileentity.BeaconTileEntityBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.potion.CraftPotionUtil;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconTileEntityMixin implements BeaconTileEntityBridge {

    // @formatter:off
    @Shadow @Nullable public MobEffect primaryPower;
    @Shadow public int levels;
    @Shadow @Nullable public MobEffect secondaryPower;
    @Shadow public abstract BlockPos getBlockPos();
    // @formatter:on

    @Inject(method = "load", at = @At("RETURN"))
    public void arclight$level(CompoundTag compound, CallbackInfo ci) {
        this.levels = compound.getInt("Levels");
    }

    public PotionEffect getPrimaryEffect() {
        return (this.primaryPower != null) ? CraftPotionUtil.toBukkit(new MobEffectInstance(this.primaryPower, this.getEffectDuration(), this.getAmplification(), true, true)) : null;
    }

    public PotionEffect getSecondaryEffect() {
        return (this.hasSecondaryEffect()) ? CraftPotionUtil.toBukkit(new MobEffectInstance(this.secondaryPower, getEffectDuration(), getAmplification(), true, true)) : null;
    }

    private byte getAmplification() {
        byte b0 = 0;
        if (this.levels >= 4 && this.primaryPower == this.secondaryPower) {
            b0 = 1;
        }
        return b0;
    }

    private int getEffectDuration() {
        int i = (9 + this.levels * 2) * 20;
        return i;
    }

    private boolean hasSecondaryEffect() {
        return this.levels >= 4 && this.primaryPower != this.secondaryPower && this.secondaryPower != null;
    }

    @Override
    public PotionEffect bridge$getPrimaryEffect() {
        return getPrimaryEffect();
    }

    @Override
    public PotionEffect bridge$getSecondaryEffect() {
        return getSecondaryEffect();
    }

    // Paper start - BeaconEffectEvent
    @Redirect(method = "applyEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    private boolean arclight$beaconEffect(ServerPlayer player, MobEffectInstance effect) {
        // Use this beacon instance
        BeaconBlockEntity beacon = (BeaconBlockEntity) (Object) this;
        Level level = beacon.level;
        BlockPos pos = beacon.getBlockPos();

        if (level != null) {
            // Create the event
            List<Player> players = new ArrayList<>();
            players.add(((io.izzel.arclight.common.bridge.core.entity.player.ServerPlayerEntityBridge) player).bridge$getBukkitEntity());

            PotionEffect bukkitEffect = CraftPotionUtil.toBukkit(effect);
            boolean isPrimary = effect.getEffect() == this.primaryPower;

            BeaconEffectEvent event = new BeaconEffectEvent(
                    CraftBlock.at(level, pos),
                    bukkitEffect,
                    players,
                    isPrimary
            );

            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return false;
            }

            // Apply the potentially modified effect
            MobEffectInstance modifiedEffect = CraftPotionUtil.fromBukkit(event.getEffect());
            return player.addEffect(modifiedEffect);
        }

        // Fallback to normal behavior
        return player.addEffect(effect);
    }
    // Paper end
}
