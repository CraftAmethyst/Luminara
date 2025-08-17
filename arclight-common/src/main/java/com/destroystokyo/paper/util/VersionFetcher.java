package com.destroystokyo.paper.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

/**
 * Paper API patch 0014: Version Command 2.0
 * Interface for fetching version information with caching support
 */
public interface VersionFetcher {
    /**
     * Amount of time to cache results for in milliseconds
     * <p>
     * Negative values will never cache.
     *
     * @return cache time
     */
    long getCacheTime();

    /**
     * Gets the version message to cache and show to command senders.
     *
     * <p>NOTE: This is run in a new thread separate from that of the command processing thread</p>
     *
     * @param serverVersion the current version of the server (will match {@link Bukkit#getVersion()})
     * @return the message to show when requesting a version
     */
    @NotNull
    Component getVersionMessage(@NotNull String serverVersion);

    class DummyVersionFetcher implements VersionFetcher {

        @Override
        public long getCacheTime() {
            return -1;
        }

        @NotNull
        @Override
        public Component getVersionMessage(@NotNull String serverVersion) {
            Bukkit.getLogger().warning("Version provider has not been set, cannot check for updates!");
            Bukkit.getLogger().info("Override the default implementation of org.bukkit.UnsafeValues#getVersionFetcher()");
            new Throwable().printStackTrace();
            return Component.text("Unable to check for updates. No version provider set.", NamedTextColor.RED);
        }
    }
}
