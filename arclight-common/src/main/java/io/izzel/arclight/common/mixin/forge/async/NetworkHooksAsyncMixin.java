package io.izzel.arclight.common.mixin.forge.async;

import io.izzel.arclight.common.mod.util.async.AsyncCatcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraftforge.network.NetworkHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(value = NetworkHooks.class, remap = false)
public abstract class NetworkHooksAsyncMixin {

    @Shadow public static void openScreen(ServerPlayer player, MenuProvider provider, Consumer<FriendlyByteBuf> writer) { }

    @Inject(
        method = "openScreen(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/MenuProvider;Ljava/util/function/Consumer;)V",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private static void arclight$guardOpenGui(
        ServerPlayer player,
        MenuProvider provider,
        Consumer<FriendlyByteBuf> writer,
        CallbackInfo ci
    ) {
        if (
            AsyncCatcher.checkVoid(
                "open gui",
                () -> {
                    openScreen(player, provider, writer);
                    return Boolean.TRUE;
                }
            )
        ) {
            ci.cancel();
        }
    }
}