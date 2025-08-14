package io.izzel.arclight.common.util;

// Removed MpemThreadManager import - MPEM functionality removed
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificationManager {
    private static final Logger LOGGER = LogManager.getLogger("Luminara-MPEM-Notification");
    private static final Pattern COLOR_PATTERN = Pattern.compile("&([0-9a-fk-or])");
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Luminara-NotificationManager");
        t.setDaemon(true);
        return t;
    });

    public static void broadcastMessage(MinecraftServer server, String message) {
        if (server == null || message == null || message.isEmpty()) return;

        Component component = parseColoredMessage(message);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(component);
        }

        // Also log to console
        LOGGER.info(stripColors(message));
    }

    public static void broadcastToOps(MinecraftServer server, String message) {
        if (server == null || message == null || message.isEmpty()) return;

        Component component = parseColoredMessage(message);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (server.getPlayerList().isOp(player.getGameProfile())) {
                player.sendSystemMessage(component);
            }
        }

        // Also log to console
        LOGGER.info(stripColors(message));
    }

    public static ScheduledFuture<?> scheduleCountdownNotification(MinecraftServer server,
                                                                   String messageTemplate,
                                                                   int seconds,
                                                                   Runnable onComplete) {
        return SCHEDULER.scheduleAtFixedRate(new Runnable() {
            private int remainingSeconds = seconds;

            @Override
            public void run() {
                if (remainingSeconds <= 0) {
                    if (onComplete != null) {
                        // Execute onComplete on main thread to avoid async chunk access
                        server.execute(onComplete);
                    }
                    return;
                }

                // Send countdown message at specific intervals
                if (remainingSeconds <= 10 || remainingSeconds % 10 == 0) {
                    String message = messageTemplate.replace("{time}", String.valueOf(remainingSeconds));
                    // Execute broadcast on main thread to avoid async player access
                    server.execute(() -> broadcastMessage(server, message));
                }

                remainingSeconds--;
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private static Component parseColoredMessage(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }

        // Convert color codes to Minecraft formatting
        String converted = convertColorCodes(message);
        return Component.literal(converted);
    }

    private static String convertColorCodes(String message) {
        if (message == null) return "";

        Matcher matcher = COLOR_PATTERN.matcher(message);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String colorCode = matcher.group(1).toLowerCase();
            ChatFormatting formatting = getFormattingByCode(colorCode);

            if (formatting != null) {
                matcher.appendReplacement(result, formatting.toString());
            } else {
                matcher.appendReplacement(result, "&" + colorCode);
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private static ChatFormatting getFormattingByCode(String code) {
        return switch (code) {
            case "0" -> ChatFormatting.BLACK;
            case "1" -> ChatFormatting.DARK_BLUE;
            case "2" -> ChatFormatting.DARK_GREEN;
            case "3" -> ChatFormatting.DARK_AQUA;
            case "4" -> ChatFormatting.DARK_RED;
            case "5" -> ChatFormatting.DARK_PURPLE;
            case "6" -> ChatFormatting.GOLD;
            case "7" -> ChatFormatting.GRAY;
            case "8" -> ChatFormatting.DARK_GRAY;
            case "9" -> ChatFormatting.BLUE;
            case "a" -> ChatFormatting.GREEN;
            case "b" -> ChatFormatting.AQUA;
            case "c" -> ChatFormatting.RED;
            case "d" -> ChatFormatting.LIGHT_PURPLE;
            case "e" -> ChatFormatting.YELLOW;
            case "f" -> ChatFormatting.WHITE;
            case "k" -> ChatFormatting.OBFUSCATED;
            case "l" -> ChatFormatting.BOLD;
            case "m" -> ChatFormatting.STRIKETHROUGH;
            case "n" -> ChatFormatting.UNDERLINE;
            case "o" -> ChatFormatting.ITALIC;
            case "r" -> ChatFormatting.RESET;
            default -> null;
        };
    }

    private static String stripColors(String message) {
        if (message == null) return "";
        return COLOR_PATTERN.matcher(message).replaceAll("");
    }

    public static String formatMessage(String template, Object... args) {
        if (template == null) return "";

        String result = template;
        for (int i = 0; i < args.length; i += 2) {
            if (i + 1 < args.length) {
                String placeholder = "{" + args[i] + "}";
                String value = String.valueOf(args[i + 1]);
                result = result.replace(placeholder, value);
            }
        }
        return result;
    }
}
