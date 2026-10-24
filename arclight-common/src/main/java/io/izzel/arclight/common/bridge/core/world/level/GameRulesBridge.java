package io.izzel.arclight.common.bridge.core.world.level;

import java.util.Set;
import net.minecraft.world.level.GameRules;

public interface GameRulesBridge {
  Set<GameRules.Key<?>> arclight$getAllRules();
}
