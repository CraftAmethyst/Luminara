package io.izzel.arclight.common.mixin.core.world.entity.animal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@Mixin(Cow.class)
public abstract class CowMixin extends AnimalMixin {

    private static final MethodHandle ARCLIGHT$ANIMAL_MOB_INTERACT = arclight$findAnimalMobInteract();

    private static MethodHandle arclight$findAnimalMobInteract() {
        var lookup = MethodHandles.lookup();
        var type = MethodType.methodType(InteractionResult.class, Player.class, InteractionHand.class);
        ReflectiveOperationException error = null;
        for (String name : new String[]{"mobInteract", "method_5992"}) {
            try {
                return lookup.findSpecial(Animal.class, name, type, Cow.class);
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

    private InteractionResult arclight$invokeAnimalMobInteract(Player player, InteractionHand hand) {
        try {
            return (InteractionResult) ARCLIGHT$ANIMAL_MOB_INTERACT.invokeExact((Cow) (Object) this, player, hand);
        } catch (Throwable throwable) {
            throw new RuntimeException("Failed to invoke Animal#mobInteract super implementation", throwable);
        }
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    public InteractionResult mobInteract(Player playerEntity, InteractionHand hand) {
        ItemStack itemstack = playerEntity.getItemInHand(hand);
        if (itemstack.getItem() == Items.BUCKET && !this.isBaby()) {
            playerEntity.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
            org.bukkit.event.player.PlayerBucketFillEvent event = CraftEventFactory.callPlayerBucketFillEvent((ServerLevel) playerEntity.level(), playerEntity, this.blockPosition(), this.blockPosition(), null, itemstack, Items.MILK_BUCKET, hand);

            if (event.isCancelled()) {
                return InteractionResult.PASS;
            }
            ItemStack itemstack1 = ItemUtils.createFilledResult(itemstack, playerEntity, CraftItemStack.asNMSCopy(event.getItemStack()));
            playerEntity.setItemInHand(hand, itemstack1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else {
            return this.arclight$invokeAnimalMobInteract(playerEntity, hand);
        }
    }
}
