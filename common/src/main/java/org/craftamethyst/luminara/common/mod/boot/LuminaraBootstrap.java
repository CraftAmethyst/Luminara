package org.craftamethyst.luminara.common.mod.boot;

import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.reflect.TypeToken;
import io.izzel.arclight.api.ArclightVersion;
import io.izzel.arclight.api.EnumHelper;
import io.izzel.arclight.api.Unsafe;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import net.minecrell.terminalconsole.TerminalConsoleAppender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.plugins.util.PluginRegistry;
import org.apache.logging.log4j.message.Message;
import org.craftamethyst.luminara.common.mod.util.log.LuminaraMinecraftFormattingConverter;
import org.craftamethyst.luminara.i18n.ArclightLocale;
import org.craftamethyst.luminara.i18n.LuminaraVersion;

public final class LuminaraBootstrap {

  private static final String SOURCE_URL = "https://github.com/CraftAmethyst/Luminara";

  private static boolean platformApplied;
  private static boolean gsonEnumFactoryApplied;
  private static boolean loggingPluginsRegistered;

  private LuminaraBootstrap() {}

  public static synchronized void applyPlatform() {
    if (platformApplied) {
      return;
    }
    try {
      ArclightVersion.current();
    } catch (IllegalStateException notSet) {
      ArclightVersion.setVersion(ArclightVersion.TRIALS);
    }
    if (System.getProperty("arclight.version") == null) {
      System.setProperty("arclight.version", LuminaraVersion.version());
    }
    platformApplied = true;
  }

  public static synchronized void registerLoggingPlugins() {
    if (loggingPluginsRegistered) {
      return;
    }
    PluginRegistry.getInstance()
        .loadFromBundle(
            0x4c554d494e4152L, LuminaraMinecraftFormattingConverter.class.getClassLoader());
    loggingPluginsRegistered = true;
  }

  // Gson's stock enum factory rejects the constants Luminara adds to the Bukkit enums.
  public static synchronized void installGsonEnumFactory() {
    if (gsonEnumFactoryApplied) {
      return;
    }
    TypeAdapters.ENUM_FACTORY.create(null, TypeToken.get(Object.class));
    Field field;
    try {
      field = TypeAdapters.class.getDeclaredField("ENUM_FACTORY");
    } catch (NoSuchFieldException e) {
      throw new IllegalStateException(
          "Unsupported Gson TypeAdapters for Minecraft 1.20.1 / Forge 47", e);
    }
    Unsafe.putObjectVolatile(
        Unsafe.staticFieldBase(field), Unsafe.staticFieldOffset(field), new EnumTypeFactory());
    gsonEnumFactoryApplied = true;
  }

  public static void logBanner(String loader, String loaderVersion) {
    var locale = ArclightLocale.getInstance();
    var terminal = TerminalConsoleAppender.getTerminal();
    var console = System.console();
    Charset encoding =
        terminal != null
            ? terminal.encoding()
            : console != null ? console.charset() : Charset.defaultCharset();
    Message banner =
        new BannerMessage(
            """

               ██▓     █    ██  ███▄ ▄███▓ ██▓ ███▄    █  ▄▄▄       ██▀███   ▄▄▄
              ▓██▒     ██  ▓██▒▓██▒▀█▀ ██▒▓██▒ ██ ▀█   █ ▒████▄    ▓██ ▒ ██▒▒████▄
              ▒██░    ▓██  ▒██░▓██    ▓██░▒██▒▓██  ▀█ ██▒▒██  ▀█▄  ▓██ ░▄█ ▒▒██  ▀█▄
              ▒██░    ▓▓█  ░██░▒██    ▒██ ░██░▓██▒  ▐▌██▒░██▄▄▄▄██ ▒██▀▀█▄  ░██▄▄▄▄██
              ░██████▒▒▒█████▓ ▒██▒   ░██▒░██░▒██░   ▓██░ ▓█   ▓██▒░██▓ ▒██▒ ▓█   ▓██▒
              ░ ▒░▓  ░░▒▓▒ ▒ ▒ ░ ▒░   ░  ░░▓  ░ ▒░   ▒ ▒  ▒▒   ▓▒█░░ ▒▓ ░▒▓░ ▒▒   ▓▒█░
              ░ ░ ▒  ░░░▒░ ░ ░ ░  ░      ░ ▒ ░░ ░░   ░ ▒░  ▒   ▒▒ ░  ░▒ ░ ▒░  ▒   ▒▒ ░
                ░ ░    ░░░ ░ ░ ░      ░    ▒ ░   ░   ░ ░   ░   ▒     ░░   ░   ░   ▒
                  ░  ░   ░            ░    ░           ░       ░  ░   ░           ░  ░
            """,
            encoding);
    LogManager.getLogger("Luminara.Banner").info(banner);
    Message details =
        BannerMessage.styled(
            """
              §7Luminara §e%s §8[§d%s§8]
              §7Minecraft §b%s §8 |  §7%s §a%s §8 |  §7Java §e%s §8 |  §7Bukkit §d%s\
            """
                .formatted(
                    LuminaraVersion.version(),
                    ArclightVersion.current().getReleaseName(),
                    LuminaraVersion.minecraftVersion(),
                    loader,
                    loaderVersion,
                    Runtime.version().feature(),
                    LuminaraVersion.bukkitPackage()));
    LogManager.getLogger("Luminara.Banner.Details").info(details);
    LogManager.getLogger("Luminara.Banner.Credits").info(
        """
          {}
          {}  {}
        """,
        locale.get("startup-banner.developer"),
        locale.get("startup-banner.source"),
        SOURCE_URL);
  }

  public static void warmUpEnumHelper() {
    Unsafe.ensureClassInitialized(EnumHelper.class);
  }

  // Reflective on purpose: Forge owns org.apache.logging.log4j, so linking against the JUL
  // bridge directly throws NoClassDefFoundError when a plugin loads.
  public static boolean isJulBridgeActive() {
    return java.util.logging.LogManager.getLogManager()
        .getClass()
        .getName()
        .equals("org.apache.logging.log4j.jul.LogManager");
  }
}
