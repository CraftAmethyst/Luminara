package io.izzel.arclight.common.mixin.core.world.level;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GameRules.IntegerValue.class)
public abstract class GameRules_IntegerValueMixin
    extends GameRules_ValueMixin<GameRules.IntegerValue> {

  @Shadow private int value;

  @Unique
  public void set(int value, @Nullable ServerLevel level) {
    this.value = value;
    onChanged(level);
  }

  @Override
  public void setFrom(GameRules.IntegerValue value, ServerLevel level) {
    set(value.get(), level);
  }

  @Override
  public void arclight$set(Object value, @org.jetbrains.annotations.Nullable ServerLevel level) {
    this.set((int) value, level);
  }
}
