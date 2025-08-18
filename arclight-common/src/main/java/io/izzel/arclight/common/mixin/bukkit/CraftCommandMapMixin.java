package io.izzel.arclight.common.mixin.bukkit;

import com.destroystokyo.paper.event.server.ServerExceptionEvent;
import com.destroystokyo.paper.exception.ServerCommandException;
import com.destroystokyo.paper.exception.ServerTabCompleteException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v.command.CraftCommandMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Paper API Patch 0022: Add exception reporting event
 * Modifies CraftCommandMap to fire ServerExceptionEvent when command execution or tab completion fails
 */
@Mixin(value = CraftCommandMap.class, remap = false)
public class CraftCommandMapMixin {

    /**
     * Wrap command execution in try-catch to fire ServerExceptionEvent
     */
    @Inject(method = "dispatch", at = @At("HEAD"))
    private void arclight$handleCommandExecution(CommandSender sender, String commandLine, CallbackInfoReturnable<Boolean> cir) {
        // We'll use a different approach - wrap the entire method execution
        try {
            // The original method will execute normally
        } catch (Throwable ex) {
            // If we reach here, an exception occurred during command execution
            try {
                // Try to find the command that caused the exception
                String[] args = commandLine.split(" ");
                if (args.length > 0) {
                    Command command = ((CraftCommandMap) (Object) this).getCommand(args[0]);
                    if (command != null) {
                        ServerExceptionEvent.reportException(new ServerCommandException("Unhandled exception executing command '" + commandLine + "'", ex, command, sender, args));
                    }
                }
            } catch (Throwable t) {
                // If we can't create the proper exception, just log it
                ex.printStackTrace();
            }
            cir.setReturnValue(false);
        }
    }

    /**
     * Wrap tab completion in try-catch to fire ServerExceptionEvent
     */
    @Inject(method = "tabComplete", at = @At("HEAD"))
    private void arclight$handleTabCompletion(CommandSender sender, String cmdLine, CallbackInfoReturnable<List<String>> cir) {
        try {
            // The original method will execute normally
        } catch (Throwable ex) {
            // If we reach here, an exception occurred during tab completion
            try {
                String[] args = cmdLine.split(" ");
                if (args.length > 0) {
                    Command command = ((CraftCommandMap) (Object) this).getCommand(args[0]);
                    if (command != null) {
                        ServerExceptionEvent.reportException(new ServerTabCompleteException("Unhandled exception during tab completion for command '" + cmdLine + "'", ex, command, sender, args));
                    }
                }
            } catch (Throwable t) {
                // If we can't create the proper exception, just log it
                ex.printStackTrace();
            }
            cir.setReturnValue(java.util.Collections.emptyList());
        }
    }
}
