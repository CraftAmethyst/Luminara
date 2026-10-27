package org.craftamethyst.luminara.common.bridge.core.entity.projectile;

import org.craftamethyst.luminara.common.bridge.core.entity.EntityBridge;

public interface ArrowEntityBridge extends EntityBridge {
  void bridge$refreshEffects();

  boolean bridge$isTipped();
}
