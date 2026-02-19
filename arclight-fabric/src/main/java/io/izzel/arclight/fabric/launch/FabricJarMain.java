package io.izzel.arclight.fabric.launch;

import io.izzel.arclight.api.ArclightVersion;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public final class FabricJarMain {

    private static final int MIN_CLASS_VERSION = 61;
    private static final int MIN_JAVA_VERSION = 17;
    private static final String EULA_URL = "https://account.mojang.com/documents/minecraft_eula";
    private static final String EULA_FILE = "eula.txt";
    private static final String CONFIG_FILE = "luminara.yml";
    private static final String LOG4J_CONFIG_PROPERTY = "log4j.configurationFile";
    private static final String LOG4J_CONFIG_SIMPLE = "arclight-log4j2.xml";
    private static final String LOG4J_CONFIG_DETAILED = "arclight-log4j2-detailed.xml";
    private static final String LOG4J_SKIP_JANSI_PROPERTY = "log4j.skipJansi";
    private static final String JUL_FORMAT_PROPERTY = "java.util.logging.SimpleFormatter.format";
    private static final String JUL_FORMAT_VALUE = "[%1$tT] [JUL] %5$s%6$s%n";
    private static final String TERMINAL_JLINE_PROPERTY = "terminal.jline";
    private static final String TERMINAL_ANSI_PROPERTY = "terminal.ansi";
    private static final String TERMINAL_PROVIDER_PROPERTY = "org.jline.terminal.provider";
    private static final String TERMINAL_PROVIDER_JNA = "jna";
    private static final String TERMINAL_ENCODING_PROPERTY = "org.jline.terminal.encoding";
    private static final String CONSOLE_CHARSET_PROPERTY = "luminara.console.charset";
    private static final String STDOUT_ENCODING_PROPERTY = "stdout.encoding";
    private static final String SUN_STDOUT_ENCODING_PROPERTY = "sun.stdout.encoding";
    private static final String NATIVE_ENCODING_PROPERTY = "native.encoding";
    private static final String FILE_ENCODING_PROPERTY = "file.encoding";
    private static final String ANSI_FLAG_PROPERTY = "terminal.ansi";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_GOLD = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_MAGENTA = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";
    private static final String ANSI_BRIGHT_BLACK = "\u001B[90m";
    private static final String ANSI_BRIGHT_RED = "\u001B[91m";
    private static final String ANSI_BRIGHT_GREEN = "\u001B[92m";
    private static final String ANSI_BRIGHT_YELLOW = "\u001B[93m";
    private static final String ANSI_BRIGHT_BLUE = "\u001B[94m";
    private static final String ANSI_BRIGHT_MAGENTA = "\u001B[95m";
    private static final String ANSI_AQUA = "\u001B[96m";
    private static final String ANSI_BRIGHT_WHITE = "\u001B[97m";
    private static final String DEFAULT_FALLBACK_LOCALE = "zh_cn";
    private static final String I18N_RESOURCE_PREFIX = "/META-INF/i18n/";

    private FabricJarMain() {
    }

    public static void main(String[] args) throws Throwable {
        configureLoggingDefaults();
        printEarlyBanner();

        int javaVersion = (int) Float.parseFloat(System.getProperty("java.class.version"));
        if (javaVersion < MIN_CLASS_VERSION) {
            System.err.println("Luminara Fabric requires Java " + MIN_JAVA_VERSION);
            System.err.println("Current: " + System.getProperty("java.version"));
            System.exit(-1);
            return;
        }

        if (!checkEula()) {
            System.err.println("You need to agree to the EULA to run the server.");
            System.exit(-1);
            return;
        }

        Main_Fabric.main(args);
    }

    static void configureLoggingDefaults() {
        if (System.getProperty(LOG4J_SKIP_JANSI_PROPERTY) == null) {
            System.setProperty(LOG4J_SKIP_JANSI_PROPERTY, "false");
        }
        if (System.getProperty(JUL_FORMAT_PROPERTY) == null) {
            System.setProperty(JUL_FORMAT_PROPERTY, JUL_FORMAT_VALUE);
        }
        if (System.getProperty(TERMINAL_JLINE_PROPERTY) == null) {
            System.setProperty(TERMINAL_JLINE_PROPERTY, "true");
        }
        if (System.getProperty(TERMINAL_ANSI_PROPERTY) == null) {
            System.setProperty(TERMINAL_ANSI_PROPERTY, "true");
        }
        if (System.getProperty(TERMINAL_PROVIDER_PROPERTY) == null) {
            System.setProperty(TERMINAL_PROVIDER_PROPERTY, TERMINAL_PROVIDER_JNA);
        }
        String charset = detectConsoleCharset();
        if (System.getProperty(CONSOLE_CHARSET_PROPERTY) == null) {
            System.setProperty(CONSOLE_CHARSET_PROPERTY, charset);
        }
        if (System.getProperty(TERMINAL_ENCODING_PROPERTY) == null) {
            System.setProperty(TERMINAL_ENCODING_PROPERTY, charset);
        }
        if (System.getProperty(LOG4J_CONFIG_PROPERTY) == null) {
            System.setProperty(LOG4J_CONFIG_PROPERTY, resolveInitialLog4jConfig());
        }
    }

    private static String detectConsoleCharset() {
        List<String> keys = List.of(STDOUT_ENCODING_PROPERTY, SUN_STDOUT_ENCODING_PROPERTY, NATIVE_ENCODING_PROPERTY, FILE_ENCODING_PROPERTY);
        for (String key : keys) {
            String value = System.getProperty(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return StandardCharsets.UTF_8.name();
    }

    private static String resolveInitialLog4jConfig() {
        return readUseSimpleFormatFromConfig() ? LOG4J_CONFIG_SIMPLE : LOG4J_CONFIG_DETAILED;
    }

    private static boolean readUseSimpleFormatFromConfig() {
        Path configPath = Paths.get(CONFIG_FILE);
        if (!Files.exists(configPath)) {
            return false;
        }
        try {
            List<String> lines = Files.readAllLines(configPath, StandardCharsets.UTF_8);
            boolean inLogging = false;
            for (String line : lines) {
                if (line == null || line.isEmpty()) {
                    continue;
                }
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                if (!Character.isWhitespace(line.charAt(0))) {
                    inLogging = false;
                }
                if ("logging:".equals(trimmed)) {
                    inLogging = true;
                    continue;
                }
                if (inLogging && trimmed.startsWith("use-simple-format:")) {
                    String value = trimmed.substring("use-simple-format:".length()).trim();
                    int commentIndex = value.indexOf('#');
                    if (commentIndex >= 0) {
                        value = value.substring(0, commentIndex).trim();
                    }
                    return "true".equalsIgnoreCase(value);
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private static void printEarlyBanner() {
        String version = "unknown";
        String buildTime = "unknown";
        try (InputStream stream = FabricJarMain.class.getModule().getResourceAsStream("/META-INF/MANIFEST.MF")) {
            if (stream != null) {
                Manifest manifest = new Manifest(stream);
                Attributes attributes = manifest.getMainAttributes();
                String v = attributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
                String t = attributes.getValue("Implementation-Timestamp");
                if (v != null && !v.isBlank()) {
                    version = v;
                }
                if (t != null && !t.isBlank()) {
                    buildTime = t;
                }
            }
        } catch (Exception ignored) {
        }
        var localeSetting = resolveLocaleSetting();
        String release = resolveReleaseName(localeSetting.current(), localeSetting.fallback());
        List<String> logoLines = resolveLogoLines(localeSetting.current(), localeSetting.fallback());
        List<String> renderedLogo = renderLogoLines(logoLines, release, version, buildTime);
        boolean ansi = !"false".equalsIgnoreCase(System.getProperty(ANSI_FLAG_PROPERTY, "true"));
        for (String line : renderedLogo) {
            System.out.println(ansi ? renderMinecraftColors(line) : stripMinecraftColors(line));
        }
    }

    private static LocaleSetting resolveLocaleSetting() {
        String current = systemLocale();
        String fallback = DEFAULT_FALLBACK_LOCALE;
        Path configPath = Paths.get(CONFIG_FILE);
        if (!Files.exists(configPath)) {
            return new LocaleSetting(current, fallback);
        }
        try {
            List<String> lines = Files.readAllLines(configPath, StandardCharsets.UTF_8);
            boolean inLocale = false;
            for (String line : lines) {
                if (line == null || line.isEmpty()) {
                    continue;
                }
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                if (!Character.isWhitespace(line.charAt(0))) {
                    inLocale = false;
                }
                if ("locale:".equals(trimmed)) {
                    inLocale = true;
                    continue;
                }
                if (inLocale && trimmed.startsWith("current:")) {
                    String value = normalizeYamlValue(trimmed.substring("current:".length()));
                    if (!value.isBlank()) {
                        current = value;
                    }
                    continue;
                }
                if (inLocale && trimmed.startsWith("fallback:")) {
                    String value = normalizeYamlValue(trimmed.substring("fallback:".length()));
                    if (!value.isBlank()) {
                        fallback = value;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return new LocaleSetting(current, fallback);
    }

    private static String resolveReleaseName(String currentLocale, String fallbackLocale) {
        String key;
        try {
            key = ArclightVersion.current().getReleaseName();
        } catch (Throwable ignored) {
            key = "Trials";
        }
        String value = readReleaseName(currentLocale, key);
        if ((value == null || value.isBlank()) && !fallbackLocale.equalsIgnoreCase(currentLocale)) {
            value = readReleaseName(fallbackLocale, key);
        }
        if (value == null || value.isBlank()) {
            value = key;
        }
        return value;
    }

    private static List<String> resolveLogoLines(String currentLocale, String fallbackLocale) {
        List<String> lines = readLogoLines(currentLocale);
        if (lines.isEmpty() && !fallbackLocale.equalsIgnoreCase(currentLocale)) {
            lines = readLogoLines(fallbackLocale);
        }
        if (lines.isEmpty()) {
            lines = List.of(
                    "",
                    "Luminara",
                    ""
            );
        }
        return lines;
    }

    private static List<String> readLogoLines(String locale) {
        String resource = I18N_RESOURCE_PREFIX + locale + ".yml";
        List<String> lines = new ArrayList<>();
        try (InputStream stream = FabricJarMain.class.getResourceAsStream(resource)) {
            if (stream == null) {
                return List.of();
            }
            List<String> content = readAllLines(stream);
            boolean inLogo = false;
            for (String line : content) {
                if (line == null) {
                    continue;
                }
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                if (!Character.isWhitespace(line.charAt(0))) {
                    if (inLogo) {
                        break;
                    }
                    inLogo = "logo:".equals(trimmed);
                    continue;
                }
                if (!inLogo) {
                    continue;
                }
                String indented = line.stripLeading();
                if (!indented.startsWith("-")) {
                    continue;
                }
                String value = indented.substring(1).trim();
                lines.add(normalizeYamlValue(value));
            }
        } catch (Exception ignored) {
        }
        return lines;
    }

    private static String readReleaseName(String locale, String releaseKey) {
        String resource = I18N_RESOURCE_PREFIX + locale + ".yml";
        try (InputStream stream = FabricJarMain.class.getResourceAsStream(resource)) {
            if (stream == null) {
                return null;
            }
            List<String> content = readAllLines(stream);
            boolean inRelease = false;
            for (String line : content) {
                if (line == null) {
                    continue;
                }
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                if (!Character.isWhitespace(line.charAt(0))) {
                    if (inRelease) {
                        break;
                    }
                    inRelease = "release-name:".equals(trimmed);
                    continue;
                }
                if (!inRelease) {
                    continue;
                }
                String indented = line.stripLeading();
                int colon = indented.indexOf(':');
                if (colon <= 0) {
                    continue;
                }
                String key = indented.substring(0, colon).trim();
                if (!key.equalsIgnoreCase(releaseKey)) {
                    continue;
                }
                return normalizeYamlValue(indented.substring(colon + 1));
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static List<String> readAllLines(InputStream stream) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private static String normalizeYamlValue(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim();
        int comment = normalized.indexOf('#');
        if (comment >= 0) {
            normalized = normalized.substring(0, comment).trim();
        }
        if (normalized.length() >= 2) {
            if ((normalized.startsWith("'") && normalized.endsWith("'")) || (normalized.startsWith("\"") && normalized.endsWith("\""))) {
                normalized = normalized.substring(1, normalized.length() - 1);
            }
        }
        return normalized.replace("''", "'");
    }

    private static String systemLocale() {
        Locale locale = Locale.getDefault();
        if (locale == null) {
            return "en_us";
        }
        return locale.getLanguage().toLowerCase(Locale.ROOT) + "_" + locale.getCountry().toLowerCase(Locale.ROOT);
    }

    private static List<String> renderLogoLines(List<String> lines, String release, String version, String buildTime) {
        if (lines.isEmpty()) {
            return lines;
        }
        String joined = String.join("\n", lines);
        joined = joined.replaceFirst("\\{}", release == null ? "" : release);
        joined = joined.replaceFirst("\\{}", version == null ? "" : version);
        joined = joined.replaceFirst("\\{}", buildTime == null ? "" : buildTime);
        return List.of(joined.split("\n", -1));
    }

    private static String stripMinecraftColors(String line) {
        StringBuilder sb = new StringBuilder(line.length());
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\u00A7' && i + 1 < line.length()) {
                i++;
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private static String renderMinecraftColors(String line) {
        StringBuilder sb = new StringBuilder(line.length() + 16);
        sb.append(ANSI_RESET);
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\u00A7' && i + 1 < line.length()) {
                String mapped = mapColorCode(line.charAt(++i));
                if (mapped != null) {
                    sb.append(mapped);
                }
                continue;
            }
            sb.append(c);
        }
        sb.append(ANSI_RESET);
        return sb.toString();
    }

    private static String mapColorCode(char code) {
        return switch (Character.toLowerCase(code)) {
            case '0' -> ANSI_BLACK;
            case '1' -> ANSI_BLUE;
            case '2' -> ANSI_GREEN;
            case '3' -> ANSI_CYAN;
            case '4' -> ANSI_RED;
            case '5' -> ANSI_MAGENTA;
            case '6' -> ANSI_GOLD;
            case '7' -> ANSI_WHITE;
            case '8' -> ANSI_BRIGHT_BLACK;
            case '9' -> ANSI_BRIGHT_BLUE;
            case 'a' -> ANSI_BRIGHT_GREEN;
            case 'b' -> ANSI_AQUA;
            case 'c' -> ANSI_BRIGHT_RED;
            case 'd' -> ANSI_BRIGHT_MAGENTA;
            case 'e' -> ANSI_BRIGHT_YELLOW;
            case 'f' -> ANSI_BRIGHT_WHITE;
            case 'r' -> ANSI_RESET;
            default -> null;
        };
    }

    private record LocaleSetting(String current, String fallback) {
    }

    private static boolean checkEula() throws IOException {
        File eulaFile = new File(EULA_FILE);
        if (eulaFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(eulaFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().startsWith("eula=")) {
                        String value = line.trim().substring(5);
                        if (value.trim().equalsIgnoreCase("true")) {
                            return true;
                        }
                        System.out.println("EULA is currently set to \"" + value.trim() + "\" and must be accepted.");
                        return promptUserForEula(eulaFile, true);
                    }
                }
            }
        }
        return promptUserForEula(eulaFile, false);
    }

    private static boolean promptUserForEula(File eulaFile, boolean isUpdating) throws IOException {
        System.out.println("By running this server, you agree to the Minecraft EULA.");
        System.out.println("Read the EULA at: " + EULA_URL);
        System.out.print("Do you agree to the EULA? (y/N): ");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String input = reader.readLine();
            if (input != null && input.trim().toLowerCase().startsWith("y")) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(eulaFile))) {
                    writer.write("# By changing the setting below to TRUE you are indicating your agreement to our EULA (https://account.mojang.com/documents/minecraft_eula).\n");
                    writer.write("# Generated via Luminara Fabric launcher\n");
                    writer.write("eula=true\n");
                }
                System.out.println("EULA has been " + (isUpdating ? "updated" : "accepted") + " and saved.");
                return true;
            }
            System.out.println("EULA not accepted. Server will not start.");
            return false;
        }
    }
}
