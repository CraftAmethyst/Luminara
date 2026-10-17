package io.izzel.arclight.common.mod.util.log;

/**
 * Converts Minecraft section-sign formatting to terminal ANSI codes.
 *
 * <p>TerminalConsoleAppender 1.2.0 understands the legacy sixteen colours but treats
 * the {@code §x} RGB prefix as an unknown code, leaving it visible in the console.
 */
public final class MinecraftConsoleFormatter {

    private static final char COLOR_CHAR = '§';
    private static final String LEGACY_CODES = "0123456789abcdefklmnor";
    private static final String ANSI_RESET = "\u001B[m";
    private static final String[] ANSI_CODES = {
        "\u001B[0;30m",
        "\u001B[0;34m",
        "\u001B[0;32m",
        "\u001B[0;36m",
        "\u001B[0;31m",
        "\u001B[0;35m",
        "\u001B[0;33m",
        "\u001B[0;37m",
        "\u001B[0;30;1m",
        "\u001B[0;34;1m",
        "\u001B[0;32;1m",
        "\u001B[0;36;1m",
        "\u001B[0;31;1m",
        "\u001B[0;35;1m",
        "\u001B[0;33;1m",
        "\u001B[0;37;1m",
        "\u001B[5m",
        "\u001B[21m",
        "\u001B[9m",
        "\u001B[4m",
        "\u001B[3m",
        ANSI_RESET
    };

    private MinecraftConsoleFormatter() {
    }

    public static void format(
        String text,
        StringBuilder output,
        int start,
        boolean ansi
    ) {
        int section = text.indexOf(COLOR_CHAR);
        int last = text.length() - 1;
        if (section == -1 || section == last) {
            return;
        }
        output.setLength(start);
        output.append(text, 0, section);
        int copiedUntil = section;
        while (section != -1 && section < last) {
            char code = Character.toLowerCase(text.charAt(section + 1));
            if (code == 'x') {
                output.append(text, copiedUntil, section);
                if (isRgbColor(text, section)) {
                    if (ansi) appendRgbColor(text, section, output);
                    copiedUntil = section + 14;
                } else {
                    // Invalid RGB prefixes used to leak into the console as a literal §x.
                    copiedUntil = section + 2;
                }
                section = text.indexOf(COLOR_CHAR, copiedUntil);
                continue;
            }
            int ansiIndex = LEGACY_CODES.indexOf(code);
            if (ansiIndex >= 0) {
                output.append(text, copiedUntil, section);
                if (ansi) output.append(ANSI_CODES[ansiIndex]);
                copiedUntil = section + 2;
                section = text.indexOf(COLOR_CHAR, copiedUntil);
            } else {
                section = text.indexOf(COLOR_CHAR, section + 1);
            }
        }
        output.append(text, copiedUntil, text.length());
        if (ansi) output.append(ANSI_RESET);
    }

    private static boolean isRgbColor(String text, int section) {
        if (section + 14 > text.length()) {
            return false;
        }
        for (int i = 0; i < 6; i++) {
            int colorSection = section + 2 + i * 2;
            if (
                text.charAt(colorSection) != COLOR_CHAR ||
                    Character.digit(text.charAt(colorSection + 1), 16) == -1
            ) {
                return false;
            }
        }
        return true;
    }

    private static void appendRgbColor(
        String text,
        int section,
        StringBuilder output
    ) {
        int red = hex(text, section + 3) * 16 + hex(text, section + 5);
        int green = hex(text, section + 7) * 16 + hex(text, section + 9);
        int blue = hex(text, section + 11) * 16 + hex(text, section + 13);
        output.append("\u001B[38;2;")
            .append(red)
            .append(';')
            .append(green)
            .append(';')
            .append(blue)
            .append('m');
    }

    private static int hex(String text, int index) {
        return Character.digit(text.charAt(index), 16);
    }
}
