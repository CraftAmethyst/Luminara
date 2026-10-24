package io.izzel.arclight.common.bridge.core.world.level.block.entity;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public interface AbstractFurnaceBlockEntityBridge {

  List<RecipeHolder<?>> bridge$dropExp(ServerPlayer entity, ItemStack itemStack, int amount);

  int bridge$getBurnDuration(ItemStack stack);

  boolean bridge$isLit();
}
