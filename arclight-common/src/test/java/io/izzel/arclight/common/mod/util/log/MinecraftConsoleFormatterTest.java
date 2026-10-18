package io.izzel.arclight.common.mod.util.log;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MinecraftConsoleFormatterTest {

  @Test
  void convertsRgbFormattingToAnsi() {
    assertFormatted(
        "§x§1§2§3§4§5§6Result: §x§F§E§D§C§B§A2.0",
        true,
        "\u001B[0;30;1m\u001B[38;2;18;52;86mResult: "
            + "\u001B[0;37;1m\u001B[38;2;254;220;186m2.0\u001B[m");
  }

  @Test
  void stripsRgbFormattingForFilesAndGui() {
    assertFormatted("§x§1§2§3§4§5§6Result: §x§F§E§D§C§B§A2.0", false, "Result: 2.0");
  }

  @Test
  void removesMalformedRgbPrefixes() {
    assertFormatted("§xResult: §x2.0", true, "Result: 2.0\u001B[m");
  }

  @Test
  void convertsAmpersandRgbFormattingToAnsi() {
    assertFormatted(
        "&x&1&2&3&4&5&6Result: &x&F&E&D&C&B&A2.0",
        true,
        "\u001B[0;30;1m\u001B[38;2;18;52;86mResult: "
            + "\u001B[0;37;1m\u001B[38;2;254;220;186m2.0\u001B[m");
  }

  @Test
  void stripsAmpersandFormattingForFilesAndGui() {
    assertFormatted("&x&1&2&3&4&5&6Result: &x&F&E&D&C&B&A2.0", false, "Result: 2.0");
  }

  @Test
  void preservesUnknownSectionCodes() {
    assertFormatted("Ping §qValue", true, "Ping §qValue\u001B[m");
  }

  @Test
  void keepsExistingLegacyColourBehaviour() {
    assertFormatted("§aGreen §lBold", true, "\u001B[0;32;1mGreen \u001B[21mBold\u001B[m");
    assertFormatted("§aGreen §lBold", false, "Green Bold");
    assertFormatted("&aGreen &lBold", true, "\u001B[0;32;1mGreen \u001B[21mBold\u001B[m");
    assertFormatted("&aGreen &lBold", false, "Green Bold");
  }

  @Test
  void preservesOrdinaryAmpersands() {
    assertFormatted("R & Z", true, "R & Z\u001B[m");
    assertFormatted("R & Z", false, "R & Z");
  }

  @Test
  void onlyRewritesTheFormattedSegment() {
    StringBuilder output = new StringBuilder("prefix ");
    MinecraftConsoleFormatter.format("§x§0§0§0§0§0§0black", output, 7, false);
    assertEquals("prefix black", output.toString());
  }

  private static void assertFormatted(String text, boolean ansi, String expected) {
    StringBuilder output = new StringBuilder();
    MinecraftConsoleFormatter.format(text, output, 0, ansi);
    assertEquals(expected, output.toString());
  }
}
