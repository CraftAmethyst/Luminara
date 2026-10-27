package org.craftamethyst.luminara.common.mod.util.log;

import java.util.List;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.core.pattern.PatternParser;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.apache.logging.log4j.util.PropertiesUtil;

/** TerminalConsoleAppender's formatter extended with support for Minecraft RGB colours. */
@Plugin(name = "luminaraFormatting", category = PatternConverter.CATEGORY)
@ConverterKeys({"luminaraFormatting"})
@PerformanceSensitive("allocation")
public final class LuminaraMinecraftFormattingConverter extends LogEventPatternConverter {

  private static final boolean KEEP_FORMATTING =
      PropertiesUtil.getProperties().getBooleanProperty("terminal.keepMinecraftFormatting");

  private final boolean ansi;
  private final List<PatternFormatter> formatters;

  private LuminaraMinecraftFormattingConverter(List<PatternFormatter> formatters, boolean strip) {
    super("luminaraFormatting", null);
    this.formatters = formatters;
    this.ansi = !strip;
  }

  @Override
  public void format(LogEvent event, StringBuilder output) {
    int start = output.length();
    for (PatternFormatter formatter : formatters) {
      formatter.format(event, output);
    }
    if (KEEP_FORMATTING || output.length() == start) {
      return;
    }
    String content = output.substring(start);
    MinecraftConsoleFormatter.format(content, output, start, ansi);
  }

  public static LuminaraMinecraftFormattingConverter newInstance(
      Configuration configuration, String[] options) {
    if (options.length < 1 || options.length > 2) {
      LOGGER.error(
          "Incorrect number of options on luminaraFormatting. Expected at least 1, max 2 received "
              + options.length);
      return null;
    }
    if (options[0] == null) {
      LOGGER.error("No pattern supplied on luminaraFormatting");
      return null;
    }
    PatternParser parser = PatternLayout.createPatternParser(configuration);
    List<PatternFormatter> formatters = parser.parse(options[0]);
    boolean strip = options.length > 1 && "strip".equals(options[1]);
    return new LuminaraMinecraftFormattingConverter(formatters, strip);
  }
}
