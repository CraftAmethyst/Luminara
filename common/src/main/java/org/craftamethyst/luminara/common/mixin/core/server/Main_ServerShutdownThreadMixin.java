package org.craftamethyst.luminara.common.mixin.core.server;

import net.minecraft.server.MinecraftServer;
import org.spigotmc.AsyncCatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net/minecraft/server/Main$1")
public class Main_ServerShutdownThreadMixin {

  @Redirect(
      method = "run",
      at =
          @At(
              value = "INVOKE",
              remap = true,
              target = "Lnet/minecraft/server/MinecraftServer;halt(Z)V"))
  private void arclight$shutdown(MinecraftServer instance, boolean b) {
    AsyncCatcher.enabled = false;
    // Signal the server loop to stop and wait for it when shutdown is externally
    // initiated. If it is already stopping (for example during a restart), waiting
    // would deadlock because the server thread is waiting for the JVM to exit.
    instance.halt(instance.isRunning() && b);
  }
}
