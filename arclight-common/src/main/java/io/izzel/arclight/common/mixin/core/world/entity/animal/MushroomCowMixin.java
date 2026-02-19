package io.izzel.arclight.common.mixin.core.world.entity.animal;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.world.WorldBridge;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MushroomCow.class)
public abstract class MushroomCowMixin extends AnimalMixin {

    // Forge: ShearsItem#interactLivingEntity
    @Inject(method = "mobInteract", require = 0, cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/MushroomCow;shear(Lnet/minecraft/sounds/SoundSource;)V"))
    private void arclight$onShear(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir, ItemStack stack) {
        if (!CraftEventFactory.handlePlayerShearEntityEvent(player, (Entity) (Object) this, stack, hand)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Redirect(method = "shear", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/MushroomCow;discard()V"))
    private void arclight$animalTransformPre(MushroomCow mushroomCow) {
    }

    @Inject(method = "shear", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private void arclight$animalTransform(SoundSource category, CallbackInfo ci, Cow cowEntity) {
        if (CraftEventFactory.callEntityTransformEvent((MushroomCow) (Object) this, cowEntity, EntityTransformEvent.TransformReason.SHEARED).isCancelled()) {
            ci.cancel();
        } else {
            ((WorldBridge) this.level()).bridge$pushAddEntityReason(CreatureSpawnEvent.SpawnReason.SHEARED);
            this.discard();
        }
    }

    @Redirect(method = "shear",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z", ordinal = 1))
    private boolean arclight$entityDropItemEvent(Level level, Entity entity) {
        if (entity instanceof ItemEntity itemEntity) {
            EntityDropItemEvent event = new EntityDropItemEvent(this.getBukkitEntity(), (org.bukkit.entity.Item) ((EntityBridge) itemEntity).bridge$getBukkitEntity());
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }
        }
        return level.addFreshEntity(entity);
    }
}
