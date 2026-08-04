package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.mod.ArclightMod;
import org.apache.logging.log4j.Logger;

public class BungeeComponentPreloader {

    private static final Logger LOGGER = ArclightMod.LOGGER;

    public static void preloadBungeeClasses() {
        try {
            // Preload the BaseComponent class which is often accessed by plugins
            Class.forName("net.md_5.bungee.api.chat.BaseComponent");
            LOGGER.debug(
                "Successfully preloaded net.md_5.bungee.api.chat.BaseComponent"
            );

            // Preload other common Bungee chat classes
            Class.forName("net.md_5.bungee.api.chat.TextComponent");
            LOGGER.debug(
                "Successfully preloaded net.md_5.bungee.api.chat.TextComponent"
            );

            Class.forName("net.md_5.bungee.api.chat.ClickEvent");
            LOGGER.debug(
                "Successfully preloaded net.md_5.bungee.api.chat.ClickEvent"
            );

            Class.forName("net.md_5.bungee.api.chat.HoverEvent");
            LOGGER.debug(
                "Successfully preloaded net.md_5.bungee.api.chat.HoverEvent"
            );

            Class.forName("net.md_5.bungee.chat.ComponentSerializer");
            LOGGER.debug(
                "Successfully preloaded net.md_5.bungee.chat.ComponentSerializer"
            );
        } catch (ClassNotFoundException e) {
            LOGGER.warn(
                "Failed to preload some Bungee API classes. This may be expected if the bungeecord-chat dependency isn't available.",
                e
            );
        } catch (Exception e) {
            LOGGER.warn("Error during preloading of Bungee API classes", e);
        }
    }
}
