package io.izzel.arclight.common.mixin.core.server;

import net.minecraft.server.MinecraftServer;
import org.spigotmc.AsyncCatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "net/minecraft/server/Main$1")
public class Main_ServerShutdownThreadMixin {

    @SuppressWarnings("target")
    @Redirect(method = "run", at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/server/MinecraftServer;halt(Z)V"))
    private void arclight$shutdown(MinecraftServer instance, boolean b) {
        AsyncCatcher.enabled = false;
        instance.close();
    }
}
