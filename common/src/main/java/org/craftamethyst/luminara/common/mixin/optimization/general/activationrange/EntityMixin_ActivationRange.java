package org.craftamethyst.luminara.common.mixin.optimization.general.activationrange;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.craftamethyst.luminara.common.bridge.core.world.WorldBridge;
import org.craftamethyst.luminara.common.bridge.optimization.EntityBridge_ActivationRange;
import org.craftamethyst.luminara.common.mod.ArclightConstants;
import org.craftamethyst.luminara.common.mod.util.DistValidate;
import org.spigotmc.ActivationRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin_ActivationRange implements EntityBridge_ActivationRange {

  @Shadow public int tickCount;

  public ActivationRange.ActivationType activationType;
  public boolean defaultActivationState;
  public long activatedTick = Integer.MIN_VALUE;

  // @formatter:off
  @Shadow
  public abstract void refreshDimensions();

  // @formatter:on

  @Shadow
  public abstract Level level();

  @Shadow
  public abstract AABB getBoundingBox();

  @Shadow
  public abstract void discard();

  @Inject(method = "<init>", at = @At("RETURN"))
  private void arclight$init(EntityType<?> entityTypeIn, Level worldIn, CallbackInfo ci) {
    activationType = ActivationRange.initializeEntityActivationType((Entity) (Object) this);
    if (DistValidate.isValid(worldIn)) {
      var config = ((WorldBridge) worldIn).bridge$spigotConfig();
      if (config != null) {
        this.defaultActivationState =
            ActivationRange.initializeEntityActivationState((Entity) (Object) this, config);
      } else {
        this.defaultActivationState = false;
      }
    } else {
      this.defaultActivationState = false;
    }
  }

  public void inactiveTick() {}

  @Override
  public void bridge$inactiveTick() {
    this.inactiveTick();
  }

  @Override
  public void bridge$updateActivation() {
    if (ArclightConstants.currentTick > this.activatedTick) {
      if (this.defaultActivationState) {
        this.activatedTick = ArclightConstants.currentTick;
      } else if (((ActivationTypeAccessor) (Object) this.activationType)
          .arclight$getBoundingBox()
          .intersects(this.getBoundingBox())) {
        this.activatedTick = ArclightConstants.currentTick;
      }
    }
  }
}
