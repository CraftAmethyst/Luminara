package io.izzel.arclight.common.mod.util;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecrell.terminalconsole.TerminalConsoleAppender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

// see net.minecraftforge.server.console.TerminalHandler
public class ArclightTerminalReader {

    private static final Logger LOGGER = LogManager.getLogger("Luminara");

    public static boolean handleCommands(DedicatedServer server) {
        var terminal = TerminalConsoleAppender.getTerminal();
        // The terminal appender owns the terminal whenever Log4j could load it. On
        // the standalone Fabric mod Log4j cannot see the bundled appender, so the
        // console has no terminal yet; create one here so the server console still
        // gets a line reader and command completion.
        boolean managed = terminal != null;
        if (!managed) {
            terminal = createTerminal();
            if (terminal == null) {
                LOGGER.debug(
                    "No console terminal available, falling back to the default server console reader"
                );
                return false;
            }
        }

        LineReader reader;
        try {
            reader = LineReaderBuilder.builder()
                .appName("Arclight")
                .terminal(terminal)
                .completer(new ConsoleCommandCompleter(server))
                .build();
        } catch (Throwable t) {
            LOGGER.debug("Failed to create the console reader", t);
            if (!managed) {
                closeQuietly(terminal);
            }
            return false;
        }
        reader.setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION);
        reader.unsetOpt(LineReader.Option.INSERT_TAB);

        if (managed) {
            TerminalConsoleAppender.setReader(reader);
        }
        LOGGER.debug(
            "Console reader started on {}",
            terminal.getClass().getName()
        );

        try {
            String line;
            while (!server.isStopped() && server.isRunning()) {
                try {
                    line = reader.readLine("> ");
                } catch (EndOfFileException ignored) {
                    // Continue reading after EOT
                    continue;
                } catch (UserInterruptException e) {
                    // DONT USE CLOSE, YOU AREN'T GOING TO SHUT IT DOWN IN ANOTHER THREAD!
                    server.halt(true);
                    break;
                }

                if (line == null)
                    break;

                line = line.trim();
                if (!line.isEmpty()) {
                    server.handleConsoleInput(line, server.createCommandSourceStack());
                }
            }
        } finally {
            if (managed) {
                TerminalConsoleAppender.setReader(null);
            } else {
                closeQuietly(terminal);
            }
        }
        return true;
    }

    private static Terminal createTerminal() {
        try {
            var builder = TerminalBuilder.builder().name("Arclight").system(true);
            String encoding = System.getProperty("luminara.console.charset");
            if (encoding != null && !encoding.isBlank()) {
                builder.encoding(encoding);
            }
            return builder.build();
        } catch (Throwable t) {
            LOGGER.debug("Failed to create the console terminal", t);
            return null;
        }
    }

    private static void closeQuietly(Terminal terminal) {
        try {
            terminal.close();
        } catch (IOException ignored) {
        }
    }

    static final class ConsoleCommandCompleter implements Completer {
        private static final Logger logger = LogManager.getLogger();
        private final DedicatedServer server;

        public ConsoleCommandCompleter(DedicatedServer server) {
            this.server = Preconditions.checkNotNull(server, "server");
        }

        @Override
        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
            String buffer = line.line();
            boolean prefix;
            if (buffer.isEmpty() || buffer.charAt(0) != '/') {
                buffer = '/' + buffer;
                prefix = false;
            } else {
                prefix = true;
            }

            final String input = buffer;
            //See NetHandlerPlayServer#processTabComplete
            StringReader stringReader = new StringReader(input);
            if (stringReader.canRead() && stringReader.peek() == '/')
                stringReader.skip();

            try {
                ParseResults<CommandSourceStack> results = this.server.getCommands().getDispatcher().parse(stringReader, this.server.createCommandSourceStack());
                Suggestions tabComplete = this.server.getCommands().getDispatcher().getCompletionSuggestions(results).get();
                for (Suggestion suggestion : tabComplete.getList()) {
                    String completion = suggestion.getText();
                    if (!completion.isEmpty()) {
                        boolean hasPrefix = prefix || completion.charAt(0) != '/';
                        Candidate candidate = new Candidate(hasPrefix ? completion : completion.substring(1));
                        candidates.add(candidate);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                logger.error("Failed to tab complete", e);
            }
        }

    }
}
