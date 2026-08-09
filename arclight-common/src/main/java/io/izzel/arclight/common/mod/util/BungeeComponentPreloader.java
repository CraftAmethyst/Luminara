package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.mod.server.ArclightServer;

public final class BungeeComponentPreloader {

    private static final String[] CHAT_CLASSES = {
        "net.md_5.bungee.api.chat.BaseComponent",
        "net.md_5.bungee.api.chat.TextComponent",
        "net.md_5.bungee.api.chat.ClickEvent",
        "net.md_5.bungee.api.chat.HoverEvent",
        "net.md_5.bungee.chat.ComponentSerializer"
    };

    private BungeeComponentPreloader() {
    }

    public static void preload() {
        ClassLoader loader = BungeeComponentPreloader.class.getClassLoader();
        for (String className : CHAT_CLASSES) {
            try {
                Class.forName(className, true, loader);
            } catch (ClassNotFoundException exception) {
                ArclightServer.LOGGER.warn("Unable to preload Bungee Chat class {}", className, exception);
                return;
            }
        }
        ArclightServer.LOGGER.debug("Preloaded Bungee Chat API classes");
    }
}
