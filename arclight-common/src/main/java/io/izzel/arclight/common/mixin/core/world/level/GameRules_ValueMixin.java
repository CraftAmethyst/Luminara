package io.izzel.arclight.common.mixin.core.world.level;

import com.mojang.brigadier.context.CommandContext;
import io.izzel.arclight.common.bridge.core.world.level.GameRules_TypeBridge;
import io.izzel.arclight.common.bridge.core.world.level.GameRules_ValueBridge;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRules.Value.class)
public abstract class GameRules_ValueMixin<T extends GameRules.Value<T>>
    implements GameRules_ValueBridge<T> {

  @Shadow @Final protected GameRules.Type<T> type;

  @Shadow
  protected abstract T getSelf();

  @Unique
  public void onChanged(ServerLevel level) {
    if (level != null) {
      ((GameRules_TypeBridge<T>) this.type).arclight$runCallback(level, getSelf());
    }
  }

  @Unique
  public abstract void setFrom(T t, ServerLevel level);

  @Override
  public void arclight$setFrom(T t, @Nullable ServerLevel level) {
    setFrom(t, level);
  }

  @Redirect(
      method = "setFromArgument",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/level/GameRules$Value;onChanged(Lnet/minecraft/server/MinecraftServer;)V"))
  private void arclight$skipGlobalCallback(
      GameRules.Value<?> instance, MinecraftServer minecraftServer) {}

  @Inject(method = "setFromArgument", at = @At("RETURN"))
  private void arclight$invokeLocalCallback(
      CommandContext<CommandSourceStack> ctx, String string, CallbackInfo ci) {
    onChanged(ctx.getSource().getLevel());
  }
}
