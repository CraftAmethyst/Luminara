package org.craftamethyst.luminara.common.bridge.core.world.raid;

import java.util.Collection;
import net.minecraft.world.entity.raid.Raider;

public interface RaidBridge {
  Collection<Raider> bridge$getRaiders();
}
