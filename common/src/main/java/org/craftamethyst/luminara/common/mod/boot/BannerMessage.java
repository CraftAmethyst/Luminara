package org.craftamethyst.luminara.common.mod.boot;

import java.nio.charset.Charset;
import java.util.Arrays;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.util.MultiFormatStringBuilderFormattable;
import org.craftamethyst.luminara.common.mod.util.log.MinecraftConsoleFormatter;

final class BannerMessage extends SimpleMessage implements MultiFormatStringBuilderFormattable {

  private final String consoleText;

  BannerMessage(String text, Charset consoleEncoding) {
    this(
        text,
        consoleEncoding.newEncoder().canEncode(text)
            ? text
            : text.replace('█', '#')
                .replace('▓', '+')
                .replace('▒', ':')
                .replace('░', '.')
                .replace('▄', '_')
                .replace('▀', '-')
                .replace('▌', '|')
                .replace('▐', '|'));
  }

  private BannerMessage(String text, String consoleText) {
    super(text);
    this.consoleText = consoleText;
  }

  static BannerMessage styled(String text) {
    var plain = new StringBuilder(text);
    var colored = new StringBuilder(text);
    MinecraftConsoleFormatter.format(text, plain, 0, false);
    MinecraftConsoleFormatter.format(text, colored, 0, true);
    return new BannerMessage(plain.toString(), colored.toString());
  }

  @Override
  public String[] getFormats() {
    return new String[] {"console"};
  }

  @Override
  public String getFormattedMessage(String[] formats) {
    return Arrays.asList(formats).contains("console") ? consoleText : getFormattedMessage();
  }

  @Override
  public void formatTo(String[] formats, StringBuilder buffer) {
    buffer.append(getFormattedMessage(formats));
  }
}
