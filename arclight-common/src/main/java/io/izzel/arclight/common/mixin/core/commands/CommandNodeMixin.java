package io.izzel.arclight.common.mixin.core.commands;

import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.izzel.arclight.common.bridge.core.command.CommandNodeBridge;
import io.izzel.arclight.common.mod.compat.CommandNodeHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.function.Predicate;

@Mixin(value = CommandNode.class, remap = false)
public class CommandNodeMixin<S> implements CommandNodeBridge {

    // @formatter:off
    @Shadow @Final private Map<String, CommandNode<S>> children;
    @Shadow @Final private Map<String, LiteralCommandNode<S>> literals;
    @Shadow @Final private Map<String, ArgumentCommandNode<S, ?>> arguments;
    @Shadow @Final private Predicate<S> requirement;
    // @formatter:on

    /**
     * @author crystalWinter666
     * @reason pre-cache the node under test so Bukkit permission checks can resolve it.
     *         Semantically identical to the original requirement test.
     */
    @Overwrite
    public boolean canUse(final S source) {
        CommandNodeHooks.setCurrent((CommandNode<?>) (Object) this);
        try {
            return this.requirement.test(source);
        } finally {
            CommandNodeHooks.setCurrent(null);
        }
    }

    @Override
    public void bridge$removeCommand(String name) {
        this.children.remove(name);
        this.literals.remove(name);
        this.arguments.remove(name);
    }
}
