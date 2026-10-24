package io.izzel.arclight.common.adventure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

class PaperAdventureTest {
  @Test
  void preservesLegacyPlainAndMiniMessageText() {
    Component component = PaperAdventure.parseMessage("<red>Hello</red>");
    assertEquals("Hello", PaperAdventure.asPlain(component));
    assertEquals("§cHello", PaperAdventure.adventureToLegacy(component));
    assertEquals("Hello", PaperAdventure.asPlain(PaperAdventure.legacyToAdventure("§aHello")));
  }
}
