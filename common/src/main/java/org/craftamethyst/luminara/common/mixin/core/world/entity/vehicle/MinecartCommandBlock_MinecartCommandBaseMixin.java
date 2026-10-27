package org.craftamethyst.luminara.common.mixin.core.world.entity.vehicle;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import org.bukkit.command.CommandSender;
import org.craftamethyst.luminara.common.bridge.core.command.ICommandSourceBridge;
import org.craftamethyst.luminara.common.bridge.core.entity.EntityBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecartCommandBlock.MinecartCommandBase.class)
public abstract class MinecartCommandBlock_MinecartCommandBaseMixin
    implements ICommandSourceBridge {

  @SuppressWarnings("target")
  @Shadow(
      aliases = {"this$0", "f_38537_"},
      remap = false)
  private MinecartCommandBlock outerThis;

  public CommandSender getBukkitSender(CommandSourceStack wrapper) {
    return ((EntityBridge) outerThis).bridge$getBukkitEntity();
  }

  @Override
  public CommandSender bridge$getBukkitSender(CommandSourceStack wrapper) {
    return getBukkitSender(wrapper);
  }
}
