package org.craftamethyst.luminara.fabric.mixin.core.server.dedicated;

import net.minecraft.server.dedicated.DedicatedServer;
import net.minecrell.terminalconsole.TerminalConsoleAppender;
import org.craftamethyst.luminara.fabric.FabricTerminalConsole;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DedicatedServer.class)
public class DedicatedServerConsoleMixin {

  @Redirect(
      method = "initServer",
      at = @At(value = "INVOKE", target = "Ljava/lang/Thread;start()V", ordinal = 0))
  private void luminara$startConsole(Thread vanillaConsole) {
    if (TerminalConsoleAppender.getTerminal() == null) {
      vanillaConsole.start();
      return;
    }

    var console = new FabricTerminalConsole((DedicatedServer) (Object) this);
    var thread = new Thread(console::start, vanillaConsole.getName());
    thread.setDaemon(vanillaConsole.isDaemon());
    thread.setUncaughtExceptionHandler(vanillaConsole.getUncaughtExceptionHandler());
    thread.start();
  }
}
