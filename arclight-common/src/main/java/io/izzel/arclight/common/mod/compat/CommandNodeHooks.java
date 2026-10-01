package io.izzel.arclight.common.mod.compat;

import com.mojang.brigadier.tree.CommandNode;
import io.izzel.arclight.common.bridge.core.command.CommandNodeBridge;
import io.izzel.arclight.common.bridge.core.command.CommandSourceBridge;

public class CommandNodeHooks {

    private static final ThreadLocal<CommandNode<?>> CURRENT = new ThreadLocal<>();

    public static void removeCommand(CommandNode<?> node, String command) {
        ((CommandNodeBridge) node).bridge$removeCommand(command);
    }

    public static CommandNode<?> getCurrent() {
        return CURRENT.get();
    }

    public static void setCurrent(CommandNode<?> node) {
        if (node == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(node);
        }
    }

    public static <S> boolean canUse(CommandNode<S> node, S source) {
        if (source instanceof CommandSourceBridge s) {
            try {
                s.bridge$setCurrentCommand(node);
                return node.canUse(source);
            } finally {
                s.bridge$setCurrentCommand(null);
            }
        } else {
            return node.canUse(source);
        }
    }
}
