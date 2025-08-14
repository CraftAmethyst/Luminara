package io.papermc.paper.command.subcommands;

import io.papermc.paper.command.PaperSubcommand;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public final class EntityCommand implements PaperSubcommand {
    @Override
    public boolean execute(final CommandSender sender, final String subCommand, final String[] args) {
        Map<EntityType, Integer> entityCounts = new HashMap<>();
        int totalEntities = 0;

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                entityCounts.merge(entity.getType(), 1, Integer::sum);
                totalEntities++;
            }
        }

        sender.sendMessage("Entity Report:");
        sender.sendMessage("Total entities: " + totalEntities);

        entityCounts.entrySet().stream()
                .sorted(Map.Entry.<EntityType, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(entry ->
                        sender.sendMessage("  " + entry.getKey().name() + ": " + entry.getValue())
                );

        return true;
    }
}