package io.izzel.arclight.common.mod.server.world.border;

import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;

public class ArclightDelegatedBorderListener
    extends BorderChangeListener.DelegateBorderChangeListener {

  public static boolean isEnabled() {
    // return ArclightConfig.spec().getCompat().isAssociateWorldBorder();
    return true;
  }

  private final BorderChangeListener.DelegateBorderChangeListener delegate;

  public ArclightDelegatedBorderListener(
      WorldBorder border, BorderChangeListener.DelegateBorderChangeListener delegate) {
    super(border);
    this.delegate = delegate;
  }

  @Override
  public void onBorderSizeSet(WorldBorder worldBorder, double d) {
    if (!isEnabled()) {
      return;
    }
    delegate.onBorderSizeSet(worldBorder, d);
  }

  @Override
  public void onBorderCenterSet(WorldBorder worldBorder, double d, double e) {
    if (!isEnabled()) {
      return;
    }
    delegate.onBorderCenterSet(worldBorder, d, e);
  }

  @Override
  public void onBorderSizeLerping(WorldBorder worldBorder, double d, double e, long l) {
    if (!isEnabled()) {
      return;
    }
    delegate.onBorderSizeLerping(worldBorder, d, e, l);
  }

  @Override
  public void onBorderSetWarningTime(WorldBorder worldBorder, int i) {
    if (!isEnabled()) {
      return;
    }
    delegate.onBorderSetWarningTime(worldBorder, i);
  }

  @Override
  public void onBorderSetWarningBlocks(WorldBorder worldBorder, int i) {
    if (!isEnabled()) {
      return;
    }
    delegate.onBorderSetWarningBlocks(worldBorder, i);
  }

  @Override
  public void onBorderSetDamagePerBlock(WorldBorder worldBorder, double d) {
    if (!isEnabled()) {
      return;
    }
    delegate.onBorderSetDamagePerBlock(worldBorder, d);
  }

  @Override
  public void onBorderSetDamageSafeZOne(WorldBorder worldBorder, double d) {
    if (!isEnabled()) {
      return;
    }
    delegate.onBorderSetDamageSafeZOne(worldBorder, d);
  }
}
