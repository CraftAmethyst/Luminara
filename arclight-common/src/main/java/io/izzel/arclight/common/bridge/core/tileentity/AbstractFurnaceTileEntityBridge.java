package io.izzel.arclight.common.bridge.core.tileentity;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public interface AbstractFurnaceTileEntityBridge {
    List<Recipe<?>> bridge$dropExp(
        ServerPlayer entity,
        ItemStack itemStack,
        int amount
    );

    int bridge$getBurnDuration(ItemStack stack);

    boolean bridge$isLit();
}
