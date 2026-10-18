package io.izzel.arclight.common.mixin.core.async;

import io.izzel.arclight.common.mod.util.async.AsyncCatcher;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelEntityGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class ServerLevelAsyncMixin {

  // @formatter:off
  @Shadow
  protected abstract boolean addEntity(Entity entity);

  @Shadow
  protected abstract LevelEntityGetter<Entity> getEntities();

  // @formatter:on

  @Inject(
      method = "addEntity(Lnet/minecraft/world/entity/Entity;)Z",
      at = @At("HEAD"),
      cancellable = true)
  private void arclight$guardEntityAdd(Entity entity, CallbackInfoReturnable<Boolean> cir) {
    Optional<Boolean> guarded = AsyncCatcher.check("entity add", () -> this.addEntity(entity));
    guarded.ifPresent(cir::setReturnValue);
  }

  @Inject(
      method = "getEntities()Lnet/minecraft/world/level/entity/LevelEntityGetter;",
      at = @At("HEAD"),
      cancellable = true)
  private void arclight$guardChunkEntityGet(CallbackInfoReturnable<LevelEntityGetter<Entity>> cir) {
    Optional<LevelEntityGetter<Entity>> guarded =
        AsyncCatcher.check("chunk entity get", this::getEntities);
    guarded.ifPresent(cir::setReturnValue);
  }
}
