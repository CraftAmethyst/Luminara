package io.izzel.arclight.common.mixin.paper.util;

import com.destroystokyo.paper.util.VersionFetcher;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.util.CraftMagicNumbers;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Paper API patch 0014: Version Command 2.0
 * Implements getVersionFetcher() method in CraftMagicNumbers
 */
@Mixin(value = CraftMagicNumbers.class, remap = false)
public class CraftMagicNumbersMixin_VersionFetcher {

    /**
     * Called once by the version command on first use, then cached.
     *
     * @return the version fetcher for this server implementation
     */
    @NotNull
    public VersionFetcher getVersionFetcher() {
        return new ServerVersionFetcher();
    }

    /**
     * Server-specific version fetcher implementation
     */
    public static class ServerVersionFetcher implements VersionFetcher {

        @Override
        public long getCacheTime() {
            return 21600000; // 6 hours in milliseconds
        }

        @NotNull
        @Override
        public Component getVersionMessage(@NotNull String serverVersion) {
            if (serverVersion.startsWith("null")) {
                return Component.text("Unknown version, custom build?", NamedTextColor.YELLOW);
            }

            String serverBrandName = Bukkit.getName();
            return Component.text()
                    .append(Component.text("This server is running ", NamedTextColor.WHITE))
                    .append(Component.text(serverBrandName, NamedTextColor.AQUA))
                    .append(Component.text(" version ", NamedTextColor.WHITE))
                    .append(Component.text(serverVersion, NamedTextColor.GREEN))
                    .append(Component.newline())
                    .append(Component.text("(Mod API base on Forge, Plugin API base on Paper)", NamedTextColor.WHITE))
                    .append(Component.newline())
                    .append(Component.text("For support, visit: ", NamedTextColor.WHITE))
                    .append(Component.text("https://github.com/QianMoo0121/Luminara", NamedTextColor.BLUE))
                    .build();
        }
    }
}
