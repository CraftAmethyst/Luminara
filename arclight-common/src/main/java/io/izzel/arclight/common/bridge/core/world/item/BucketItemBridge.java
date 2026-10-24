package io.izzel.arclight.common.bridge.core.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface BucketItemBridge {
  @Nullable
  Direction arclight$getDirection();

  void arclight$setDirection(@Nullable Direction value);

  @Nullable
  BlockPos arclight$getClick();

  void arclight$setClick(@Nullable BlockPos value);

  @Nullable
  InteractionHand arclight$getHand();

  void arclight$setHand(@Nullable InteractionHand value);

  @Nullable
  ItemStack arclight$getStack();

  void arclight$setStack(@Nullable ItemStack value);

  @Nullable
  org.bukkit.inventory.ItemStack arclight$getCaptureItem();

  void arclight$setCaptureItem(@Nullable org.bukkit.inventory.ItemStack value);
}
