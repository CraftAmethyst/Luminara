package io.izzel.arclight.common.adventure;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaperAdventureTest {
    @Test
    void preservesLegacyPlainAndMiniMessageText() {
        Component component = PaperAdventure.parseMessage("<red>Hello</red>");
        assertEquals("Hello", PaperAdventure.asPlain(component));
        assertEquals("§cHello", PaperAdventure.adventureToLegacy(component));
        assertEquals("Hello", PaperAdventure.asPlain(PaperAdventure.legacyToAdventure("§aHello")));
    }
}
