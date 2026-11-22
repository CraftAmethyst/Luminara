package org.craftamethyst.luminara.fabric;

import net.minecraft.server.dedicated.DedicatedServer;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Attributes.InputFlag;
import org.jline.terminal.impl.AbstractWindowsTerminal;

public final class FabricTerminalConsole extends SimpleTerminalConsole {

  private final DedicatedServer server;

  public FabricTerminalConsole(DedicatedServer server) {
    this.server = server;
  }

  @Override
  protected LineReader buildReader(LineReaderBuilder builder) {
    var reader = super.buildReader(builder);
    var terminal = reader.getTerminal();
    if (terminal instanceof AbstractWindowsTerminal) {
      reader.getWidgets().put(LineReader.CALLBACK_INIT, () -> {
        // JLine can ignore carriage returns in narrow terminals. Windows Enter sends CR.
        var attributes = terminal.getAttributes();
        attributes.setInputFlag(InputFlag.IGNCR, false);
        attributes.setInputFlag(InputFlag.ICRNL, true);
        attributes.setInputFlag(InputFlag.INLCR, false);
        terminal.setAttributes(attributes);
        return true;
      });
    }
    return reader;
  }

  @Override
  protected boolean isRunning() {
    return server.isRunning() && !server.isStopped();
  }

  @Override
  protected void runCommand(String command) {
    server.handleConsoleInput(command, server.createCommandSourceStack());
  }

  @Override
  protected void shutdown() {
    server.halt(false);
  }
}
