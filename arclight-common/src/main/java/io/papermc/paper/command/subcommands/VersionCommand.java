package io.papermc.paper.command.subcommands;

import com.destroystokyo.paper.util.VersionFetcher;
import io.papermc.paper.command.PaperSubcommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Paper API patch 0014: Version Command 2.0
 * Updated VersionCommand to use VersionFetcher with Adventure components and caching
 */
public final class VersionCommand implements PaperSubcommand {

    private final ReentrantLock versionLock = new ReentrantLock();
    private final Set<CommandSender> versionWaiters = new HashSet<>();
    private VersionFetcher versionFetcher;
    private boolean hasVersion = false;
    private Component versionMessage = null;
    private boolean versionTaskStarted = false;
    private long lastCheck = 0;

    private VersionFetcher getVersionFetcher() {
        if (versionFetcher == null) {
            versionFetcher = new io.izzel.arclight.common.mixin.paper.util.CraftMagicNumbersMixin_VersionFetcher.ServerVersionFetcher();
        }
        return versionFetcher;
    }

    @Override
    public boolean execute(final CommandSender sender, final String subCommand, final String[] args) {
        sendVersion(sender);
        return true;
    }

    private void sendVersion(CommandSender sender) {
        if (hasVersion) {
            if (System.currentTimeMillis() - lastCheck > getVersionFetcher().getCacheTime()) {
                lastCheck = System.currentTimeMillis();
                hasVersion = false;
            } else {
                sender.sendMessage(net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(versionMessage));
                return;
            }
        }

        versionLock.lock();
        try {
            if (hasVersion) {
                sender.sendMessage(net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(versionMessage));
                return;
            }
            versionWaiters.add(sender);
            sender.sendMessage("Checking version, please wait...");
            if (!versionTaskStarted) {
                versionTaskStarted = true;
                new Thread(this::obtainVersion, "Paper Version Fetcher").start();
            }
        } finally {
            versionLock.unlock();
        }
    }

    private void obtainVersion() {
        String version = Bukkit.getVersion();
        if (version.startsWith("null")) {
            setVersionMessage(Component.text("Unknown version, custom build?", NamedTextColor.YELLOW));
            return;
        }
        setVersionMessage(getVersionFetcher().getVersionMessage(version));
    }

    private void setVersionMessage(final Component msg) {
        lastCheck = System.currentTimeMillis();
        // Use the message directly from VersionFetcher, add click-to-copy functionality
        this.versionMessage = Component.text()
                .append(msg)
                .hoverEvent(Component.text("Click to copy to clipboard", NamedTextColor.WHITE))
                .clickEvent(ClickEvent.copyToClipboard(PlainTextComponentSerializer.plainText().serialize(msg)))
                .build();

        versionLock.lock();
        try {
            hasVersion = true;
            versionTaskStarted = false;
            for (CommandSender sender : versionWaiters) {
                sender.sendMessage(net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(versionMessage));
            }
            versionWaiters.clear();
        } finally {
            versionLock.unlock();
        }
    }
}