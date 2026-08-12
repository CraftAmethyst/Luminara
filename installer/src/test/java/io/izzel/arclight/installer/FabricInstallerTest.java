package io.izzel.arclight.installer;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FabricInstallerTest {

    @Test
    void loadsStandaloneJansiBeforeJlineTwo() {
        var libraries = List.of(
            "jline:jline:2.12.1",
            "org.jline:jline-terminal-jansi:3.20.0",
            "org.fusesource.jansi:jansi:2.3.2",
            "net.minecrell:terminalconsoleappender:1.3.0"
        );

        assertEquals(List.of(
            "org.fusesource.jansi:jansi:2.3.2",
            "net.minecrell:terminalconsoleappender:1.3.0",
            "org.jline:jline-terminal-jansi:3.20.0",
            "jline:jline:2.12.1"
        ), FabricInstaller.orderedGameLibraries(libraries));
    }
}
