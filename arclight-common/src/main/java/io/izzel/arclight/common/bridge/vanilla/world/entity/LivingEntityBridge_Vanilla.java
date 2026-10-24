package io.izzel.arclight.common.bridge.vanilla.world.entity;

import java.util.List;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;

public interface LivingEntityBridge_Vanilla {
  void arclight$vanilla$callLivingDropsEvent(DamageSource source, List<ItemEntity> capturedDrops);
}
