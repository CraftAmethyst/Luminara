package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.mixins.annotation.LoadIfMod;
import io.izzel.arclight.common.mod.server.entity.ArclightFakePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.FakePlayer;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@LoadIfMod(modid = "forge", condition = LoadIfMod.ModCondition.PRESENT)
@Mixin(value = CraftEntity.class, remap = false)
public abstract class CraftEntityMixin_Forge {

    @Inject(method = "getEntity", cancellable = true, at = @At("HEAD"))
    private static void arclight$forge$fakePlayer(CraftServer server, Entity entity, CallbackInfoReturnable<CraftEntity> cir) {
        if (entity instanceof FakePlayer) {
            cir.setReturnValue(new ArclightFakePlayer(server, (FakePlayer) entity));
        }
    }
}
