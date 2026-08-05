package io.izzel.arclight.common.bridge.core.world.raid;

import net.minecraft.world.entity.raid.Raider;

import java.util.Collection;

public interface RaidBridge {
    Collection<Raider> bridge$getRaiders();
}
