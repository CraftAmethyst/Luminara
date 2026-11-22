package org.craftamethyst.luminara.common.mod.boot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.Test;

class BannerMessageTest {

  @Test
  void preservesTheOriginalArtInUtf8ConsolesAndLogFiles() {
    String art = "\n  █▓▒░▄▀▌▐\n";
    var message = new BannerMessage(art, StandardCharsets.UTF_8);
    assertEquals(art, render(message, "%msg{console}"));
    assertEquals(art, render(message, "%msg{nolookup}"));
  }

  @Test
  void usesAsciiInGbkConsolesWhileKeepingTheOriginalArtInLogFiles() {
    String art = "\n  █▓▒░▄▀▌▐\n";
    var message = new BannerMessage(art, Charset.forName("GBK"));
    assertEquals("\n  #+:._-||\n", render(message, "%msg{console}"));
    assertEquals(art, render(message, "%msg{nolookup}"));
  }

  @Test
  void colorsVersionFieldsOnlyInTheConsole() {
    var message = BannerMessage.styled("§7Minecraft §b1.20.1 §8| §7Forge §a47.4.0");
    assertEquals(
        "\u001B[0;37mMinecraft \u001B[0;36;1m1.20.1 \u001B[0;30;1m| "
            + "\u001B[0;37mForge \u001B[0;32;1m47.4.0\u001B[m",
        render(message, "%msg{console}"));
    assertEquals("Minecraft 1.20.1 | Forge 47.4.0", render(message, "%msg{nolookup}"));
  }

  private static String render(BannerMessage message, String pattern) {
    var event = Log4jLogEvent.newBuilder().setLevel(Level.INFO).setMessage(message).build();
    return PatternLayout.newBuilder().withPattern(pattern).build().toSerializable(event);
  }
}
