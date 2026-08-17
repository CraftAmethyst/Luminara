package io.izzel.arclight.fabric.mixin.core.commands;

import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.izzel.arclight.common.mod.mixins.annotation.TransformAccess;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;
import java.util.function.Predicate;

/*
 * Brigadier lives in the game layer on Fabric. The ASM CommandNode hack used by
 * the legacy launcher cannot redefine a class that is already requested by the
 * game loader, so the standalone mod applies the same patch through Mixin.
 */
@Mixin(value = CommandNode.class, remap = false)
public class CommandNodeMixin_Fabric<S> {

    // @formatter:off
    @Shadow @Final private Map<String, CommandNode<S>> children;
    @Shadow @Final private Map<String, LiteralCommandNode<S>> literals;
    @Shadow @Final private Map<String, ArgumentCommandNode<S, ?>> arguments;
    @Shadow @Final private Predicate<S> requirement;
    // @formatter:on

    /*
     * This is used when the current command is not present in CommandSourceStack.
     */
    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static CommandNode<?> CURRENT_COMMAND;

    @Unique
    public void removeCommand(String name) {
        children.remove(name);
        literals.remove(name);
        arguments.remove(name);
    }

    /**
     * @author Arclight
     * @reason pre-cache CommandNode for requirement test.
     */
    @Overwrite
    public boolean canUse(final S source) {
        if (source instanceof final io.izzel.arclight.common.bridge.core.commands.CommandSourceStackBridge bridge) {
            try {
                bridge.bridge$setCurrentCommand((CommandNode<?>) (Object) this);
                return requirement.test(source);
            } finally {
                bridge.bridge$setCurrentCommand(null);
            }
        }
        return requirement.test(source);
    }
}