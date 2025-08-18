package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v.command.CraftCommandMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Paper API Patch 0029: Add sender name to commands.yml replacement
 * Modifies command execution to support $sender placeholder replacement
 */
@Mixin(value = CraftCommandMap.class, remap = false)
public class CraftCommandMapMixin_SenderPlaceholder {

    /**
     * Replace $sender placeholder in command line with the sender's name
     */
    @ModifyVariable(method = "dispatch", at = @At("HEAD"), argsOnly = true)
    private String arclight$replaceSenderPlaceholder(String commandLine, CommandSender sender) {
        if (commandLine != null && commandLine.contains("$sender")) {
            String senderName = sender.getName();
            return commandLine.replace("$sender", senderName);
        }
        return commandLine;
    }
}
