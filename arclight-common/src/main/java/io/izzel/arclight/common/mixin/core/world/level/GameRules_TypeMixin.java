package io.izzel.arclight.common.mixin.core.world.level;

import io.izzel.arclight.common.bridge.core.world.level.GameRules_TypeBridge;
import java.util.function.BiConsumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GameRules.Type.class)
public class GameRules_TypeMixin<T extends GameRules.Value<T>> implements GameRules_TypeBridge<T> {

  @Unique private BiConsumer<ServerLevel, T> arclight$perWorldCallback;

  @Override
  public void arclight$setPerWorldCallback(BiConsumer<ServerLevel, T> callback) {
    arclight$perWorldCallback = callback;
  }

  @Override
  public void arclight$runCallback(ServerLevel level, T value) {
    if (arclight$perWorldCallback != null) {
      arclight$perWorldCallback.accept(level, value);
    }
  }
}
