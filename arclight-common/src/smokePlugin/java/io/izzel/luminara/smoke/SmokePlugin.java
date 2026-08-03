package io.izzel.luminara.smoke;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class SmokePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("LUMINARA_SMOKE_PLUGIN_ENABLED");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("LUMINARA_SMOKE_COMMAND_OK");
        return true;
    }
}
