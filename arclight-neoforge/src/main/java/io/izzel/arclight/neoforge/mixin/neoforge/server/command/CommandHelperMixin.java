package io.izzel.arclight.neoforge.mixin.neoforge.server.command;

import com.mojang.brigadier.tree.CommandNode;
import io.izzel.arclight.common.mod.compat.CommandNodeHooks;
import net.neoforged.neoforge.server.command.CommandHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/*
 * NeoForge replaces vanilla's fillUsableCommands with CommandHelper.mergeCommandNode when a
 * player's command tree is built, so the CommandNode.canUse redirect in CommandsMixin never
 * runs on that path. Brigadier is loaded from the MC-BOOTSTRAP module layer on NeoForge and
 * cannot be mixed into directly, so the call site is wrapped here instead. This keeps Bukkit
 * command permissions effective while the client command tree is assembled.
 */
@Mixin(value = CommandHelper.class, remap = false)
public abstract class CommandHelperMixin {

  @Redirect(
      method = "mergeCommandNode",
      at =
          @At(
              value = "INVOKE",
              remap = false,
              target = "Lcom/mojang/brigadier/tree/CommandNode;canUse(Ljava/lang/Object;)Z"),
      require = 0)
  private static <S> boolean arclight$canUse(CommandNode<S> node, S source) {
    return CommandNodeHooks.canUse(node, source);
  }
}
