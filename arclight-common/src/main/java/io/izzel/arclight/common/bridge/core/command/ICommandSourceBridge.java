package io.izzel.arclight.common.bridge.core.command;

import net.minecraft.commands.CommandSourceStack;
import org.bukkit.command.CommandSender;

public interface ICommandSourceBridge {
    /*
     * Offer a way to recognize whether we have implemented a proper getBukkitSender
     * for the specified CommandSource.
     */
    default CommandSender bridge$getBukkitSender(CommandSourceStack wrapper) {
        return null;
    }
}
