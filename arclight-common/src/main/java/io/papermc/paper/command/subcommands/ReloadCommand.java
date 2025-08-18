package io.papermc.paper.command.subcommands;

import io.papermc.paper.command.PaperSubcommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ReloadCommand implements PaperSubcommand {

    // Paper API Patch 0030: Add command to reload permissions.yml and require confirmation
    private static final Map<UUID, Long> confirmationMap = new HashMap<>();
    private static final long CONFIRMATION_TIMEOUT = 30000; // 30 seconds

    @Override
    public boolean execute(final CommandSender sender, final String subCommand, final String[] args) {
        if (args.length > 0) {
            String target = args[0].toLowerCase();

            if ("permissions".equals(target)) {
                try {
                    // Use the mixin method we added to CraftServer
                    if (Bukkit.getServer() instanceof io.izzel.arclight.common.mixin.bukkit.CraftServerMixin) {
                        ((io.izzel.arclight.common.mixin.bukkit.CraftServerMixin) Bukkit.getServer()).reloadPermissions();
                    } else {
                        // Fallback to general reload
                        Bukkit.getServer().reloadData();
                    }
                    sender.sendMessage("Permissions reloaded from permissions.yml");
                } catch (Exception e) {
                    sender.sendMessage("Failed to reload permissions: " + e.getMessage());
                }
                return true;
            }

            if ("confirm".equals(target)) {
                UUID senderId = getSenderId(sender);
                if (confirmationMap.containsKey(senderId)) {
                    long confirmTime = confirmationMap.get(senderId);
                    if (System.currentTimeMillis() - confirmTime < CONFIRMATION_TIMEOUT) {
                        confirmationMap.remove(senderId);
                        try {
                            Bukkit.getServer().reload();
                            sender.sendMessage("Server reloaded successfully.");
                        } catch (Exception e) {
                            sender.sendMessage("Failed to reload server: " + e.getMessage());
                        }
                        return true;
                    } else {
                        confirmationMap.remove(senderId);
                        sender.sendMessage("Confirmation expired. Please run the reload command again.");
                        return true;
                    }
                } else {
                    sender.sendMessage("No pending reload confirmation.");
                    return true;
                }
            }
        }

        // Default behavior - require confirmation for full reload
        UUID senderId = getSenderId(sender);
        confirmationMap.put(senderId, System.currentTimeMillis());
        sender.sendMessage("This will reload the entire server configuration and may cause lag.");
        sender.sendMessage("Run '/paper reload confirm' within 30 seconds to confirm.");
        sender.sendMessage("Or use '/paper reload permissions' to reload only permissions.yml");

        return true;
    }

    private UUID getSenderId(CommandSender sender) {
        if (sender instanceof org.bukkit.entity.Player) {
            return ((org.bukkit.entity.Player) sender).getUniqueId();
        }
        return UUID.nameUUIDFromBytes(sender.getName().getBytes());
    }
}