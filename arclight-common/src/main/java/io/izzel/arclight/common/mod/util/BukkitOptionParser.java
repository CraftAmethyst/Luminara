package io.izzel.arclight.common.mod.util;

import joptsimple.OptionParser;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

import static java.util.Arrays.asList;

public class BukkitOptionParser extends OptionParser {

    public BukkitOptionParser() {
        acceptsAll(asList("c", "config"), "Properties file to use")
                .withRequiredArg()
                .ofType(File.class)
                .defaultsTo(new File("server.properties"))
                .describedAs("Properties file");

        acceptsAll(asList("P", "plugins"), "Plugin directory to use")
                .withRequiredArg()
                .ofType(File.class)
                .defaultsTo(new File("plugins"))
                .describedAs("Plugin directory");

        // Paper start - Add command line option to load extra plugin jars
        accepts("add-plugin", "Extra plugin jar to load")
                .withRequiredArg()
                .ofType(File.class)
                .describedAs("Plugin jar file");

        acceptsAll(asList("h", "host", "server-ip"), "Host to listen on")
                .withRequiredArg()
                .ofType(String.class)
                .describedAs("Hostname or IP");

        acceptsAll(asList("W", "world-dir", "universe", "world-container"), "World container")
                .withRequiredArg()
                .ofType(File.class)
                .describedAs("Directory containing worlds");

        acceptsAll(asList("w", "world", "level-name"), "World name")
                .withRequiredArg()
                .ofType(String.class)
                .describedAs("World name");

        acceptsAll(asList("p", "port", "server-port"), "Port to listen on")
                .withRequiredArg()
                .ofType(Integer.class)
                .describedAs("Port");

        acceptsAll(asList("o", "online-mode"), "Whether to use online authentication")
                .withRequiredArg()
                .ofType(Boolean.class)
                .describedAs("Authentication");

        acceptsAll(asList("s", "size", "max-players"), "Maximum amount of players")
                .withRequiredArg()
                .ofType(Integer.class)
                .describedAs("Server size");

        acceptsAll(asList("d", "date-format"), "Format of the date to display in the console (for log entries)")
                .withRequiredArg()
                .ofType(SimpleDateFormat.class)
                .describedAs("Log date format");

        acceptsAll(List.of("log-pattern"), "Specfies the log filename pattern")
                .withRequiredArg()
                .ofType(String.class)
                .defaultsTo("server.log")
                .describedAs("Log filename");

        acceptsAll(List.of("log-limit"), "Limits the maximum size of the log file (0 = unlimited)")
                .withRequiredArg()
                .ofType(Integer.class)
                .defaultsTo(0)
                .describedAs("Max log size");

        acceptsAll(List.of("log-count"), "Specified how many log files to cycle through")
                .withRequiredArg()
                .ofType(Integer.class)
                .defaultsTo(1)
                .describedAs("Log count");

        acceptsAll(List.of("log-append"), "Whether to append to the log file")
                .withRequiredArg()
                .ofType(Boolean.class)
                .defaultsTo(true)
                .describedAs("Log append");

        acceptsAll(List.of("log-strip-color"), "Strips color codes from log file");

        acceptsAll(asList("b", "bukkit-settings"), "File for bukkit settings")
                .withRequiredArg()
                .ofType(File.class)
                .defaultsTo(new File("bukkit.yml"))
                .describedAs("Yml file");

        acceptsAll(asList("C", "commands-settings"), "File for command settings")
                .withRequiredArg()
                .ofType(File.class)
                .defaultsTo(new File("commands.yml"))
                .describedAs("Yml file");

        acceptsAll(List.of("forceUpgrade"), "Whether to force a world upgrade");
        acceptsAll(List.of("eraseCache"), "Whether to force cache erase during world upgrade");

        acceptsAll(List.of("nojline"), "Disables jline and emulates the vanilla console");

        acceptsAll(List.of("noconsole"), "Disables the console");

        acceptsAll(List.of("demo"), "Demo mode");

        // Spigot Start
        acceptsAll(asList("S", "spigot-settings"), "File for spigot settings")
                .withRequiredArg()
                .ofType(File.class)
                .defaultsTo(new File("spigot.yml"))
                .describedAs("Yml file");

        allowsUnrecognizedOptions();
    }
}
