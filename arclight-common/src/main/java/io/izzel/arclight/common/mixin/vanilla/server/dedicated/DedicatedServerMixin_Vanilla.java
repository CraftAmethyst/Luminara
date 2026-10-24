package io.izzel.arclight.common.mixin.vanilla.server.dedicated;

import io.izzel.arclight.common.bridge.core.server.dedicated.DedicatedServerBridge;
import java.io.IOException;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecrell.terminalconsole.TerminalConsoleAppender;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin_Vanilla implements DedicatedServerBridge {

  @Override
  public void bridge$platform$exitNow() {
    try {
      TerminalConsoleAppender.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
