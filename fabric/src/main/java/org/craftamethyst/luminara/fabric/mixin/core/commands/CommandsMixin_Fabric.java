package org.craftamethyst.luminara.fabric.mixin.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.craftamethyst.luminara.common.mod.util.BukkitDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Commands.class)
public abstract class CommandsMixin_Fabric {

  @Mutable @Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;

  public void arclight$constructor() {
    this.dispatcher = new BukkitDispatcher((Commands) (Object) this);
    this.dispatcher.setConsumer(
        (context, b, i) -> context.getSource().onCommandComplete(context, b, i));
  }
}
